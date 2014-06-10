package com.hekangping.testmap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.widget.Toast;

public class ToastShow {

	private Context context;
	private Toast toast = null;

	public ToastShow(Context context) {
		this.context = context;
	}

	public void toastShow(String text) {
		if (toast == null) {
			toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		} else {
			toast.setText(text);
		}
		toast.show();
		try {
			this.writeFile("log.txt", new Date().toString() + "\t" + text
					+ "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Ð´Êý¾Ý
	public void writeFile(String fileName, String writestr) throws IOException {
		try {
			FileOutputStream fout = context.openFileOutput(fileName,
					Context.MODE_APPEND);
			byte[] bytes = writestr.getBytes();
			fout.write(bytes);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
