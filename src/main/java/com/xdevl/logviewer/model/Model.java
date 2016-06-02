/*
 * Copyright (C) 2016 XdevL
 *
 * This file is part of Log viewer.

 * Log viewer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Log viewer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Log viewer. If not, see <http://www.gnu.org/licenses/>.
 */
package com.xdevl.logviewer.model;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import com.xdevl.logviewer.bean.Log;
import com.xdevl.logviewer.bean.ProcessInputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Model
{
    private interface LogParser
    {
        Log parse(long lineNumber,String logLine) ;
    }

    private static final int IO_BUFFER_SIZE=64*1024 ;

    private static void copy(OutputStream output, InputStream input) throws IOException
    {
        byte[] buffer=new byte[IO_BUFFER_SIZE] ;
        int size ;
        while((size=input.read(buffer))>=0)
            output.write(buffer,0,size) ;
    }

    private static void close(Closeable...closeables)
    {
        for(Closeable closeable: closeables)
            if(closeable!=null) try { closeable.close(); } catch(IOException e) {}
    }

    private static InputStream runCommand(String ...args) throws IOException
    {
        ProcessBuilder builder=new ProcessBuilder(args) ;
        builder.redirectErrorStream(true) ;
        return new ProcessInputStream(builder.start()) ;
    }

    private static InputStream runRootCommand(String ...args) throws IOException
    {
        StringBuilder builder=new StringBuilder() ;
        for(String arg: args)
            builder.append(' ').append(arg) ;
        return runCommand("su","-c",builder.toString()) ;
    }

    private static boolean hasReadLogsPermission(Context context)
    {
        return context.getPackageManager().checkPermission(Manifest.permission.READ_LOGS,
                context.getPackageName())==PackageManager.PERMISSION_GRANTED ;
    }

    private static final LogParser sAdbLogParser=new LogParser()
    {
        @Override
        public Log parse(long lineNumber,String logLine)
        {
            return new Log(lineNumber,getSeverity(logLine),logLine) ;
        }

        public Log.Severity getSeverity(String logLine)
        {
            switch(logLine.charAt(19))
            {
                case 'D': return Log.Severity.DEBUGGING ;
                case 'W': return Log.Severity.WARNING ;
                case 'E': return Log.Severity.ERROR ;
                default: return Log.Severity.INFO ;
            }
        }
    } ;

    private static final LogParser sDmesgLogParser=new LogParser()
    {
        @Override
        public Log parse(long lineNumber,String logLine)
        {
            return new Log(lineNumber,getSeverity(logLine),logLine) ;
        }

        public Log.Severity getSeverity(String logLine)
        {
            return logLine.charAt(1)<'4'?Log.Severity.ERROR:Log.Severity.INFO ;
        }
    } ;

    private static List<Log> parse(LogParser logParser,InputStream input) throws IOException
    {
        try {
            List<Log> logEntries=new ArrayList<Log>() ;
            BufferedReader reader=new BufferedReader(new InputStreamReader(input)) ;
            String line ;
            long counter=0 ;
            while((line=reader.readLine())!=null)
                logEntries.add(logParser.parse(counter++,line)) ;
            input.close() ;
            return logEntries ;
        } finally {
            input.close() ;
        }
    }

    public static List<Log> getLogCat(Context context) throws IOException
    {
        String cmd[]={"logcat","-v","time","-d"} ;
        if(Model.hasReadLogsPermission(context))
            return parse(sAdbLogParser,runCommand(cmd)) ;
        else return parse(sAdbLogParser,runRootCommand(cmd)) ;
    }

    public static List<Log> getDmesg() throws IOException
    {
        return parse(sDmesgLogParser,runRootCommand("dmesg")) ;
    }
}
