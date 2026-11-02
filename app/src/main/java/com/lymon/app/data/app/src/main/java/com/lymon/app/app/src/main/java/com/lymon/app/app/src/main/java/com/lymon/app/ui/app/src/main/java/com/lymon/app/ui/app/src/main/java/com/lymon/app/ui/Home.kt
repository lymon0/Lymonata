package com.lymon.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lymon.app.LymonVM
import com.lymon.app.data.UserItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: LymonVM) {
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf("chats") }
    val s = vm.session!!

    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Surface1) {
                Column(Modifier.padding(24.dp)) {
                    Text("Lymon", style = MaterialTheme.typography.headlineMedium, color = Accent)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(s.username, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        if (s.admin) Badge("gold")
                    }
                }
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("المحادثات") }, selected = tab == "chats",
                    icon = { Icon(Icons.Default.Chat, null) },
                    onClick = { tab = "chats"; scope.launch { drawer.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp))
                // يظهر فقط إذا أكّد الخادم أن الجلسة جلسة مشرف
                if (s.admin) NavigationDrawerItem(
                    label = { Text("لوحة الإشراف") }, selected = tab == "admin",
                    icon = { Icon(Icons.Default.AdminPanelSettings, null, tint = Gold) },
                    onClick = { tab = "admin"; vm.loadUsers(); scope.launch { drawer.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp))
                NavigationDrawerItem(
                    label = { Text("تسجيل الخروج") }, selected = false,
                    icon = { Icon(Icons.Default.Logout, null) },
                    onClick = { vm.logout() },
                    modifier = Modifier.padding(horizontal = 12.dp))
            }
        }
    ) {
        Scaffold(
            containerColor = Bg,
            topBar = {
                TopAppBar(
                    title = { Text(if (tab == "admin") "لوحة الإشراف" else "Lymon", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton({ scope.launch { drawer.open() } }) { Icon(Icons.Default.Menu, "القائمة") }
                    },
                    actions = { IconButton({ vm.loadUsers() }) { Icon(Icons.Default.Refresh, "تحديث") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface1)
                )
            }
        ) { pad ->
            Box(Modifier.padding(pad).fillMaxSize()) {
                if (tab == "admin" && s.admin) AdminList(vm) else ChatList(vm)
            }
        }
    }
}

@Composable
private fun Avatar(name: String) {
    Box(
        Modifier.size(46.dp).clip(CircleShape).background(Surface2),
        contentAlignment = Alignment.Center
    ) { Text(name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Accent) }
}

@Composable
private fun ChatList(vm: LymonVM) {
    val list = vm.users.filter { !it.banned }
    if (list.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("لا يوجد مستخدمون بعد", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn {
        items(list, key = { it.name }) { u ->
            Row(
                Modifier.fillMaxWidth().clickable { vm.openChat(u) }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(u.name)
                Spacer(Modifier.width(14.dp))
                Text(u.name, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(4.dp))
                Badge(u.badge)
            }
            HorizontalDivider(color = Surface2)
        }
    }
}

@Composable
private fun AdminList(vm: LymonVM) {
    LazyColumn {
        items(vm.users.filter { it.badge != "gold" }, key = { it.name }) { u -> AdminRow(vm, u) }
    }
}

@Composable
private fun AdminRow(vm: LymonVM, u: UserItem) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(u.name)
            Spacer(Modifier.width(12.dp))
            Text(u.name, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(4.dp)); Badge(u.badge)
            if (u.banned) {
                Spacer(Modifier.width(8.dp))
                Text("محظور", color = MaterialTheme.colorScheme.error)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (u.banned) OutlinedButton({ vm.admin("unban", u.name) }) { Text("فك الحظر") }
            else OutlinedButton({ vm.admin("ban", u.name) }) { Text("حظر") }
            if (u.badge == "blue") OutlinedButton({ vm.admin("unverify", u.name) }) { Text("إلغاء التوثيق") }
            else OutlinedButton({ vm.admin("verify", u.name) }) { Text("توثيق ✓") }
        }
    }
    HorizontalDivider(color = Surface2)
}
