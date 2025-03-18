package com.hac.dittosample

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import live.ditto.Ditto
import live.ditto.DittoAuthenticationCallback
import live.ditto.DittoAuthenticator
import live.ditto.DittoError
import live.ditto.DittoLiveQuery
import live.ditto.DittoLoginCallback


var docValues by mutableStateOf(mutableStateListOf<String>())
var liveQuery: DittoLiveQuery? = null
const val collection = "RealDeviceDb3"
fun performAction(
    action: String,
    value: String,
    ditto: Ditto,
    updateId: String = "",
) {
    try {

        if (value.isNotEmpty()) {
            Log.d("Ditto", "Action: $action, Value: $value")
            val totalSize = ditto.store.collection(collection).findAll()
            Log.d("Ditto", totalSize.toString())

            when (action) {
                "Insert" -> {
                    ditto.store.collection(collection).upsert(
                        mapOf("_id" to "$action - $value", "data" to value)
                    )
                }

                "UpdateValue" -> {
                    if (updateId.isNotEmpty()) {
                        ditto.store.collection(collection).upsert(
                            mapOf("_id" to "Insert - $updateId", "data" to value)
                        )
                    } else {
                        ditto.store.collection(collection).upsert(
                            mapOf("_id" to "$action - $value", "data" to value)
                        )
                    }
                }

                "Delete" -> {
                    val id = ditto.store.collection(collection).findById(value).docId.value
                    val id2 =
                        ditto.store.collection(collection).findById("Insert - $value").docId.value
                    id?.let { ditto.store.collection(collection).findById(it).remove() }
                    id2?.let { ditto.store.collection(collection).findById(it).remove() }
                }
            }
        } else {
            Log.d("Ditto", "Value is empty")
        }

    } catch (e: DittoError) {
        Log.e("Ditto error", e.message!!)
    }
    Log.d("Ditto", "Action: $action, Value: $value")

}

@Composable
fun CrudOperations(ditto: Ditto) {
    var insert by remember { mutableStateOf("") }
    var update by remember { mutableStateOf("") }
    var updateValue by remember { mutableStateOf("") }
    var delete by remember { mutableStateOf("") }

    Row {
        Column(
            modifier = Modifier
                .width(300.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = insert,
                onValueChange = { insert = it },
                label = { Text("Insert") },
                modifier = Modifier.height(100.dp)
            )
            Button(onClick = {
                performAction("Insert", insert, ditto = ditto)
                insert = ""
            }) {
                Text(text = "Submit")
            }

            TextField(
                value = update,
                onValueChange = { update = it },
                label = { Text("UpdateId") },
                modifier = Modifier.height(100.dp)
            )
            TextField(
                value = updateValue,
                onValueChange = { updateValue = it },
                label = { Text("UpdateValue") },
                modifier = Modifier.height(100.dp)
            )
            Button(onClick = {
                performAction("UpdateValue", updateValue, ditto = ditto, update)
                update = ""
                updateValue = ""
            }) {
                Text(text = "Submit")
            }

            TextField(
                value = delete,
                onValueChange = { delete = it },
                label = { Text("Delete") },
                modifier = Modifier.height(100.dp)
            )
            Button(onClick = {
                performAction("Delete", delete, ditto = ditto)
                delete = ""
            }) {
                Text(text = "Submit")
            }

        }
        Text(text = "Total documents Size is: ${docValues.size} ")
        Column {
            for (i in docValues) {
                Text(text = i)
            }

        }
    }

}


class AuthCallBack : DittoAuthenticationCallback {
    override fun authenticationRequired(authenticator: DittoAuthenticator) {
        Log.d("Ditto", "Authentication Required - Initiating Login")
        authenticator.login(
            token = "Chinna",
            provider = "replit-auth",
            object : DittoLoginCallback {
                override fun callback(clientInfoJSON: String?, error: DittoError?) {
                    if (error != null) {
                        Log.w("Ditto", "Login failed: ${error.message}")
                    } else {
                        Log.d("Ditto", "Login successful")
                    }
                }
            })

        //authenticator.logout()
        Log.d("Ditto", "Login request sent to Ditto")
    }

    override fun authenticationExpiringSoon(
        authenticator: DittoAuthenticator,
        secondsRemaining: Long,
    ) {
        Log.d("Ditto", "Authentication Expiring Soon - $secondsRemaining seconds remaining")
    }
}



