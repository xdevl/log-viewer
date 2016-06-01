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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.xdevl.logviewer.R;
import org.w3c.dom.Text;

import java.util.Calendar;

public class FragmentAbout extends Fragment
{
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState)
	{

		View rootView=inflater.inflate(R.layout.fragment_about,container,false) ;
		((Activity)getActivity()).setTitle(getString(R.string.title_about),true) ;
		try {
			PackageInfo info=getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(),0) ;
			this.<TextView>findViewById(rootView,R.id.app_version).setText(info.versionName) ;
			this.<TextView>findViewById(rootView,R.id.copyright).setText(getString(R.string.msg_copyright,Calendar.getInstance().get(Calendar.YEAR))) ;
		} catch(PackageManager.NameNotFoundException e) {}
		return rootView ;
	}

	protected <T extends View> T findViewById(View view, int id)
	{
		return (T)view.findViewById(id) ;
	}
}
