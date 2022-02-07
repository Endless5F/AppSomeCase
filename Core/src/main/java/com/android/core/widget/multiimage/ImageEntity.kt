package com.android.core.widget.multiimage

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

    var width: Int = 0

    var height: Int = 0

    var imageUrl: String? = null

    var textTag: String? = null
}