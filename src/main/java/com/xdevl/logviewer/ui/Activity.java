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

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import com.xdevl.logviewer.R;
import com.xdevl.logviewer.model.ExportRunnable;

public class Activity extends AppCompatActivity
{
	private FragmentState mFragmentState ;
	private int mMenuIconTint ;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState) ;
		setContentView(R.layout.activity) ;

		Toolbar toolBar=(Toolbar)findViewById(R.id.tool_bar) ;
		setSupportActionBar(toolBar) ;

		mFragmentState=FragmentState.getInstance(getSupportFragmentManager()) ;
		mFragmentState.setCoordinatorLayout((CoordinatorLayout)findViewById(R.id.coordinator_layout)) ;

		TypedValue typedValue=new TypedValue() ;
		toolBar.getContext().getTheme().resolveAttribute(android.R.attr.textColorSecondary,typedValue,true) ;
		mMenuIconTint=ContextCompat.getColor(toolBar.getContext(),typedValue.resourceId) ;

		if(savedInstanceState==null)
			getSupportFragmentManager().beginTransaction().add(R.id.content,new FragmentCards()).commit() ;

		Intent intent=getIntent() ;
		if(Intent.ACTION_SEND.equals(intent.getAction()) && savedInstanceState==null)
			new Thread(new ExportRunnable(getApplicationContext(),(Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM),mFragmentState)).start() ;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy() ;
		mFragmentState.setCoordinatorLayout(null) ;
	}

	@Override
	public void onBackPressed()
	{
		back() ;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
				back() ;
				break ;
			default: return super.onOptionsItemSelected(item) ;
		}
		return true ;
	}

	public void back()
	{
		if(getSupportFragmentManager().getBackStackEntryCount()>0)
			getSupportFragmentManager().popBackStack() ;
		else moveTaskToBack(true) ;
	}

	public void setTitle(String title,boolean backButton)
	{
		getSupportActionBar().setTitle(title) ;
		getSupportActionBar().setDisplayHomeAsUpEnabled(backButton) ;
	}

	protected void displayFragment(Fragment fragment)
	{
		getSupportFragmentManager().beginTransaction().
				replace(R.id.content, fragment).addToBackStack(null).commit() ;
	}

	public void tintMenuIcons(Menu menu)
	{
		for(int i=0;i<menu.size() && mMenuIconTint!=0;++i)
		{
			Drawable icon=menu.getItem(i).getIcon() ;
			if(icon!=null)
				DrawableCompat.setTint(DrawableCompat.wrap(icon),mMenuIconTint) ;
		}
	}
}
