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
package com.xdevl.logviewer.bean;

import java.io.IOException;
import java.io.InputStream;

public class ProcessInputStream extends InputStream
{
    private final boolean mEndless ;
    private final Process mProcess ;
    private final InputStream mInputStream ;

    public ProcessInputStream(boolean endless, Process process) throws IOException
    {
        mEndless=endless ;
        mProcess=process ;
        mInputStream=process.getInputStream() ;
        if(mInputStream==null)
            throw new IOException("Process input stream unavailable") ;
    }

    @Override
    public int read(byte[] buffer) throws IOException
    {
        return mInputStream.read(buffer) ;
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException
    {
        return mInputStream.read(buffer,byteOffset,byteCount) ;
    }

    @Override
    public int read() throws IOException
    {
        return mInputStream.read() ;
    }

    @Override
    public void close() throws IOException
    {
        mInputStream.close() ;
        if(mEndless)
            mProcess.destroy() ;
        try {
            int exitValue=mProcess.waitFor() ;
            if(exitValue!=0 && !mEndless)
                throw new IOException("Process exited with value: "+exitValue);
        } catch(InterruptedException e) {
            throw new IOException(e) ;
        }
    }
}
