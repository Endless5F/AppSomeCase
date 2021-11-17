package com.android.core.restful.retroft

import com.android.core.restful.*
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*
import java.lang.IllegalStateException

/**
 *
 * @author jiaochengyun
 * @version
 * @since 2021/11/15
 */
class RetrofitCallFactory(baseUrl: String) : HiCall.Factory {

    private val covert: HiConvert
    private val apiService: ApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .build()

        covert = GsonConvert()
        apiService = retrofit.create(ApiService::class.java)
    }

    override fun newCall(request: HiRequest): HiCall<Any> {
        return RetrofitCall(request)
    }

    internal inner class RetrofitCall<T>(private val request: HiRequest) : HiCall<T> {
        override fun execute(): HiResponse<T> {
            val realCall = createRealCall(request)
            val response = realCall.execute()
            return parseResponse(response)
        }

        override fun enqueue(callback: HiCallback<T>) {
            val realCall = createRealCall(request)
            realCall.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    callback.onSuccess(parseResponse(response))
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    callback.onFailed(t)
                }
            })
        }

        private fun parseResponse(response: Response<ResponseBody>): HiResponse<T> {
            var rawData: String? = null
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    rawData = body.string()
                }
            } else {
                val body = response.errorBody()
                if (body != null) {
                    rawData = body.string()
                }
            }
            return covert.convert(rawData!!, request.returnType!!)
        }

        private fun createRealCall(request: HiRequest): Call<ResponseBody> {
            when (request.httpMethod) {
                HiRequest.METHOD.GET -> {
                    return apiService.get(request.headers, request.endPointUrl(), request.parameters)
                }
                HiRequest.METHOD.POST -> {
                    val params = request.parameters
                    val builder = FormBody.Builder()
                    val requestBody: RequestBody?
                    val jsonObj = JSONObject()
                    for ((key, value) in params!!) {
                        if (request.formPost) {
                            builder.add(key, value)
                        } else {
                            jsonObj.put(key, value)
                        }
                    }
                    requestBody = if (request.formPost) {
                        builder.build()
                    } else {
                        RequestBody.create(MediaType.parse("application/json;utf-8"), jsonObj.toString())
                    }
                    return apiService.post(request.headers, request.endPointUrl(), requestBody)
                }
                else -> {
                    throw  IllegalStateException("仅支持Get 和 Post 请求")
                }
            }
        }
    }

    interface ApiService {
        @GET
        fun get(
            @HeaderMap headers: MutableMap<String, String>?, @Url url: String,
            @QueryMap(encoded = true) params: MutableMap<String, String>?): Call<ResponseBody>

        @POST
        fun post(
            @HeaderMap headers: MutableMap<String, String>?, @Url url: String,
            @Body body: RequestBody?
        ): Call<ResponseBody>
    }
}
