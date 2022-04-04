package com.android.app.planettab

import com.android.app.R

fun initData(): List<PlanetItemData> {
    val model = PlanetListModel()
    model.planetList.apply {
        add(PlanetModel("1", "国漫", ""))
        add(PlanetModel("2", "潮玩", ""))
        add(PlanetModel("3", "冲浪", ""))
        add(PlanetModel("4", "小说", ""))
        add(PlanetModel("5", "漫画", ""))
        add(PlanetModel("6", "游戏", ""))
    }
    return model.convertViewData()
}

class PlanetModel(val id: String?, val name: String, val topicId: String?)

class PlanetListModel {
    val planetList = arrayListOf<PlanetModel>()
}

fun PlanetListModel?.convertViewData(): List<PlanetItemData> {
    var selectIndex = 0
    val id = "1"
    val dataArray = arrayListOf<PlanetItemData>()
    this?.planetList?.forEachIndexed { index, model ->
        dataArray.add(PlanetItemData(
                model.id,
                model.name,
                model.topicId,
                getImageFromId(model.id)
        ).apply {
            // 初始化index
            initIndex(index)
            if (id == model.id) {
                // 设置选中态
                isSelected = true
                selectIndex = index
            }
        })
    }
    val planetCount = dataArray.size
    if (planetCount > 0) {
        // 初始化根据选中星球调整数据源的真实顺序和显示角度
        dataArray.forEach {
            // 根据选中态重置currentIndex
            it.currentIndex = (it.currentIndex - selectIndex + planetCount) % planetCount
        }
    }
    return dataArray
}

/** 获取对应内置图片 */
fun getImageFromId(id: String?): Int {
    return when (id) {
        "1" -> R.drawable.icon_guo_man_star // 国漫
        "2" -> R.drawable.icon_tide_play_star // 潮玩
        "3" -> R.drawable.icon_surfer_star // 冲浪
        "4" -> R.drawable.icon_novel_star // 小说
        "5" -> R.drawable.icon_comic_star // 漫画
        "6" -> R.drawable.icon_game_star // 游戏
        else -> R.drawable.planet_default_star // 默认
    }
}
