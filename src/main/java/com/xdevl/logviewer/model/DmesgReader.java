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

public class DmesgReader extends LogReader
{
    public DmesgReader(Context context, OnLogParsedListener onLogParsedListener)
    {
        super(context,onLogParsedListener) ;
    }

    @Override
    public String getTitle()
    {
        return mContext.getString(R.string.title_dmesg) ;
    }

    @Override
    protected InputStream streamLogs() throws IOException
    {
        String cmd[]={"cat","/proc/kmsg"} ;
        return Model.runRootCommand(true,cmd) ;
    }

    @Override
    protected Log parse(String line)
    {
        return new Log(getSeverity(line),line) ;
    }

    public Log.Severity getSeverity(String logLine)
    {
        return logLine.charAt(1)<'4'?Log.Severity.ERROR:Log.Severity.INFO ;
    }
}
