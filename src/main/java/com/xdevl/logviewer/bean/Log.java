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
package com.xdevl.logviewer.bean;

public class Log
{
	public enum Severity
	{
		INFO,WARNING,ERROR,DEBUGGING ;
	}

	public long mId ;
	public Severity mSeverity ;
	public String mContent ;

	public Log(long id,Severity severity,String content)
	{
		mId=id ;
		mSeverity=severity ;
		mContent=content ;
	}
}
