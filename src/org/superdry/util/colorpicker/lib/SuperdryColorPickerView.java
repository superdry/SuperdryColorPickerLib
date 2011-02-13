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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.ComposeShader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SuperdryColorPickerView extends View {
	private Paint mPaint;
	private Paint mPaintGray;
	private Paint mPointerPaint;
	private Paint mPointerPaintShadow;
	private Paint mSliderPaint;
	private float mHSV[];
	private int[] mColors;
	private ColorPickHSVCallback callback = null;
	private float pointerx;
	private float pointery;
	private float slidery;
	int RADIUS;
	int SLIDER_WIDTH;
	private int CENTER_X;
	private int CENTER_Y;
	private static int PADDING = 5;

	private boolean inCircle = false;
	private boolean inSlider = false;

	public SuperdryColorPickerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void initView(int color) {

		mColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF,
				0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };
		mPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPointerPaint.setStyle(Paint.Style.STROKE);
		mPointerPaint.setStrokeWidth(1);
		mPointerPaint.setColor(Color.BLACK);

		mPointerPaintShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPointerPaintShadow.setStyle(Paint.Style.STROKE);
		mPointerPaintShadow.setStrokeWidth(1);
		mPointerPaintShadow.setColor(Color.WHITE);

		CENTER_X = RADIUS;
		CENTER_Y = RADIUS;
		mHSV = new float[3];
		Color.colorToHSV(color, mHSV);
		changePointerColor(mHSV);
		changeSlideColor(mHSV);
		color2PointerPosition(mHSV);
		color2SliderPosition(mHSV);
	}

	private void color2PointerPosition(float[] hsv) {
		double angle = hsv[0] / 180 * PI;
		pointerx = (float) (RADIUS * hsv[1] * Math.cos(angle));
		pointery = (float) ((-1) * RADIUS * hsv[1] * Math.sin(angle));
	}

	private void color2SliderPosition(float[] hsv) {
		slidery = RADIUS - RADIUS * 2 * hsv[2];
	}

	private void changePointerColor(float[] hsv) {

		float[] tempHSV = new float[3];
		tempHSV[2] = hsv[2];
		tempHSV[0] = 0.0f;
		tempHSV[1] = 0.0f;
		Shader shaderHue = new SweepGradient(0, 0, mColors, null);
		Shader shaderBrightness = new RadialGradient(0, 0, RADIUS, 0xFFFFFFFF,
				0xFF000000, Shader.TileMode.CLAMP);
		Shader shader = new ComposeShader(shaderHue, shaderBrightness,
				PorterDuff.Mode.SCREEN);

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setShader(shader);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(RADIUS);

		mPaintGray = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintGray.setStyle(Paint.Style.STROKE);
		mPaintGray.setStrokeWidth(RADIUS + 1);
		mPaintGray.setDither(true);
		mPaintGray
				.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
		mPaintGray.setColor(Color.HSVToColor(tempHSV));

	}

	private void changeSlideColor(float[] hsv) {
		float[] tempHSV = new float[3];
		tempHSV[0] = hsv[0];
		tempHSV[1] = hsv[1];
		tempHSV[2] = 1.0f;
		Shader shaderDarkness = new LinearGradient(0, -CENTER_X, 0, CENTER_X,
				Color.HSVToColor(tempHSV), 0xFF000000, Shader.TileMode.CLAMP);
		mSliderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mSliderPaint.setStyle(Paint.Style.FILL);
		mSliderPaint.setShader(shaderDarkness);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		float r = CENTER_X - mPaint.getStrokeWidth() * 0.5f;
		canvas.translate(CENTER_X + PADDING, CENTER_X + PADDING);
		// 円形パレット
		canvas.drawOval(new RectF(-r, -r, r, r), mPaint);
		canvas.drawOval(new RectF(-r, -r, r, r), mPaintGray);
		// ポインタ
		canvas.drawRect(new RectF(pointerx - 5, pointery - 5, pointerx + 3,
				pointery + 3), mPointerPaintShadow);
		canvas.drawRect(new RectF(pointerx - 4, pointery - 4, pointerx + 4,
				pointery + 4), mPointerPaint);
		// スライダーパレット
		canvas.drawRect(new RectF(CENTER_X + PADDING * 2, -CENTER_X, CENTER_X
				+ SLIDER_WIDTH + PADDING * 2, CENTER_X), mSliderPaint);
		// スライダー
		canvas.drawRect(new RectF(CENTER_X - 4 + PADDING * 2, slidery - 4,
				CENTER_X + SLIDER_WIDTH + PADDING * 2 + 2, slidery + 2),
				mPointerPaintShadow);
		canvas.drawRect(new RectF(CENTER_X - 3 + PADDING * 2, slidery - 3,
				CENTER_X + SLIDER_WIDTH + PADDING * 2 + 3, slidery + 3),
				mPointerPaint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(CENTER_X * 2 + PADDING * 4 + SLIDER_WIDTH,
				CENTER_Y * 2 + PADDING * 4);
	}

	private int ave(int s, int d, float p) {
		return s + java.lang.Math.round(p * (d - s));
	}

	private int interpColor(int colors[], float unit) {
		if (unit <= 0) {
			return colors[0];
		}
		if (unit >= 1) {
			return colors[colors.length - 1];
		}

		float p = unit * (colors.length - 1);
		int i = (int) p;
		p -= i;

		int c0 = colors[i];
		int c1 = colors[i + 1];
		int a = ave(Color.alpha(c0), Color.alpha(c1), p);
		int r = ave(Color.red(c0), Color.red(c1), p);
		int g = ave(Color.green(c0), Color.green(c1), p);
		int b = ave(Color.blue(c0), Color.blue(c1), p);

		return Color.argb(a, r, g, b);
	}

	private static final float PI = 3.1415926f;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX() - CENTER_X;
		float y = event.getY() - CENTER_Y;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			if ((x * x + y * y) < RADIUS * RADIUS) {
				inCircle = true;
				inSlider = false;
			} else if ((x > RADIUS + PADDING * 2)
					&& (x < RADIUS + SLIDER_WIDTH + PADDING * 4)
					&& (-RADIUS < y) && (RADIUS > y)) {
				inSlider = true;
				inCircle = false;
			} else {
				inSlider = false;
				inCircle = false;
			}
			break;
		case MotionEvent.ACTION_MOVE:

			float[] tempHSV = mHSV;
			if (inCircle) {

				inSlider = false;

				float angle = (float) java.lang.Math.atan2(y, x);
				float unit = angle / (2 * PI);
				if (unit < 0) {
					unit += 1;
				}
				float[] newHSV = new float[3];
				Color.colorToHSV(interpColor(mColors, unit), newHSV);

				if ((x * x + y * y) < RADIUS * RADIUS) {
					pointerx = x; // pointer 用
					pointery = y;
					newHSV[1] = (float) (Math.sqrt(pointerx * pointerx
							+ pointery * pointery) / RADIUS);
				} else {
					pointerx = (float) (x / Math.sqrt(x * x + y * y) * RADIUS);
					pointery = (float) (y / Math.sqrt(x * x + y * y) * RADIUS);
					newHSV[1] = 1;
				}

				newHSV[2] = tempHSV[2];

				// sliderの色を変える
				changeSlideColor(newHSV);
				callback.onChangeColor(newHSV);
				mHSV = newHSV;

			} else if (inSlider) {
				inCircle = false;

				if ((-RADIUS < y) && (RADIUS > y)) {
					slidery = y;// slider 用
				} else if (y >= RADIUS) {
					slidery = RADIUS;
				} else if (y <= -RADIUS) {
					slidery = -RADIUS;
				}

				mHSV[2] = 1 - ((slidery + RADIUS) / RADIUS / 2);
				changePointerColor(mHSV);
				callback.onChangeColor(mHSV);

			}

			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			inSlider = false;
			inCircle = false;
			break;
		}

		return true;
	}

	public void setColorCallback(ColorPickHSVCallback callback, int color,
			int dpi) {
		RADIUS = dpi / 2;
		SLIDER_WIDTH = dpi / 5;
		this.callback = callback;
		initView(color);
	}
}
