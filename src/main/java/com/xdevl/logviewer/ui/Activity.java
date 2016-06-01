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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.xdevl.logviewer.R;

public class Activity extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState) ;
		setContentView(R.layout.activity) ;

		Toolbar toolBar=(Toolbar)findViewById(R.id.tool_bar) ;
		setSupportActionBar(toolBar) ;

		if(savedInstanceState==null)
			getSupportFragmentManager().beginTransaction().add(R.id.content,new FragmentCards()).commit() ;
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
}
