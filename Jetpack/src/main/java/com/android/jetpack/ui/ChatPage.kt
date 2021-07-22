package com.android.jetpack.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.jetpack.R
import com.android.jetpack.WeViewModel
import com.android.jetpack.data.Chat
import com.android.jetpack.ui.theme.WeTheme

@Composable
fun ChatPage(modifier: Modifier, chat: Chat, onBack: () -> Unit) {
    val viewModel: WeViewModel = viewModel()
    viewModel.read(chat)

    // 偏移可行，有动画
    Scaffold(
        modifier
            .background(WeTheme.colors.badge)
            .fillMaxSize(),
        topBar = {
            TopBar(title = chat.friend.name, onBack = onBack)
        },
        bottomBar = {
            ChatBottomBar(onBombClicked = {
                viewModel.boom(chat)
            })
        }
    ) {

    }

    // 偏移可行，无动画
//    Box(modifier = Modifier
//        .layout { measurable, contraints ->
//            val placeable = measurable.measure(contraints)
//            layout(placeable.width, placeable.height) {
//                val offset = if (viewModel.chatting) 0 else placeable.width
//                placeable.placeRelative(offset, 0)
//            }
//        }
//        .background(Color.Magenta)
//        .fillMaxSize()
//    ) {
//
//    }


    // 偏移异常
//    val percentOffset by animateFloatAsState(if (viewModel.chatting) 0f else 1f)
//    Scaffold(
//        modifier
//            .background(WeTheme.colors.badge)
//            .layout { measurable, contraints ->
//                val placeable = measurable.measure(contraints)
//                layout(placeable.width, placeable.height) {
//                    val offset = (percentOffset * placeable.width).toInt()
//                    placeable.placeRelative(offset, 0)
//                }
//            }
//            .fillMaxSize(),
//        topBar = {
//            TopBar(title = chat.friend.name, onBack = onBack)
//        }
//    ) {
//
//    }
}

@Composable
fun ChatBottomBar(onBombClicked: () -> Unit) {
    var editingText by remember { mutableStateOf("") }
//    var editingText by mutableStateOf("")
    BottomBar {
        Icon(
            painterResource(R.drawable.ic_voice),
            contentDescription = null,
            Modifier
                .align(Alignment.CenterVertically)
                .padding(4.dp)
                .size(28.dp),
            tint = WeTheme.colors.icon
        )
        BasicTextField(
            editingText, onValueChange = { editingText = it },
            Modifier
                .weight(1f)
                .padding(4.dp, 8.dp)
                .height(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(WeTheme.colors.textFieldBackground)
                .padding(start = 8.dp, top = 10.dp, end = 8.dp),
            cursorBrush = SolidColor(WeTheme.colors.textPrimary)
        )
        Text(
            "\uD83D\uDCA3",
            Modifier
                .clickable(onClick = onBombClicked)
                .padding(4.dp)
                .align(Alignment.CenterVertically),
            fontSize = 24.sp
        )
        Icon(
            painterResource(R.drawable.ic_add),
            contentDescription = null,
            Modifier
                .align(Alignment.CenterVertically)
                .padding(4.dp)
                .size(28.dp),
            tint = WeTheme.colors.icon
        )
    }
}

