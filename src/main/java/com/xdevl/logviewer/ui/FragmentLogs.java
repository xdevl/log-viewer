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
import com.xdevl.logviewer.model.LogReader;

import java.util.EnumSet;

public class FragmentLogs extends Fragment implements LogReader.OnErrorListener, DialogInterface.OnMultiChoiceClickListener, DialogInterface.OnClickListener,
		SearchView.OnQueryTextListener, MenuItemCompat.OnActionExpandListener
{
	private static final String ARGUMENT_TYPE=FragmentLogs.class.getName()+".ARGUMENT_TYPE" ;
	private static final String ADAPTER_ID=FragmentLogs.class.getName()+".ADAPTER_ID" ;

	private FragmentState mFragmentState ;
	private boolean mSavedInstance=false ;
	private AdapterLog mAdapter ;
	private LinearLayoutManager mLayoutManager ;
	private RecyclerView mRecyclerView ;
	private TextView mEmptyView ;
	private boolean mFilters[]=new boolean[Log.Severity.values().length] ;
	private SearchView mSearchView ;

	public static FragmentLogs createFragment(String type)
	{
		Bundle bundle=new Bundle() ;
		bundle.putString(ARGUMENT_TYPE,type) ;
		FragmentLogs fragment=new FragmentLogs() ;
		fragment.setArguments(bundle) ;
		return fragment ;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState)
	{
		View view=inflater.inflate(R.layout.fragment_logs,null) ;
		FragmentState.AdapterType type=FragmentState.AdapterType.valueOf(getArguments().getString(ARGUMENT_TYPE)) ;
		((Activity)getActivity()).setTitle(getString(type==FragmentState.AdapterType.LOGCAT?
				R.string.title_logcat:R.string.title_dmesg),true) ;
		setHasOptionsMenu(true) ;
		mFragmentState=FragmentState.getInstance(getFragmentManager()) ;
		mRecyclerView=(RecyclerView)view.findViewById(R.id.logs) ;
		mLayoutManager=new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,true) ;
		mRecyclerView.setLayoutManager(mLayoutManager) ;
		// Animations don't seem to work well when inserting and removing elements simultaneously :/
		mRecyclerView.setItemAnimator(null) ;

		if(savedInstanceState!=null && savedInstanceState.containsKey(ADAPTER_ID))
			mAdapter=mFragmentState.getAdapter(savedInstanceState.getInt(ADAPTER_ID)) ;
		else mAdapter=mFragmentState.getAdapter(getContext(),type) ;
		mRecyclerView.setAdapter(mAdapter) ;
		mFragmentState.registerErrorListener(mAdapter.mId,this) ;

		mEmptyView=(TextView)view.findViewById(R.id.empty_view) ;

		return view;
	}

	@Override
	public void onResume()
	{
		super.onResume() ;
		mSavedInstance=false ;
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState) ;
		outState.putInt(ADAPTER_ID,mAdapter.mId) ;
		mSavedInstance=true ;
	}

	@Override
	public void onDestroyView()
	{
		super.onStop() ;
		mFragmentState.registerErrorListener(mAdapter.mId,null) ;
		// Freeing those resources could be triggered from the activity instead of tracking
		// the fragment saved instance state
		if(!mSavedInstance)
			mFragmentState.releaseAdapter(mAdapter.mId) ;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu,MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu,inflater) ;
		inflater.inflate(R.menu.logs,menu) ;
		((Activity)getActivity()).tintMenuIcons(menu) ;
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
				for(Log.Severity severity: Log.Severity.values())
					mFilters[severity.ordinal()]=mAdapter.getFilters().contains(severity) ;
				AlertDialog.Builder builder=new AlertDialog.Builder(getActivity()) ;
				builder.setTitle(R.string.label_filter)
						.setMultiChoiceItems(R.array.filters,mFilters,this)
						.setPositiveButton(android.R.string.ok,this).create().show() ;
				break ;
			case R.id.action_search:
				mSearchView=(SearchView)MenuItemCompat.getActionView(item) ;
				mSearchView.setOnQueryTextListener(this) ;
				MenuItemCompat.setOnActionExpandListener(item,this) ;
				mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH) ;
				break ;
			default:
				return super.onOptionsItemSelected(item) ;
		}
		return true ;
	}

	public void scrollToNextMatch()
	{
		mRecyclerView.scrollToPosition(mAdapter.getMatchingPosition(
				mLayoutManager.findFirstCompletelyVisibleItemPosition(),true)) ;
		refresh() ;
	}

	public void scrollToPreviousMatch()
	{
		mRecyclerView.scrollToPosition(mAdapter.getMatchingPosition(
				mLayoutManager.findLastCompletelyVisibleItemPosition(),false)) ;
		refresh() ;
	}

	public void refresh()
	{
		int firstVisible=mLayoutManager.findFirstVisibleItemPosition(),lastVisible=mLayoutManager.findLastVisibleItemPosition() ;
		mRecyclerView.getAdapter().notifyItemRangeChanged(firstVisible,lastVisible-firstVisible+1) ;
	}

	// If this listener isn't passed to the dialog builder, tmpFilters doesn't get updated
	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		EnumSet<Log.Severity> filters=EnumSet.noneOf(Log.Severity.class) ;
		for(Log.Severity severity: Log.Severity.values())
			if(mFilters[severity.ordinal()])
				filters.add(severity) ;
		mAdapter.filter(filters) ;
	}

	@Override
	public boolean onQueryTextSubmit(String query)
	{
		if(mAdapter.search(query))
		{
			mRecyclerView.scrollToPosition(mAdapter.getMatchingPosition(0,true)) ;
			refresh() ;
		}
		mSearchView.clearFocus() ;
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
		mAdapter.search(null) ;
		refresh() ;
		return true ;
	}

	@Override
	public void onError(Exception exception)
	{
		mRecyclerView.setVisibility(View.GONE) ;
		mEmptyView.setVisibility(View.VISIBLE) ;
		mEmptyView.setText(R.string.msg_root_required) ;
	}
}
