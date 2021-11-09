package com.android.core.util;

import static com.android.core.util.StringUtils.toHexString;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * zip包工具类
 */
public final class ZipUtils {

    /**
     * TAG
     */
    private static final String TAG = "ZipUtils";
    /**
     * DEBUG flag
     */
    private static final boolean DEBUG = false;

    /**
     * 私有构造
     */
    private ZipUtils() {
    }

    /**
     * zip壓縮
     *
     * @param src  源文件
     * @param dest 目標文件
     *
     */
    public static void zip(String src, String dest) {
        // 提供了一个数据项压缩成一个ZIP归档输出流
        ZipOutputStream out = null;
        try {
            if (TextUtils.isEmpty(src) || TextUtils.isEmpty(dest)) {
                return;
            }
            if (DEBUG) {
                Log.v(TAG, TAG + " zip file src:" + src + ", dest:" + dest);
            }
            // 源文件或者目录
            File outFile = new File(dest);
            // 压缩文件路径
            File fileOrDirectory = new File(src);
            out = new ZipOutputStream(new FileOutputStream(outFile));
            // 如果此文件是一个文件，否则为false。
            if (fileOrDirectory.isFile()) {
                zipFileOrDirectory(out, fileOrDirectory, "");
            } else {
                // 返回一个文件或空阵列。
                File[] entries = fileOrDirectory.listFiles();
                if (entries != null) {
                    for (File entry : entries) {
                        // 递归压缩，更新curPaths
                        zipFileOrDirectory(out, entry, "");
                    }
                }
            }
        } catch (IOException ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }
        } finally {
            // 关闭输出流
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * 将多个文件压缩到同一个zip文件中
     *
     * @param srcs 源文件
     * @param dest zip文件
     *
     * @throws IOException
     */
    public static void zip(List<File> srcs, File dest) throws IOException {
        if (dest == null || !dest.exists()) {
            return;
        }
        if (srcs == null || srcs.size() == 0) {
            return;
        }

        InputStream input = null;
        ZipOutputStream zipOut = null;
        try {
            byte[] buffer = new byte[4096]; // SUPPRESS CHECKSTYLE
            zipOut = new ZipOutputStream(new FileOutputStream(dest));
            zipOut.setComment(dest.getName());

            for (File srcFile : srcs) {
                // android8.0/system/build.prop 不可读
                if (srcFile.canRead()) {
                    input = new FileInputStream(srcFile);
                    zipOut.putNextEntry(new ZipEntry(srcFile.getName()));

                    int readSize = 0;
                    while ((readSize = input.read(buffer)) != -1) {
                        zipOut.write(buffer, 0, readSize);
                    }
                    input.close();
                }
            }
            zipOut.flush();
            zipOut.close();
        } catch (FileNotFoundException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        } finally {
            Closeables.closeSafely(input);
            Closeables.closeSafely(zipOut);
        }
    }

    /**
     * zip文件或目錄
     *
     * @param out             out流
     * @param fileOrDirectory 文件目錄
     * @param curPath         文件路徑
     *
     * @throws IOException io異常
     */
    private static void zipFileOrDirectory(ZipOutputStream out,
                                           File fileOrDirectory, String curPath) throws IOException {
        // 从文件中读取字节的输入流
        FileInputStream in = null;
        try {
            if (DEBUG) {
                Log.v(TAG, TAG + " zipFileOrDirectory curPath:" + curPath);
            }
            // 如果此文件是一个目录，否则返回false。
            if (!fileOrDirectory.isDirectory()) {
                // 压缩文件
                byte[] buffer = new byte[4096]; // SUPPRESS CHECKSTYLE
                int bytesRead; // SUPPRESS CHECKSTYLE
                in = new FileInputStream(fileOrDirectory);
                // 实例代表一个条目内的ZIP归档
                ZipEntry entry = new ZipEntry(curPath + fileOrDirectory.getName());
                // 条目的信息写入底层流
                out.putNextEntry(entry);
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.closeEntry();
            } else {
                // 压缩目录
                File[] entries = fileOrDirectory.listFiles();
                if (entries != null) {
                    for (int i = 0; i < entries.length; i++) {
                        // 递归压缩，更新curPaths
                        zipFileOrDirectory(out, entries[i], curPath + fileOrDirectory.getName() + "/");
                    }
                }
            }
        } catch (IOException ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * 判断是否是zip文件，zip文件前4个字节是：504B0304
     *
     * @param srcfile 指定文件file，不能为空
     *
     * @return true 是zip文件，false不是zip文件
     */
    public static boolean isZipFile(File srcfile) {
        if (!srcfile.exists()) {
            return false;
        }
        // 取出前4个字节进行判断
        byte[] filetype = new byte[4]; // SUPPRESS CHECKSTYLE
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(srcfile);
            fis.read(filetype);
            if ("504B0304".equalsIgnoreCase(toHexString(filetype, "", true))) {
                return true;
            }
            return false;
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            return false;
        } finally {
            Closeables.closeSafely(fis);
        }
    }

    /**
     * unzip file.
     *
     * @param srcFileName 源file绝对路径.
     * @param savePath    目标file父目录路径
     *
     * @return boolean 是否解压成功
     */
    public static boolean unzipFile(String srcFileName, String savePath) {
        long startTime = System.currentTimeMillis();
        if (srcFileName == null) {
            return false;
        }
        if (savePath == null) {
            savePath = new File(srcFileName).getParent();
        }

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            ZipFile zipFile = new ZipFile(srcFileName);
            Enumeration<? extends ZipEntry> enu = zipFile.entries();

            while (enu.hasMoreElements()) {
                ZipEntry zipEntry = enu.nextElement();
                if (zipEntry.getName().contains("../")) {
                    continue;
                }
                File saveFile = new File(savePath + "/" + zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    if (!saveFile.exists()) {
                        saveFile.mkdirs();
                    }
                    continue;
                }
                if (!saveFile.exists()) {
                    FileUtils.createFileSafely(saveFile);
                }
                FileOutputStream fos = null;
                try {
                    bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                    fos = new FileOutputStream(saveFile);
                    bos = new BufferedOutputStream(fos, FileUtils.getFSBlockSize());
                    int count = -1;
                    byte[] buf = new byte[FileUtils.getFSBlockSize()];
                    while ((count = bis.read(buf, 0, FileUtils.getFSBlockSize())) != -1) {
                        bos.write(buf, 0, count);
                    }

                    bos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    Closeables.closeSafely(bos);
                    Closeables.closeSafely(bis);
                    Closeables.closeSafely(fos);
                }

            }
            zipFile.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            long endTime = System.currentTimeMillis();
            if (DEBUG) {
                Log.i(TAG, "unZip:" + srcFileName + "cost:" + (endTime - startTime) + "ms");
            }
        }
        return true;
    }
}
