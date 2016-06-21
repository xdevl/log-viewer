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

import android.content.Context;
import com.xdevl.logviewer.R;
import com.xdevl.logviewer.bean.Log;

import java.io.IOException;
import java.io.InputStream;

public class LogcatReader extends LogReader
{
    public LogcatReader(Context context, OnLogParsedListener onLogParsedListener)
    {
        super(context,onLogParsedListener) ;
    }

    @Override
    public String getTitle()
    {
        return mContext.getString(R.string.title_logcat) ;
    }

    @Override
    protected InputStream streamLogs() throws IOException
    {
        String cmd[]={"logcat","-v","time"} ;
        if(Model.hasReadLogsPermission(mContext))
            return Model.runCommand(true,cmd) ;
        else return Model.runRootCommand(true,cmd) ;
    }

    @Override
    protected Log parse(String line)
    {
        return new Log(getSeverity(line),line) ;
    }

    protected Log.Severity getSeverity(String logLine)
    {
        switch(logLine.charAt(19))
        {
            case 'D': return Log.Severity.DEBUGGING ;
            case 'W': return Log.Severity.WARNING ;
            case 'E': return Log.Severity.ERROR ;
            default: return Log.Severity.INFO ;
        }
    }
}
