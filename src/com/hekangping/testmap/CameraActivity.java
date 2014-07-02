/**
 * 
 */
package com.hekangping.testmap;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * @author developer
 * 
 */
public class CameraActivity extends Activity {

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri fileUri;
	private ImageView mImageView = null;
	/** 图片文件存放目录 */
	private static File mediaStorageDir = null;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private static final String TAG = "CameraActivity";
	public static String DIRECTORY_PICTURES = "MapPictures";
	private static File mediaFile;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button mButton = (Button) findViewById(R.id.button);
		mButton.setOnClickListener(new ButtonOnClickListener());

		mImageView = (ImageView) findViewById(R.id.imageView);
	}

	private class ButtonOnClickListener implements View.OnClickListener {
		public void onClick(View v) {
			Log.d(TAG, "ButtonOnClickListener.onClick()");
			// create Intent to take a picture and return control to the calling
			// application
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			// create a file to save the image
			fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
			// set the image file name
			intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
			// start the image capture Intent
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
		}
	}

	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type) {
		Log.d(TAG, "getOutputMediaFile()");
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		mediaStorageDir = new File(Environment.getExternalStorageDirectory(),
				DIRECTORY_PICTURES);
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(TAG, "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			Log.d(TAG, "onActivityResult()");
			Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			// inJustDecodeBounds属性设置为true就可以让解析方法禁止为bitmap分配内存，返回值也不再是一个Bitmap对象，而是null。
			// 虽然Bitmap是null了，但是BitmapFactory.Options的outWidth、outHeight和outMimeType属性都会被赋值
			BitmapFactory.decodeFile(mediaFile.getAbsolutePath(), opts);
			// 图片缩放比例,2的倍数
			opts.inSampleSize = calculateInSampleSize(opts, 160, 240);

			Log.d(TAG, "opts.inSampleSize=" + opts.inSampleSize);
			opts.inJustDecodeBounds = false;
			opts.inInputShareable = true;
			// 产生的位图将得到像素空间，如果系统gc，那么将被清空。当像素再次被访问，如果Bitmap已经decode，那么将被自动重新解码
			opts.inPurgeable = true;
			// 默认为ARGB_8888
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			opts.inInputShareable = true;// 位图可以共享一个参考输入数据(inputstream、阵列等)
			// opts.inSampleSize = 10;// decode 原图1/4

			Bitmap bitmap = BitmapFactory.decodeFile(
					mediaFile.getAbsolutePath(), opts);
			if (bitmap != null) {
				mImageView.setImageBitmap(bitmap);
			}
		}
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

}
