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
import android.os.Handler;
import android.os.Looper;
import com.xdevl.logviewer.bean.Log;

import java.io.*;

public abstract class LogReader implements Runnable
{
    public interface OnLogParsedListener
    {
        void onLogParsed(Log log) ;
    }

    public interface OnErrorListener
    {
        void onError(Exception exception) ;
    }

    protected static final Handler sHandler=new Handler(Looper.getMainLooper()) ;

    protected abstract InputStream streamLogs() throws IOException ;
    protected abstract Log parse(String line) ;

    protected final Context mContext ;
    private final BufferedReader mReader ;
    protected final OnLogParsedListener mOnLogParsedListener ;
    private volatile OnErrorListener mOnErrorListener ;
    private volatile Exception mException ;

    public LogReader(Context context, OnLogParsedListener onLogParsedListener)
    {
        mContext=context ;
        mReader=streamLogsWrapper() ;
        mOnLogParsedListener=onLogParsedListener ;
    }

    // Wrapper function to initialize the buffered reader
    private BufferedReader streamLogsWrapper()
    {
        try {
            return new BufferedReader(new InputStreamReader(streamLogs())) ;
        } catch(IOException e) {
            notify(e) ;
            return new BufferedReader(new StringReader("")) ;
        }
    }

    protected void notify(final Log log)
    {
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnLogParsedListener.onLogParsed(log); ;
            }
        }) ;
    }

    protected void notify(final Exception exception)
    {
        mException=exception ;
        if(mOnErrorListener!=null)
            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    mOnErrorListener.onError(mException) ;
                }
            }) ;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener)
    {
        mOnErrorListener=onErrorListener ;
        if(mException!=null)
            notify(mException) ;
    }

    public void stop()
    {
        // Closing the reader can take a bit of time if it is attached to a process input stream
        new Thread(new Runnable() {
            @Override
            public void run() {
                try { mReader.close(); } catch(IOException e) {}
            }
        }).start() ;
    }

    @Override
    public void run()
    {
        try {
            String line;
            while((line=mReader.readLine())!=null)
                notify(parse(line)) ;
            mReader.close() ;
        } catch(IOException e) {
            stop() ;
            notify(e) ;
        }
    }
}
