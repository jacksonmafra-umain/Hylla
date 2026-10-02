package com.umain.hylla.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.umain.hylla.R
import com.umain.hylla.posture.PostureReadout
import com.umain.hylla.posture.WindowPosture

/** What the app knows about the device it runs on. Chapter 17 adds registering it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThisDeviceScreen(posture: WindowPosture, modifier: Modifier = Modifier) {
    Scaffold(modifier, topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_this_device)) }) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            PostureReadout(posture)
        }
    }
}
