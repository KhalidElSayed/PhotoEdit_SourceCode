package com.kal.custom;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

public class ImageProcessing {
	
	
	public static Bitmap applyBCS(Bitmap src, float bright, float contr, float satur) {

		if (contr == 0 && bright == 0 && satur == 1) {
			return src;
		} else {
			Bitmap bitmapOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
					src.getConfig());

			Canvas canvas = new Canvas(bitmapOut);
			Paint paint = new Paint();
			ColorMatrix cm = new ColorMatrix();
			
			if (satur != 0) {
				Log.i("log_tag", ""+(float)satur/2);
				cm.setSaturation((float)satur/2);
			}

			final float m[] = cm.getArray();

			float c = contr + 1.f;
			float translate = (-.5f * c + .5f) * 255.f;
			cm.set(new float[] { m[0] * c, m[1], m[2], m[3],
					m[4] * translate + bright, m[5], m[6] * c, m[7], m[8],
					m[9] * translate + bright, m[10], m[11], m[12] * c, m[13],
					m[14] * translate + bright, m[15], m[16], m[17], m[18],
					m[19] });

			paint.setColorFilter(new ColorMatrixColorFilter(cm));

			Matrix matrix = new Matrix();

			canvas.drawBitmap(src, matrix, paint);
			return bitmapOut;
		}

	}
	
	
	
	public static Bitmap doColorFilter(Bitmap src, double red, double green,
			double blue) {
		// image size
		int width = src.getWidth();
		int height = src.getHeight();
		// create output bitmap
		Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
		// color information
		int A, R, G, B;
		int pixel;

		// scan through all pixels
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				// get pixel color
				pixel = src.getPixel(x, y);
				// apply filtering on each channel R, G, B
				A = Color.alpha(pixel);
				R = (int) (Color.red(pixel) * red);
				G = (int) (Color.green(pixel) * green);
				B = (int) (Color.blue(pixel) * blue);
				// set new color pixel to output bitmap
				bmOut.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}

		// return final image
		return bmOut;
	}

	public static Bitmap doBrightness(Bitmap src, int value) {
		// image size
		int width = src.getWidth();
		int height = src.getHeight();
		// create output bitmap
		Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
		// color information
		int A, R, G, B;
		int pixel;

		// scan through all pixels
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				// get pixel color
				pixel = src.getPixel(x, y);
				A = Color.alpha(pixel);
				R = Color.red(pixel);
				G = Color.green(pixel);
				B = Color.blue(pixel);

				// increase/decrease each channel
				R += value;
				if (R > 255) {
					R = 255;
				} else if (R < 0) {
					R = 0;
				}

				G += value;
				if (G > 255) {
					G = 255;
				} else if (G < 0) {
					G = 0;
				}

				B += value;
				if (B > 255) {
					B = 255;
				} else if (B < 0) {
					B = 0;
				}

				// apply new pixel color to output bitmap
				bmOut.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}

		// return final image
		return bmOut;
	}

	public static Bitmap doContrast(Bitmap src, float contrast) {
		// image size
		int width = src.getWidth();
		int height = src.getHeight();
		// create output bitmap
		Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
		// color information

		Canvas canvas = new Canvas(bmOut);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();

		final float m[] = cm.getArray();

		float c = contrast + 1.f;
		float translate = (-.5f * c + .5f) * 255.f;
		cm.set(new float[] { m[0] * c, m[1], m[2], m[3], m[4] * translate,
				m[5], m[6] * c, m[7], m[8], m[9] * translate, m[10], m[11],
				m[12] * c, m[13], m[14] * translate, m[15], m[16], m[17],
				m[18], m[19] });

		paint.setColorFilter(new ColorMatrixColorFilter(cm));

		Matrix matrix = new Matrix();

		canvas.drawBitmap(src, matrix, paint);

		// return final image
		return bmOut;
	}

