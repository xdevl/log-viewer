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
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import com.xdevl.logviewer.R;

public class ExportRunnable implements Runnable
{
    public interface Notifier
    {
        void notify(String text) ;
    }

    private Handler mHandler=new Handler(Looper.getMainLooper()) ;
    private Context mContext ;
    private Uri mUri ;
    private Notifier mNotifier ;

    public ExportRunnable(Context context, Uri uri, Notifier notifier)
    {
        mContext=context.getApplicationContext() ;
        mUri=uri ;
        mNotifier=notifier ;
    }

    @Override
    public void run()
    {
        ContentProviderClient client=mContext.getContentResolver().acquireContentProviderClient(mUri) ;
        if(client!=null) {
            try {
                ParcelFileDescriptor fd=client.openFile(mUri,"r") ;
                String path=Model.exportToStorage(new ParcelFileDescriptor.AutoCloseInputStream(fd)) ;
                notify(mContext.getString(R.string.msg_exported_to,path)) ;
            } catch(Exception e) {
                notify(mContext.getString(R.string.msg_export_failed,e.getMessage())) ;
            } finally {
                client.release() ;
            }
        }
    }

    public void notify(final String text)
    {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mNotifier.notify(text) ;
            }
        }) ;
    }
}
