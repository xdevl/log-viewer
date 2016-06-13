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
import android.util.Log;
import com.xdevl.logviewer.R;
import com.xdevl.logviewer.bean.ProcessInputStream;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Model
{
    private static final int IO_BUFFER_SIZE=64*1024 ;

    private static void copy(OutputStream output, InputStream input) throws IOException
    {
        try {
            byte[] buffer=new byte[IO_BUFFER_SIZE] ;
            int size ;
            while((size=input.read(buffer))>=0)
                output.write(buffer,0,size) ;
            input.close() ;
        } finally {
            try { input.close(); } catch(IOException e) {}
        }
    }

    public static InputStream runCommand(boolean endless, String ...args) throws IOException
    {
        ProcessBuilder builder=new ProcessBuilder(args) ;
        builder.redirectErrorStream(true) ;
        return new ProcessInputStream(endless,builder.start()) ;
    }

    public static InputStream runRootCommand(boolean endless, String ...args) throws IOException
    {
        StringBuilder builder=new StringBuilder() ;
        for(String arg: args)
            builder.append(' ').append(arg) ;
        return runCommand(endless,"su","-c",builder.toString()) ;
    }

    public static boolean hasReadLogsPermission(Context context)
    {
        return context.getPackageManager().checkPermission(Manifest.permission.READ_LOGS,
                context.getPackageName())==PackageManager.PERMISSION_GRANTED ;
    }

    public static void zipLogs(Context context, OutputStream outputStream)
    {
        ZipOutputStream zipOutputStream=new ZipOutputStream(outputStream) ;
        try
        {
            try {
                zipOutputStream.putNextEntry(new ZipEntry("logcat.txt")) ;
                String cmd[]={"logcat","-d","-v","time"} ;
                if(Model.hasReadLogsPermission(context))
                    Model.copy(zipOutputStream,Model.runCommand(false,cmd)) ;
                else Model.copy(zipOutputStream,Model.runRootCommand(false,cmd)) ;
                zipOutputStream.closeEntry() ;
            } catch(IOException e) {
                Log.e(context.getString(R.string.adb_tag),e.getMessage()) ;
            }
            try {
                zipOutputStream.putNextEntry(new ZipEntry("dmesg.txt")) ;
                Model.copy(zipOutputStream,Model.runRootCommand(false,"dmesg")) ;
                zipOutputStream.closeEntry() ;
                zipOutputStream.close() ;
            } catch(IOException e) {
                Log.e(context.getString(R.string.adb_tag),e.getMessage()) ;
            }
        } finally {
            try { zipOutputStream.close(); } catch(IOException e) {}
        }
    }
}
