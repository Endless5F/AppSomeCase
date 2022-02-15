package com.android.core.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.zip.GZIPOutputStream;

/**
 * 文件处理辅助类
 * <br>
 * 这个类新加了得到缓存文件的路径的方法
 */
public final class FileUtil {

    /**
     * DEBUG
     */
    private static final boolean DEBUG = false/*true && LibUtilConfig.GLOBAL_DEBUG*/;
    /**
     * DEBUG
     */
    private static final String TAG = "FileUtils";
    /**
     * 缓存路径，形式是：sdcard/Android/data/package-name
     */
    private static String sCacheDir = null;
    /**
     * 9.3.5 缓存大小设置
     */
    static final int BUFFER_SIZE = 1024;
    /**
     * File buffer stream size
     */
    public static final int FILE_STREAM_BUFFER_SIZE = 8192;
    /**
     * 文件系统Block Size大小
     */
    private static int FS_BLOCK_SIZE = 0;
    /**
     * unzip buffer size.
     */
    static final int UNZIP_BUFFER = 2048;
    /**
     * file的schema
     */
    public static final String FILE_SCHEMA = "file://";
    /**
     * invalid index
     */
    public static int INVALID_INDEX = -1;
    /**
     * increament one step
     */
    public static int ONE_INCREAMENT = 1;

    /**
     * 针对拿到的size为<=0的情况 ，展示未知
     */
    public static final String UNKNOW = "未知";

    /**
     * SD卡存储cache路径
     */
    private static final String EXTERNAL_STORAGE_DIRECTORY = "/baidu";
    /**
     * 旧版本SD卡存储cache路径
     */
    private static final String SEARCHBOX_FOLDER = "searchbox";

    /**
     * private
     */
    private FileUtil() {

    }

    /**
     * 得到应用程序的缓存根目录
     * <p>
     * <li>路径是/mnt/sdcard/Android/data/package-name/cache，这个目录在应用卸载后会自动删除
     *
     * @return 缓存目录
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static String getCacheDir(@NonNull Context context) {
        if (TextUtils.isEmpty(sCacheDir)) {
            sCacheDir = getDeviceCacheDir(context.getApplicationContext());
        }

        if (DEBUG) {
            if (TextUtils.isEmpty(sCacheDir)) {
                Log.e(TAG, "FileUtils#getCacheDir  cache dir = null");
            } else {
                Log.d(TAG, "FileUtils#getCacheDir  cache dir = " + sCacheDir);
            }
        }

        return sCacheDir;
    }

    /**
     * 判断文件是否存在
     *
     * @param fileName
     * @return
     */
    public static boolean exists(String fileName) {
        return !TextUtils.isEmpty(fileName) && new File(fileName).exists();
    }

