package com.android.jetpack.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.core.ext.startCoroutine
import com.android.jetpack.WeViewModel
import com.android.jetpack.ext.percentOffsetX
import com.android.jetpack.ui.theme.WeTheme
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalPagerApi::class)
fun Home() {
    val viewModel: WeViewModel = viewModel()
    Box(
        Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            val pagerState = rememberPagerState(
                pageCount = 4, // 总页数
                initialOffscreenLimit = 2, // 预加载的个数
                infiniteLoop = false, // 无限循环
                initialPage = 0, // 初始页面
            )

            HorizontalPager(state = pagerState, Modifier.weight(1f)) {
                when (it) {
                    0 -> ChatList(viewModel.chats)
                    1 -> Column(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .align(Alignment.Center)
                            .background(WeTheme.colors.background)) {
                        Text(text = "通讯录")
                    }
                    2 -> Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .align(Alignment.Center)
                            .background(WeTheme.colors.background)) {
                        Text(text = "发现")
                    }
                    3 -> Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .align(Alignment.Center)
                            .background(WeTheme.colors.background)) {
                        Text(text = "我")
                    }
                }
            }

            val composableScope = rememberCoroutineScope()

            BottomBar(pagerState.currentPage) {
//                startCoroutine {
//                    pagerState.animateScrollToPage(it)
//                }
                composableScope.launch {
                    pagerState.animateScrollToPage(it)
                }
            }
        }

        val chat = viewModel.currentChat
        val percentOffset by animateFloatAsState(if (viewModel.chatting) 0f else 1f)
        // 控制显示 和 offset的变化，需要通过@Composable 参数来控制
        if (chat != null) {
            ChatPage(Modifier.percentOffsetX(percentOffset), chat, onBack = {
                viewModel.endChat()
            })
        }
    }
}