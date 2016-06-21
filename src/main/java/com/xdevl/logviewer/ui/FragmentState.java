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
package com.xdevl.logviewer.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.view.View;
import com.xdevl.logviewer.model.*;

import java.util.HashMap;
import java.util.Map;

public class FragmentState extends Fragment implements ExportRunnable.Notifier
{
    public static FragmentState getInstance(FragmentManager fragmentManager)
    {
        FragmentState fragmentState=(FragmentState)fragmentManager.findFragmentByTag(TAG) ;
        if(fragmentState==null)
        {
            fragmentState=new FragmentState() ;
            fragmentManager.beginTransaction().add(fragmentState,TAG).commit() ;
        }
        return fragmentState ;
    }

    public enum AdapterType
    {
        LOGCAT, DMESG, URI
    }

    private static final String TAG=FragmentState.class.getName() ;

    private static LogReader getLogReader(Context context, LogReader.OnLogParsedListener onLogParsedListener, AdapterType type, Uri uri)
    {
        switch(type)
        {
            case DMESG:
                return new DmesgReader(context,onLogParsedListener) ;
            case URI:
                return new UriReader(context,onLogParsedListener,uri) ;
            default:
                return new LogcatReader(context,onLogParsedListener) ;
        }
    }

    private int mCount=1 ;
    private Map<Integer,AdapterLog> mAdapters=new HashMap<>();
    private Map<Integer,LogReader> mReaders=new HashMap<>() ;
    private CoordinatorLayout mCoordinatorLayout ;
    private String mNotificationText ;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState) ;
        setRetainInstance(true) ;
    }

    @Override
    public void notify(String text)
    {
        mNotificationText=text ;
        if(mCoordinatorLayout!=null)
        {
            Snackbar.make(mCoordinatorLayout,Html.fromHtml(text),Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok,new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mNotificationText=null ;
                }
            }).show() ;
        }
    }

    public AdapterLog getAdapter(Context context, AdapterType type, Uri uri)
    {
        AdapterLog adapter=new AdapterLog(mCount++,20480) ;
        mAdapters.put(adapter.mId,adapter) ;
        LogReader reader=getLogReader(context,adapter,type,uri) ;
        mReaders.put(adapter.mId,reader) ;
        reader.start() ;
        return adapter ;
    }

    public AdapterLog getAdapter(int id)
    {
        return mAdapters.get(id) ;
    }

    public String getTitle(int id)
    {
        return mReaders.get(id).getTitle() ;
    }

    public void registerErrorListener(int id, LogReader.OnErrorListener onErrorListener)
    {
        mReaders.get(id).setOnErrorListener(onErrorListener) ;
    }

    public void releaseAdapter(int id)
    {
        mAdapters.remove(id) ;
        mReaders.remove(id).stop() ;
    }

    public void setCoordinatorLayout(CoordinatorLayout coordinatorLayout)
    {
        mCoordinatorLayout=coordinatorLayout ;
        if(mNotificationText!=null)
            notify(mNotificationText) ;
    }
}
