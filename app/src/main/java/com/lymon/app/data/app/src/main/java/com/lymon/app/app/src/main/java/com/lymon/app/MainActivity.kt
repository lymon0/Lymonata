package com.lymon.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lymon.app.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LymonTheme {
                val vm: LymonVM = viewModel()
                val snack = remember { SnackbarHostState() }
                LaunchedEffect(vm.error, vm.session) {
                    // أخطاء شاشة الدخول تُعرض داخلها، والباقي في Snackbar
                    if (vm.session != null) vm.error?.let { snack.showSnackbar(it); vm.clearError() }
                }
                Box(Modifier.fillMaxSize()) {
                    when {
                        !vm.ready -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                        vm.session == null -> AuthScreen(vm)
                        vm.peer != null -> { BackHandler { vm.closeChat() }; ChatScreen(vm) }
                        else -> HomeScreen(vm)
                    }
                    SnackbarHost(snack, Modifier.align(Alignment.BottomCenter))
                }
            }
        }
    }
}
