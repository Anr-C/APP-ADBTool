package com.lckiss.adbtools.util

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

/**
 * CMD 命令行执行工具
 */
object CmdUtils {

    // 表示获取root权限（APP必须已root）
    private const val COMMAND_SU = "su"
    private const val COMMAND_LINE_END = "\n"
    private const val COMMAND_EXIT = "exit\n"

    /**
     * Android手机用Wifi连上电脑ADB调试
     * 须在手机终端输入如下命令
     * 此终端必须已经Root
     */
    fun execute(commands: Array<String>): Result {
        //----------------- 待写：检查此手机是否已经Root-------------
        val successMsg = StringBuilder()
        val errorMsg = StringBuilder()

        kotlin.runCatching {
            val runtime = Runtime.getRuntime()
            val process = runtime.exec(COMMAND_SU)
            // 用于向终端进程输入命令
            val output = DataOutputStream(process.outputStream)
            output.use {
                for (command in commands) {
                    output.write(command.toByteArray())
                    // 输完一行命令要按回车
                    output.writeBytes(COMMAND_LINE_END)
                    output.flush()
                }
                output.writeBytes(COMMAND_EXIT)
                output.flush()
            }
            // 当前线程等待，直到process线程执行结束
            process.waitFor()

            var s: String
            val successReader = BufferedReader(InputStreamReader(process.inputStream))
            successReader.use {
                while (successReader.readLine().also { s = it } != null) {
                    successMsg.append(s).append("\n")
                }
            }
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            errorReader.use {
                while (errorReader.readLine().also { s = it } != null) {
                    errorMsg.append(s).append("\n")
                }
            }
            process
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            it?.destroy()
        }
        return Result(successMsg.toString(), errorMsg.toString())
    }

    class Result(var successMsg: String, var errorMsg: String)
}