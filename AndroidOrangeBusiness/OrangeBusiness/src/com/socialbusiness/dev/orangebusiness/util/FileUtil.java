package com.socialbusiness.dev.orangebusiness.util;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import com.socialbusiness.dev.orangebusiness.R;

public final class FileUtil {
	public static String CAHCHEDIR;
	private static final String SERIALIZABLE_DIR_PATH = "/data/data/" + R.class.getPackage().getName();

    public static byte [] readFromFile(File file){
        byte [] result;
        try {
            result = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
            result = new byte[0];
        }
        return result;
    }

    public static boolean writeToFile(File file, byte [] data){
        try {
            FileUtils.writeByteArrayToFile(file, data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
    public static boolean deleteSerializableFileForDefaultPath(String name) {
    	File file = new File(getSerializableDirPath(), name);
    	return file.exists() && file.delete();
    }
    
    public static boolean writeSerializableForDefaultPath(String name, Serializable data) {
    	File file = new File(getSerializableDirPath(), name);
    	return writeSerializable(file.getAbsolutePath(), data);
    }
    
    public static boolean writeSerializable(String filePath, Serializable data){
    	boolean success = false;
        if(TextUtils.isEmpty(filePath)){
            return success;
        }

        ObjectOutputStream output = null;
        try{
            File dataFile = new File(filePath);
            if(data == null){
                dataFile.delete();
            }else{
                output = new ObjectOutputStream(new FileOutputStream(dataFile));
                output.writeObject(data);
            }
            success = true;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(output != null){
                    output.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return success;
    }
    
    public static Serializable readSerializableFromDefaultPath(String name){
    	File file = new File(getSerializableDirPath(), name);
    	return readSerializable(file.getAbsolutePath());
    }

    public static Serializable readSerializable(String filePath){
        Serializable result = null;
        if(TextUtils.isEmpty(filePath)){
            return result;
        }
        ObjectInputStream input = null;
        try{
            File dataFile = new File(filePath);
            if(! dataFile.exists()){
                return result;
            }
            input = new ObjectInputStream(new FileInputStream(dataFile));
            result = (Serializable)input.readObject();
        }catch(Exception e){
            e.printStackTrace();
            e.getMessage();
        }finally{
            try{
                if(input != null){
                    input.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     *
     * @param url eg.http://myar.kollway.com/data/uploads/images/material/1389619056651.jpg
     * @param prefix eg.http://myar.kollway.com
     * @return eg./data/uploads/images/material
     */
    public static String getDirByUrl(String url, String prefix){
        int hostIndex = url.indexOf(prefix);
        String sub1 = url.substring(hostIndex + prefix.length(), url.length());
        int lastDiv = sub1.lastIndexOf('/');
        return sub1.substring(0, lastDiv);
    }

    /**
     *
     * @param url eg.http://myar.kollway.com/data/uploads/images/material/1389619056651.jpg
     * @return eg.1389619056651.jpg
     */
    public static String getFileNameByUrl(String url){
        int lastDiv = url.lastIndexOf('/');
        return url.substring(lastDiv + 1, url.length());
    }


    /**
     * 根据文件名获取该文件的目录
     * /root/a/b/c.txt => /root/a/b
     * @param fileName
     * @return
     */
    public static String getDirNameByFile(String fileName) {
        if(! TextUtils.isEmpty(fileName)){
            int lastSeparater = fileName.lastIndexOf('/');
            if(lastSeparater >= 0){
                return fileName.substring(0, lastSeparater);
            }
        }
        return "";
    }

    /**
     * 根据绝对路径获取该文件名
     * @param absolutePath
     * @param containExtendName 是否包含扩展名
     * @return
     */
    public static String getFileNameByPath(String absolutePath, boolean containExtendName) {
        String result = "";
        if(absolutePath != null){
            int lastSeparater = absolutePath.lastIndexOf('/');
            int len = absolutePath.length();
            if(lastSeparater >= 0 && lastSeparater < len){
                result = absolutePath.substring(lastSeparater + 1, len);
                if(! containExtendName){
                    int dotIndex = result.lastIndexOf('.');
                    result = result.substring(0, dotIndex);
                }
            }
        }
        return result;
    }

    public static void delete(File file) {
        if(file != null && file.exists() && file.isDirectory()){
            File[] files = file.listFiles();
            if(files!=null) { //some JVMs return null for empty dirs
                for(File f: files) {
                    if(f.isDirectory()) {
                    	delete(f);
//                    	if ((f + "").endsWith(".zip") || (f + "").endsWith("rar")) {
//                    		f.delete();
////                    		continue;
//                    	} else {
//                    		delete(f);
//                    	}
                    } else {
                        f.delete();
                    }
                }
            }
            file.delete();
        }else{
            file.delete();
        }
    }
    
    /**
     * 清除本应用所有文件(/data/data/com.xxx.xxx/)
     * @param context
     */
    public static void cleanProjectCacheFile(Context context) {
    	delete(new File("/data/data/" + context.getPackageName()));
    }
    
    /**
     * 清除本应用内部缓存(/data/data/com.xxx.xxx/cache)
     * @param context
     */
    public static void cleanInternalCache(Context context) {
    	// getCacheDir()能够得到当前项目的缓存地址
    	delete(context.getCacheDir());
    }
    
    /**
     * 清除本应用所有数据库(/data/data/com.xxx.xxx/databases)
     * @param context
     */
    public static void cleanDatabases(Context context) {
    	delete(new File("/data/data/" + context.getPackageName() + "/databases"));
    }
    
    /**
     * 按名字清除本应用数据库
     * @param context
     * @param dbName
     */
    public static void cleanDatabasesByName(Context context, String dbName) {
    	if (!TextUtils.isEmpty(dbName)) {
    		context.deleteDatabase(dbName);
    	}
    }
    
    /**
     * 清除本应用SharedPreference(/data/data/com.xxx.xxx/shared_prefs)
     * @param context
     */
    public static void cleanSharedPreference(Context context) {
    	delete(new File("/data/data/" + context.getPackageName() + "/shared_prefs"));
    }
    
    /**
     * 清除(/data/data/com.xxx.xxx/files)下所有文件
     * @param context
     */
    public static void cleanFiles(Context context) {
    	delete(context.getFilesDir());
    }
    
    /**
     * 清除外部项目下的文件(/mnt/sdcard/android/data/com.xxx.xxx/)
     * @param context
     */
    public static void cleanExternalCacheFile(Context context) {
    	if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
    		String file = Environment.getExternalStorageDirectory() + "/android/data/" + context.getPackageName()+File.separator+"files"+File.separator+"unzip";
    		delete(new File(file));
    	}
    }
    
    /**
     * 删除自定义路径下的文件，使用需小心，表不要误删。而且只支持目录下的文件删除
     * @param filePath
     */
    public static void cleanCustomCache(String filePath) {
    	delete(new File(filePath));
    }
    
    /**
     * 清除本应用所有的数据
     * @param context
     * @param filepath
     */
    public static void cleanApplicationData(Context context, String ... filepath) {
    	cleanInternalCache(context);
    	cleanExternalCacheFile(context);
    	cleanDatabases(context);
    	cleanSharedPreference(context);
    	cleanFiles(context);
    	for (String filePath : filepath) {
    		cleanCustomCache(filePath);
    	}
    }
    
    public static boolean copyFile(File sourceFile, File destFile) {
        FileChannel source = null;
        FileChannel destination = null;
        try{
            if(!destFile.exists()) {
                destFile.createNewFile();
            }

            try {
                source = new FileInputStream(sourceFile).getChannel();
                destination = new FileOutputStream(destFile).getChannel();
                destination.transferFrom(source, 0, source.size());
            }
            finally {
                if(source != null) {
                    source.close();
                }
                if(destination != null) {
                    destination.close();
                }
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(source != null){
                try {
                    source.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(destination != null){
                try {
                    destination.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static boolean saveBitmapToJPG(Bitmap bitmap, String filePath){
        boolean result = false;
        OutputStream outputStream = null;
        try{
            outputStream = new BufferedOutputStream(new FileOutputStream(filePath));
            result = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 根据指定路径创建一个文件
     * @param absolutePath
     * @return
     */
    public static File touchFile(String absolutePath){
        File file = null;
        try{
            String dirName = getDirNameByFile(absolutePath);
            File dir = new File(dirName);
            if(! dir.exists()){
                dir.mkdirs();
            }

            file = new File(absolutePath);
            file.createNewFile();
        }catch (Exception e){
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 下载文件
     * @param downloadFromUrl 从该URL下载
     * @param downloadTo 下载到目标文件
     * @return
     */
    public static boolean downloadFile(String downloadFromUrl, File downloadTo){
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(downloadFromUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                return false;
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(downloadTo);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) > 0) {
                total += count;
                output.write(data, 0, count);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            }
            catch (IOException ignored) { }

            if (connection != null)
                connection.disconnect();
        }
        return false;
    }
    
    public static void copyAssetFile(Context context, String assetsFileName, File to) {
          AssetManager assetManager = context.getAssets();
          InputStream in = null;
          OutputStream out = null;
          try {
        	  if(!to.getParentFile().exists()) {
        		  to.getParentFile().mkdirs();
        	  }
              in = assetManager.open(assetsFileName);
              out = new FileOutputStream(to.getAbsolutePath());
              byte[] buffer = new byte[1024];
              int read = -1;
              while((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
              }
              in.close();
              in = null;
              out.flush();
              out.close();
              out = null;
          }
          catch(IOException e) {
              Log.e("tag", "Failed to copy asset file: " + assetsFileName);
          } finally {
        	 closeStream(out);
        	 closeStream(in);
          }
    }
    
    public static void closeStream(Closeable stream) {
    	if(stream != null) {
    		try {
				stream.close();
			} catch (IOException e) {
				
			}
    		stream = null;
    	}
    }
    
    public static String getSerializableDirPath() {
    	File dir = new File(SERIALIZABLE_DIR_PATH);
    	if(!dir.exists()) {
    		dir.mkdirs();
    	}
    	return dir.getAbsolutePath();
    }
    
}