//	public static Bitmap doContrast(Bitmap src, double value) {
//		// image size
//		int width = src.getWidth();
//		int height = src.getHeight();
//		// create output bitmap
//		Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
//		// color information
//		int A, R, G, B;
//		int pixel;
//		// get contrast value
//		double contrast = Math.pow((100 + value) / 100, 2);
//
//		// scan through all pixels
//		for (int x = 0; x < width; ++x) {
//			for (int y = 0; y < height; ++y) {
//				// get pixel color
//				pixel = src.getPixel(x, y);
//				A = Color.alpha(pixel);
//				// apply filter contrast for every channel R, G, B
//				R = Color.red(pixel);
//				R = (int) (((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
//				if (R < 0) {
//					R = 0;
//				} else if (R > 255) {
//					R = 255;
//				}
//
//				G = Color.red(pixel);
//				G = (int) (((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
//				if (G < 0) {
//					G = 0;
//				} else if (G > 255) {
//					G = 255;
//				}
//
//				B = Color.red(pixel);
//				B = (int) (((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
//				if (B < 0) {
//					B = 0;
//				} else if (B > 255) {
//					B = 255;
//				}
//
//				// set new pixel color to output bitmap
//				bmOut.setPixel(x, y, Color.argb(A, R, G, B));
//			}
//		}
//
//		// return final image
//		return bmOut;
//	}

	public static Bitmap doSaturation(Bitmap source, int level) {
		// get image size
		int width = source.getWidth();
		int height = source.getHeight();
		int[] pixels = new int[width * height];
		float[] HSV = new float[3];
		// get pixel array from source
		source.getPixels(pixels, 0, width, 0, 0, width, height);

		int index = 0;
		// iteration through pixels
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				// get current index in 2D-matrix
				index = y * width + x;
				// convert to HSV
				Color.colorToHSV(pixels[index], HSV);
				// increase Saturation level
				HSV[1] *= level;
				HSV[1] = (float) Math.max(0.0, Math.min(HSV[1], 1.0));
				// take color back
				pixels[index] |= Color.HSVToColor(HSV);
			}
		}
		// output bitmap
		Bitmap bmOut = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
		return bmOut;
	}

	

	public static final double PI = 3.14159d;
	public static final double FULL_CIRCLE_DEGREE = 360d;
	public static final double HALF_CIRCLE_DEGREE = 180d;
	public static final double RANGE = 256d;

	public static Bitmap doTintFilter(Bitmap src, int degree) {

		int width = src.getWidth();
		int height = src.getHeight();

		int[] pix = new int[width * height];
		src.getPixels(pix, 0, width, 0, 0, width, height);

		int RY, GY, BY, RYY, GYY, BYY, R, G, B, Y;
		double angle = (PI * (double) degree) / HALF_CIRCLE_DEGREE;

		int S = (int) (RANGE * Math.sin(angle));
		int C = (int) (RANGE * Math.cos(angle));

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				int index = y * width + x;
				int r = (pix[index] >> 16) & 0xff;
				int g = (pix[index] >> 8) & 0xff;
				int b = pix[index] & 0xff;
				RY = (70 * r - 59 * g - 11 * b) / 100;
				GY = (-30 * r + 41 * g - 11 * b) / 100;
				BY = (-30 * r - 59 * g + 89 * b) / 100;
				Y = (30 * r + 59 * g + 11 * b) / 100;
				RYY = (S * BY + C * RY) / 256;
				BYY = (C * BY - S * RY) / 256;
				GYY = (-51 * RYY - 19 * BYY) / 100;
				
				R = Y + RYY;
				R = (R < 0) ? 0 : ((R > 255) ? 255 : R);
				G = Y + GYY;
				G = (G < 0) ? 0 : ((G > 255) ? 255 : G);
				B = Y + BYY;
				B = (B < 0) ? 0 : ((B > 255) ? 255 : B);
				pix[index] = 0xff000000 | (R << 16) | (G << 8) | B;
			}

		Bitmap outBitmap = Bitmap.createBitmap(width, height, src.getConfig());
		outBitmap.setPixels(pix, 0, width, 0, 0, width, height);

		pix = null;

		return outBitmap;
	}
	
	public static Bitmap doTintFilter(Bitmap src, int degree, int rrr,int ggg, int bbb) {

		int width = src.getWidth();
		int height = src.getHeight();

		int[] pix = new int[width * height];
		src.getPixels(pix, 0, width, 0, 0, width, height);

		int RY, GY, BY, RYY, GYY, BYY, R, G, B, Y;
		double angle = (PI * (double) degree) / HALF_CIRCLE_DEGREE;

		int S = (int) (RANGE * Math.sin(angle));
		int C = (int) (RANGE * Math.cos(angle));

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				int index = y * width + x;
				int r = (pix[index] >> 16) & 0xff;
				int g = (pix[index] >> 8) & 0xff;
				int b = pix[index] & 0xff;
				RY = (70 * r - 59 * g - 11 * b) / 100;
				GY = (-30 * r + 41 * g - 11 * b) / 100;
				BY = (-30 * r - 59 * g + 89 * b) / 100;
				Y = (30 * r + 59 * g + 11 * b) / 100;
				RYY = (S * BY + C * RY) / 256;
				BYY = (C * BY - S * RY) / 256;
				GYY = (-51 * RYY - 19 * BYY) / 100;
				
				R = Y + RYY+rrr;
				R = (R < 0) ? 0 : ((R > 255) ? 255 : R);
				G = Y + GYY+ggg;
				G = (G < 0) ? 0 : ((G > 255) ? 255 : G);
				B = Y + BYY+bbb;
				B = (B < 0) ? 0 : ((B > 255) ? 255 : B);
				pix[index] = 0xff000000 | (R << 16) | (G << 8) | B;
			}

		Bitmap outBitmap = Bitmap.createBitmap(width, height, src.getConfig());
		outBitmap.setPixels(pix, 0, width, 0, 0, width, height);

		pix = null;

		return outBitmap;
	}

	public static Bitmap doGreyscale(Bitmap src) {
		// constant factors
		final double GS_RED = 0.299;
		final double GS_GREEN = 0.587;
		final double GS_BLUE = 0.114;

		// create output bitmap
		Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src
				.getConfig());
		// pixel information
		int A, R, G, B;
		int pixel;

		// get image size
		int width = src.getWidth();
		int height = src.getHeight();

		// scan through every single pixel
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				// get one pixel color
				pixel = src.getPixel(x, y);
				// retrieve color of all channels
				A = Color.alpha(pixel);
				R = Color.red(pixel);
				G = Color.green(pixel);
				B = Color.blue(pixel);
				// take conversion up to one single value
				R = G = B = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);
				// set new pixel color to output bitmap
				bmOut.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}

		// return final image
		return bmOut;
	}

	public static Bitmap doSepiaEffect(Bitmap bitmap, int depth) {
		// TODO Auto-generated method stub

		int width, height, r, g, b, c, gry;
		height = bitmap.getHeight();
		width = bitmap.getWidth();
		Bitmap bitmapNewPhoto = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmapNewPhoto);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setScale(0.6f, .6f, 0.3f, 1.0f);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		canvas.drawBitmap(bitmap, 0, 0, paint);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				c = bitmap.getPixel(x, y);

				r = Color.red(c);
				g = Color.green(c);
				b = Color.blue(c);

				gry = (r + g + b) / 3;
				r = g = b = gry;

				r = r + (depth * 2);
				g = g + depth;

				if (r > 255) {
					r = 255;
				}
				if (g > 255) {
					g = 255;
				}
				bitmapNewPhoto.setPixel(x, y, Color.rgb(r, g, b));
			}
		}

		return bitmapNewPhoto;

	}

	public static Bitmap doFastBlur(Bitmap bitmap, int radius) {

	       
        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = 0xff000000 | (dv[rsum] << 16) | (dv[gsum] << 8)
                        | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

}
