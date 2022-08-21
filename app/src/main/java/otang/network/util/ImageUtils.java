package otang.network.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

public class ImageUtils {

	public static Bitmap createBitmapFromString(String speed, String unit) {
		// Speed
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(60f);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
		// Unit
		Paint unitPaint = new Paint();
		unitPaint.setAntiAlias(true);
		unitPaint.setTextSize(40f);
		unitPaint.setTextAlign(Paint.Align.CENTER);
		unitPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
		// Speed
		Rect speedBounds = new Rect();
		paint.getTextBounds(speed, 0, speed.length(), speedBounds);
		// Unit
		Rect unitBounds = new Rect();
		unitPaint.getTextBounds(unit, 0, unit.length(), unitBounds);
		int width = speedBounds.width() > unitBounds.width() ? speedBounds.width() : unitBounds.width();
		// Create Bitmap
		Bitmap bitmap = Bitmap.createBitmap(width, 90, Bitmap.Config.ARGB_8888);
		// Draw Canvas
		Canvas canvas = new Canvas(bitmap);
		canvas.drawText(speed, width / 2f, 50f, paint);
		canvas.drawText(unit, width / 2f, 90f, unitPaint);
		return bitmap;
	}
}