package com.android.kmmshared.common

/**
 * 请求成功，没有错误
 */
const val ERR_CODE_OK = 0

/**
 * 未知错误
 */
const val ERR_CODE_UNKNOWN = -1

/**
 * 网络错误，没有联网，或域名解析失败，IP 不通等
 */
const val ERR_CODE_CONNECT = -100

/**
 * 参数错误，如：URL 不合法，参数传值不合法等
 */
const val ERR_CODE_PARAMS = -200

/**
 * 由 Server 明确返回的请求错误，如：HTTP 400，500
 */
const val ERR_CODE_SERVER = -300

/**
 * 请求超时，Server 没有响应，Client 主动断开
 */
const val ERR_CODE_TIMEOUT = -400

/**
 * 业务逻辑错误，Server 响应 200 状态码，但内容非法，如：JSON 中 Code/Status 非法
 */
const val ERR_CODE_BUSINESS_LOGIC = -500

/**
 * 数据处理错误，Server 响应 200 状态码，JSON 也合法，但本地处理时产生异常
 */
const val ERR_CODE_DATA = -600

/**
 * 请求被取消
 */
const val ERR_CODE_CANCELED = -700

const val ERR_MSG_UNKNOWN = "未知错误"
const val ERR_MSG_NETWORK = "网络错误"
const val ERR_MSG_PARAMS = "参数错误"
const val ERR_MSG_SERVER = "服务器错误"
const val ERR_MSG_TIMEOUT = "请求超时"
const val ERR_MSG_BUSINESS_LOGIC = "业务逻辑错误"
const val ERR_MSG_DATA = "数据处理错误"
const val ERR_MSG_CANCELED = "请求被取消"
