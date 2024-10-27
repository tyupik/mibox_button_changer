package com.tyupik.changeremotebuttons

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.tyupik.changeremotebuttons.ui.theme.ChangeRemoteButtonsTheme
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, KeyEventAccessibilityService::class.java)
        startService(intent)
        setContent {
            ChangeRemoteButtonsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Откройте Настройки -> Настройки устройства -> Специальные возможности -> ChangeRemoteButtons -> Вкл.")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { startActivity(Intent(Settings.ACTION_SETTINGS)) }) {
                                Text(text = "Открыть настройки")
                            }
                        }
                    }
                }
            }
        }
    }
}

class KeyEventAccessibilityService : AccessibilityService() {

    private var isAppLaunched = AtomicBoolean(false)
    private val handler = Handler()
    private val launchRunnable = Runnable {
        if (isAppLaunched.compareAndSet(false, true)) {
            // На что меняем
            launchApp("com.wireguard.android")
        }
    }
    private val resetFlag = Runnable {
        isAppLaunched.compareAndSet(true, false)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event != null) {
            // Что меняем
            if (event.packageName == "com.google.android.tv") {
                handler.postDelayed(launchRunnable, 500)
            }
            handler.postDelayed(resetFlag, 5000)
        }
    }

    private fun launchApp(packageName: String) {
        val pm: PackageManager = this.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            this.startActivity(launchIntent)
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.startActivity(intent)
        }
    }

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        }
        serviceInfo = info
    }

    override fun onInterrupt() {}
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChangeRemoteButtonsTheme {
        Greeting("Android")
    }
}