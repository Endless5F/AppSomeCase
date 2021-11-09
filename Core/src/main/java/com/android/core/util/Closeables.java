package com.android.core.util;

import android.database.Cursor;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;

public final class Closeables {
    private static final String TAG = "Closeables";

    private Closeables() {
    }

    public static void closeSafely(@Nullable Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }
    }

    public static void close(@Nullable Closeable closeable, boolean swallowIOException) throws IOException {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException var3) {
                if (!swallowIOException) {
                    throw var3;
                }

                Log.d("Closeables", "IOException thrown while closing Closeable.", var3);
            }

        }
    }

    public static void closeSafely(Cursor cursor) {
        try {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }
}
