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

## Credits

Made By [GhostDragon5](https://github.com/GhostDragon5/)
