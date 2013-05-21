package com.sunshine.photoedit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.kal.custom.ImageProcessing;

public class PhotoEditorActivity extends Activity {
	/** Called when the activity is first created. */

	private float brightness = 0;
	private float contrast = 0;
	private float saturation = 0;

	private HorizontalScrollView hsvEffectsBar, hsvFramesBar;
	private LinearLayout llBrightness;
	private SeekBar seekBarBrightness, seekBarContrast, seekBarSaturation;

	private ImageView ivPreview, ivFrame;
	private ProgressDialog progressDialog;
	
	private FrameLayout flMainPreview;
	Resources mResources;
	private int resIdFrame;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_editor);
		
		GlobalPE.bitmapEffect=Bitmap.createBitmap(GlobalPE.bitmapBackUp);
		
		mResources=getResources();
		
		initObjects();
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		int squaresize=dm.widthPixels;
		
		ivFrame.getLayoutParams().width=squaresize;
		ivFrame.getLayoutParams().height=squaresize;
		
		ivPreview.getLayoutParams().width=squaresize;
		ivPreview.getLayoutParams().height=squaresize;
		
		ivPreview.setImageBitmap(GlobalPE.bitmapEffect);

	}

	private void initObjects() {
		// TODO Auto-generated method stub
		ivPreview = (ImageView) findViewById(R.id.ivPreview);
		ivFrame = (ImageView) findViewById(R.id.ivFrame);
		hsvEffectsBar = (HorizontalScrollView) findViewById(R.id.hsvEffectsBar);
		hsvFramesBar = (HorizontalScrollView) findViewById(R.id.hsvFramesBar);
		llBrightness = (LinearLayout) findViewById(R.id.llBrightness);
		
		flMainPreview=(FrameLayout)findViewById(R.id.flMainPreview);

		seekBarBrightness = (SeekBar) findViewById(R.id.seekBarBrightness);
		seekBarContrast = (SeekBar) findViewById(R.id.seekBarContrast);
		seekBarSaturation = (SeekBar) findViewById(R.id.seekBarSaturation);

		seekBarBrightness.setMax(200);
		seekBarContrast.setMax(200);
		seekBarSaturation.setMax(14);

		seekBarBrightness.setOnSeekBarChangeListener(onBCSChangeListener);
		seekBarContrast.setOnSeekBarChangeListener(onBCSChangeListener);
		seekBarSaturation.setOnSeekBarChangeListener(onBCSChangeListener);
	}

	public void onHeaderBarClick(View v) {
		
		if (llBrightness.getVisibility() == View.VISIBLE) {
			ivPreview.setImageBitmap(GlobalPE.bitmapEffect);
		}
		
		switch (v.getId()) {
		case R.id.ivLeftArrowBtn:
			finish();
			break;

		case R.id.ivRotateBtn:
			hsvFramesBar.setVisibility(View.GONE);
			hsvEffectsBar.setVisibility(View.GONE);
			llBrightness.setVisibility(View.GONE);

			((ImageView) findViewById(R.id.ivEffectsBtn)).setSelected(false);
			((ImageView) findViewById(R.id.ivFramesBtn)).setSelected(false);
			((ImageView) findViewById(R.id.ivBrightnessBtn)).setSelected(false);

			rotateBitmapAtAngle90();
			break;

		case R.id.ivFramesBtn:

			if (hsvFramesBar.getVisibility() == View.GONE) {
				hsvEffectsBar.setVisibility(View.GONE);
				llBrightness.setVisibility(View.GONE);

				((ImageView) findViewById(R.id.ivEffectsBtn)).setSelected(false);
				((ImageView) findViewById(R.id.ivFramesBtn)).setSelected(true);
				((ImageView) findViewById(R.id.ivBrightnessBtn))
						.setSelected(false);

				hsvFramesBar.setVisibility(View.VISIBLE);
				hsvFramesBar.startAnimation(AnimationUtils.loadAnimation(
						PhotoEditorActivity.this, R.anim.slide_right_to_left));
			}

			break;

		case R.id.ivBrightnessBtn:

			if (llBrightness.getVisibility() == View.GONE) {
				hsvEffectsBar.setVisibility(View.GONE);
				hsvFramesBar.setVisibility(View.GONE);

				((ImageView) findViewById(R.id.ivEffectsBtn))
						.setSelected(false);
				((ImageView) findViewById(R.id.ivFramesBtn)).setSelected(false);
				((ImageView) findViewById(R.id.ivBrightnessBtn))
						.setSelected(true);

				resetBrightnessAll();

				llBrightness.setVisibility(View.VISIBLE);
			}

			break;

		case R.id.ivEffectsBtn:

			if (hsvEffectsBar.getVisibility() == View.GONE) {
				hsvFramesBar.setVisibility(View.GONE);
				llBrightness.setVisibility(View.GONE);
				((ImageView) findViewById(R.id.ivFramesBtn)).setSelected(false);

				((ImageView) findViewById(R.id.ivEffectsBtn)).setSelected(true);
				((ImageView) findViewById(R.id.ivBrightnessBtn))
						.setSelected(false);

				hsvEffectsBar.setVisibility(View.VISIBLE);
				hsvEffectsBar.startAnimation(AnimationUtils.loadAnimation(
						PhotoEditorActivity.this, R.anim.slide_right_to_left));
			}

			break;

		case R.id.ivRightArrowBtn:
			ImageView ivSave=(ImageView)findViewById(R.id.ivSaveBtn);
			if(ivSave.getVisibility()==View.GONE)
			{
				v.setSelected(true);
				((ImageView)findViewById(R.id.ivRotateBtn)).setVisibility(View.GONE);
				ivSave.setVisibility(View.VISIBLE);
			}
			else
			{
				v.setSelected(false);
				((ImageView)findViewById(R.id.ivRotateBtn)).setVisibility(View.VISIBLE);
				ivSave.setVisibility(View.GONE);
			}
			break;
			
		case R.id.ivSaveBtn:
			saveBitmapInGallery();
			break;
		}
	}

	private void saveBitmapInGallery() {
		// TODO Auto-generated method stub
		progressDialog=ProgressDialog.show(PhotoEditorActivity.this, "", "Saving Image...");
		new Thread(new Runnable() {
			private int chk_error=0;
			private File file=null;
			
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				File folder=null;
				
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					String extStorageDirectory=Environment.getExternalStorageDirectory().getAbsolutePath();
					
					folder=new File(extStorageDirectory, "/"+getResources().getString(R.string.app_name)+"/");
					
					if(!folder.isDirectory())
					{
						folder.mkdir();
					}
				}
				else
				{
					folder=getDir(getResources().getString(R.string.app_name), MODE_WORLD_READABLE);
				}
				
				file = new File(folder, "Photo_Edit_"+(folder.listFiles().length+1)+".png");

				
				
				try {
					
					
					FileOutputStream outStream = new FileOutputStream(file);
					if(resIdFrame!=0)
					{
						Bitmap bitmapFrame=scaleBitmap(BitmapFactory.decodeResource(mResources, resIdFrame), GlobalPE.fixedWidth, GlobalPE.fixedHeight);
						
						Bitmap bitmapForSave = Bitmap.createBitmap(GlobalPE.fixedWidth,GlobalPE.fixedHeight,Config.ARGB_8888);
						Canvas canvas=new Canvas(bitmapForSave);
						canvas.drawBitmap(GlobalPE.bitmapEffect, new Matrix(), new Paint());
						canvas.drawBitmap(bitmapFrame, new Matrix(), new Paint());
						
						bitmapForSave.compress(Bitmap.CompressFormat.PNG, 100, outStream);
						
						if(bitmapFrame!=null)
						{
							bitmapFrame.recycle();
							bitmapFrame=null;
						}
						
					}
					else
					{
						GlobalPE.bitmapEffect.compress(Bitmap.CompressFormat.PNG, 100, outStream);
					}
					
					outStream.flush();
					outStream.close();
					if(outStream!=null)
					{
						outStream=null;
					}
					System.gc();
					
					new SingleMediaScanner(PhotoEditorActivity.this,file);
					chk_error=0;

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					chk_error=1;

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					chk_error=2;
				}
				catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					chk_error=3;
				}
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try
						{
							progressDialog.dismiss();
							
						}
						catch (Exception e) {
							// TODO: handle exception
						}
						if(chk_error>0)
						{
							Log.e("log_tag", "Error at Save Bitmap in Gallery..."+chk_error);
						}
						else
						{
							Toast.makeText(PhotoEditorActivity.this, "Your Image is saved as "+file.getName(), Toast.LENGTH_SHORT).show();	
						}
						
					}
				});
			}
		}).start();
	}
	
	public class SingleMediaScanner implements MediaScannerConnectionClient {

		private MediaScannerConnection mMs;
		private File mFile;

		public SingleMediaScanner(Context context,File f) {
		    mFile = f;
		    mMs = new MediaScannerConnection(context, this);
		    mMs.connect();
		}

		@Override
		public void onMediaScannerConnected() {
		    mMs.scanFile(mFile.getAbsolutePath(), null);
		}

		@Override
		public void onScanCompleted(String path, Uri uri) {
		    mMs.disconnect();
		}

	}

	private void rotateBitmapAtAngle90() {
		// TODO Auto-generated method stub
		int width = GlobalPE.bitmapEffect.getWidth();
		int height = GlobalPE.bitmapEffect.getHeight();

		GlobalPE.bitmapTemp = Bitmap.createBitmap(width, height,
				GlobalPE.bitmapEffect.getConfig());
		Canvas canvas = new Canvas(GlobalPE.bitmapTemp);
		Matrix matrix = new Matrix();
		matrix.setRotate(90, width / 2, height / 2);
		canvas.drawBitmap(GlobalPE.bitmapEffect, matrix, new Paint());

		GlobalPE.bitmapEffect = GlobalPE.bitmapTemp;
		GlobalPE.bitmapTemp=null;
		
		GlobalPE.bitmapTemp = Bitmap.createBitmap(width, height,
				GlobalPE.bitmapBackUp.getConfig());
		Canvas canvas2 = new Canvas(GlobalPE.bitmapTemp);
		matrix.setRotate(90, width / 2, height / 2);
		canvas2.drawBitmap(GlobalPE.bitmapBackUp, matrix, new Paint());
		GlobalPE.bitmapBackUp = GlobalPE.bitmapTemp;
		GlobalPE.bitmapTemp=null;
		
		ivPreview.setImageBitmap(GlobalPE.bitmapEffect);
	}

	private void resetBrightnessAll() {
		// TODO Auto-generated method stub
		brightness = 0;
		contrast = 0;
		saturation = 0;

		seekBarBrightness.setProgress(100);
		seekBarContrast.setProgress(100);
		seekBarSaturation.setProgress(7);
	}

	public void onEffectsClick(final View v) {
		
		DeselectAllEffectThumb();
		
		v.setSelected(true);
		progressDialog=ProgressDialog.show(PhotoEditorActivity.this,"", "Applying effect...");
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int id = v.getId();

				if (id == R.id.ivECancelBtn) 
				{
					GlobalPE.bitmapEffect=GlobalPE.bitmapBackUp;
				}
				else if (id == R.id.ivE1Btn) 
				{
					Bitmap bitmapTint = ImageProcessing.doTintFilter(GlobalPE.bitmapBackUp, 7);
					Bitmap bitmapTint2 = ImageProcessing.doTintFilter(bitmapTint, 7,5,5,5);
					Bitmap bitmapContrast = ImageProcessing.doContrast(bitmapTint2,
							0.2f);
					GlobalPE.bitmapEffect = ImageProcessing.doBrightness(bitmapContrast,
							20);
					
					if(bitmapTint!=null)
					{
						bitmapTint.recycle();
						bitmapTint=null;
					}
					if(bitmapTint2!=null)
					{
						bitmapTint2.recycle();
						bitmapTint2=null;
					}
					if(bitmapContrast!=null)
					{
						bitmapContrast.recycle();
						bitmapContrast=null;
					}
					
				}
				else if (id == R.id.ivE2Btn) 
				{

					Bitmap bitmapTint = ImageProcessing.doTintFilter(GlobalPE.bitmapBackUp, 20, 10, -10, 0);
					Bitmap bitmapContrast=ImageProcessing.doContrast(bitmapTint, 0.3f);
					GlobalPE.bitmapEffect=ImageProcessing.doBrightness(bitmapContrast, 20);
					
					if(bitmapTint!=null)
					{
						bitmapTint.recycle();
						bitmapTint=null;
					}
					if(bitmapContrast!=null)
					{
						bitmapContrast.recycle();
						bitmapContrast=null;
					}

				}
				else if (id == R.id.ivE3Btn) 
				{
					Bitmap bitmapTint = ImageProcessing.doTintFilter(GlobalPE.bitmapBackUp, 5, 40,15,0);
					Bitmap bitmapContrast = ImageProcessing.doContrast(bitmapTint,0.3f);
					GlobalPE.bitmapEffect=ImageProcessing.doBrightness(bitmapContrast, 10);
					if(bitmapTint!=null)
					{
						bitmapTint.recycle();
						bitmapTint=null;
					}
					
					if(bitmapContrast!=null)
					{
						bitmapContrast.recycle();
						bitmapContrast=null;
					}
					
				}
				else if (id == R.id.ivE4Btn) 
				{
					Bitmap bitmapGrayscale=ImageProcessing.doGreyscale(GlobalPE.bitmapBackUp);
					Bitmap bitmapTint = ImageProcessing.doTintFilter(bitmapGrayscale, 310, 0, 0, 10);
					Bitmap bitmapContrast=ImageProcessing.doContrast(bitmapTint, .2f);
					Bitmap bitmapBright=ImageProcessing.doBrightness(bitmapContrast, -50);
					GlobalPE.bitmapEffect=applyWhiteLayer(bitmapBright);
					
					if(bitmapGrayscale!=null)
					{
						bitmapGrayscale.recycle();
						bitmapGrayscale=null;
					}
					if(bitmapTint!=null)
					{
						bitmapTint.recycle();
						bitmapTint=null;
					}
					if(bitmapContrast!=null)
					{
						bitmapContrast.recycle();
						bitmapContrast=null;
					}
					if(bitmapBright!=null)
					{
						bitmapBright.recycle();
						bitmapBright=null;
					}
				}
				else if (id == R.id.ivE5Btn) 
				{
					Bitmap bitmapTint = ImageProcessing.doTintFilter(GlobalPE.bitmapBackUp, 340,30,15,0);
					Bitmap bitmapContrast=ImageProcessing.doContrast(bitmapTint, .2f);
					Bitmap bitmapBright=ImageProcessing.doBrightness(bitmapContrast, -50);
					GlobalPE.bitmapEffect=applyWhiteLayer(bitmapBright);
					
					if(bitmapTint!=null)
					{
						bitmapTint.recycle();
						bitmapTint=null;
					}
					if(bitmapContrast!=null)
					{
						bitmapContrast.recycle();
						bitmapContrast=null;
					}
					if(bitmapBright!=null)
					{
						bitmapBright.recycle();
						bitmapBright=null;
					}

				}
				else if (id == R.id.ivE6Btn) 
				{
					Bitmap bitmapSepia=ImageProcessing.doSepiaEffect(GlobalPE.bitmapBackUp, 7);
					Bitmap bitmapTint = ImageProcessing.doTintFilter(bitmapSepia, 20, 100, 20, 0);
					Bitmap bitmapContrast=ImageProcessing.doContrast(bitmapTint, .1f);
					Bitmap bitmapBright=ImageProcessing.doBrightness(bitmapContrast, -90);
					GlobalPE.bitmapEffect=applyWhiteLayer(bitmapBright);
					
					if(bitmapSepia!=null)
					{
						bitmapSepia.recycle();
						bitmapSepia=null;
					}
					if(bitmapTint!=null)
					{
						bitmapTint.recycle();
						bitmapTint=null;
					}
					if(bitmapContrast!=null)
					{
						bitmapContrast.recycle();
						bitmapContrast=null;
					}
					if(bitmapBright!=null)
					{
						bitmapBright.recycle();
						bitmapBright=null;
					}
				}
				else if (id == R.id.ivE7Btn) 
				{
					Bitmap bitmapTint = ImageProcessing.doTintFilter(GlobalPE.bitmapBackUp, 60,15,-20,40);
					Bitmap bitmapContrast=ImageProcessing.doContrast(bitmapTint, .2f);
					Bitmap bitmapBright=ImageProcessing.doBrightness(bitmapContrast, -30);
					GlobalPE.bitmapEffect=applyWhiteLayer(bitmapBright);
					
					if(bitmapTint!=null)
					{
						bitmapTint.recycle();
						bitmapTint=null;
					}
					if(bitmapContrast!=null)
					{
						bitmapContrast.recycle();
						bitmapContrast=null;
					}
					if(bitmapBright!=null)
					{
						bitmapBright.recycle();
						bitmapBright=null;
					}
					
				}
				else if (id == R.id.ivE8Btn) 
				{
					Bitmap bitmapTint=ImageProcessing.doTintFilter(GlobalPE.bitmapBackUp, 20, 20, 0, 30);
					GlobalPE.bitmapEffect=ImageProcessing.doBrightness(bitmapTint, -50);
					
					if(bitmapTint!=null)
					{
						bitmapTint.recycle();
						bitmapTint=null;
					}
					
				}
				else if (id == R.id.ivE9Btn) 
				{
					Bitmap bitmapSepia=ImageProcessing.doSepiaEffect(GlobalPE.bitmapBackUp, 20);

					Bitmap bitmapBright=ImageProcessing.doBrightness(bitmapSepia, -60);
					GlobalPE.bitmapEffect=applyWhiteLayer(bitmapBright);
					
					if(bitmapSepia!=null)
					{
						bitmapSepia.recycle();
						bitmapSepia=null;
					}
					
					if(bitmapBright!=null)
					{
						bitmapBright.recycle();
						bitmapBright=null;
					}
				}
				else if (id == R.id.ivE10Btn) 
				{
					Bitmap bitmapSepia=ImageProcessing.doSepiaEffect(GlobalPE.bitmapBackUp,40);
					Bitmap bitmapBright=ImageProcessing.doBrightness(bitmapSepia, -80);
					GlobalPE.bitmapEffect=applyWhiteLayer(bitmapBright);
					

					if(bitmapSepia!=null)
					{
						bitmapSepia.recycle();
						bitmapSepia=null;
					}
					
					if(bitmapBright!=null)
					{
						bitmapBright.recycle();
						bitmapBright=null;
					}
				}
				else if (id == R.id.ivE11Btn) 
				{
					Resources res = getResources();
					Bitmap bitmapLayer1=scaleBitmap(BitmapFactory.decodeResource(res, R.drawable.e_11_layer_1),GlobalPE.fixedWidth, GlobalPE.fixedHeight);
					Bitmap bitmapLayer2=scaleBitmap(BitmapFactory.decodeResource(res, R.drawable.e_11_layer_2),GlobalPE.fixedWidth, GlobalPE.fixedHeight);
					
					Paint p = new Paint();
					p.setXfermode(new PorterDuffXfermode(Mode.SRC_OVER));
					p.setShader(new BitmapShader(bitmapLayer1, TileMode.CLAMP, TileMode.CLAMP));

					Paint p2 = new Paint();
					p2.setXfermode(new PorterDuffXfermode(Mode.DARKEN));
					p2.setShader(new BitmapShader(bitmapLayer2, TileMode.CLAMP, TileMode.CLAMP));
					
					GlobalPE.bitmapEffect=Bitmap.createBitmap(GlobalPE.bitmapBackUp.getWidth(), GlobalPE.bitmapBackUp.getHeight(), GlobalPE.bitmapBackUp.getConfig());
					Canvas c = new Canvas();
					c.setBitmap(GlobalPE.bitmapEffect);
					c.drawBitmap(GlobalPE.bitmapBackUp, 0, 0, null);
					c.drawRect(0, 0, bitmapLayer1.getWidth(), bitmapLayer1.getHeight(), p);
					c.drawRect(0, 0, bitmapLayer2.getWidth(), bitmapLayer2.getHeight(), p2);
					
					
					if(bitmapLayer1!=null)
					{
						bitmapLayer1.recycle();
						bitmapLayer1=null;
					}
					if(bitmapLayer2!=null)
					{
						bitmapLayer2.recycle();
						bitmapLayer2=null;
					}
				}
				else if (id == R.id.ivE12Btn) 
				{
					Resources res = getResources();
					Bitmap bitmapLayer1=scaleBitmap(BitmapFactory.decodeResource(res, R.drawable.e_12_layer_1),GlobalPE.fixedWidth, GlobalPE.fixedHeight);
					Bitmap bitmapLayer2=scaleBitmap(BitmapFactory.decodeResource(res, R.drawable.e_12_layer_2),GlobalPE.fixedWidth, GlobalPE.fixedHeight);
					
					Paint p = new Paint();
					p.setXfermode(new PorterDuffXfermode(Mode.DARKEN));
					p.setShader(new BitmapShader(bitmapLayer1, TileMode.CLAMP, TileMode.CLAMP));

					Paint p2 = new Paint();
//					p2.setXfermode(new PorterDuffXfermode(Mode.SRC_OVER));
					p2.setShader(new BitmapShader(bitmapLayer2, TileMode.CLAMP, TileMode.CLAMP));
					
					GlobalPE.bitmapEffect=Bitmap.createBitmap(GlobalPE.bitmapBackUp.getWidth(), GlobalPE.bitmapBackUp.getHeight(), GlobalPE.bitmapBackUp.getConfig());
					Canvas c = new Canvas();
					c.setBitmap(GlobalPE.bitmapEffect);
					c.drawBitmap(GlobalPE.bitmapBackUp, 0, 0, null);
					c.drawRect(0, 0, bitmapLayer1.getWidth(), bitmapLayer1.getHeight(), p);
					c.drawRect(0, 0, bitmapLayer2.getWidth(), bitmapLayer2.getHeight(), p2);
					
					if(bitmapLayer1!=null)
					{
						bitmapLayer1.recycle();
						bitmapLayer1=null;
					}
					if(bitmapLayer2!=null)
					{
						bitmapLayer2.recycle();
						bitmapLayer2=null;
					}
					
				}
				else if (id == R.id.ivE13Btn) 
				{
					Resources res = getResources();
					Bitmap bitmapLayer1=scaleBitmap(BitmapFactory.decodeResource(res, R.drawable.e_13_layer_1),GlobalPE.fixedWidth, GlobalPE.fixedHeight);
					
					Paint p = new Paint();
					
					p.setXfermode(new PorterDuffXfermode(Mode.DARKEN));
					
					p.setShader(new BitmapShader(bitmapLayer1, TileMode.CLAMP, TileMode.CLAMP));

					Bitmap bitmapOut=Bitmap.createBitmap(GlobalPE.bitmapBackUp.getWidth(), GlobalPE.bitmapBackUp.getHeight(), GlobalPE.bitmapBackUp.getConfig());
					Canvas c = new Canvas();
					c.setBitmap(bitmapOut);
					c.drawBitmap(GlobalPE.bitmapBackUp, 0, 0, null);
					c.drawRect(0, 0, bitmapLayer1.getWidth(), bitmapLayer1.getHeight(), p);
					
					Bitmap bitmapTint = ImageProcessing.doTintFilter(bitmapOut, 340,40,15,0);
					Bitmap bitmapContrast=ImageProcessing.doContrast(bitmapTint, .3f);
					GlobalPE.bitmapEffect=ImageProcessing.doBrightness(bitmapContrast, -40);
					
					if(bitmapLayer1!=null)
					{
						bitmapLayer1.recycle();
						bitmapLayer1=null;
					}
					if(bitmapTint!=null)
					{
						bitmapTint.recycle();
						bitmapTint=null;
					}
					if(bitmapContrast!=null)
					{
						bitmapContrast.recycle();
						bitmapContrast=null;
					}
				}else if (id == R.id.ivE14Btn) 
				{
					Resources res = getResources();
					Bitmap bitmapLayer1=scaleBitmap(BitmapFactory.decodeResource(res, R.drawable.e_14_layer_1),GlobalPE.fixedWidth, GlobalPE.fixedHeight);
					Bitmap bitmapLayer2=scaleBitmap(BitmapFactory.decodeResource(res, R.drawable.e_14_layer_2),GlobalPE.fixedWidth, GlobalPE.fixedHeight);
					Bitmap bitmapLayer3=scaleBitmap(BitmapFactory.decodeResource(res, R.drawable.e_14_layer_3),GlobalPE.fixedWidth, GlobalPE.fixedHeight);
					Bitmap bitmapLayer4=scaleBitmap(BitmapFactory.decodeResource(res, R.drawable.e_14_layer_4),GlobalPE.fixedWidth, GlobalPE.fixedHeight);
					
					Paint p = new Paint();
					p.setXfermode(new PorterDuffXfermode(Mode.DARKEN));
					p.setShader(new BitmapShader(bitmapLayer1, TileMode.CLAMP, TileMode.CLAMP));

					Paint p2 = new Paint();
					p2.setXfermode(new PorterDuffXfermode(Mode.SRC_OVER));
					p2.setShader(new BitmapShader(bitmapLayer2, TileMode.CLAMP, TileMode.CLAMP));
					
					Paint p3 = new Paint();
					p3.setXfermode(new PorterDuffXfermode(Mode.SCREEN));
					p3.setShader(new BitmapShader(bitmapLayer3, TileMode.CLAMP, TileMode.CLAMP));
					
					Paint p4 = new Paint();
					p4.setXfermode(new PorterDuffXfermode(Mode.LIGHTEN));
					p4.setShader(new BitmapShader(bitmapLayer4, TileMode.CLAMP, TileMode.CLAMP));
					
					GlobalPE.bitmapEffect=Bitmap.createBitmap(GlobalPE.bitmapBackUp.getWidth(), GlobalPE.bitmapBackUp.getHeight(), GlobalPE.bitmapBackUp.getConfig());
					Canvas c = new Canvas();
					c.setBitmap(GlobalPE.bitmapEffect);
					c.drawBitmap(GlobalPE.bitmapBackUp, 0, 0, null);
					c.drawRect(0, 0, bitmapLayer1.getWidth(), bitmapLayer1.getHeight(), p);
					c.drawRect(0, 0, bitmapLayer2.getWidth(), bitmapLayer2.getHeight(), p2);
					c.drawRect(0, 0, bitmapLayer3.getWidth(), bitmapLayer3.getHeight(), p3);
					c.drawRect(0, 0, bitmapLayer4.getWidth(), bitmapLayer4.getHeight(), p4);
					
					if(bitmapLayer1!=null)
					{
						bitmapLayer1.recycle();
						bitmapLayer1=null;
					}
					if(bitmapLayer2!=null)
					{
						bitmapLayer2.recycle();
						bitmapLayer2=null;
					}
					if(bitmapLayer3!=null)
					{
						bitmapLayer3.recycle();
						bitmapLayer3=null;
					}
					if(bitmapLayer4!=null)
					{
						bitmapLayer4.recycle();
						bitmapLayer4=null;
					}
				}
				else if (id == R.id.ivE15Btn) 
				{
					Resources res = getResources();
					Bitmap bitmapLayer1=scaleBitmap(BitmapFactory.decodeResource(res, R.drawable.e_15_layer_1),GlobalPE.fixedWidth, GlobalPE.fixedHeight);
					Bitmap bitmapLayer2=scaleBitmap(BitmapFactory.decodeResource(res, R.drawable.e_15_layer_2),GlobalPE.fixedWidth, GlobalPE.fixedHeight);
					
					Paint p = new Paint();
					p.setXfermode(new PorterDuffXfermode(Mode.DARKEN));
					p.setShader(new BitmapShader(bitmapLayer1, TileMode.CLAMP, TileMode.CLAMP));

					Paint p2 = new Paint();
					p2.setXfermode(new PorterDuffXfermode(Mode.LIGHTEN));
					p2.setShader(new BitmapShader(bitmapLayer2, TileMode.CLAMP, TileMode.CLAMP));
					
					GlobalPE.bitmapEffect=Bitmap.createBitmap(GlobalPE.bitmapBackUp.getWidth(), GlobalPE.bitmapBackUp.getHeight(), GlobalPE.bitmapBackUp.getConfig());
					Canvas c = new Canvas();
					c.setBitmap(GlobalPE.bitmapEffect);
					c.drawBitmap(GlobalPE.bitmapBackUp, 0, 0, null);
					
					c.drawRect(0, 0, bitmapLayer2.getWidth(), bitmapLayer2.getHeight(), p2);
					c.drawRect(0, 0, bitmapLayer1.getWidth(), bitmapLayer1.getHeight(), p);
					
					
					if(bitmapLayer1!=null)
					{
						bitmapLayer1.recycle();
						bitmapLayer1=null;
					}
					if(bitmapLayer2!=null)
					{
						bitmapLayer2.recycle();
						bitmapLayer2=null;
					}
					
				}
				System.gc();
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try
						{
							System.gc();
							progressDialog.dismiss();
						}catch (Exception e) {
							// TODO: handle exception
						}
						
						ivPreview.setImageBitmap(GlobalPE.bitmapEffect);
					}
				});
			}
		}).start();
		
	}
	
	private Bitmap applyWhiteLayer(Bitmap bitmapMain2) {
		// TODO Auto-generated method stub
		Resources res = getResources();
		Bitmap bitmapLayer1=scaleBitmap(BitmapFactory.decodeResource(res, R.drawable.layer_white),bitmapMain2.getWidth(), bitmapMain2.getHeight());
		
		Paint p = new Paint();
		
//		p.setXfermode(new PorterDuffXfermode(Mode.MULTIPLY));
		
		p.setShader(new BitmapShader(bitmapLayer1, TileMode.CLAMP, TileMode.CLAMP));

		Bitmap bitmapOut=Bitmap.createBitmap(bitmapMain2.getWidth(), bitmapMain2.getHeight(), bitmapMain2.getConfig());
		Canvas c = new Canvas();
		c.setBitmap(bitmapOut);
		c.drawBitmap(bitmapMain2, 0, 0, null);
		c.drawRect(0, 0, bitmapLayer1.getWidth(), bitmapLayer1.getHeight(), p);
		
		
		if(bitmapLayer1!=null)
		{
			bitmapLayer1.recycle();
			bitmapLayer1=null;
		}
		System.gc();
		return bitmapOut;
	}

	private void DeselectAllEffectThumb() {
		// TODO Auto-generated method stub
		LinearLayout view=(LinearLayout) hsvEffectsBar.getChildAt(0);
		for (int i = 0; i < view.getChildCount(); i++) {
			(view.getChildAt(i)).setSelected(false);
		}
	}

	public void onFramesClick(View v) {
		
		DeselectAllFrameThumb();
		
		v.setSelected(true);
		
		String tag = v.getTag().toString();
		if (tag.equals("cancel")) {
			resIdFrame=0;
			ivFrame.setImageDrawable(null);
		} else {
			resIdFrame = getResources().getIdentifier("frame_" + tag,
					"drawable", getPackageName());
			ivFrame.setImageResource(resIdFrame);
//			ivFrame.setImageBitmap(scaleBitmap(BitmapFactory.decodeResource(mResources, resId), GlobalPE.fixedWidth/2, GlobalPE.fixedHeight/2));
		}
	}
	
	public static Bitmap scaleBitmap(Bitmap bitmap, int wantedWidth, int wantedHeight) {
		if(bitmap.getWidth()==wantedWidth)
		{
			return bitmap;
		}
		
        Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
        canvas.drawBitmap(bitmap, m, new Paint());
        
        return output;
    }
	

	private void DeselectAllFrameThumb() {
		// TODO Auto-generated method stub
		LinearLayout view=(LinearLayout) hsvFramesBar.getChildAt(0);
		for (int i = 0; i < view.getChildCount(); i++) {
			(view.getChildAt(i)).setSelected(false);
		}
	}

	public void onBrightnessClick(View v) {
		switch (v.getId()) {
		case R.id.ivDoneBrightBtn:

			if (llBrightness.getVisibility() == View.VISIBLE) {
				GlobalPE.bitmapEffect = GlobalPE.bitmapTemp;
				GlobalPE.bitmapTemp = null;

				((ImageView) findViewById(R.id.ivBrightnessBtn))
						.setSelected(false);

				llBrightness.setVisibility(View.GONE);
			}

			break;

		case R.id.ivResetBrightBtn:
			resetBrightnessAll();
			GlobalPE.bitmapTemp=ImageProcessing.applyBCS(GlobalPE.bitmapEffect, brightness, contrast, saturation);
			ivPreview.setImageBitmap(GlobalPE.bitmapTemp);
			break;
		}
	}

	private OnSeekBarChangeListener onBCSChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub

			switch (seekBar.getId()) {
			case R.id.seekBarBrightness:
				if (fromUser) {
					if (progress == 100) {
						brightness = 0;
					} else {
						brightness = (progress - 100);
					}

				}
				break;

			case R.id.seekBarContrast:

				if (fromUser) {
					if (progress == 100) {
						contrast = 0;
					} else {
						contrast = (progress - 100);
					}
					contrast = contrast / 100.f;

				}

				break;

			case R.id.seekBarSaturation:
				if (fromUser) {
					if (progress == 7) {
						saturation = 1;
					} else if(progress<7) {
						saturation = (progress - 7);
					}
					else if(progress>7)
					{
						saturation = (progress - 7)+2;
					}

				}
				break;
			}
			Log.e("log_tag", brightness+" : "+contrast+" : "+saturation);
			GlobalPE.bitmapTemp=ImageProcessing.applyBCS(GlobalPE.bitmapEffect, brightness, contrast, saturation);
			ivPreview.setImageBitmap(GlobalPE.bitmapTemp);
		}
	};

	

}