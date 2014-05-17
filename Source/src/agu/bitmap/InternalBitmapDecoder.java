package agu.bitmap;

import static agu.caching.ResourcePool.CANVAS;
import static agu.caching.ResourcePool.MATRIX;
import static agu.caching.ResourcePool.PAINT;
import static agu.caching.ResourcePool.RECT;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

public class InternalBitmapDecoder extends BitmapDecoder {
	private Bitmap bitmap;
	private boolean scaleFilter;
	private Rect region;
	private boolean mutable = false;
	private Config targetConfig;
	
	InternalBitmapDecoder(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	@Override
	public int sourceWidth() {
		return bitmap.getWidth();
	}

	@Override
	public int sourceHeight() {
		return bitmap.getHeight();
	}
	
	private Bitmap redraw(Bitmap bitmap, Rect rectSrc, int targetWidth, int targetHeight) {
		Config config = (targetConfig != null ? targetConfig : bitmap.getConfig());
		Bitmap bitmap2 = Bitmap.createBitmap(targetWidth, targetHeight, config);
		Canvas cv = CANVAS.obtain(bitmap2);
		
		Rect rectDest = RECT.obtain(0, 0, bitmap2.getWidth(), bitmap2.getHeight());
		Paint paint = (scaleFilter ? PAINT.obtain(Paint.FILTER_BITMAP_FLAG) : null);
		
		cv.drawBitmap(bitmap, rectSrc, rectDest, paint);
		
		if (paint != null) {
			PAINT.recycle(paint);
		}
		RECT.recycle(rectDest);
		
		CANVAS.recycle(cv);
		
		return (mutable ? bitmap2 : Bitmap.createBitmap(bitmap2));
	}
	
	@Override
	public Bitmap decode() {
		resolveQueries();
		
		final boolean redraw = !((targetConfig == null || bitmap.getConfig().equals(targetConfig)) && !mutable);
		
		if (region != null) {
			if (ratioWidth == 1f && ratioHeight == 1f) {
				if (!redraw) {
					return Bitmap.createBitmap(bitmap, region.left, region.top, region.width(), region.height());
				} else {
					return redraw(bitmap, region, region.width(), region.height());
				}
			} else {
				if (!redraw) {
					Matrix m = MATRIX.obtain();
					m.setScale(ratioWidth, ratioHeight);
					
					Bitmap b = Bitmap.createBitmap(bitmap,
							region.left, region.top,
							region.width(), region.height(),
							m, scaleFilter);
					
					MATRIX.recycle(m);
					
					return b;
				} else {
					return redraw(bitmap, region,
							(int) Math.round(ratioWidth * region.width()),
							(int) Math.round(ratioHeight * region.height()));
				}
			}
		} else if (ratioWidth != 1f || ratioHeight != 1f) {
			if (!redraw) {
				Matrix m = MATRIX.obtain();
				m.setScale(ratioWidth, ratioHeight);
				
				Bitmap b = Bitmap.createBitmap(bitmap,
						0, 0, bitmap.getWidth(), bitmap.getHeight(),
						m, scaleFilter);
				
				MATRIX.recycle(m);
				
				return b;
			} else {
				return redraw(bitmap, null,
						(int) Math.round(ratioWidth * bitmap.getWidth()),
						(int) Math.round(ratioHeight * bitmap.getHeight()));
			}
		} else {
			if (!redraw) {
				return bitmap;
			} else {
				return redraw(bitmap, null, bitmap.getWidth(), bitmap.getHeight());
			}
		}
	}
	
	@Override
	public BitmapDecoder region(int left, int top, int right, int bottom) {
		if (region == null) {
			region = RECT.obtainNotReset();
		}
		region.set(left, top, right, bottom);
		
		return this;
	}

	@Override
	public void draw(Canvas cv, Rect rectDest) {
		final Paint p = PAINT.obtain(Paint.FILTER_BITMAP_FLAG);
		cv.drawBitmap(bitmap, region, rectDest, p);
		PAINT.recycle(p);
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (region != null) {
			RECT.recycle(region);
		}
		super.finalize();
	}

	@Override
	public void cancel() {
	}

	@Override
	public BitmapDecoder clone() {
		// TODO: Implement this
		return this;
	}

	@Override
	public BitmapDecoder region(Rect region) {
		if (region == null) {
			if (this.region != null) {
				RECT.recycle(region);
			}
			this.region = null;
		} else {
			region(region.left, region.top, region.right, region.bottom);
		}
		
		return this;
	}

	@Override
	public BitmapDecoder config(Config config) {
		targetConfig = config;
		return this;
	}

	@Override
	public BitmapDecoder useBuiltInDecoder(boolean force) {
		return this;
	}

	@Override
	public BitmapDecoder mutable(boolean mutable) {
		this.mutable = mutable;
		return this;
	}
	
	@Override
	public int hashCode() {
		final int hashBitmap = bitmap.hashCode();
		final int hashRegion = (region == null ? HASHCODE_NULL_REGION : region.hashCode());
		final int hashOptions = (mutable ? 0x55555555 : 0) | (scaleFilter ? 0xAAAAAAAA : 0);
		final int hashConfig = (targetConfig == null ? 0 : targetConfig.hashCode());
		
		return hashBitmap ^ hashRegion ^ hashOptions ^ hashConfig ^ queriesHash();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof InternalBitmapDecoder)) return false;
		
		final InternalBitmapDecoder d = (InternalBitmapDecoder) o;
		return bitmap.equals(d.bitmap) &&
				(region == null ? d.region == null : region.equals(d.region)) &&
				mutable == d.mutable &&
				scaleFilter == d.scaleFilter &&
				(targetConfig == null ? d.targetConfig == null : targetConfig.equals(d.targetConfig));
	}
	
	@Override
	public BitmapDecoder filterBitmap(boolean filter) {
		scaleFilter = filter;
		return this;
	}
}
