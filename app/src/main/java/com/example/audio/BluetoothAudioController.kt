package com.example.audio

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AudioRoutingMode(val title: String, val subtitle: String) {
    SPLIT_EARBUD_PHONE(
        "Earbud + Phone Split",
        "Bangla speech in your Earbud; English speech out loud via Phone Speaker for partner"
    ),
    EARBUD_ALL(
        "Earbud Exclusive",
        "All translations played privately inside your connected Bluetooth earbuds"
    ),
    SPEAKER_ONLY(
        "Speakerphone",
        "All translations played out loud on phone loudspeaker"
    )
}

data class BluetoothStatus(
    val isHeadsetConnected: Boolean = false,
    val deviceName: String = "No Earbuds Detected",
    val isScoActive: Boolean = false,
    val routingMode: AudioRoutingMode = AudioRoutingMode.SPLIT_EARBUD_PHONE,
    val isBluetoothEnabled: Boolean = false,
    val hasAudioDevices: Boolean = false
)

class BluetoothAudioController(private val context: Context) {

    private val TAG = "BluetoothAudio"
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()

    private val _status = MutableStateFlow(BluetoothStatus())
    val status: StateFlow<BluetoothStatus> = _status.asStateFlow()

    private var bluetoothHeadset: BluetoothHeadset? = null

    private val profileListener = object : BluetoothProfile.ServiceListener {
        @SuppressLint("MissingPermission")
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = proxy as BluetoothHeadset
                checkConnectedDevices()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = null
                _status.value = _status.value.copy(
                    isHeadsetConnected = false,
                    deviceName = "Earbuds Disconnected",
                    isScoActive = false
                )
            }
        }
    }

    private val audioReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            when (action) {
                AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                    val state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR)
                    val isSco = state == AudioManager.SCO_AUDIO_STATE_CONNECTED
                    Log.d(TAG, "SCO Audio State: $state (Connected=$isSco)")
                    _status.value = _status.value.copy(isScoActive = isSco)
                }
                BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED)
                    if (state == BluetoothProfile.STATE_CONNECTED) {
                        checkConnectedDevices()
                    } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                        _status.value = _status.value.copy(
                            isHeadsetConnected = false,
                            deviceName = "No Earbuds Connected",
                            isScoActive = false
                        )
                    }
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    checkConnectedDevices()
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    checkConnectedDevices()
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    _status.value = _status.value.copy(isBluetoothEnabled = btState == BluetoothAdapter.STATE_ON)
                    if (btState == BluetoothAdapter.STATE_ON) {
                        checkConnectedDevices()
                    }
                }
            }
        }
    }

    init {
        try {
            bluetoothAdapter?.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET)
        } catch (e: Exception) {
            Log.e(TAG, "Error obtaining Bluetooth profile proxy: ${e.message}")
        }

        val filter = IntentFilter().apply {
            addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
            addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        context.registerReceiver(audioReceiver, filter)
        checkConnectedDevices()
    }

    @SuppressLint("MissingPermission")
    fun checkConnectedDevices() {
        val isBtEnabled = bluetoothAdapter?.isEnabled == true
        var connected = false
        var name = "No Earbuds Detected"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                for (dev in devices) {
                    if (dev.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                        dev.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        dev.type == AudioDeviceInfo.TYPE_BLE_HEADSET
                    ) {
                        connected = true
                        name = dev.productName?.toString() ?: "Bluetooth Earbuds"
                        break
                    }
                }
            } else {
                val connectedDevices = bluetoothHeadset?.connectedDevices
                if (!connectedDevices.isNullOrEmpty()) {
                    connected = true
                    name = connectedDevices.first().name ?: "Bluetooth Earbuds"
                } else if (audioManager.isBluetoothA2dpOn || audioManager.isBluetoothScoOn) {
                    connected = true
                    name = "Bluetooth Audio Device"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking devices: ${e.message}")
        }

        _status.value = _status.value.copy(
            isHeadsetConnected = connected,
            deviceName = if (connected) name else "Phone Mic & Speaker (No Earbuds)",
            isBluetoothEnabled = isBtEnabled
        )
    }

    fun setRoutingMode(mode: AudioRoutingMode) {
        _status.value = _status.value.copy(routingMode = mode)
    }

    /**
     * Prepares audio routing right before speaking translation to target listener.
     * @param toRole Target speaker who will hear this audio ("YOU_BANGLA" -> user wearing earbuds, "PARTNER_ENGLISH" -> partner)
     */
    fun routeAudioForSpeaker(toRole: String) {
        try {
            when (_status.value.routingMode) {
                AudioRoutingMode.SPLIT_EARBUD_PHONE -> {
                    if (toRole == "YOU_BANGLA") {
                        // Routing to Earbud for User
                        if (_status.value.isHeadsetConnected) {
                            audioManager.isSpeakerphoneOn = false
                            try {
                                audioManager.startBluetoothSco()
                                audioManager.isBluetoothScoOn = true
                            } catch (e: Exception) {
                                Log.w(TAG, "Could not start SCO: ${e.message}")
                            }
                        } else {
                            audioManager.isSpeakerphoneOn = true
                        }
                    } else {
                        // Routing to Phone Loudspeaker for English Partner
                        try {
                            audioManager.isBluetoothScoOn = false
                            audioManager.stopBluetoothSco()
                        } catch (_: Exception) {}
                        audioManager.isSpeakerphoneOn = true
                    }
                }
                AudioRoutingMode.EARBUD_ALL -> {
                    if (_status.value.isHeadsetConnected) {
                        audioManager.isSpeakerphoneOn = false
                        try {
                            audioManager.startBluetoothSco()
                            audioManager.isBluetoothScoOn = true
                        } catch (_: Exception) {}
                    }
                }
                AudioRoutingMode.SPEAKER_ONLY -> {
                    try {
                        audioManager.isBluetoothScoOn = false
                        audioManager.stopBluetoothSco()
                    } catch (_: Exception) {}
                    audioManager.isSpeakerphoneOn = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in routeAudioForSpeaker: ${e.message}")
        }
    }

    fun startScoMic() {
        try {
            if (_status.value.isHeadsetConnected) {
                audioManager.startBluetoothSco()
                audioManager.isBluetoothScoOn = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed startScoMic: ${e.message}")
        }
    }

    fun stopScoMic() {
        try {
            audioManager.isBluetoothScoOn = false
            audioManager.stopBluetoothSco()
        } catch (e: Exception) {
            Log.e(TAG, "Failed stopScoMic: ${e.message}")
        }
    }

    fun release() {
        try {
            stopScoMic()
            context.unregisterReceiver(audioReceiver)
            if (bluetoothHeadset != null && bluetoothAdapter != null) {
                bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing BluetoothAudioController: ${e.message}")
        }
    }
}