    /**
     * 删除指定文件
     *
     * @param path 文件路径
     * @return 是否成功删除
     */
    public static boolean deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        if (file.exists()) {
            return deleteFile(file);
        }
        return false;
    }

    /**
     * 删除指定文件、文件夹内容
     *
     * @param file 文件或文件夹
     * @return 是否成功删除
     */
    public static boolean deleteFile(File file) {
        if (DEBUG) {
            Log.d(TAG, "delete file:" + file);
        }

        if (file == null) {
            return false;
        }

        boolean isDeletedAll = true;

        if (file.exists()) {
            // 判断是否是文件,直接删除文件
            if (file.isFile()) {
                isDeletedAll &= file.delete();

                // 遍历删除一个文件目录
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        isDeletedAll &= deleteFile(files[i]); // 迭代删除文件夹内容
                    }
                }

                isDeletedAll &= file.delete();

            } else {
                if (DEBUG) {
                    Log.d(TAG, "a special file:" + file);
                }
            }
        } else {
            if (DEBUG) {
                Log.d(TAG, "not found the file to delete:" + file);
            }
        }

        return isDeletedAll;
    }

    /**
     * 保存文件
     *
     * @param data     data
     * @param saveFile saveFile
     * @return true 成功 如果文件已经存在不会重新写入直接返回false, 或者是内容为空则直接返回false
     */
    public static boolean saveFile(String data, File saveFile) {
        if (TextUtils.isEmpty(data)) {
            return false;
        }

        if (saveFile.exists()) {
            return false;
        }

        saveFileCommon(data.getBytes(), saveFile);

        return true;
    }

    /**
     * 保存文件
     *
     * @param data     data
     * @param saveFile saveFile
     * @param force    是否强制更新如果已经存在该文件
     * @return
     */
    public static boolean saveFile(String data, File saveFile, boolean force) {
        if (TextUtils.isEmpty(data)) {
            return false;
        }

        if (saveFile.exists() && !force) {
            return false;
        }

        saveFileCommon(data.getBytes(), saveFile);

        return true;
    }

    /**
     * 保存文件
     *
     * @param data         data
     * @param saveFilePath saveFile路径
     * @param force        是否强制更新如果已经存在该文件
     * @return
     */
    public static boolean saveFile(String data, String saveFilePath, boolean force) {
        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(saveFilePath)) {
            return false;
        }
        File saveFile = new File(saveFilePath);
        return saveFile(data, saveFile, force);
    }

    /**
     * 保存文件-对应为external dir,使用需要慎重
     *
     * @param context  Context
     * @param data     要保存的数据
     * @param dirName  存储目录
     * @param fileName 存储文件名
     * @return 保存的文件
     */
    public static File saveFile(Context context, byte[] data, String dirName, String fileName) {
        if (context == null || data == null || TextUtils.isEmpty(dirName) || TextUtils.isEmpty(fileName)) {
            if (DEBUG) {
                Log.e(TAG, "saveFile: invalid parameter!");
            }
            return null;
        }

        File dirFile = context.getExternalFilesDir(dirName);
        if (dirFile != null && !dirFile.exists()) {
            // 如果不存在文件夹，创建文件夹
            dirFile.mkdirs();
        }

        File fileToSave = new File(dirFile, fileName);

        saveFileCommon(data, fileToSave);

        return fileToSave;
    }

    /**
     * 保存文件common
     *
     * @param data   data
     * @param saveTo 保存文件
     */
    private static void saveFileCommon(byte[] data, File saveTo) {
        InputStream is = new ByteArrayInputStream(data);
        saveToFile(is, saveTo);
        Closeables.closeSafely(is);
    }

    /**
     * 存文件到应用Cache目录
     *
     * @param context  应用上下文
     * @param data     数据
     * @param fileName 文件名
     * @return 存储的文件
     */
    public static File saveCacheFile(Context context, byte[] data, String fileName) {
        if (context == null || data == null || TextUtils.isEmpty(fileName)) {
            if (DEBUG) {
                Log.e(TAG, "saveFile: invalid parameter!");
            }
            return null;
        }

        File fileToSave = new File(context.getCacheDir(), fileName);

        InputStream is = new ByteArrayInputStream(data);
        saveToFile(is, fileToSave);
        Closeables.closeSafely(is);

        return fileToSave;
    }

    /**
     * 将输入流存储到指定文件
     *
     * @param inputStream 输入流
     * @param file        存储的文件
     */
    public static void saveToFile(InputStream inputStream, File file) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            copy(inputStream, outputStream);
        } catch (FileNotFoundException e) {
            if (DEBUG) {
                Log.d(TAG, "catch FileNotFoundException");
            }
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.d(TAG, "catch IOException");
                    }
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将文本内容存储到指定文件
     *
     * @param content
     * @param file
     * @param append  if <code>true</code>, then bytes will be written
     *                to the end of the file rather than the beginning
     * @return boolean true:存储成功 false:存储失败
     */
    public static boolean saveToFileWithReturn(String content, File file, boolean append) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        return saveToFileWithReturn(inputStream, file, append);
    }

    /**
     * 将输入流存储到指定文件
     *
     * @param inputStream 输入流
     * @param file        存储的文件
     * @param append      是否追加
     * @return boolean true:存储成功 false:存储失败
     */
    public static boolean saveToFileWithReturn(InputStream inputStream, File file, boolean append) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file, append);
            long size = copy(inputStream, outputStream);
            return size != 0;
        } catch (FileNotFoundException e) {
            if (DEBUG) {
                Log.d(TAG, "catch FileNotFoundException");
            }
            return false;
        } finally {
            Closeables.closeSafely(outputStream);
        }
    }

    /**
     * 拷贝文件
     *
     * @param src 源文件
     * @param dst 目标文件
     * @return 拷贝的字节数
     */
    public static long copy(File src, File dst) {
        if (null == src || null == dst) {
            return 0;
        }

        if (!src.exists()) {
            return 0;
        }

        long size = 0;

        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(src);
            os = new FileOutputStream(dst);
            size = copy(is, os);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Closeables.closeSafely(is);
            Closeables.closeSafely(os);
        }

        return size;
    }

    /**
     * 从输入流中读取字节写入输出流
     *
     * @param is 输入流
     * @param os 输出流
     * @return 复制大字节数
     */
    public static long copy(InputStream is, OutputStream os) {
        if (null == is || null == os) {
            return 0;
        }

        try {
            final int defaultBufferSize = 1024 * 3;
            byte[] buf = new byte[defaultBufferSize];
            long size = 0;
            int len = 0;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
                size += len;
            }
            os.flush();
            return size;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 得到应用程序的缓存目录
     * <p>
     * <li>API Level >= 8时，路径是/mnt/sdcard/Android/data/package-name/cache，这个目录会自动删除
     * <li>API Level < 8时，路径是/sdcard/，当应用删除时，这个目录不会被删除
     *
     * @param context context
     * @return 缓存目录
     */
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.FROYO)
    private static String getDeviceCacheDir(Context context) {
        File cacheDir = null;
        cacheDir = context.getExternalCacheDir();

        if (null == cacheDir) {
            cacheDir = context.getCacheDir();
        }

        if (null == cacheDir) {
            cacheDir = context.getFilesDir();
        }

        return (null != cacheDir) ? cacheDir.getAbsolutePath() : null;
    }

    /**
     * 将字节压缩成gzip文件
     *
     * @param bytes
     * @param outputFile
     */
    public static void saveToGzip(byte[] bytes, File outputFile) {
        if (bytes == null || bytes.length <= 0 || outputFile == null) {
            return;
        }
        InputStream in = null;
        GZIPOutputStream gzipOutputStream = null;
        int len = 0;
        try {
            gzipOutputStream = new GZIPOutputStream(new FileOutputStream(outputFile, false));
            byte[] buffer = new byte[BUFFER_SIZE];
            in = new ByteArrayInputStream(bytes);
            while ((len = in.read(buffer, 0, BUFFER_SIZE)) > 0) {
                gzipOutputStream.write(buffer, 0, len);
            }
            gzipOutputStream.finish();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Closeables.closeSafely(gzipOutputStream);
            Closeables.closeSafely(in);
        }
    }

    /**
     * 读取文件内容
     *
     * @param file file
     * @return 文件内容String
     */
    public static String readFileData(File file) {
        try {
            return readInputStream(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }
        }

        return "";
    }

    /**
     * 缓存文件
     *
     * @param context Context Object
     * @param file    本地文件名
     * @param data    要保存的数据
     * @param mode    打开文件的方式
     * @return 是否保存成功
     */
    public static boolean cache(Context context, String file, String data, int mode) {
        return cache(context, file, data.getBytes(), mode);
    }

    /**
     * 缓存文件
     *
     * @param context Context Object
     * @param file    本地文件名
     * @param data    要保存的数据
     * @param mode    打开文件的方式
     * @return 是否保存成功
     */
    public static boolean cache(Context context, String file, byte[] data, int mode) {
        boolean bResult = false;
        if (null == data) {
            data = new byte[0];
        }

        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(file, mode);
            fos.write(data);
            fos.flush();
            bResult = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bResult;
    }

    /**
     * 删除缓存文件，通常这个文件是存在应用程序的系统数据目录里面，典型的目录是data/data/package-name/files
     *
     * @param context context
     * @param name    本地文件名，不要包含路径分隔符
     * @return true：成功，false：失败
     */
    public static boolean deleteCache(Context context, String name) {
        boolean succeed = false;

        try {
            succeed = context.deleteFile(name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return succeed;
    }

    /**
     * 读取缓存文件中的数据
     *
     * @param context 上下文
     * @param file    文件名
     * @return 文件中的字符串数据
     */
    public static String readCacheData(Context context, String file) {
        try {
            return readInputStream(context.openFileInput(file));
        } catch (FileNotFoundException ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }
        }

        return "";
    }

    /**
     * 读取文件数据
     *
     * @param inputStream inputStream
     * @return string
     */
    static String readInputStream(InputStream inputStream) {
        if (inputStream == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            String str = "";
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除指定的文件
     *
     * @param file file
     * @return true/false
     */
    public static boolean safeDeleteFile(File file) {
        // 由于在某些手机上面File#delete()方法是异步的，会导致后续的创建文件夹失败，
        // 推荐的做法是对要删除的文件重命名，然后再删除，这样就不会影响后续创建文件夹。
        try {
            if (file == null || !file.exists()) {
                return true;
            }
            String filePath = file.getAbsolutePath();
            File oldFile = new File(filePath);
            // 构造一个不存在的文件名
            long time = System.currentTimeMillis();
            File tempFile = new File(filePath + time + ".tmp");
            oldFile.renameTo(tempFile);
            return tempFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 安全的创建一个新文件.如果其上层文件夹不存在，则会先创建上层文件夹，避免No such file exception
     *
     * @param saveFile file
     * @return result
     */
    public static boolean createFileSafely(File saveFile) {
        if (saveFile == null || saveFile.exists()) {
            return false;
        }
        File saveFileParent = saveFile.getParentFile();
        if (saveFileParent != null && !saveFileParent.exists()) {
            saveFileParent.mkdirs();
        }
        try {
            return saveFile.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 获取不带文件扩展名的文件名称
     *
     * @param filename 文件全名称
     * @return 不带文件扩展名的文件名称
     */
    public static String getFileNameNoExt(String filename) {
        if (!TextUtils.isEmpty(filename)) {
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex > -1 && dotIndex < filename.length()) {
                return filename.substring(0, dotIndex);
            }
        }
        return filename;
    }

    /**
     * 获取不带文件扩展名的文件名称
     *
     * @param filename 文件全名称
     * @param tag      要插入的tag
     * @return 不带文件扩展名的文件名称
     */
    public static String insertTagInFileName(String filename, String tag) {
        if (!TextUtils.isEmpty(filename)) {
            int dotIndex = filename.lastIndexOf('.');
            StringBuilder sb = new StringBuilder();
            if (dotIndex > -1 && dotIndex < filename.length()) {
                sb.append(filename.substring(0, dotIndex));
                if (!TextUtils.isEmpty(tag)) {
                    sb.append(tag);
                }
                sb.append(filename.substring(dotIndex));
                return sb.toString();
            }
        }
        return filename;
    }

    /**
     * 从文件路径中获取文件名(包括文件后缀)
     *
     * @param path 文件路径
     * @return
     */
    public static String getFileNameFromPath(String path) {
        if (TextUtils.isEmpty(path) || path.endsWith(File.separator)) {
            return "";
        }
        int start = path.lastIndexOf(File.separator);
        int end = path.length();
        if (start != INVALID_INDEX && end > start) {
            return path.substring(start + ONE_INCREAMENT, end);
        } else {
            return path;
        }
    }

    /**
     * app存储文件到sd卡
     *
     * @param fileName   存储的文件名称
     * @param saveFolder 当前app统一存储cache文件的文件夹名称，例如{@link #SEARCHBOX_FOLDER}
     * @return File
     */
    public static File getPublicExternalDiretory(String fileName, String saveFolder) {
        File dir = new File(Environment.getExternalStorageDirectory(),
                EXTERNAL_STORAGE_DIRECTORY + File.separator + saveFolder);
        File file = null;
        if (ensureDirectoryExist(dir)) {
            file = new File(dir, fileName);
        }

        return file;
    }

    /**
     * 为了兼容手百线上版本 保留旧的方法
     *
     * @param fileName
     * @return
     */
    public static File getPublicExternalDiretory(String fileName) {
        return getPublicExternalDiretory(fileName, SEARCHBOX_FOLDER);
    }

    /**
     * 判断路径是否存在
     *
     * @param dir
     * @return
     */
    public static boolean ensureDirectoryExist(final File dir) {
        if (dir == null) {
            return false;
        }
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (SecurityException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 生成文件大小的字符串.
     *
     * @param size 文件大小
     * @return 表示经过格式的字符串
     */
    public static String generateFileSizeText(long size) {
        String unit;
        Float outNumber;
        if (size <= 0) {
            return UNKNOW;
        } else if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            unit = "KB";
            outNumber = (float) size / 1024;
        } else if (size < 1024 * 1024 * 1024) {
            unit = "MB";
            outNumber = (float) size / 1024 * 1024;
        } else {
            unit = "GB";
            outNumber = (float) size / 1024 * 1024 * 1024;
        }
        /*
         * 文件大小显示格式化. 大于1KB的文件大小数字显示形如1011.11,小于1KB的文件显示具体大小
         */
        DecimalFormat formatter = new DecimalFormat("####.##");
        return formatter.format(outNumber) + unit;
    }

    /**
     * 获取文件夹里所有文件的总大小
     *
     * @param f
     */
    public static long getDirectorySize(File f) throws IOException {
        long size = 0;
        File[] flist = f.listFiles();
        if (flist == null) {
            return f.length();
        }
        int length = flist.length;
        for (int i = 0; i < length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getDirectorySize(flist[i]);
            } else {
                size = size + flist[i].length();
            }
        }
        return size;
    }

    /**
     * 通过文件夹路径获取文件夹大小
     *
     * @param fp 文件夹路径
     */
    public static long getDirectorySize(String fp) throws IOException {
        long size = 0;
        File f = new File(fp);
        File[] flist = f.listFiles();
        if (flist == null) {
            return f.length();
        }
        int length = flist.length;
        for (int i = 0; i < length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getDirectorySize(flist[i]);
            } else {
                size = size + flist[i].length();
            }
        }
        return size;
    }

    /**
     * 从url从抽取文件名
     *
     * @param url String
     * @return /xxxx.mp4?yyy, 返回xxxx.mp4
     */
    public static String getFileNameFromUrl(String url) {
        String filename = null;
        String decodedUrl = Uri.decode(url);
        if (decodedUrl != null) {
            int queryIndex = decodedUrl.indexOf('?');
            // If there is a query string strip it, same as desktop browsers
            if (queryIndex > 0) {
                decodedUrl = decodedUrl.substring(0, queryIndex);
            }
            if (!decodedUrl.endsWith("/")) {
                int index = decodedUrl.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = decodedUrl.substring(index);
                }
            }
        }

        return filename;
    }

    /**
     * 获取文件系统block size，用于设置buffer大小
     *
     * @return
     */
    public static int getFSBlockSize() {
        if (FS_BLOCK_SIZE == 0) {
            FS_BLOCK_SIZE = new StatFs("/data").getBlockSize();

            if (FS_BLOCK_SIZE <= 0) {
                FS_BLOCK_SIZE = FILE_STREAM_BUFFER_SIZE;
            }
        }

        return FS_BLOCK_SIZE;
    }

}
