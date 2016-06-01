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
import com.xdevl.logviewer.bean.Log;
import com.xdevl.logviewer.R;

import java.util.List;

public class AdapterLog extends RecyclerView.Adapter<AdapterLog.ViewHolder>
{
	private List<Log> mLogs ;
	private String mHighlight ;

	static class ViewHolder extends RecyclerView.ViewHolder
	{
		private final TextView mContent ;

		public ViewHolder(View itemView)
		{
			super(itemView) ;
			mContent=(TextView)itemView.findViewById(R.id.content) ;
		}

		public void setLog(Log log,String highlight)
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

	public AdapterLog(List<Log> logs)
	{
		mLogs=logs ;
	}
	
	@Override
	public int getItemCount()
	{
		return mLogs.size() ;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		Log logEntry=mLogs.get(position) ;
		holder.setLog(logEntry,mHighlight) ;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.view_log_entry,parent,false) ;
		return new ViewHolder(view) ;
	}

	public List<Log> getLogEntries()
	{
		return mLogs ;
	}

	public void setHighliht(String highlight)
	{
		mHighlight=highlight ;
	}
}
	
