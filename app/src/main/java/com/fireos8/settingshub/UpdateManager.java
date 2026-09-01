package com.fireos8.settingshub;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class UpdateManager {

    private static final String LATEST_RELEASE_URL =
            "https://api.github.com/repos/GhostDragon5/FireOS8-Settings-Hub/releases/latest";

    public interface CheckCallback {
        void onUpdateAvailable(UpdateInfo update);
    }

    public interface InstallCallback {
        void onFinished(boolean success);
    }

    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UpdateManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void checkForUpdate(CheckCallback callback) {
        executor.execute(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(LATEST_RELEASE_URL).openConnection();
                connection.setConnectTimeout(10_000);
                connection.setReadTimeout(10_000);
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setRequestProperty("User-Agent", "FireOS8-Settings-Hub");

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return;
                }

                String releaseJson = read(connection.getInputStream());
                JSONObject release = new JSONObject(releaseJson);
                String tag = release.getString("tag_name");
                String apkUrl = findApkUrl(release.getJSONArray("assets"));
                if (apkUrl != null && isNewer(tag, BuildConfig.VERSION_NAME)) {
                    callback.onUpdateAvailable(new UpdateInfo(tag, apkUrl,
                            release.optString("body", "")));
                }
            } catch (Exception ignored) {
                // An unavailable network or release must not prevent app startup.
            }
        });
    }

    public void downloadAndInstall(UpdateInfo update, RootShell rootShell, InstallCallback callback) {
        executor.execute(() -> {
            try {
                File apk = new File(context.getCacheDir(), "fireos8-settings-hub-update.apk");
                HttpURLConnection connection = (HttpURLConnection) new URL(update.downloadUrl).openConnection();
                connection.setConnectTimeout(15_000);
                connection.setReadTimeout(30_000);
                connection.setRequestProperty("User-Agent", "FireOS8-Settings-Hub");

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    callback.onFinished(false);
                    return;
                }

                try (InputStream input = connection.getInputStream();
                     FileOutputStream output = new FileOutputStream(apk)) {
                    byte[] buffer = new byte[16 * 1024];
                    int count;
                    while ((count = input.read(buffer)) != -1) {
                        output.write(buffer, 0, count);
                    }
                }

                rootShell.execute("pm install -r " + apk.getAbsolutePath(), result ->
                        callback.onFinished(result.success && !result.output.contains("Failure"))
                );
            } catch (Exception ignored) {
                callback.onFinished(false);
            }
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    private String findApkUrl(JSONArray assets) throws Exception {
        for (int index = 0; index < assets.length(); index++) {
            JSONObject asset = assets.getJSONObject(index);
            if (asset.getString("name").endsWith(".apk")) {
                return asset.getString("browser_download_url");
            }
        }
        return null;
    }

    private boolean isNewer(String remoteVersion, String installedVersion) {
        int[] remote = versionParts(remoteVersion);
        int[] installed = versionParts(installedVersion);
        for (int index = 0; index < 3; index++) {
            if (remote[index] != installed[index]) {
                return remote[index] > installed[index];
            }
        }
        return false;
    }

    private int[] versionParts(String version) {
        String[] values = version.replaceAll("[^0-9.]", "").split("\\.");
        int[] parts = new int[]{0, 0, 0};
        for (int index = 0; index < values.length && index < parts.length; index++) {
            if (!values[index].isEmpty()) {
                parts[index] = Integer.parseInt(values[index]);
            }
        }
        return parts;
    }

    private String read(InputStream input) throws Exception {
        StringBuilder content = new StringBuilder();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = input.read(buffer)) != -1) {
            content.append(new String(buffer, 0, count));
        }
        input.close();
        return content.toString();
    }
}
