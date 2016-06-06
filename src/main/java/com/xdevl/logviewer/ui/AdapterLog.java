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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class AdapterLog extends RecyclerView.Adapter<AdapterLog.ViewHolder> implements LogReader.OnLogParsedListener
{
	public final int mId ;
	private final int mSize ;
	private List<Log> mAllLogs, mFilteredLogs ;
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
			mContent.setTextColor(mContent.getContext().getResources().getColor(colorRes)) ;
		}
	}

	public AdapterLog(int id, int size)
	{
		mId=id ;
		mSize=size ;
		mAllLogs=new ArrayList<>() ;
		mFilteredLogs=new ArrayList<>() ;
	}
	
	@Override
	public int getItemCount()
	{
		return mFilteredLogs.size() ;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		Log logEntry=mFilteredLogs.get(position) ;
		holder.setLog(logEntry,mSearch) ;
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
		mAllLogs.add(log) ;
		if(isFiltered(log))
		{
			mFilteredLogs.add(log);
			notifyItemInserted(mFilteredLogs.size()-1);
		}
		if(mAllLogs.size()>mSize && isFiltered(mAllLogs.remove(0)))
		{
			mFilteredLogs.remove(0) ;
			notifyItemRemoved(0) ;
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
			return next?mFilteredLogs.size()-1:0 ;

		int step=next?1:-1, i=position ;
		do {
			i+=step ;
			if(i>=mFilteredLogs.size())
				i=0 ;
			else if(i<0)
				i=mFilteredLogs.size()-1 ;
		} while(!mFilteredLogs.get(i).mMatch && i!=position) ;
		return i ;
	}
}
	
