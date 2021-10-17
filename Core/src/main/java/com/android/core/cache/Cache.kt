package com.android.core.cache

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *
 * @author jiaochengyun
 * @version
 * @since 2021/10/17
 */

@Entity(tableName = "Cache")
class Cache {

    @PrimaryKey(autoGenerate = false)
    @NonNull
    var key: String = ""

    var data: ByteArray? = null
}