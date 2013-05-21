package com.sunshine.photoedit;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.media.ExifInterface;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.kal.cropboxapi.CropImageView;
import com.kal.cropboxapi.HighlightView;
import com.kal.cropboxapi.IImage;
import com.kal.cropboxapi.MonitoredActivity;
import com.kal.cropboxapi.Util;

public class MainActivity extends MonitoredActivity {
    /** Called when the activity is first created. */
	
	private static final int ACTION_REQUEST_GALLERY = 1003;
	private static final int ACTION_REQUEST_CAMERA = 1004;
	
	private RelativeLayout rlDialogSelectImage;
	private File outputFilePath;
	
	
	private CropImageView mImageView;
	private IImage mImage;
	private final Handler mHandler = new Handler();
	private boolean mCircleCrop = false;
	private boolean mDoFaceDetection = true;
	
	//for maintaing aspectration of crop box...
	private int mAspectX, mAspectY;
	// These options specifiy the output image size and whether we should
	// scale the output to fit it (or just crop it).
	private int mOutputX, mOutputY;
	private boolean mScale=true;
	private boolean mScaleUp = true;
	
	private LayoutInflater inflater;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        inflater=LayoutInflater.from(this);
        rlDialogSelectImage=(RelativeLayout)findViewById(R.id.dialogSelectImage);
       
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(getResources(), R.drawable.frame_1, bfo);
        
		
		//for low memory...
		if(isFreeSpace("/data"))
		{
			GlobalPE.scale=2;
		}
		else
		{
			GlobalPE.scale=1.5f;
		}
		GlobalPE.fixedWidth=(int) (bfo.outWidth*GlobalPE.scale);
		GlobalPE.fixedHeight=(int) (bfo.outHeight*GlobalPE.scale);	 
		
		
		Log.i("log_tag", GlobalPE.fixedWidth+" : "+GlobalPE.fixedHeight);
        mAspectX=1;
        mAspectY=1;
        mOutputX=GlobalPE.fixedWidth;
        mOutputY=GlobalPE.fixedHeight;
        Log.e("log_tag", GlobalPE.fixedWidth+" : "+GlobalPE.fixedHeight);
    }
    
    public static boolean isFreeSpace(String path) {
    	//....path for sd card..."/mnt/sdcard"
    	//.....path for internal storage......"/data"
    	StatFs stat = new StatFs(path);
    	double sdAvailSize = (double) stat.getAvailableBlocks()
    			* (double) stat.getBlockSize();
    	double megaAvailable = sdAvailSize / 1048576;
    	if (megaAvailable > 30) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	if(GlobalPE.mBitmap!=null)
    	{
    		 mImageView = (CropImageView) inflater.inflate(R.layout.crop_image_view, null);
    		 mImageView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    		 ((LinearLayout)findViewById(R.id.llCropBoxContainer)).addView(mImageView);
    		startFaceDetection();
    	}
    	
    }
   
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	mImageView=null;
    	 ((LinearLayout)findViewById(R.id.llCropBoxContainer)).removeAllViews();
    	 
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	Log.i("log_tag", "onDestroy_main");
    	if(GlobalPE.bitmapBackUp!=null)
    	{
    		GlobalPE.bitmapBackUp.recycle();
    		GlobalPE.bitmapBackUp=null;
    	}
    	if(GlobalPE.bitmapEffect!=null)
    	{
    		GlobalPE.bitmapEffect.recycle();
    		GlobalPE.bitmapEffect=null;
    	}
    	if(GlobalPE.bitmapTemp!=null)
    	{
    		GlobalPE.bitmapTemp.recycle();
    		GlobalPE.bitmapTemp=null;
    	}
    	if(GlobalPE.mBitmap!=null)
    	{
    		GlobalPE.mBitmap.recycle();
    		GlobalPE.mBitmap=null;
    	}
    	
    	super.onDestroy();
    	
    }
    public void onButtonClick(View v) {
		switch (v.getId()) {
		case R.id.ivCaptureBtn:
			if(rlDialogSelectImage.getVisibility()==View.GONE)
			{
				rlDialogSelectImage.setVisibility(View.VISIBLE);
				rlDialogSelectImage.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up));
			}
			break;

		case R.id.ivDoneBtn:
			if(GlobalPE.mBitmap!=null)
			{
				onSaveClicked();
				
				Intent intentPhotoEditor=new Intent(MainActivity.this, PhotoEditorActivity.class);
				startActivity(intentPhotoEditor);
			}
			
			
			break;
			
		case R.id.ivCancelBtn:
			GlobalPE.mBitmap=null;
			mImageView=null;
	    	 ((LinearLayout)findViewById(R.id.llCropBoxContainer)).removeAllViews();
			break;
		}
	}
    
	public void onDialogSelectImageClick(View v) {
		rlDialogSelectImage.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down));
		rlDialogSelectImage.setVisibility(View.GONE);
    	
		String tag=v.getTag().toString();
		if(tag.equals("photo_gallery"))
		{
			Intent intentGallery = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intentGallery, ACTION_REQUEST_GALLERY);
		}
		else if(tag.equals("camera"))
		{
			Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			outputFilePath = new File(Environment.getExternalStorageDirectory(), "test.jpg");
			intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFilePath));
			startActivityForResult(intentCamera, ACTION_REQUEST_CAMERA);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
		if (resultCode == RESULT_OK) {
			
			if(requestCode==ACTION_REQUEST_GALLERY)
			{
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

    			Cursor cursor = getContentResolver().query(selectedImage,
    					filePathColumn, null, null, null);
    			cursor.moveToFirst();

    			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
    			
    			String filePath = cursor.getString(columnIndex);
    			
    			cursor.close();
    			
				GlobalPE.mBitmap=getBitmapFromUri(filePath);
//				if(GlobalPE.mBitmap!=null)
//				{
//					startFaceDetection();
//				}
				
				
				System.gc();
			}
			else if(requestCode==ACTION_REQUEST_CAMERA)
			{
				if(outputFilePath!=null)
				{
					GlobalPE.mBitmap=getBitmapFromUri(outputFilePath.getAbsolutePath());
				}
				
//				if(GlobalPE.mBitmap!=null)
//				{
//					startFaceDetection();
//				}
				System.gc();
			}
			
		
		}

	}
	private Bitmap getBitmapFromUri(String filePath) {
		// TODO Auto-generated method stub
		int orientation =0;
		try {
			ExifInterface exif = new ExifInterface(filePath);
			orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Matrix matrix=null;
		if(orientation==6 || orientation==8 || orientation==3)
		{
			matrix = new Matrix();
			if(orientation==6)
			{
				
				matrix.postRotate(90);
				
			}
			else if(orientation==8)
			{
				
				matrix.postRotate(-90);
			}
			else if(orientation==3)
			{
				
				matrix.postRotate(180);
			}
			
		}
		
		Bitmap sourceBitmap=ShrinkBitmap(filePath,1000,1000);
		
		if(matrix!=null)
		{
			return Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
		}
		return sourceBitmap;
	}
	private Bitmap ShrinkBitmap(String file, int width, int height) {

		BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
		bmpFactoryOptions.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);

		int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight
				/ (float) height);
		int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth
				/ (float) width);

		if (heightRatio > 1 || widthRatio > 1) {
			if (heightRatio > widthRatio) {
				bmpFactoryOptions.inSampleSize = heightRatio;
			} else {
				bmpFactoryOptions.inSampleSize = widthRatio;
			}
		}

		bmpFactoryOptions.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
		return bitmap;
	}
	
	Runnable mRunFaceDetection = new Runnable() {
		float mScale = 1F;
		Matrix mImageMatrix;
		FaceDetector.Face[] mFaces = new FaceDetector.Face[3];
		int mNumFaces;

		// For each face, we create a HightlightView for it.
		private void handleFace(FaceDetector.Face f) {
			PointF midPoint = new PointF();

			int r = ((int) (f.eyesDistance() * mScale)) * 2;
			f.getMidPoint(midPoint);
			midPoint.x *= mScale;
			midPoint.y *= mScale;

			int midX = (int) midPoint.x;
			int midY = (int) midPoint.y;

			HighlightView hv = new HighlightView(mImageView);

			int width = GlobalPE.mBitmap.getWidth();
			int height = GlobalPE.mBitmap.getHeight();

			Rect imageRect = new Rect(0, 0, width, height);

			RectF faceRect = new RectF(midX, midY, midX, midY);
			faceRect.inset(-r, -r);
			if (faceRect.left < 0) {
				faceRect.inset(-faceRect.left, -faceRect.left);
			}

			if (faceRect.top < 0) {
				faceRect.inset(-faceRect.top, -faceRect.top);
			}

			if (faceRect.right > imageRect.right) {
				faceRect.inset(faceRect.right - imageRect.right, faceRect.right
						- imageRect.right);
			}

			if (faceRect.bottom > imageRect.bottom) {
				faceRect.inset(faceRect.bottom - imageRect.bottom,
						faceRect.bottom - imageRect.bottom);
			}

			hv.setup(mImageMatrix, imageRect, faceRect, mCircleCrop,
					mAspectX != 0 && mAspectY != 0);

			mImageView.add(hv);
		}

		// Create a default HightlightView if we found no face in the picture.
		private void makeDefault() {
			HighlightView hv = new HighlightView(mImageView);

			int width = GlobalPE.mBitmap.getWidth();
			int height = GlobalPE.mBitmap.getHeight();

			Rect imageRect = new Rect(0, 0, width, height);

			// make the default size about 4/5 of the width or height
			int cropWidth = Math.min(width, height) * 4 / 5;
			int cropHeight = cropWidth;

			if (mAspectX != 0 && mAspectY != 0) {
				if (mAspectX > mAspectY) {
					cropHeight = cropWidth * mAspectY / mAspectX;
				} else {
					cropWidth = cropHeight * mAspectX / mAspectY;
				}
			}

			int x = (width - cropWidth) / 2;
			int y = (height - cropHeight) / 2;

			RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
			hv.setup(mImageMatrix, imageRect, cropRect, mCircleCrop,
					mAspectX != 0 && mAspectY != 0);
			mImageView.add(hv);
		}

		// Scale the image down for faster face detection.
		private Bitmap prepareBitmap() {
			if (GlobalPE.mBitmap == null) {
				return null;
			}

			// 256 pixels wide is enough.
			if (GlobalPE.mBitmap.getWidth() > 256) {
				mScale = 256.0F / GlobalPE.mBitmap.getWidth();
			}
			Matrix matrix = new Matrix();
			matrix.setScale(mScale, mScale);
			Bitmap faceBitmap = Bitmap.createBitmap(GlobalPE.mBitmap, 0, 0, GlobalPE.mBitmap
					.getWidth(), GlobalPE.mBitmap.getHeight(), matrix, true);
			return faceBitmap;
		}

		public void run() {
			mImageMatrix = mImageView.getImageMatrix();
			Bitmap faceBitmap = prepareBitmap();

			mScale = 1.0F / mScale;
			if (faceBitmap != null && mDoFaceDetection) {
				FaceDetector detector = new FaceDetector(faceBitmap.getWidth(),
						faceBitmap.getHeight(), mFaces.length);
				mNumFaces = detector.findFaces(faceBitmap, mFaces);
			}

			if (faceBitmap != null && faceBitmap != GlobalPE.mBitmap) {
				faceBitmap.recycle();
			}

			mHandler.post(new Runnable() {
				public void run() {
					mImageView.mWaitingToPick = mNumFaces > 1;
					if (mNumFaces > 0) {
						for (int i = 0; i < mNumFaces; i++) {
							handleFace(mFaces[i]);
						}
					} else {
						makeDefault();
					}
					mImageView.invalidate();
					if (mImageView.mHighlightViews.size() == 1) {
						mImageView.mCrop = mImageView.mHighlightViews.get(0);
						mImageView.mCrop.setFocus(true);
					}

					if (mNumFaces > 1) {
						Toast t = Toast.makeText(MainActivity.this,
								"Multi face crop help", Toast.LENGTH_SHORT);
						t.show();
					}
				}
			});
		}
	};
	
	private void startFaceDetection() {
		if (isFinishing()) {
			return;
		}

		mImageView.setImageBitmapResetBase(GlobalPE.mBitmap, true);

		Util.startBackgroundJob(this, null, "Please wait\u2026",
				new Runnable() {
					public void run() {
						final CountDownLatch latch = new CountDownLatch(1);
						final Bitmap b = (mImage != null) ? mImage
								.fullSizeBitmap(IImage.UNCONSTRAINED,
										1024 * 1024) : GlobalPE.mBitmap;
						mHandler.post(new Runnable() {
							public void run() {
								if (b != GlobalPE.mBitmap && b != null) {
									mImageView.setImageBitmapResetBase(b, true);
									GlobalPE.mBitmap.recycle();
									GlobalPE.mBitmap = b;
								}
								if (mImageView.getScale() == 1F) {
									mImageView.center(true, true);
								}
								latch.countDown();
							}
						});
						try {
							latch.await();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
						mRunFaceDetection.run();
					}
				}, mHandler);
	}
	
	
	private void onSaveClicked() {
		// TODO this code needs to change to use the decode/crop/encode single
		// step api so that we don't require that the whole (possibly large)
		// bitmap doesn't have to be read into memory
		if (mImageView.mSaving)
			return;

		if (mImageView.mCrop == null) {
			return;
		}

		mImageView.mSaving = true;

		Rect r = mImageView.mCrop.getCropRect();

		int width = r.width();
		int height = r.height();

		// If we are circle cropping, we want alpha channel, which is the
		// third param here.
		Bitmap croppedImage = Bitmap.createBitmap(width, height,
				mCircleCrop ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
		{
			Canvas canvas = new Canvas(croppedImage);
			Rect dstRect = new Rect(0, 0, width, height);
			canvas.drawBitmap(GlobalPE.mBitmap, r, dstRect, null);
		}

		if (mCircleCrop) {
			// OK, so what's all this about?
			// Bitmaps are inherently rectangular but we want to return
			// something that's basically a circle. So we fill in the
			// area around the circle with alpha. Note the all important
			// PortDuff.Mode.CLEAR.
			Canvas c = new Canvas(croppedImage);
			Path p = new Path();
			p.addCircle(width / 2F, height / 2F, width / 2F, Path.Direction.CW);
			c.clipPath(p, Region.Op.DIFFERENCE);
			c.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
		}

		/* If the output is required to a specific size then scale or fill */
		if (mOutputX != 0 && mOutputY != 0) {
			if (mScale) {
				/* Scale the image to the required dimensions */
				Bitmap old = croppedImage;
				croppedImage = Util.transform(new Matrix(), croppedImage,
						mOutputX, mOutputY, mScaleUp);
				if (old != croppedImage) {
					old.recycle();
				}
			} else {

				/*
				 * Don't scale the image crop it to the size requested. Create
				 * an new image with the cropped image in the center and the
				 * extra space filled.
				 */

				// Don't scale the image but instead fill it so it's the
				// required dimension
				Bitmap b = Bitmap.createBitmap(mOutputX, mOutputY,
						Bitmap.Config.RGB_565);
				Canvas canvas = new Canvas(b);

				Rect srcRect = mImageView.mCrop.getCropRect();
				Rect dstRect = new Rect(0, 0, mOutputX, mOutputY);

				int dx = (srcRect.width() - dstRect.width()) / 2;
				int dy = (srcRect.height() - dstRect.height()) / 2;

				/* If the srcRect is too big, use the center part of it. */
				srcRect.inset(Math.max(0, dx), Math.max(0, dy));

				/* If the dstRect is too big, use the center part of it. */
				dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));

				/* Draw the cropped bitmap in the center */
				canvas.drawBitmap(GlobalPE.mBitmap, srcRect, dstRect, null);

				/* Set the cropped bitmap as the new bitmap */
				croppedImage.recycle();
				croppedImage = b;

			}

		}
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		croppedImage.setDensity(metrics.densityDpi);
		Log.e("log_tag", croppedImage.getWidth()+" : "+metrics.densityDpi );
		GlobalPE.bitmapBackUp=Bitmap.createBitmap(croppedImage);
	}
}