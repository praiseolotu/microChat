package com.example.microchatter.network.wifip2p

import android.content.*
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import com.example.microchatter.model.PeerDevice

class WifiP2PManagerHelper(
    private val context: Context,
    private val lifecycle: Lifecycle
) : LifecycleObserver {

    private val manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private val channel = manager.initialize(context, Looper.getMainLooper(), null)

    private var receiver: BroadcastReceiver? = null
    var onPeerDisconnected: (() -> Unit)? = null

    fun discoverPeers(onPeersDiscovered: (List<PeerDevice>) -> Unit) {
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("WifiP2P", "Discovery started.")
                manager.requestPeers(channel) { list ->
                    val peers = list.deviceList.map {
                        PeerDevice(it.deviceName ?: "Unknown", it.deviceAddress)
                    }
                    onPeersDiscovered(peers)
                }
            }

            override fun onFailure(reason: Int) {
                Log.e("WifiP2P", "Peer discovery failed: $reason")
                onPeersDiscovered(emptyList())
            }
        })
    }

    fun connectToPeer(address: String, onSuccess: () -> Unit) {
        val config = WifiP2pConfig().apply {
            deviceAddress = address
            wps.setup = WpsInfo.PBC
        }
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("WifiP2P", "Connecting to $address...")
                onSuccess()
            }

            override fun onFailure(reason: Int) {
                Log.e("WifiP2P", "Connection failed: $reason")
            }
        })
    }

    fun disconnect(onComplete: () -> Unit) {
        manager.requestGroupInfo(channel) { group ->
            if (group != null) {
                manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.d("WifiP2P", "Disconnected successfully.")
                        onComplete()
                    }

                    override fun onFailure(reason: Int) {
                        Log.e("WifiP2P", "Disconnection failed: $reason")
                        onComplete()
                    }
                })
            } else {
                onComplete()
            }
        }
    }

    fun onGroupOwnerAvailable(callback: (hostAddress: java.net.InetAddress, isGroupOwner: Boolean) -> Unit) {
        manager.requestConnectionInfo(channel) { info ->
            if (info.groupFormed) {
                Log.d("WifiP2P", "Group formed. IsOwner=${info.isGroupOwner}")
                callback(info.groupOwnerAddress, info.isGroupOwner)
            }
        }
    }

    fun registerReceiver(context: Context) {
        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                if (action == WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION) {
                    val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                    if (networkInfo?.isConnected == false) {
                        onPeerDisconnected?.invoke()
                    }
                }
            }
        }

        context.registerReceiver(receiver, intentFilter)
    }

    fun unregisterReceiver(context: Context) {
        try {
            receiver?.let { context.unregisterReceiver(it) }
        } catch (_: Exception) {}
    }
}