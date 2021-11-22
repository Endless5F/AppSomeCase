package com.android.core.restful.annotation

/**
 * @BaseUrl("https://api.mock.com/")
 *fun test(@Filed("province") int provinceId)
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Filed(val value: String)