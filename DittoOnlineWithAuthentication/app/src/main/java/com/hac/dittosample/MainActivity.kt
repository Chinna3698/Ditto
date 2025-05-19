package com.hac.dittosample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.hac.dittosample.ui.theme.DittoSampleTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import live.ditto.Ditto
import live.ditto.DittoError
import live.ditto.DittoIdentity
import live.ditto.DittoLiveQueryEvent
import live.ditto.DittoLogLevel
import live.ditto.DittoLogger
import live.ditto.DittoSyncSubscription
import live.ditto.android.DefaultAndroidDittoDependencies
import live.ditto.transports.DittoSyncPermissions
import live.ditto.transports.DittoTransportConfig
import live.ditto.transports.java.DittoConnect

lateinit var subscription: DittoSyncSubscription

class MainActivity : ComponentActivity() {
    private val config = DittoTransportConfig()
    private lateinit var androidDependencies: DefaultAndroidDittoDependencies
    private lateinit var ditto: Ditto
    var value: MutableState<SnapshotStateList<String>> = mutableStateOf(docValues)

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { this.ditto.refreshPermissions() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       // val serverUrl = "wss://d68742e1-380f-4d55-94cd-d2b344f3dc63.cloud.ditto.live"
        try {
            val syncScopes = mapOf(
                "local_mesh_orders" to "SmallPeersOnly"
            )
            requestPermissions()
            //config.connect = DittoConnect(websocketUrls = mutableSetOf(serverUrl))
            androidDependencies = DefaultAndroidDittoDependencies(this.applicationContext)
            val identity =
               /* DittoIdentity.OnlineWithAuthentication(
                    dependencies = androidDependencies,
                    appId = "d68742e1-380f-4d55-94cd-d2b344f3dc63",
                    callback = AuthCallBack()
                )*/
                DittoIdentity.OnlinePlayground(
                    appId = "d68742e1-380f-4d55-94cd-d2b344f3dc63",
                    token = "674b1e04-6d27-4209-9174-10434e41745d",
                    dependencies = androidDependencies
                )
            DittoLogger.minimumLogLevel = DittoLogLevel.DEBUG
            ditto = Ditto(androidDependencies, identity)
            CoroutineScope(Dispatchers.IO).launch {
                ditto.store.execute(
                    "ALTER SYSTEM SET USER_COLLECTION_SYNC_SCOPES = :syncScopes",
                    mapOf("syncScopes" to syncScopes)
                )

            }
            //config.enableAllPeerToPeer()
            config.peerToPeer.bluetoothLe.enabled = true
            config.peerToPeer.lan.enabled = true
            config.peerToPeer.wifiAware.enabled = true
            //ditto.transportConfig = config
            //ditto.disableSyncWithV3()

            ditto.startSync()
            observeItems(ditto)
            ditto.store.observers

        } catch (e: DittoError) {
            Log.e("Ditto error", e.message!!)
        }

        setContent {
            DittoSampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CrudOperations(ditto)
                }
            }
        }
    }

    fun requestPermissions() {
        val missing = DittoSyncPermissions(this).missingPermissions()
        if (missing.isNotEmpty()) {
            this.requestPermissions(missing, 0)
        }
    }

}

private fun observeItems(ditto: Ditto) {
    val query = ditto.store.collection(collection).findAll()

    subscription = ditto.sync.registerSubscription(query = "SELECT * FROM $collection")

    liveQuery = query.observeLocal { docs, event ->
        Log.d("docValue321", docs.size.toString())

        when (event) {

            is DittoLiveQueryEvent.Initial -> {}

            is DittoLiveQueryEvent.Update -> {

                event.updates.forEach { index ->
                    val doc = docs[index]
                    val docStation = doc["_id"].value
                    Log.d("docStation", doc.toString())
                }
            }
        }
        docValues.clear()
        for (i in docs) {
            Log.d("docValue32132423", i.value.toString())
            docValues.add(i.value.toString())
        }
    }
}
