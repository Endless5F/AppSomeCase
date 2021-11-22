package com.android.core.restful.annotation


/**
 * @GET("/cities/{province}")
 *fun test(@Path("province") int provinceId)
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Path(val value: String)