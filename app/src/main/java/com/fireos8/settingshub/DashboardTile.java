package com.fireos8.settingshub;

public final class DashboardTile {

    public final String id;
    public final int titleRes;
    public final String command;
    public final int spanX;
    public final int spanY;
    public final boolean optional;

    public DashboardTile(String id, int titleRes, String command,
                         int spanX, int spanY, boolean optional) {
        this.id = id;
        this.titleRes = titleRes;
        this.command = command;
        this.spanX = spanX;
        this.spanY = spanY;
        this.optional = optional;
    }

    public DashboardTile withSpans(int newSpanX, int newSpanY) {
        return new DashboardTile(id, titleRes, command, newSpanX, newSpanY, optional);
    }

    public DashboardTile nextSize() {
        if (spanX == 1) {
            return withSpans(2, 2);
        }
        return withSpans(1, 2);
    }
}
