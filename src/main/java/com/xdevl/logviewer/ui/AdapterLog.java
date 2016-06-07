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

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.xdevl.logviewer.R;
import com.xdevl.logviewer.bean.Log;
import com.xdevl.logviewer.model.LogReader;

import java.util.EnumSet;
import java.util.LinkedList;

public class AdapterLog extends RecyclerView.Adapter<AdapterLog.ViewHolder> implements LogReader.OnLogParsedListener
{
	public final int mId ;
	private final int mSize ;
	private final LinkedList<Log> mAllLogs=new LinkedList<>(), mFilteredLogs=new LinkedList<>() ;
	private final Log mEmptyLog=new Log(Log.Severity.INFO,"") ;
	private EnumSet<Log.Severity> mFilters=EnumSet.allOf(Log.Severity.class) ;
	private String mSearch ;

	static class ViewHolder extends RecyclerView.ViewHolder
	{
		private final TextView mContent ;

		public ViewHolder(View itemView)
		{
			super(itemView) ;
			mContent=(TextView)itemView.findViewById(R.id.content) ;
		}

		public void setLog(Log log, String highlight)
		{
			Spannable content=new SpannableString(log.mContent) ;
			int matchingIndex=0 ;
			while(highlight!=null && (matchingIndex=log.mContent.toLowerCase().indexOf(highlight.toLowerCase(),matchingIndex))!=-1)
				content.setSpan(new BackgroundColorSpan(Color.LTGRAY),matchingIndex,(matchingIndex+=highlight.length()),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) ;
			mContent.setText(content) ;

			switch(log.mSeverity)
			{
				case ERROR:
					setTextViewColor(R.color.error) ;
					break ;
				case DEBUGGING:
					setTextViewColor(R.color.debug) ;
					break ;
				case WARNING:
					setTextViewColor(R.color.warning) ;
					break ;
				default:
					setTextViewColor(R.color.info) ;
					break ;
			}
		}

		private void setTextViewColor(int colorRes)
		{
			mContent.setTextColor(ContextCompat.getColor(mContent.getContext(),colorRes)) ;
		}
	}

	public AdapterLog(int id, int size)
	{
		mId=id ;
		mSize=size ;
	}
	
	@Override
	public int getItemCount()
	{
		return mFilteredLogs.size()+1 ;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		Log log=position>0?mFilteredLogs.get(position-1):mEmptyLog ;
		holder.setLog(log,mSearch) ;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.view_log_entry,parent,false) ;
		return new ViewHolder(view) ;
	}

	@Override
	public void onLogParsed(Log log)
	{
		log.setMatch(mSearch) ;
		mAllLogs.addFirst(log) ;
		if(isFiltered(log))
		{
			mFilteredLogs.addFirst(log);
			notifyItemInserted(1);
		}
		if(mAllLogs.size()>mSize && isFiltered(mAllLogs.removeLast()))
		{
			mFilteredLogs.remove(mFilteredLogs.removeLast()) ;
			notifyItemRemoved(mFilteredLogs.size()) ;
		}
	}

	protected boolean isFiltered(Log log)
	{
		return mFilters==null || mFilters.contains(log.mSeverity) ;
	}

	public void filter(EnumSet<Log.Severity> filters)
	{
		mFilters=filters ;
		mFilteredLogs.clear() ;
		for(Log log: mAllLogs)
			if(isFiltered(log))
				mFilteredLogs.add(log) ;
		notifyDataSetChanged() ;
	}

	public EnumSet<Log.Severity> getFilters()
	{
		return mFilters ;
	}

	public boolean search(String search)
	{
		if(search==mSearch || (search!=null && search.equals(mSearch)))
			return false ;

		mSearch=search ;
		for(Log log: mAllLogs)
			log.setMatch(mSearch) ;
		return true ;
	}

	public int getNextMatchingPosition(int position, boolean next)
	{
		if(mFilteredLogs.isEmpty())
			return 0 ;
		else if(mSearch==null)
			return next?0:mFilteredLogs.size() ;

		int step=next?-1:1, i=position ;
		do {
			i+=step ;
			if(i>mFilteredLogs.size())
				i=1 ;
			else if(i<1)
				i=mFilteredLogs.size() ;
		} while(!mFilteredLogs.get(i-1).mMatch && i!=position) ;
		return i ;
	}
}
	
