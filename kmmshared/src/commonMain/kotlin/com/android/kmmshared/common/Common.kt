package com.android.kmmshared.common

/**
 * 默认每页加载 20 条
 */
const val DEFAULT_LOAD_PAGE_SIZE = 20

/**
 * 错误信息
 */
data class ErrorInfo(val code: Int = ERR_CODE_OK, val msg: String? = null)

/**
 * 通用回调
 */
typealias CommonCallback = () -> Unit

/**
 * 通用状态码：成功
 */
const val COMMON_RESULT_SUCCEED = 1
/**
 * 通用状态码：失败
 */
const val COMMON_RESULT_FAILED = -1
/**
 * 通用状态码：取消
 */
const val COMMON_RESULT_CANCEL = 0