package com.fireos8.settingshub;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private final ExecutorService commandExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindCommand(R.id.button_display_sounds,
                "am start -n com.amazon.tv.settings.v2/.tv.display_sounds.DisplayAndSoundsActivity");
        bindCommand(R.id.button_network,
                "am start -n com.amazon.tv.settings.v2/.tv.network.NetworkActivity");
        bindCommand(R.id.button_apps,
                "am start -n com.amazon.tv.settings.v2/.tv.applications.ApplicationsActivity");
        bindCommand(R.id.button_controllers,
                "am start -n com.amazon.tv.settings.v2/.tv.controllers_bluetooth_devices.ControllersAndBluetoothActivity");
        bindCommand(R.id.button_device,
                "am start -n com.amazon.tv.settings.v2/.tv.device.DeviceActivity");
        bindCommand(R.id.button_account,
                "am start -n com.amazon.tv.settings.v2/.tv.my_account.MyAccountActivity");
        bindCommand(R.id.button_accessibility,
                "am start -n com.amazon.tv.settings.v2/.tv.accessibility.AccessibilityActivity");
        findViewById(R.id.button_close).setOnClickListener(view -> finishAndRemoveTask());

        findViewById(R.id.button_display_sounds).requestFocus();
    }

    private void bindCommand(int viewId, String command) {
        findViewById(viewId).setOnClickListener(view -> launchWithRoot(command));
    }

    private void launchWithRoot(String command) {
        commandExecutor.execute(() -> {
            try {
                Process process = Runtime.getRuntime().exec("su");
                try (DataOutputStream shell = new DataOutputStream(process.getOutputStream())) {
                    shell.writeBytes(command + "\n");
                    shell.writeBytes("exit\n");
                    shell.flush();
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    showRootError();
                }
            } catch (IOException | InterruptedException exception) {
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                showRootError();
            }
        });
    }

    private void showRootError() {
        runOnUiThread(() -> Toast.makeText(
                this,
                R.string.root_error,
                Toast.LENGTH_LONG
        ).show());
    }

    @Override
    protected void onDestroy() {
        commandExecutor.shutdownNow();
        super.onDestroy();
    }
}
