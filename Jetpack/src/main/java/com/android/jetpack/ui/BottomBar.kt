package com.android.jetpack.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.jetpack.R
import com.android.jetpack.WeViewModel
import com.android.jetpack.ui.theme.WeTheme

@Composable
fun BottomBar(selected: Int, onClick: ((Int) -> Unit)? = null) {
    Row(
        Modifier.background(WeTheme.colors.bottomBar)
    ) {
        TabItem(
            id = if (selected == 0) R.drawable.ic_chat_filled else R.drawable.ic_chat_outlined,
            tabName = "微信",
            tint = if (selected == 0) WeTheme.colors.iconCurrent else WeTheme.colors.icon,
            Modifier
                .weight(1f)
                .clickable {
                    onClick?.invoke(0)
                }
        )
        TabItem(
            id = if (selected == 1) R.drawable.ic_contacts_filled else R.drawable.ic_contacts_outlined,
            tabName = "通讯录",
            tint = if (selected == 1) WeTheme.colors.iconCurrent else WeTheme.colors.icon,
            Modifier
                .weight(1f)
                .clickable {
                    onClick?.invoke(1)
                }
        )
        TabItem(
            id = if (selected == 2) R.drawable.ic_discover_filled else R.drawable.ic_discover_outlined,
            tabName = "发现",
            tint = if (selected == 2) WeTheme.colors.iconCurrent else WeTheme.colors.icon,
            Modifier
                .weight(1f)
                .clickable {
                    onClick?.invoke(2)
                }
        )
        TabItem(
            id = if (selected == 3) R.drawable.ic_me_filled else R.drawable.ic_me_outlined,
            tabName = "我",
            tint = if (selected == 3) WeTheme.colors.iconCurrent else WeTheme.colors.icon,
            Modifier
                .weight(1f)
                .clickable {
                    onClick?.invoke(3)
                }
        )
    }
}

@Composable
private fun TabItem(@DrawableRes id: Int, tabName: String, tint: Color, modifier: Modifier) {
    Column(
        modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//            Image(painter = , contentDescription = )
        Icon(
            painter = painterResource(id = id),
            contentDescription = tabName,
            Modifier
                .padding(8.dp)
                .size(24.dp), tint = tint
        )
        Text(text = tabName, fontSize = 11.sp, color = tint)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLight() {
    BottomBar(0)
}

//    @Preview(showBackground = true)
//    @Composable
//    fun PreviewDark() {
//        WeTheme(WeTheme.Theme.Dark) {
//            BottomBar(0)
//        }
//    }
//
//    @Preview(showBackground = true)
//    @Composable
//    fun PreviewNewYear() {
//        WeTheme(WeTheme.Theme.NewYear) {
//            BottomBar(0)
//        }
//    }