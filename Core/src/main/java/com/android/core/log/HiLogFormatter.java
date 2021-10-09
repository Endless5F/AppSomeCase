package com.android.core.log;

public interface HiLogFormatter<T> {

    String format(T data);
}