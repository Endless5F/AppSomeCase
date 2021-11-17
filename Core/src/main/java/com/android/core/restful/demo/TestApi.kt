package com.android.core.restful.demo

import com.android.core.restful.HiCall
import com.android.core.restful.annotation.GET
import com.android.core.restful.annotation.Path

/**
 *
 * @author jiaochengyun
 * @version
 * @since 2021/11/15
 */
interface TestApi {

    @GET("/article/list/{page}/json")
    fun getArticleList(@Path("page") page: Int): HiCall<String>
}