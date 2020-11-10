package com.lckiss.adbtools

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.lckiss.adbtools.util.CmdUtils
import com.lckiss.adbtools.util.WifiUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    private val sp by lazy { getSharedPreferences("Setting", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        rootSwitchCompt.isChecked = sp.getBoolean(KEY_IS_ROOT, false)
        rootSwitchCompt.setOnCheckedChangeListener(this)
        adbSwitchCompt.setOnCheckedChangeListener(this)
        val running = isRunning
        adbSwitchCompt.isChecked = running
        if (running) {
            refreshTvInfo()
        }
    }

    private val isRunning: Boolean
        get() = CmdUtils.execute(arrayOf(
                "getprop init.svc.adbd"
        )).successMsg.contains("running")

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.adbSwitchCompt -> {
                if (!sp.getBoolean(KEY_IS_ROOT, false)) {
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
                val result = CmdUtils.execute(arrayOf(
                        "ls /data"
                ))
                if (!TextUtils.isEmpty(result.successMsg)) {
                    sp.edit().putBoolean(KEY_IS_ROOT, true).apply()
                } else {
                    sp.edit().putBoolean(KEY_IS_ROOT, false).apply()
                    rootSwitchCompt.isChecked = false
                    msg.text = getString(R.string.root_deny)
                }
            } else {
                sp.edit().putBoolean(KEY_IS_ROOT, false).apply()
            }
            else -> {
            }
        }
    }

    private fun disconnect() {
        val cmd = arrayOf(
                "stop adbd"
        )
        val result = CmdUtils.execute(cmd)
        status_msg.text = resources.getString(R.string.status, getString(R.string.adbd_not_running))
        msg.text = getString(R.string.adbd_closed)
        val successMsg = result.successMsg
        val errorMsg = result.errorMsg
        if (!TextUtils.isEmpty(errorMsg)) {
            errorDialog(errorMsg)
        }
    }

    private fun connect() {
        val p = port.text.toString().trim { it <= ' ' }
        val cmd = arrayOf(
                "setprop service.adb.tcp.port $p",
                "stop adbd",
                "start adbd"
        )
        val result = CmdUtils.execute(cmd)
        val successMsg = result.successMsg
        val errorMsg = result.errorMsg
        if (!TextUtils.isEmpty(errorMsg)) {
            errorDialog(errorMsg)
            return
        }
        refreshTvInfo()
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