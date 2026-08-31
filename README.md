# FireOS8 Settings Hub

## Projektübersicht

Eine minimalistische Android TV / Fire TV App als Kontrollzentrum für den direkten Zugriff auf versteckte System-Einstellungen. Sie kann über jeden kompatiblen Android-TV-Launcher gestartet werden.

## Das Problem

Unter Fire OS 8 blockiert Amazon die Haupteinstellungen, insbesondere **Anzeige & Ton** und damit verbunden die **HDMI-CEC-Steuerung**, so rigoros, dass Standard-ADB-Befehle oder normale App-Aufrufe mit einer `SecurityException` fehlschlagen. Ohne Root ist der Zugriff auf diese Menüs unmöglich.

## Die Lösung

Die App nutzt vorhandene Root-Rechte über `su`, um die geschützten System-Intents direkt und ohne Sicherheitsabfragen im Hintergrund zu triggern. Dadurch werden die gesperrten Menüs, inklusive HDMI-CEC, voll funktionsfähig geöffnet.

## Unterstützte Menüs / Intents

| Menü | Aktivität |
| --- | --- |
| Anzeige, Ton & HDMI-CEC | `.tv.display_sounds.DisplayAndSoundsActivity` |
| Netzwerk & WLAN | `.tv.network.NetworkActivity` |
| Apps & Benachrichtigungen | `.tv.applications.ApplicationsActivity` |
| Controller & Bluetooth | `.tv.controllers_bluetooth_devices.ControllersAndBluetoothActivity` |
| Geräte-Informationen | `.tv.device.DeviceActivity` |
| Mein Konto | `.tv.my_account.MyAccountActivity` |
| Barrierefreiheit | `.tv.accessibility.AccessibilityActivity` |

## Voraussetzungen

- Gerooteter Fire TV Stick mit funktionierender `su`-Binary
- Möglichkeit zum Sideloading, zum Beispiel via ADB oder Downloader

## Installation

### Per ADB

1. Aktiviere ADB-Debugging in den Entwickleroptionen des Fire TV Sticks.
2. Verbinde den Computer mit dem Gerät und bestätige die Debugging-Anfrage auf dem Fernseher.
3. Installiere die APK mit:

```bash
adb install app-debug.apk
```

### Per Dateimanager

1. Kopiere `app-debug.apk` auf den Fire TV Stick, zum Beispiel per USB, Netzwerkfreigabe oder Downloader.
2. Öffne die Entwickleroptionen und erlaube dem verwendeten Dateimanager die Installation unbekannter Apps.
3. Öffne die APK im Dateimanager.
4. Wähle **Installieren** und starte die App anschließend über einen kompatiblen Android-TV-Launcher.

## Credits

Made By [GhostDragon5](https://github.com/GhostDragon5/)
