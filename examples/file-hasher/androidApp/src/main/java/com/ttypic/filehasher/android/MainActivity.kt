package com.ttypic.filehasher.android

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.ttypic.filehasher.FileHasherFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    FileHasherView()
                }
            }
        }
    }
}

@Composable
fun FileHasherView() {
    val context = LocalContext.current
    var fileMd5 by remember { mutableStateOf<String?>(null) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { context.contentResolver.openInputStream(it) }?.use {
                fileMd5 = FileHasherFactory.createMd5Hasher().hash(it.readBytes())
            }
        }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        fileMd5?.let { Text(text = "File's MD5 hash:", style = MaterialTheme.typography.h4) }
        fileMd5?.let { Text(text = it) }
        Button(onClick = {
            launcher.launch("*/*")
        }) {
            Text(text = if (fileMd5 == null) "Choose File" else "Choose Another")
        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        FileHasherView()
    }
}
