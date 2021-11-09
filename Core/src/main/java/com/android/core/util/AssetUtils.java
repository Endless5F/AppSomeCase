package com.android.core.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * asset工具类
 */
public final class AssetUtils {

    /**
     * 全局debug开关
     */
    private static final boolean DEBUG = false;

    /**
     * DEBUG TAG
     */
    private static final String TAG = "AssetUtils";

    /**
     * private constructor
     */
    private AssetUtils() {

    }

    /**
     * 判断assets目录中是否存在某个文件
     *
     * @param context  context
     * @param filePath filePath
     *
     * @return exists
     */
    public static boolean exists(Context context, String filePath) {
        if (context == null || TextUtils.isEmpty(filePath)) {
            return false;
        }

        boolean exists = false;
        InputStream is = null;
        try {
            is = context.getAssets().open(filePath, AssetManager.ACCESS_UNKNOWN);
            exists = true;

        } catch (IOException ex) {
            exists = false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return exists;
    }

    /**
     * 从asset目标文件夹中复制所有文件至指定目录
     *
     * @param amgr      AssetManager
     * @param srcFolder asset文件夹目录
     * @param dstFolder 目标文件加目录
     *
     * @return true 复制成功
     */
    public static boolean extractFolderFromAsset(AssetManager amgr, String srcFolder, String dstFolder) {
        boolean bRet = false;
        try {
            String[] subFolders = amgr.list(srcFolder);
            String[] grandSubFolders;
            String subFolder;
            if (subFolders != null) {
                for (String folder : subFolders) {
                    // 经过7zip压缩以后,会获取到一个folder为空(""),导致dstFolder被创建成一个文件夹
                    if (TextUtils.isEmpty(folder)) {
                        continue;
                    }
                    subFolder = srcFolder + File.separator + folder;
                    grandSubFolders = amgr.list(subFolder);

                    if (DEBUG) {
                        Log.d(TAG, "extractFolderFromAsset srcFolder: " + srcFolder + " subFolder: " + subFolder + " "
                                + "dstFolder: " + dstFolder);
                    }

                    if (grandSubFolders == null || grandSubFolders.length == 0) {
                        bRet = extractFileFromAsset(amgr, subFolder, dstFolder + File.separator + folder);
                    } else {
                        bRet = extractFolderFromAsset(amgr, subFolder, dstFolder + File.separator + folder);
                    }

                    if (!bRet) {
                        break;
                    }
                }
            }
        } catch (IOException ioe) {
            if (DEBUG) {
                ioe.printStackTrace();
            }
        }
        return bRet;
    }

    /**
     * 从asset文件夹中复制文件到目标目录
     *
     * @param amgr AssetManager
     * @param src  asset文件目录
     * @param dst  目标目录
     *
     * @return true:复制成功； false:复制失败
     */
    public static boolean extractFileFromAsset(AssetManager amgr, String src, String dst) {
        boolean bRet = false;
        try {
            bRet = StreamUtils.streamToFile(amgr.open(src, Context.MODE_PRIVATE), new File(dst));
            if (!bRet) {
                new File(dst).delete();
            }
        } catch (IOException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        return bRet;
    }

    /**
     * 载入assets中文件
     *
     * @param context context
     * @param path    文件路径
     *
     * @return 文件中字符串
     */
    public static String loadFile(Context context, String path) {
        InputStream is = null;
        String configStr = null;
        try {
            AssetManager am = context.getAssets();
            is = am.open(path);
            if (null == is) {
                return null;
            }

            // 获取预置文件中的字符串
            configStr = StreamUtils.streamToString(is);
        } catch (IOException e) {
            if (DEBUG) {
                Log.w(TAG, "loadPresetDatas", e);
            }
        } finally {
            Closeables.closeSafely(is);
        }
        return configStr;
    }

    /**
     * 从asset目录释放并解压zip文件
     *
     * @param assetPath assetPath
     * @param savePath  savePath
     * @param context   app context
     *
     * @return true 解压成功
     */
    public static boolean unzipFileFromAsset(String assetPath, String savePath, Context context) {
        if (TextUtils.isEmpty(assetPath) || TextUtils.isEmpty(savePath)) {
            return false;
        }

        File saveFile = new File(savePath);
        if (!saveFile.exists()) {
            saveFile.mkdirs();
        }

        InputStream inputStream = null;
        ZipInputStream zipInputStream = null;
        BufferedOutputStream bos = null;
        try {
            inputStream = context.getApplicationContext().getAssets().open(assetPath);
            zipInputStream = new ZipInputStream(inputStream);
            ZipEntry nextEntry;
            byte[] buffer = new byte[FileUtils.BUFFER_SIZE];
            int count;

            while ((nextEntry = zipInputStream.getNextEntry()) != null) {
                if (nextEntry.getName().contains("../")) {
                    continue;
                }
                saveFile = new File(savePath + File.separator + nextEntry.getName());
                if (nextEntry.isDirectory()) {
                    if (!saveFile.exists()) {
                        saveFile.mkdir();
                    }
                    continue;
                }

                if (!saveFile.exists()) {
                    FileUtils.createFileSafely(saveFile);
                    try {
                        bos = new BufferedOutputStream(new FileOutputStream(saveFile), FileUtils.UNZIP_BUFFER);
                        while ((count = zipInputStream.read(buffer)) != -1) {
                            bos.write(buffer, 0, count);
                        }
                    } finally {
                        Closeables.closeSafely(bos);
                    }
                }
            }
        } catch (IOException ioe) {
            if (DEBUG) {
                ioe.printStackTrace();
            }

            return false;
        } finally {
            Closeables.closeSafely(inputStream);
            Closeables.closeSafely(zipInputStream);
        }

        return true;
    }

    /**
     * 读取Asset文件数据
     *
     * @param context   context
     * @param assetPath assetPath
     *
     * @return string data
     */
    public static String readAsset(Context context, String assetPath) {
        if (context == null || TextUtils.isEmpty(assetPath)) {
            return null;
        }
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(assetPath);
            return FileUtils.readInputStream(inputStream);
        } catch (IOException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        } finally {
            Closeables.closeSafely(inputStream);
        }

        return null;
    }
}
