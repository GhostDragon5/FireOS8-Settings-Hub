package com.fireos8.settingshub;

public final class UpdateInfo {

    public final String version;
    public final String downloadUrl;
    public final String changelog;

    public UpdateInfo(String version, String downloadUrl, String changelog) {
        this.version = version;
        this.downloadUrl = downloadUrl;
        this.changelog = changelog;
    }
}
