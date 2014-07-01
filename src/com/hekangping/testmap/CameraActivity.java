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
	/** ͼƬ�ļ����Ŀ¼ */
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

			fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file
																// to
																// save the
																// image
			intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image
																// file
																// name

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
			// ����ͼƬ���ű���
			final int minSideLength = Math.min(320, 480);
			opts.inSampleSize = computeSampleSize(opts, minSideLength,
					320 * 480);
			opts.inJustDecodeBounds = false;
			opts.inInputShareable = true;
			// ������λͼ���õ����ؿռ䣬���ϵͳgc����ô������ա��������ٴα����ʣ����Bitmap�Ѿ�decode����ô�����Զ����½���
			opts.inPurgeable = true;
			// Ĭ��ΪARGB_8888
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			opts.inInputShareable = true;// λͼ���Թ���һ���ο���������(inputstream�����е�)
			opts.inSampleSize = 10;// decode ԭͼ1/4

			Bitmap bitmap = BitmapFactory.decodeFile(
					mediaFile.getAbsolutePath(), opts);
			mImageView.setImageBitmap(bitmap);
		}
	}

	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

}
