package com.lckiss.adbtools

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lckiss.adbtools.util.AdbdCommand
import com.lckiss.adbtools.util.WifiUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    private val adbdCommand by lazy {
        AdbdCommand()
    }

    private val sp by lazy { getSharedPreferences("Setting", Context.MODE_PRIVATE) }

    private val isRoot
        get() = sp.getBoolean(KEY_IS_ROOT, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        rootSwitchCompt.setOnCheckedChangeListener(this)
        adbSwitchCompt.setOnCheckedChangeListener(this)

        lifecycleScope.launch {
            refreshRoot()

            val running = adbdCommand.isRunning()
            adbSwitchCompt.isChecked = running
            if (running) {
                refreshTvInfo()
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.adbSwitchCompt -> {
                if (!isRoot) {
                    adbSwitchCompt.isChecked = false
                    msg.text = getString(R.string.root_un_grant)
                    status_msg.text = resources.getString(R.string.status, getString(R.string.root_deny))
                    return
                }
                if (isChecked) {
                    connect()
                } else {
                    disconnect()
                }
            }
            R.id.rootSwitchCompt -> if (isChecked) {
                lifecycleScope.launch {
                    refreshRoot()
                }
            } else {
                sp.edit().putBoolean(KEY_IS_ROOT, false).apply()
            }
            else -> {
            }
        }
    }

    private suspend fun refreshRoot() {
        val result = adbdCommand.fetchRoot()
        withContext(Dispatchers.Main) {
            sp.edit().putBoolean(KEY_IS_ROOT, result).apply()
            rootSwitchCompt.isChecked = result
            if (!result) {
                msg.text = getString(R.string.root_deny)
            }
        }
    }

    private fun disconnect() {
        lifecycleScope.launch {
            val result = adbdCommand.stopAdbd()
            status_msg.text = resources.getString(R.string.status, getString(R.string.adbd_not_running))
            msg.text = getString(R.string.adbd_closed)
            val successMsg = result.successMsg
            Log.d(TAG, "connect: $successMsg")
            val errorMsg = result.errorMsg
            if (!TextUtils.isEmpty(errorMsg)) {
                errorDialog(errorMsg)
            }
        }
    }

    private fun connect() {
        lifecycleScope.launch {
            val result = adbdCommand.connectAdbd(port.text.trim().toString())
            val successMsg = result.successMsg
            Log.d(TAG, "connect: $successMsg")
            val errorMsg = result.errorMsg
            if (errorMsg.isNotEmpty()) {
                errorDialog(errorMsg)
            } else {
                refreshTvInfo()
            }
        }
    }

    private fun refreshTvInfo() {
        val p = port.text.toString().trim { it <= ' ' }
        val ipAddress = WifiUtils.getIpAddress(this)
        val msgRes: String
        msgRes = if (p == DEFAULT_PORT) {
            resources.getString(R.string.status_msg_without_port, ipAddress)
        } else {
            resources.getString(R.string.status_msg, ipAddress, p)
        }
        msg.text = msgRes
        status_msg.text = resources.getString(R.string.status, getString(R.string.adbd_running))
    }

    private fun errorDialog(msg: String) {
        AlertDialog.Builder(this)
                .setTitle(R.string.error_dialog_title)
                .setMessage(msg)
                .setPositiveButton(getString(R.string.btn_ok), null).show()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val DEFAULT_PORT = "5555"
        private const val KEY_IS_ROOT = "isRoot"
    }
}