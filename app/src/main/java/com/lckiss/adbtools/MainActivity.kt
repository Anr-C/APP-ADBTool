package com.lckiss.adbtools

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lckiss.adbtools.util.Adbd
import com.lckiss.adbtools.util.Net
import com.lckiss.adbtools.util.createCircularReveal
import com.lckiss.adbtools.util.dp
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    private val adbdCommand by lazy {
        Adbd()
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
            rootSwitchCompt.isChecked = isRoot
            val running = adbdCommand.isRunning()
            adbSwitchCompt.isChecked = running
            if (running) {
                refreshDisplayInfo()
            }
        }
        content.post {
            content.createCircularReveal(0, 0, 30f.dp, content.measuredWidth.toFloat())
            content.clearFocus()
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
                lifecycleScope.launch {
                    if (isChecked) {
                        connect()
                    } else {
                        disconnect()
                    }
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
            delay(1000)
            rootSwitchCompt.isChecked = result
            if (!result) {
                msg.text = getString(R.string.root_grant_message)
            }
        }
    }

    private suspend fun disconnect() {
        val result = adbdCommand.stopAdbd()
        withContext(Dispatchers.Main) {
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

    private suspend fun connect() {
        val result = adbdCommand.connectAdbd(port.text.trim().toString())
        withContext(Dispatchers.Main) {
            val successMsg = result.successMsg
            Log.d(TAG, "connect: $successMsg")
            val errorMsg = result.errorMsg
            if (errorMsg.isNotEmpty()) {
                errorDialog(errorMsg)
            } else {
                refreshDisplayInfo()
            }
        }
    }

    private suspend fun refreshDisplayInfo() {
        val ipAddress = withContext(Dispatchers.IO) {
            Net.getIpAddress(this@MainActivity)
        }
        withContext(Dispatchers.Main) {
            val p = port.text.toString().trim { it <= ' ' }
            val msgRes = if (p == DEFAULT_PORT) {
                resources.getString(R.string.status_msg_without_port, ipAddress)
            } else {
                resources.getString(R.string.status_msg, ipAddress, p)
            }
            msg.text = msgRes
            status_msg.text = resources.getString(R.string.status, getString(R.string.adbd_running))
        }
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