package com.android.jetpack.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.jetpack.R
import com.android.jetpack.WeViewModel
import com.android.jetpack.data.Chat
import com.android.jetpack.data.Msg
import com.android.jetpack.data.User
import com.android.jetpack.ext.unread
import com.android.jetpack.ui.theme.WeTheme

@Composable
fun ChatList(chats: List<Chat>) {
    Column {
        TopBar(title = "仍新")
        LazyColumn(
            Modifier
                .fillMaxSize()
                .background(WeTheme.colors.background)
        ) {
            itemsIndexed(chats) { index, chat ->
                ChatListItem(chat)
                if (index < chats.size - 1) {
                    Divider(
                        startIndent = 60.dp,
                        color = WeTheme.colors.chatListDivider,
                        thickness = 0.8f.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(chat: Chat) {
    val viewModel: WeViewModel = viewModel()
    Row(
        Modifier
            .background(WeTheme.colors.listItem)
            .padding(8.dp, 8.dp, 8.dp)
            .clickable {
                viewModel.startChat(chat = chat)
            }
    ) {

        Image(
            painter = painterResource(id = chat.friend.avatar),
            contentDescription = chat.friend.name,
            Modifier
                .size(48.dp)
                .padding(8.dp)
                // 绘制角标要在修剪之前
                .unread(!chat.msgs.last().read)
                .clip(RoundedCornerShape(4.dp))
        )
        Column(
            Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(text = chat.friend.name, fontSize = 14.sp, color = WeTheme.colors.textPrimary)
            Text(
                text = chat.msgs.last().text,
                fontSize = 11.sp,
                color = WeTheme.colors.textSecondary
            )
        }
        Text(
            text = "14:20",
            Modifier.padding(8.dp, 8.dp, 12.dp, 8.dp),
            fontSize = 11.sp,
            color = WeTheme.colors.textSecondary
        )
    }
}



@Preview
@Composable
private fun PreviewChatList() {
    val chats =
        listOf(
            Chat(
                friend = User("gaolaoshi", "高老师", R.drawable.avatar_gaolaoshi),
                mutableListOf(
                    Msg(User("gaolaoshi", "高老师", R.drawable.avatar_gaolaoshi), "锄禾日当午"),
                    Msg(User.Me, "汗滴禾下土"),
                    Msg(User("gaolaoshi", "高老师", R.drawable.avatar_gaolaoshi), "谁知盘中餐"),
                    Msg(User.Me, "粒粒皆辛苦"),
                    Msg(
                        User("gaolaoshi", "高老师", R.drawable.avatar_gaolaoshi),
                        "唧唧复唧唧，木兰当户织。不闻机杼声，惟闻女叹息。"
                    ),
                    Msg(User.Me, "双兔傍地走，安能辨我是雄雌？"),
                    Msg(User("gaolaoshi", "高老师", R.drawable.avatar_gaolaoshi), "床前明月光，疑是地上霜。"),
                    Msg(User.Me, "吃饭吧？"),
                )
            ),
            Chat(
                friend = User("diuwuxian", "丢物线", R.drawable.avatar_diuwuxian),
                mutableListOf(
                    Msg(User("diuwuxian", "丢物线", R.drawable.avatar_diuwuxian), "哈哈哈"),
                    Msg(User.Me, "你笑个屁呀"),
                )
            ),
        )

    ChatList(chats)
}