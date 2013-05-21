package com.kal.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class MyTextView extends TextView {
	
	public MyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFont(context);
	}
	


	private void setFont(Context context) {
		Typeface font = Typeface
				.createFromAsset(context.getAssets(), "Helvetica.ttf");
		setTypeface(font);

	}
}
