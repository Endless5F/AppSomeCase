package com.android.core.restful

import com.android.core.restful.retroft.RetrofitCallFactory

/**
 *
 * @author jiaochengyun
 * @version
 * @since 2021/11/15
 *
 * demo：ApiFactory.create(TestApi::class.java).getInfo()
 */
object ApiFactory {

    private const val baseUrl = ""
    private val hiRestful = HiRestful(baseUrl, RetrofitCallFactory(baseUrl))

    init {
//        hiRestful.addInterceptor()
    }

    fun <T> create(service: Class<T>): T {
        return hiRestful.create(service)
    }
}