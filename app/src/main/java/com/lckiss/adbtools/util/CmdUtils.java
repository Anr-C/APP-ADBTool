package com.lckiss.adbtools.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CMD 命令行执行工具
 */
public class CmdUtils {
    public static final String COMMAND_SU = "su"; // 表示获取root权限（APP必须已root）
    public static final String COMMAND_LINE_END = "\n"; 
    public static final String COMMAND_EXIT = "exit\n";
    
    /**
     * Android手机用Wifi连上电脑ADB调试
     * 须在手机终端输入如下命令
     * 此终端必须已经Root
     */
    
    public static Result execute(String[] commands) {
        //----------------- 待写：检查此手机是否已经Root-------------
        
        Runtime runtime = Runtime.getRuntime();
        
        Process process = null;
        DataOutputStream output = null; // 用于向终端进程输入命令
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        
        try {
            process = runtime.exec(COMMAND_SU);
            output = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                
                output.write(command.getBytes());
                output.writeBytes(COMMAND_LINE_END); // 输完一行命令要按回车
                output.flush();
            }
            
            output.writeBytes(COMMAND_EXIT);
            output.flush();
            process.waitFor(); // 当前线程等待，直到process线程执行结束
            
            successMsg = new StringBuilder();
            errorMsg = new StringBuilder();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            
            String s;
            while ( (s = successResult.readLine()) != null) {
                successMsg.append(s).append("\n");
            }
            while ( (s = errorResult.readLine()) != null) {
                errorMsg.append(s).append("\n");
            }
            
            
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally { // 回收资源
                try {
                    if (output != null) {
                        output.close();
                    }
                    if (successResult != null) {
                        successResult.close();
                    }
                    if (errorResult != null) {
                        errorResult.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                if (process != null) {
                    process.destroy();
                }
        }
        return new Result(successMsg == null ? null : successMsg.toString()
                , errorMsg == null ? null : errorMsg.toString());
    }
    
    public static class Result {
        public String successMsg; 
        public String errorMsg; 
        
        public Result(String successMsg, String errorMsg) {
            super();
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
        
    }
}
