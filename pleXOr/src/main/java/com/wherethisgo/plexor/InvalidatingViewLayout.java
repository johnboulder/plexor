package com.wherethisgo.plexor;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by dell on 4/14/2015.
 */
public class InvalidatingViewLayout extends RelativeLayout
{

	public InvalidatingViewLayout(Context context)
	{
		super(context);
	}

	public InvalidatingViewLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public InvalidatingViewLayout(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		this.invalidate();
	}
}
