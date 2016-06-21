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

    public abstract String getTitle() ;
    protected abstract InputStream streamLogs() throws IOException ;
    protected abstract Log parse(String line) ;

    protected final Context mContext ;
    private volatile BufferedReader mReader ;
    protected final OnLogParsedListener mOnLogParsedListener ;
    private volatile OnErrorListener mOnErrorListener ;
    private volatile Exception mException ;

    public LogReader(Context context, OnLogParsedListener onLogParsedListener)
    {
        mContext=context ;
        mOnLogParsedListener=onLogParsedListener ;
    }

    public final void start()
    {
        try {
            mReader=new BufferedReader(new InputStreamReader(streamLogs())) ;
        } catch(IOException e) {
            notify(e) ;
            mReader=new BufferedReader(new StringReader("")) ;
        }
        new Thread(this).start() ;
    }

    protected final void notify(final Log log)
    {
        if(Thread.currentThread()==sHandler.getLooper().getThread())
            mOnLogParsedListener.onLogParsed(log) ;
        else sHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnLogParsedListener.onLogParsed(log) ;
            }
        }) ;
    }

    protected final void notify(final Exception exception)
    {
        mException=exception ;
        if(mOnErrorListener!=null)
        {
            if(Thread.currentThread()==sHandler.getLooper().getThread())
                mOnErrorListener.onError(mException) ;
            else sHandler.post(new Runnable() {
                @Override
                public void run() {
                    mOnErrorListener.onError(mException);
                }
            });
        }
    }

    public final void setOnErrorListener(OnErrorListener onErrorListener)
    {
        mOnErrorListener=onErrorListener ;
        if(mException!=null)
            notify(mException) ;
    }

    public final void stop()
    {
        if(Thread.currentThread()!=sHandler.getLooper().getThread())
            try { mReader.close(); } catch(IOException e) {}
        // Closing the reader can take a bit of time if it is attached to a process input stream
        else new Thread(new Runnable() {
            @Override
            public void run() {
                try { mReader.close(); } catch(IOException e) {}
            }
        }).start() ;
    }

    @Override
    public final void run()
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
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                onStopped() ;
            }
        }) ;
    }

    /**
     * Subclasses can overload this method to free resources when the reader has stopped.
     */
    protected void onStopped() {}
}
