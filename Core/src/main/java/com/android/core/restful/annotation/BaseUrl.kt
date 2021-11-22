package com.android.core.restful.annotation


/**
 * @BaseUrl("https://api.test.com/")
 *fun test(@Filed("province") int provinceId)
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BaseUrl(val value: String)