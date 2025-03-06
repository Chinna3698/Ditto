package com.hac.dittosample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.hac.dittosample.ui.theme.DittoSampleTheme
import live.ditto.Ditto
import live.ditto.DittoError
import live.ditto.DittoIdentity
import live.ditto.android.DefaultAndroidDittoDependencies
import live.ditto.transports.DittoTransportConfig
import live.ditto.transports.java.DittoConnect

class MainActivity : ComponentActivity() {
    private val config = DittoTransportConfig()
    private lateinit var androidDependencies: DefaultAndroidDittoDependencies
    private lateinit var ditto: Ditto
    var value: MutableState<SnapshotStateList<String>> = mutableStateOf(docValues)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serverUrl = "wss://e728cd18-dcef-404b-be90-721cc24cfe22.cloud.ditto.live"
        try {
            config.enableAllPeerToPeer()
            config.connect = DittoConnect(websocketUrls = mutableSetOf(serverUrl))
            config.peerToPeer.bluetoothLe.enabled = false
            config.peerToPeer.lan.enabled = true
            config.peerToPeer.wifiAware.enabled = true
            androidDependencies = DefaultAndroidDittoDependencies(this.applicationContext)
            val identity =
                DittoIdentity.OnlineWithAuthentication(
                    dependencies = androidDependencies,
                    appId = "e728cd18-dcef-404b-be90-721cc24cfe22",
                    callback = AuthCallBack()
                )
            ditto = Ditto(androidDependencies, identity)
            ditto.startSync()
            observeItems(ditto)

        } catch (e: DittoError) {
            Log.e("Ditto error", e.message!!)
        }

        setContent {
            DittoSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CrudOperations(ditto)
                }
            }
        }
    }

}


