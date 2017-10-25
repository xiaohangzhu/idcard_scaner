package sign.myapplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * @Title：
 * @Description：
 * @Package
 * @ClassName FileUtils
 * @author harris
 * @date
 * @version
 */

public class FileImageUtils {

	/**
	 * @Fields DIR_NAME : TODO()
	 */
	public static String DIR_NAME = "rongchuanghaha";

	public static final int SDCARD_MINSIZE = 5;
	/**
	 * @Fields sdcardState : TODO()
	 */
	private static final int FREE_SD_SPACE_NEEDED_TO_CACHE = 10;

	public static String sdcardState = "";

	public static void createPicFile(Context context, String picName, InputStream is) {
		FileOutputStream out = null;
		if (is != null) {
			byte[] bufByte = new byte[1024];
			try {
				int len;
				File file = null;
				sdcardState = android.os.Environment.getExternalStorageState();

				if (sdcardState.equals(Environment.MEDIA_MOUNTED)) {
					file = new File(android.os.Environment.getExternalStorageDirectory(), DIR_NAME);

					if (!file.exists()) {
						file.mkdirs();
					}
					out = new FileOutputStream(android.os.Environment.getExternalStorageDirectory()
							+ DIR_NAME + picName);
				} else {
					file = new File(
							context.getApplicationContext().getFilesDir().getAbsolutePath(),
							DIR_NAME);

					if (!file.exists()) {
						file.mkdirs();
					}
					out = new FileOutputStream(context.getApplicationContext().getFilesDir()
							.getAbsolutePath()
							+ DIR_NAME + picName);
				}

				while ((len = is.read(bufByte)) != -1) {
					out.write(bufByte, 0, len);
				}
				out.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (out != null)
						out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @Title: deleteAllFiles
	 * @Description:删除所有的文件
	 * @param filePath
	 */

	public static void deleteAllFiles(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (filePath.endsWith(File.separator)) {
				temp = new File(filePath + tempList[i]);
			} else {
				temp = new File(filePath + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				deleteAllFiles(filePath + "/" + tempList[i]);//
				deleteFolder(filePath + "/" + tempList[i]);//
			}
		}
	}

	/**
	 * @Title: deleteFolder
	 * @Description:删除文件
	 * @param folderPath
	 */

	public static void deleteFolder(String folderPath) {
		try {
			deleteAllFiles(folderPath);
			String filePath = folderPath;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			myFilePath.delete();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveMyBitmap(String bitName, Bitmap mBitmap) {
		File f = new File(android.os.Environment.getExternalStorageDirectory() + DIR_NAME + bitName
				+ ".png");
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}




	public static File saveBitmapForFile(String bitName, Bitmap mBitmap) {
		File f = new File(android.os.Environment.getExternalStorageDirectory() + DIR_NAME + bitName
				+ ".png");
		try {
			f.createNewFile();
		} catch (IOException e) {

		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f;
	}


	/**
	 * 根据URL获取文件
	 * @return
	 */
	public static File getFile(final  String url ,Context context){
		File file = new File(url);
		if (file.exists()) {
			return file;
		}
		return null;
	}
	/**
	 *
	 * @param bitName
	 * @param mBitmap
	 * @param context
	 * @param catchpath
     */
	public static void saveCatchBitmap(String bitName, Bitmap mBitmap, Context context,String  catchpath) {
		File f = new File(catchpath, bitName);
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}




	/**
	 * @Title: getfileBitmap
	 * @Description:从文件获取图片
	 * @param url
	 * @param context
	 * @return
	 */

	public static Bitmap getfileBitmap(final String url, Context context) {

		File file = new File(url);
		if (file.exists()) {
			Bitmap bmp = BitmapFactory.decodeFile(url);
			if (bmp == null) {
				file.delete();
			} else {

				return bmp;
			}
		}
		return null;

	}

//	/**
//	 * @Title: readJson
//	 * @Description:获取本地json
//	 * @param context
//	 * @param name
//	 * @return
//	 */
//
//	public static String readJson(Context context, String name) {
//		String fileName = name; // 文件名字
//		String res = "";
//		try {
//			InputStream in = context.getResources().getAssets().open(fileName);
//			int length = in.available();
//			byte[] buffer = new byte[length];
//			in.read(buffer);
//			res = EncodingUtils.getString(buffer, "UTF-8");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return res;
//	}

	/**
	 * 剩余空间
	 * 
	 * @param
	 * @return
	 */
	public static boolean isAvaiableSpace() {
		boolean ishasSpace = false;
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			String sdcard = Environment.getExternalStorageDirectory().getPath();
			StatFs statFs = new StatFs(sdcard);
			long blockSize = statFs.getBlockSize();
			long blocks = statFs.getAvailableBlocks();
			long availableSpare = (blocks * blockSize) / (1024 * 1024);
			Log.d("剩余空间", "availableSpare = " + availableSpare);
			if (availableSpare > SDCARD_MINSIZE) {
				ishasSpace = true;
			}
		}
		return ishasSpace;
	}

	/**
	 * 根据URL获取文件
	 *
	 * @return
	 */
	public static File getFile(final String url) {
		File file = new File(url);
		if (file.exists()) {
			return file;
		}
		return null;
	}



	/**
	 * 获取文件并压缩
	 * @param imageFile
	 * @param activity
	 * @return
	 */
	public static File compressByFile(File imageFile, Context activity) {
		final int MAX_NUM_OF_REPAIR = 800 * 800;
		File bitmap = null;
		try {
			if (imageFile.length() > 0) {
				final String catchpath = activity.getCacheDir().getAbsolutePath();
				Bitmap zoombitmapBitmap = Util.zoombitmapBitmap(
						imageFile.getAbsolutePath(), MAX_NUM_OF_REPAIR);
				FileImageUtils.saveCatchBitmap(StringUtils.md5(imageFile.getName()), zoombitmapBitmap, activity, catchpath);
				bitmap = getFile(catchpath + "/" + StringUtils.md5(imageFile.getName()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bitmap;
	}

	/**
	 * 判断一下文件尺寸
	 *
	 * @param file
	 * @return
	 */
	public static boolean isFileChack(File file) {
		try {
			long size = getFileSize(file);
			if (size > 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 获取文件尺寸
	 *
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static long getFileSize(File file) throws Exception {
		long size = 0;
		if (file.exists()) {
			FileInputStream fis = null;
			fis = new FileInputStream(file);
			size = fis.available();
		} else {
			file.createNewFile();
			Log.e("获取文件大小", "文件不存在!");
		}
		return size;
	}



}
