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

import android.content.ContentProviderClient;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import com.xdevl.logviewer.bean.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UriReader extends LogReader
{
    private final Uri mUri ;
    private ContentProviderClient mClient ;

    public UriReader(Context context, OnLogParsedListener onLogParsedListener, Uri uri)
    {
        super(context,onLogParsedListener) ;
        mUri=uri ;
    }

    @Override
    public String getTitle()
    {
        return mUri!=null?mUri.getLastPathSegment():"" ;
    }

    @Override
    protected InputStream streamLogs() throws IOException
    {
        if(mUri!=null && "file".equals(mUri.getScheme()))
            return new FileInputStream(mUri.getPath()) ;
        else if(mUri!=null)
        {
            mClient=mContext.getContentResolver().acquireContentProviderClient(mUri) ;
            try {
                if(mClient!=null)
                    return new ParcelFileDescriptor.AutoCloseInputStream(mClient.openFile(mUri,"r")) ;
            } catch(RemoteException e) {
                throw new IOException(e) ;
            }
        }
        throw new IOException("Unable to open uri: "+mUri) ;
    }

    @Override
    protected Log parse(String line)
    {
        return new Log(Log.Severity.DEBUGGING,line) ;
    }

    @Override
    protected void onStopped()
    {
        if(mClient!=null)
            mClient.release() ;
    }
}
