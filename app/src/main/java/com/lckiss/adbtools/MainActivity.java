package com.lckiss.adbtools;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.lckiss.adbtools.util.CmdUtils;
import com.lckiss.adbtools.util.WifiUtils;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MainActivity";
    private EditText port;
    private TextView status;
    private TextView msg;
    private SharedPreferences sp;
    private SwitchCompat rootSwitch;
   private SwitchCompat adbSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }
    /**
     * 初始化
     **/
    private void initView() {
        rootSwitch = findViewById(R.id.rootSwitchCompt);
        adbSwitch = findViewById(R.id.adbSwitchCompt);
        port = findViewById(R.id.port);
        status = findViewById(R.id.status_msg);
        msg = findViewById(R.id.msg);


        sp = getSharedPreferences("Setting", MODE_PRIVATE);
        rootSwitch.setChecked(sp.getBoolean("isRoot", false));

        rootSwitch.setOnCheckedChangeListener(this);
        adbSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.adbSwitchCompt:
                if (!sp.getBoolean("isRoot", false)) {
                    adbSwitch.setChecked(false);
                    msg.setText("ROOT权限未授予！无法操作！");
                    status.setText(getResources().getString(R.string.status, "未开启"));
                    return;
                }
                if (isChecked) {
                    connect();
                } else {
                    disconnect();
                }
                break;
            case R.id.rootSwitchCompt:
                if (isChecked) {
                    CmdUtils.Result result = CmdUtils.execute(new String[]{
                            "ls /data"
                    });
                    if (!TextUtils.isEmpty(result.successMsg)) {
                        sp.edit().putBoolean("isRoot", true).apply();
                    } else {
                        sp.edit().putBoolean("isRoot", false).apply();
                        rootSwitch.setChecked(false);
                        msg.setText("您的设备未ROOT或已拒绝！");
                    }
                }else {
                    sp.edit().putBoolean("isRoot", false).apply();
                }
                break;
            default:
                break;
        }
    }

    private void disconnect() {
        String[] cmd = {
                "stop adbd"
        };
        CmdUtils.Result result = CmdUtils.execute(cmd);
        status.setText(getResources().getString(R.string.status, "未开启"));

        msg.setText("您已关闭ADB调试！");
        String successMsg = result.successMsg;
        String errorMsg = result.errorMsg;
        if(!TextUtils.isEmpty(errorMsg)){
            errorDialog(errorMsg);
        }
    }


    private void connect() {
        String p = port.getText().toString().trim();
        String[] cmd = {
                "setprop service.adb.tcp.port " + p,
                "stop adbd",
                "start adbd"
        };
        CmdUtils.Result result = CmdUtils.execute(cmd);
        String successMsg = result.successMsg;
        String errorMsg = result.errorMsg;
        if(!TextUtils.isEmpty(errorMsg)){
            errorDialog(errorMsg);
            return;
        }

        String ipAddress = WifiUtils.getIpAddress(this);
        String msgRes = getResources().getString(R.string.statusMsg, ipAddress, p);
        msg.setText(msgRes);
        status.setText(getResources().getString(R.string.status, "已开启"));
    }

    private void errorDialog(String msg){
        new AlertDialog.Builder(this)
                .setTitle("出错啦！")
                .setMessage(msg)
                .setPositiveButton("好的，我知道了",null).show();
    }
}
