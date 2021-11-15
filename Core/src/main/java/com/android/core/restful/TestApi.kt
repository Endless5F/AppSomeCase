package com.android.core.restful

import com.google.gson.JsonObject
import retrofit2.http.GET

/**
 *
 * @author jiaochengyun
 * @version
 * @since 2021/11/15
 */
interface TestApi {
    @GET("info")
    fun getInfo(): HiCall<JsonObject>
}