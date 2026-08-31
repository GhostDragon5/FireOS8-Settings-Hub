# FireOS8 Settings Hub

Eine schlanke Android-TV-App, die den direkten Zugriff auf versteckte Fire OS 8 Systemeinstellungen ermöglicht.

## Voraussetzungen

- Fire-TV-Gerät mit Fire OS 8
- Root-Zugriff mit verfügbarer `su`-Binary
- Installation von Apps aus unbekannten Quellen erlaubt

Ohne Root-Zugriff kann die App die Einstellungen nicht öffnen.

## Funktionen

- Anzeige, Ton und HDMI-CEC
- Netzwerk und WLAN
- Apps und Benachrichtigungen
- Controller und Bluetooth
- Geräte-Informationen
- Mein Konto
- Barrierefreiheit
- Deutsch und Englisch entsprechend der Systemsprache
- Fernbedienungsfreundliche D-Pad-Navigation

## Installation

1. Lade die aktuelle `app-debug.apk` aus den [Releases](../../releases) herunter.
2. Übertrage die APK auf dein Fire-TV-Gerät und installiere sie, beispielsweise über ADB:

```bash
adb install app-debug.apk
```

3. Starte **FireOS8 Settings Hub** über den Fire-TV-Launcher.

## Build

Das Projekt kann direkt in Android Studio geöffnet werden. Für einen Debug-Build:

```bash
./gradlew assembleDebug
```

Die erzeugte APK befindet sich anschließend unter:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Hinweis

Die verwendeten Fire-OS-Einstellungsaktivitäten sind versionsabhängig. Die App ist für Fire OS 8 ausgelegt.

## Credits

Made By [GhostDragon5](https://github.com/GhostDragon5/)
