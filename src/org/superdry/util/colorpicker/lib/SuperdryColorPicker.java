/******
 * SuperdryColorPickerLib
 * 
 * The MIT License
 * Copyright (c) 2011 superdry
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package org.superdry.util.colorpicker.lib;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SuperdryColorPicker extends Activity implements
		ColorPickHSVCallback {

	private int mInitialColor;
	private int mSelectedColor;
	private TextView colorCode;
	private TextView colorRGB;
	private TextView colorHSV;
	private TextView colorYUV;
	private View pColor;
	private View nColor;
	private SuperdryColorPickerView colorView;
	private Button ok;
	private Button ng;

	public static final int ACTION_GETCOLOR = 1;

	private float[] HSVcolorCode;
	private int[] YUVcolorCode;
	private int dpi = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		mInitialColor = b.getInt("SelectedColor");
		HSVcolorCode = new float[3];
		YUVcolorCode = new int[3];

		setContentView(R.layout.colorpickerlayout);
		setTitle(R.string.app_name);
		if (dpi == 0) {
			DisplayMetrics metrics = new DisplayMetrics();
			this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			dpi = metrics.densityDpi;
		}
		colorView = (SuperdryColorPickerView) findViewById(R.id.colorView);
		colorView.setColorCallback(this, mInitialColor, dpi);

		colorCode = (TextView) findViewById(R.id.colorCode);
		colorCode.setText(String.format("#%02x%02x%02x",
				Color.red(mInitialColor), Color.green(mInitialColor),
				Color.blue(mInitialColor)));

		colorRGB = (TextView) findViewById(R.id.colorRGB);
		colorRGB.setText(String.format("RGB: R=%03d,G=%03d,B=%03d",
				Color.red(mInitialColor), Color.green(mInitialColor),
				Color.blue(mInitialColor)));

		Color.colorToHSV(mInitialColor, HSVcolorCode);
		colorHSV = (TextView) findViewById(R.id.colorHSV);
		colorHSV.setText(String.format("HSV: H=%03d,S=%03d,V=%03d",
				(int) HSVcolorCode[0], (int) (HSVcolorCode[1] * 100.0f),
				(int) (HSVcolorCode[2] * 100.0f)));

		YUVcolorCode = convertRGB2YUV(mInitialColor);
		colorYUV = (TextView) findViewById(R.id.colorYUV);
		colorYUV.setText(String.format("YUV: Y=%03d,U=%03d,V=%03d",
				YUVcolorCode[0], YUVcolorCode[1], YUVcolorCode[2]));

		pColor = (View) findViewById(R.id.pColor);
		pColor.setBackgroundColor(mInitialColor);
		nColor = (View) findViewById(R.id.nColor);
		nColor.setBackgroundColor(mInitialColor);
		mSelectedColor = mInitialColor;
		ok = (Button) findViewById(R.id.ok);
		ng = (Button) findViewById(R.id.ng);
		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("SelectedColor", mSelectedColor);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		ng.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}

	@Override
	public void onChangeColor(float[] hsv) {
		// TODO Auto-generated method stub
		mSelectedColor = Color.HSVToColor(hsv);
		colorCode.setText(String.format("#%02x%02x%02x",
				Color.red(mSelectedColor), Color.green(mSelectedColor),
				Color.blue(mSelectedColor)));
		colorRGB.setText(String.format("RGB: R=%03d,G=%03d,B=%03d",
				Color.red(mSelectedColor), Color.green(mSelectedColor),
				Color.blue(mSelectedColor)));

		nColor.setBackgroundColor(mSelectedColor);
		nColor.invalidate();

		colorHSV.setText(String.format("HSV: H=%03d,S=%03d,V=%03d",
				(int) hsv[0], (int) (hsv[1] * 100.0f), (int) (hsv[2] * 100.0f)));

		YUVcolorCode = convertRGB2YUV(mSelectedColor);
		colorYUV.setText(String.format("YUV: Y=%03d,U=%03d,V=%03d",
				YUVcolorCode[0], YUVcolorCode[1], YUVcolorCode[2]));

	}

	private int[] convertRGB2YUV(int color) {
		ColorMatrix cm = new ColorMatrix();
		cm.setRGB2YUV();
		final float[] yuvArray = cm.getArray();

		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);
		int[] result = new int[3];
		result[0] = floatToByte(yuvArray[0] * r + yuvArray[1] * g + yuvArray[2]
				* b);
		result[1] = floatToByte(yuvArray[5] * r + yuvArray[6] * g + yuvArray[7]
				* b) + 127;
		result[2] = floatToByte(yuvArray[10] * r + yuvArray[11] * g
				+ yuvArray[12] * b) + 127;
		return result;
	}

	private int floatToByte(float x) {
		int n = java.lang.Math.round(x);
		return n;
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		mSelectedColor = savedInstanceState.getInt("SelectedColor");
		colorView.setColorCallback(this, mSelectedColor, dpi);
		nColor.setBackgroundColor(mSelectedColor);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("SelectedColor", mSelectedColor);
		super.onSaveInstanceState(outState);
	}
}
