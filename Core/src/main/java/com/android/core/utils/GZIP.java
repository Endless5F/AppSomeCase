package com.android.core.utils;

import static com.android.core.utils.StringUtil.toHexString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GZIP算法
 */
public final class GZIP {

    /**
     * DEBUG
     */
    private static final boolean DEBUG = false;

    /**
     * 1024,用于计算app大小
     */
    public static final int NUM_1024 = 1024;

    /**
     * 私有构造函数
     */
    private GZIP() {

    }

    /**
     * 压缩数据为gzip
     *
     * @param data 要压缩的数据
     *
     * @return 返回压缩过的数据
     */
    public static byte[] gZip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(data);
            gzip.finish();
            gzip.close();
            b = bos.toByteArray();
            bos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }

    /***
     * 解压GZip
     *
     * @param data 要解压的数据
     * @return 解压过的数据
     */
    public static byte[] unGZip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            GZIPInputStream gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[NUM_1024];
            int num = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, num);
            }
            b = baos.toByteArray();
            baos.flush();
            baos.close();
            gzip.close();
            bis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }

    /**
     * 判断是否是Gzip文件，gzip文件前4个字节是：1F8B0800
     *
     * @param srcfile 指定文件file，不能为空
     *
     * @return true 是Gzip文件，false不是Gzip文件
     */
    public static boolean isGzipFile(String srcfile) {
        File file = new File(srcfile);
        if (!file.exists()) {
            return false;
        }
        // 取出前4个字节进行判断
        byte[] filetype = new byte[4]; // SUPPRESS CHECKSTYLE
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            fis.read(filetype);
            if ("1F8B0800".equalsIgnoreCase(toHexString(filetype, "", true))) {
                return true;
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        } finally {
            Closeables.closeSafely(fis);
        }
        return false;
    }

}
