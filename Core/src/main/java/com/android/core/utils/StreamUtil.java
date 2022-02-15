package com.android.core.utils;

import android.text.TextUtils;
import android.util.Xml;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 公共类：用于封装常用的 I/O 操作。
 */
public class StreamUtil {

    /**
     * TAG
     */
    private static final String TAG = StreamUtil.class.getSimpleName();

    /**
     * 全局debug开关
     */
    private static final boolean DEBUG = false; /*LibUtilConfig.GLOBAL_DEBUG;*/

    /**
     * File buffer stream size
     */
    public static final int FILE_STREAM_BUFFER_SIZE = 8192;

    /**
     * 将byte数组写入文件
     *
     * @param data byte数组
     * @param file 文件
     *
     * @return 是否写入成功的标志
     */
    public static boolean bytesToFile(byte[] data, File file) {
        if (data == null || file == null) {
            return false;
        }
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(data);
            os.flush();
            return true;
        } catch (IOException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        } finally {
            Closeables.closeSafely(os);
        }
        return false;
    }

    /**
     * stream to bytes
     *
     * @param is input stream
     *
     * @return bytes
     */
    public static byte[] streamToBytes(InputStream is) {
        if (null == is) {
            return null;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] ret = null;
        try {
            byte[] buffer = new byte[FILE_STREAM_BUFFER_SIZE];
            int n = 0;
            while (-1 != (n = is.read(buffer))) {
                output.write(buffer, 0, n);
            }
            ret = output.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            Closeables.closeSafely(is);
            Closeables.closeSafely(output);
        }
        return ret;
    }

    /**
     * 转换Stream成string
     *
     * @param is Stream源
     *
     * @return 目标String
     */
    public static String streamToString(InputStream is) {
        return streamToString(is, Xml.Encoding.UTF_8.toString());
    }

    /**
     * 按照特定的编码格式转换Stream成string
     *
     * @param is  Stream源
     * @param enc 编码格式
     *
     * @return 目标String
     */
    public static String streamToString(InputStream is, String enc) {
        if (null == is) {
            return null;
        }

        StringBuilder buffer = new StringBuilder();
        String line = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is, enc), FILE_STREAM_BUFFER_SIZE);
            while (null != (line = reader.readLine())) {
                buffer.append(line);
            }
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        } finally {
            Closeables.closeSafely(is);
            Closeables.closeSafely(reader);
        }
        return buffer.toString();
    }

    /**
     * 将输入流中的数据保存到文件，已废弃，请使用{@link FileUtil#saveToFileWithReturn(InputStream inputStream, File file, boolean append)}
     *
     * @param is   输入流
     * @param file 目标文件
     *
     * @return true:保存成功，false:保存失败
     */
    @Deprecated
    public static boolean streamToFile(InputStream is, File file) {
        boolean bRet = false;
        if (null == is) {
            return false;
        }

        if (null == file) {
            Closeables.closeSafely(is);
            return false;
        }

        // 下载后文件存在临时目录中
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (file.exists()) {
            file.delete();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[FILE_STREAM_BUFFER_SIZE];
            int length = -1;
            while ((length = is.read(buffer)) != -1) {
                fos.write(buffer, 0, length);
            }
            fos.flush();
            bRet = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Closeables.closeSafely(fos);
            Closeables.closeSafely(is);
        }
        return bRet;
    }

    /**
     * 将输入流in中的数据保存到压缩文件zip的file文件
     *
     * @param in   输入流，本函数内勿close，哪里创建哪里close
     * @param out  压缩到的zip文件输出流，，本函数内勿close，哪里创建哪里close
     * @param file 压缩到的zip文件中的file文件
     *
     * @return true:成功，false:失败
     *
     * @author yanbo02
     */
    public static boolean streamToZipFile(InputStream in, ZipOutputStream out, String file) {
        boolean result = false;

        if (null == in || null == out || TextUtils.isEmpty(file)) {
            return false;
        }

        try {
            // 压缩文件
            byte[] buffer = new byte[4096]; // SUPPRESS CHECKSTYLE
            int bytesRead; // SUPPRESS CHECKSTYLE
            // 实例代表一个条目内的ZIP归档
            ZipEntry entry = new ZipEntry(file);
            // 条目的信息写入底层流
            out.putNextEntry(entry);
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.closeEntry();

            result = true;
        } catch (IOException ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 从输入流中获得字符串.
     *
     * @param inputStream {@link InputStream}
     *
     * @return 字符串
     */
    public static String getStringFromInput(InputStream inputStream) {
        String str = FileUtil.readInputStream(inputStream);
        if (str.startsWith("\ufeff")) {
            str = str.substring(1);
        }
        return str;
    }

}
