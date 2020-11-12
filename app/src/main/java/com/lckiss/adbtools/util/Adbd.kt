package com.lckiss.adbtools.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Adbd {

    suspend fun isRunning(): Boolean {
        return withContext(Dispatchers.IO) {
            CmdUtils.execute(arrayOf(
                    "getprop init.svc.adbd"
            )).successMsg.contains("running")
        }
    }

    suspend fun fetchRoot(): Boolean {
        return withContext(Dispatchers.IO) {
            val result = CmdUtils.execute(arrayOf(
                    "ls /data"
            ))
            result.successMsg.isNotEmpty()
        }
    }

    suspend fun stopAdbd(): Result {
        return withContext(Dispatchers.IO) {
            val cmd = arrayOf(
                    "stop adbd"
            )
            CmdUtils.execute(cmd)
        }
    }

    suspend fun connectAdbd(port:String): Result {
        return withContext(Dispatchers.IO) {
            val p = port.trim { it <= ' ' }
            val cmd = arrayOf(
                    "setprop service.adb.tcp.port $p",
                    "stop adbd",
                    "start adbd"
            )
          CmdUtils.execute(cmd)
        }
    }

}