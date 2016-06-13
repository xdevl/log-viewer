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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogProvider extends ContentProvider
{
    public static final String AUTHORITY="com.xdevl.logprovider" ;
    public static final String ZIP_MIME_TYPE="application/zip" ;

    public static String getFileName()
    {
        return "logs - "+Build.MODEL+" - "+DATE_FORMAT.format(new Date())+".zip" ;
    }

    private static final DateFormat DATE_FORMAT=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") ;

    private static final String[] COLUMNS={
            OpenableColumns.DISPLAY_NAME,
            OpenableColumns.SIZE
    } ;

    @Override
    public boolean onCreate()
    {
        return true ;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        if(projection==null)
            projection=COLUMNS ;

        String[] columns=new String[projection.length] ;
        Object[] values=new Object[projection.length] ;
        int i=0 ;
        for(String col: projection)
        {
            if(OpenableColumns.DISPLAY_NAME.equals(col))
            {
                columns[i]=OpenableColumns.DISPLAY_NAME ;
                values[i++]=getFileName() ;
            }
            else if(OpenableColumns.SIZE.equals(col))
            {
                columns[i]=OpenableColumns.SIZE;
                // We can't predict the exact size of the final zip file so we give a constant
                // rough estimate of 1Mb instead
                values[i++]=1024*1024 ;
            }
        }

        MatrixCursor cursor=new MatrixCursor(columns,1) ;
        cursor.addRow(values) ;
        return cursor ;
    }

    @Nullable
    @Override
    public String getType(Uri uri)
    {
        return ZIP_MIME_TYPE ;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri,ContentValues contentValues)
    {
        throw new UnsupportedOperationException() ;
    }

    @Override
    public int delete(Uri uri,String s,String[] strings)
    {
        throw new UnsupportedOperationException() ;
    }

    @Override
    public int update(Uri uri,ContentValues contentValues,String s,String[] strings)
    {
        throw new UnsupportedOperationException() ;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException
    {
        try {
            final ParcelFileDescriptor pipes[]=ParcelFileDescriptor.createPipe() ;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Model.zipLogs(getContext(),new ParcelFileDescriptor.AutoCloseOutputStream(pipes[1])) ;
                }
            }).start() ;
            return pipes[0] ;
        } catch(IOException e) {
            throw new FileNotFoundException(e.getMessage()) ;
        }
    }
}
