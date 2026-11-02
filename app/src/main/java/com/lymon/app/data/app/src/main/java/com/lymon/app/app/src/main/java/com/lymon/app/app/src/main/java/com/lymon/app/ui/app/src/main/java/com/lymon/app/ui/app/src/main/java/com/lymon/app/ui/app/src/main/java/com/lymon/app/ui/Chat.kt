package com.lymon.app.ui

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lymon.app.LymonVM
import com.lymon.app.data.Msg
import java.io.File

class Recorder(private val ctx: Context) {
    private var rec: MediaRecorder? = null
    private var file: File? = null

    fun start() {
        val f = File(ctx.cacheDir, "rec_${System.currentTimeMillis()}.m4a"); file = f
        rec = (if (Build.VERSION.SDK_INT >= 31) MediaRecorder(ctx) else @Suppress("DEPRECATION") MediaRecorder()).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(32000)
            setAudioSamplingRate(16000)
            setOutputFile(f.absolutePath)
            prepare(); start()
        }
    }

    fun stop(): File? {
        try { rec?.stop() } catch (_: Exception) { file = null }
        rec?.release(); rec = null
        return file
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: LymonVM) {
    val peer = vm.peer ?: return
    val me = vm.session!!.username
    val ctx = LocalContext.current
    val recorder = remember { Recorder(ctx) }
    var text by remember { mutableStateOf("") }
    var recording by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) { recorder.start(); recording = true }
    }

    LaunchedEffect(vm.messages.size) {
        if (vm.messages.isNotEmpty()) listState.animateScrollToItem(vm.messages.lastIndex)
    }

    Scaffold(
        containerColor = Bg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(peer.name); Spacer(Modifier.width(4.dp)); Badge(peer.badge)
                    }
                },
                navigationIcon = {
                    IconButton({ vm.closeChat() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface1)
            )
        },
        bottomBar = {
            Row(
                Modifier.fillMaxWidth().background(Surface1).padding(8.dp).navigationBarsPadding().imePadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (recording) {
                    Text("● جارٍ التسجيل… اضغط للإرسال", Modifier.weight(1f).padding(12.dp),
                        color = MaterialTheme.colorScheme.error)
                } else {
                    TextField(
                        text, { text = it }, Modifier.weight(1f),
                        placeholder = { Text("اكتب رسالة") },
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Surface2, unfocusedContainerColor = Surface2,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                    )
                }
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        when {
                            recording -> { recording = false; recorder.stop()?.let { vm.sendVoice(it) } }
                            text.isNotBlank() -> { vm.sendText(text); text = "" }
                            else -> permission.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Accent)
                ) {
                    Icon(
                        when { recording || text.isNotBlank() -> Icons.AutoMirrored.Filled.Send; else -> Icons.Default.Mic },
                        "إرسال")
                }
            }
        }
    ) { pad ->
        LazyColumn(
            Modifier.padding(pad).fillMaxSize().padding(horizontal = 10.dp),
            state = listState, verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(vm.messages, key = { it.id }) { m -> Bubble(m, m.from == me) { vm.play(m) } }
        }
    }
}

@Composable
private fun Bubble(m: Msg, mine: Boolean, onPlay: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
        Box(
            Modifier.widthIn(max = 280.dp).clip(RoundedCornerShape(14.dp))
                .background(if (mine) MineBubble else Surface2).padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (m.type == "voice") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onPlay, Modifier.size(34.dp)) { Icon(Icons.Default.PlayArrow, "تشغيل") }
                    Text("رسالة صوتية")
                }
            } else Text(m.body)
        }
    }
}
