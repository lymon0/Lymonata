package com.lymon.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lymon.app.LymonVM

@Composable
fun AuthScreen(vm: LymonVM) {
    var register by remember { mutableStateOf(false) }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Lymon", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Accent)
        Spacer(Modifier.height(4.dp))
        Text(if (register) "إنشاء حساب جديد" else "تسجيل الدخول",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(user, { user = it }, label = { Text("اسم المستخدم") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(pass, { pass = it }, label = { Text("كلمة المرور") },
            singleLine = true, visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth())
        vm.error?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { vm.auth(register, user, pass) },
            enabled = !vm.busy && user.isNotBlank() && pass.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (vm.busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            else Text(if (register) "إنشاء الحساب" else "دخول")
        }
        TextButton(onClick = { register = !register; vm.clearError() }) {
            Text(if (register) "لدي حساب بالفعل" else "ليس لدي حساب؟ سجّل الآن")
        }
    }
}
