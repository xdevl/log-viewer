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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import com.xdevl.logviewer.model.DmesgReader;
import com.xdevl.logviewer.model.LogReader;
import com.xdevl.logviewer.model.LogcatReader;

import java.util.HashMap;
import java.util.Map;

public class FragmentState extends Fragment
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
        LOGCAT, DMESG
    }

    private static final String TAG=FragmentState.class.getName() ;

    private int mCount=1 ;
    private Map<Integer,AdapterLog> mAdapters=new HashMap<>();
    private Map<Integer,LogReader> mReaders=new HashMap<>() ;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState) ;
        setRetainInstance(true) ;
    }

    public AdapterLog getAdapter(Context context, AdapterType type)
    {
        AdapterLog adapter=new AdapterLog(mCount++,20480) ;
        mAdapters.put(adapter.mId,adapter) ;
        LogReader reader=type==AdapterType.LOGCAT?new LogcatReader(context,adapter):new DmesgReader(context,adapter) ;
        mReaders.put(adapter.mId,reader) ;
        new Thread(reader).start() ;
        return adapter ;
    }

    public AdapterLog getAdapter(int id)
    {
        return mAdapters.get(id) ;
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
}
