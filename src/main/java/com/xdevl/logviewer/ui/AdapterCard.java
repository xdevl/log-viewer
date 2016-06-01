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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.xdevl.logviewer.R;
import com.xdevl.logviewer.bean.Card;

import java.util.List;

public class AdapterCard extends RecyclerView.Adapter<AdapterCard.ViewHolder>
{
	public interface OnCardSelectedListener
	{
		void onCardSelected(Card card) ;
	}

	private Context mContext ;
	private OnCardSelectedListener mListener ;
	private List<Card> mCards ;

	static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		private Card mCard ;
		private final OnCardSelectedListener mListener ;
		private final ImageView mIcon ;
		private final TextView mTitle ;
		private final TextView mDescription ;

		public ViewHolder(View itemView, OnCardSelectedListener listener)
		{
			super(itemView) ;
			itemView.setOnClickListener(this) ;
			mListener=listener ;
			mIcon=(ImageView)itemView.findViewById(R.id.icon) ;
			mTitle=(TextView)itemView.findViewById(R.id.title) ;
			mDescription=(TextView)itemView.findViewById(R.id.description) ;
		}

		public void setCard(Context context, Card card)
		{
			mCard=card ;
			mIcon.setImageDrawable(ContextCompat.getDrawable(context,card.mIcon)) ;
			mTitle.setText(card.mTitle) ;
			mDescription.setText(card.mDescription) ;
		}

		@Override
		public void onClick(View view)
		{
			mListener.onCardSelected(mCard) ;
		}
	}

	public AdapterCard(Context context,OnCardSelectedListener listener,List<Card> cards)
	{
		mContext=context ;
		mListener=listener ;
		mCards=cards ;
	}
	
	@Override
	public int getItemCount()
	{
		return mCards.size() ;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		Card card=mCards.get(position) ;
		holder.setCard(mContext,card) ;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.view_card,parent,false) ;
		return new ViewHolder(view,mListener) ;
	}
}
	
