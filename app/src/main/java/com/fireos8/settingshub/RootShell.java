package com.fireos8.settingshub;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RootShell {

    public interface Callback {
        void onComplete(RootResult result);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void execute(String command, Callback callback) {
        executor.execute(() -> {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec("su");
                try (DataOutputStream shell = new DataOutputStream(process.getOutputStream())) {
                    shell.writeBytes(command + "\n");
                    shell.writeBytes("exit\n");
                    shell.flush();
                }

                String output = read(process.getInputStream()) + read(process.getErrorStream());
                int exitCode = process.waitFor();
                boolean success = exitCode == 0
                        && !output.contains("SecurityException")
                        && !output.contains("Permission Denial")
                        && !output.contains("Error:");
                callback.onComplete(new RootResult(exitCode, output, success));
            } catch (IOException | InterruptedException exception) {
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                callback.onComplete(new RootResult(-1, exception.getMessage(), false));
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    private String read(InputStream stream) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append('\n');
            }
        }
        return output.toString();
    }
}
