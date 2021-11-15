package com.android.core.restful.retroft

import com.android.core.restful.HiConvert
import com.android.core.restful.HiResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Type

/**
 *
 * @author jiaochengyun
 * @version
 * @since 2021/11/15
 */
class GsonConvert: HiConvert {

    private val gson: Gson = Gson()

    override fun <T> convert(rawData: String, dataType: Type): HiResponse<T> {
        val response = HiResponse<T>()
        try {
            val jsonObj = JSONObject(rawData)
            response.code = jsonObj.optInt("code")
            response.msg = jsonObj.optString("msg")
            val data = jsonObj.optString("data")

            if (response.code == HiResponse.SUCCESS) {
                response.data = gson.fromJson(data, dataType)
            } else {
                response.errorData = gson.fromJson<MutableMap<String, String>>(data, object:
                        TypeToken<MutableMap<String, String>>(){}.type)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            response.code = -1
            response.msg = e.message
        }

        return response
    }
}