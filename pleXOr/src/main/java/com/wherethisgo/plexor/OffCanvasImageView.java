package com.wherethisgo.plexor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.widget.ImageView;


/**
 * TODO: document your custom view class.
 */
public class OffCanvasImageView extends ImageView
{

	public OffCanvasImageView(Context context)
	{
		super(context);
	}

	public OffCanvasImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public OffCanvasImageView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

//	public OffCanvasImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
//	{
//		super(context, attrs, defStyleAttr, defStyleRes);
//	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		Rect bounds = canvas.getClipBounds();
		int height = bounds.height();
		int width = bounds.width();
		//make the rect larger
		bounds.inset(-width, -height);

		canvas.clipRect (bounds, Region.Op.REPLACE);
	}
}
