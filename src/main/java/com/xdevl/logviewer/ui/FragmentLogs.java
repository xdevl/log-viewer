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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.xdevl.logviewer.R;
import com.xdevl.logviewer.bean.Log;
import com.xdevl.logviewer.model.Model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FragmentLogs extends Fragment implements DialogInterface.OnMultiChoiceClickListener, DialogInterface.OnClickListener,
		SearchView.OnQueryTextListener, MenuItemCompat.OnActionExpandListener
{
	public static final int ADB_LOGS=0 ;
	public static final int KERNEL_LOGS=1 ;

	private static final String ARGUMENT_TYPE=FragmentLogs.class.getName()+".ARGUMENT_TYPE" ;

	private RecyclerView mRecyclerView ;
	private TextView mEmptyView ;
	private List<Log> mAllLogs ;
	// In order: info, error, warning and debug
	private boolean mFilters[]={true,true,true,true}, mTmpFilters[]=new boolean[mFilters.length] ;
	private String mSearch=null ;
	private List<Integer> mMatchingPositions=new ArrayList<Integer>() ;

	public static FragmentLogs createFragment(int type)
	{
		Bundle bundle=new Bundle() ;
		bundle.putInt(ARGUMENT_TYPE,type) ;
		FragmentLogs fragment=new FragmentLogs() ;
		fragment.setArguments(bundle) ;
		return fragment ;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState)
	{
		View view=inflater.inflate(R.layout.fragment_logs,null) ;
		((Activity)getActivity()).setTitle(getString(getArguments().getInt(ARGUMENT_TYPE,ADB_LOGS)==ADB_LOGS?
				R.string.title_logcat:R.string.title_dmesg),true) ;
		setHasOptionsMenu(true) ;
		mRecyclerView=(RecyclerView)view.findViewById(R.id.logs) ;
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity())) ;
		mEmptyView=(TextView)view.findViewById(R.id.empty_view) ;

		try {
			// TODO: log fetching and parsing should be done in a dedicated thread
			mAllLogs=getArguments().getInt(ARGUMENT_TYPE,ADB_LOGS)==ADB_LOGS?
					Model.getLogCat(getContext()):Model.getDmesg() ;
		} catch (IOException e) {
			// If we get an exception we always assume su permission hasn't been granted
			mEmptyView.setText(R.string.msg_root_required) ;
		}

		update(mAllLogs) ;

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu,MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu,inflater) ;
		if(mAllLogs!=null && !mAllLogs.isEmpty())
			inflater.inflate(R.menu.logs,menu) ;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_up:
				scrollToPreviousMatch() ;
				break;
			case R.id.action_down:
				scrollToNextMatch() ;
				break;
			case R.id.action_filter:
				System.arraycopy(mFilters,0,mTmpFilters,0,mFilters.length) ;
				AlertDialog.Builder builder=new AlertDialog.Builder(getActivity()) ;
				builder.setTitle(R.string.label_filter)
						.setMultiChoiceItems(R.array.filters,mTmpFilters,this)
						.setPositiveButton(android.R.string.ok,this).create().show() ;
				break ;
			case R.id.action_search:
				SearchView searchView=(SearchView)MenuItemCompat.getActionView(item) ;
				searchView.setOnQueryTextListener(this) ;
				MenuItemCompat.setOnActionExpandListener(item,this) ;
				searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH) ;
				break ;
			default:
				return super.onOptionsItemSelected(item) ;
		}
		return true ;
	}

	public void update(List<Log> entries)
	{
		mRecyclerView.setAdapter(new AdapterLog(entries)) ;
		boolean isEmpty=entries==null || entries.isEmpty() ;
		mRecyclerView.setVisibility(isEmpty?View.GONE:View.VISIBLE) ;
		mEmptyView.setVisibility(isEmpty?View.VISIBLE:View.GONE) ;
	}

	public void filterLogs()
	{
		List<Log> filteredLogEntries=new ArrayList<Log>() ;

		for(Log logEntry: mAllLogs)
			if(mFilters[getSeverityIndex(logEntry.mSeverity)])
				filteredLogEntries.add(logEntry) ;
		update(filteredLogEntries) ;
		if(mSearch!=null && !mSearch.isEmpty())
			searchLogs(mSearch) ;
	}

	public void searchLogs(String search)
	{
		mSearch=search ;
		AdapterLog adapter=(AdapterLog)mRecyclerView.getAdapter() ;
		adapter.setHighliht(mSearch) ;
		mMatchingPositions.clear() ;

		int position=0 ;
		for(Log logEntry: adapter.getLogEntries())
		{
			if(logEntry.mContent.toLowerCase().contains(search.toLowerCase()))
			{
				if(mMatchingPositions.isEmpty())
				{
					mRecyclerView.scrollToPosition(position) ;
					refresh() ;
				}
				mMatchingPositions.add(position) ;
			}

			++position ;
		}
	}

	public void scrollToNextMatch()
	{
		int match,from=((LinearLayoutManager)mRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() ;
		for(match=0;match<mMatchingPositions.size() && mMatchingPositions.get(match)<=from;++match) ;
		mRecyclerView.scrollToPosition(match==mMatchingPositions.size()?
				(mMatchingPositions.isEmpty()?mRecyclerView.getAdapter().getItemCount()-1:mMatchingPositions.get(0)):mMatchingPositions.get(match)) ;
	}

	public void scrollToPreviousMatch()
	{
		int match,from=((LinearLayoutManager)mRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition() ;
		for(match=mMatchingPositions.size()-1;match>=0 && mMatchingPositions.get(match)>=from;--match) ;
		mRecyclerView.scrollToPosition(match<0?
				(mMatchingPositions.isEmpty()?0:mMatchingPositions.get(mMatchingPositions.size()-1)):mMatchingPositions.get(match)) ;
	}

	public void refresh()
	{
		LinearLayoutManager layoutManager=(LinearLayoutManager)mRecyclerView.getLayoutManager() ;
		int firstVisible=layoutManager.findFirstVisibleItemPosition(),lastVisible=layoutManager.findLastVisibleItemPosition() ;
		mRecyclerView.getAdapter().notifyItemRangeChanged(firstVisible,lastVisible-firstVisible) ;
	}

	private int getSeverityIndex(Log.Severity severity)
	{
		switch(severity)
		{
			case ERROR:
				return 0 ;
			case DEBUGGING:
				return 1 ;
			case WARNING:
				return 2 ;
			default:
				return 3 ;
		}
	}

	// If this listener isn't passed to the dialog builder, tmpFilters doesn't get updated
	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		System.arraycopy(mTmpFilters,0,mFilters,0,mFilters.length) ;
		filterLogs() ;
	}

	@Override
	public boolean onQueryTextSubmit(String query)
	{
		if(mSearch==null || mSearch.compareTo(query)!=0)
			searchLogs(query) ;
		else scrollToNextMatch() ;

		return true ;
	}

	@Override
	public boolean onQueryTextChange(String newText)
	{
		return false;
	}

	@Override
	public boolean onMenuItemActionExpand(MenuItem item)
	{
		return true ;
	}

	@Override
	public boolean onMenuItemActionCollapse(MenuItem item)
	{
		((AdapterLog)mRecyclerView.getAdapter()).setHighliht(null) ;
		mMatchingPositions.clear() ;
		refresh() ;
		return true ;
	}
}
