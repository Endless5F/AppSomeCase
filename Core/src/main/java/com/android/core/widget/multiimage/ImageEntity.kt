package com.android.core.widget.multiimage

/** 图片类型  */
const val STYLE_STATIC = "normal"
const val STYLE_MOTIVE = "gif"
const val STYLE_LONG = "longpic"

/**
 * 图片元素model
 */
class ImageEntity {

    constructor()

    constructor(imageUrl: String) {
        this.imageUrl = imageUrl
    }

    constructor(width: Int, height: Int, imageUrl: String, textTag: String) {
        this.width = width
        this.height = height
        this.imageUrl = imageUrl
        this.textTag = textTag
    }

    constructor(
        width: Int?,
        height: Int?,
        percent: Float?,
        imageUrl: String?,
        imgOrigin: String?,
        type: String?,
        tag: String?
    ) {
        this.width = width ?: 0
        this.height = height ?: 0
        this.imageUrl = imageUrl
        this.imgOrigin = imgOrigin
        this.type = type ?: STYLE_STATIC
        this.textTag = tag
        this.heightWidthRatio = percent ?: 1f
    }


    var width: Int = 0

    var height: Int = 0

    var imageUrl: String? = null

    /**
     * 高 / 宽
     */
    var heightWidthRatio: Float = 1f

    /**
     * 图片原始连接
     * 注：如果类型是gif的话。imgUrl 返回的是静态链接，imgOrigin 返回的是gif链接地址
     */
    var imgOrigin: String? = null

    var textTag: String? = null

    /** 图片类型  */
    var type: String = STYLE_STATIC
}