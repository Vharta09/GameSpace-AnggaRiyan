package com.example.gamespace

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.gamespace.ui.GameDetailScreen
import com.example.gamespace.ui.GsBg
import com.example.gamespace.ui.GameSpaceTheme
import com.example.gamespace.ui.HomeScreen
import com.example.gamespace.ui.MainViewModel
import com.example.gamespace.ui.PickAppScreen
import com.example.gamespace.ui.PermissionsScreen

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameSpaceTheme {
                Root(vm) { startActivity(it) }
            }
        }
    }
}

@Composable
private fun Root(vm: MainViewModel, launch: (Intent) -> Unit) {
    // "home", "perms", "pick", atau "game:<pkg>"
    var route by rememberSaveable { mutableStateOf("home") }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }

    // Segarkan status izin setiap kali kembali dari layar Pengaturan.
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) vm.refreshPerms()
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }

    val modifier = Modifier.fillMaxSize().background(GsBg).statusBarsPadding()
    when {
        route == "perms" -> PermissionsScreen(vm, modifier, launch) { route = "home" }
        route == "pick" -> PickAppScreen(vm, modifier) { route = "home" }
        route.startsWith("game:") -> GameDetailScreen(
            vm, route.removePrefix("game:"), modifier
        ) { route = "home" }
        else -> HomeScreen(
            vm, modifier,
            onOpenGame = { route = "game:$it" },
            onPick = { route = "pick" },
            onPerms = { route = "perms" }
        )
    }
}
