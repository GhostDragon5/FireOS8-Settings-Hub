# Changelog

Alle wichtigen Änderungen an diesem Projekt werden in dieser Datei dokumentiert.
## [1.2.0] - 2026-09-02

### Fixed

- Overlay erscheint erst, nachdem die Starter-Activity vollstaendig zerstoert wurde.
  Zuvor ueberlappte der Overlay-Fokus mit dem Pausieren der Activity, was
  Lifecycle-Timeouts und einen laengeren schwarzen Moment beim Start verursachte.
- Starter-Activity rendert kein Fenster mehr (eigenes transparentes Theme ohne Preview
  und ohne Window-Animation).


## [1.1.12] - 2026-09-01
### Changed
- Show dashboard as launcher overlay
## [1.1.11] - 2026-09-01

### Added

- Dashboard schwebt nun als echtes Overlay-Fenster (`SYSTEM_ALERT_WINDOW`) über dem Launcher,
  damit dieser aktiv bleibt und nicht schwarz rendert.
- Neuer `OverlayService` haelt das Dashboard stabil im Vordergrund.
- Dialoge (Theme-Auswahl, Update) werden als Views im Overlay dargestellt.

### Changed

- `MainActivity` ist jetzt ein schlanker Starter: Sie prueft die Overlay-Berechtigung,
  startet den Service und beendet sich sofort, damit der Launcher wieder fortgesetzt wird.
- Die Dashboard-Logik wurde in die neue Klasse `DashboardOverlay` verschoben.

### Fixed

- Schwarzer Hintergrund beim Oeffnen behoben: Da der Launcher (Monet) nicht mehr pausiert wird,
  bleibt er waehrend und nach der Einfliege-Animation sichtbar.
- Einfliege-Animation funktioniert wieder: Die Animation wurde vor dem Anhaengen der View
  gestartet und dadurch verworfen.


## [1.1.10] - 2026-09-01

### Fixed

- Schwarzer Hintergrund während der Einfliege-Animation endgültig behoben:
  Theme-Parent auf `Theme.Material.NoActionBar` gewechselt, `windowContentOverlay` auf `@null` gesetzt
  und die Einblendung über eine echte Window-Animation (`Animation.TransparentSlide`) realisiert,
  damit Android keinen schwarzen Render-Buffer mehr unter das aufsteigende Panel legt.

## [1.1.9] - 2026-09-01

### Changed

- Fix black background during open animation

## [1.1.8] - 2026-09-01

### Fixed

- Schwarzer Hintergrund während der Einblendanimation behoben (Fenster ist nun sofort transparent)

### Added

- Dezente Versionsanzeige oben rechts im Dashboard


## [1.1.7] - 2026-09-01

### Added

- Einblendanimation für das Dashboard beim App-Start

### Changed

- Releases enthalten nur noch die Änderungen ihrer jeweiligen Version


## [1.1.3] - 2026-09-01

### Changed

- Update-Popup an das Dashboard-Design angepasst
- Release-Changelog wird im Update-Popup angezeigt

## [1.1.0] - 2026-09-01

### Added

- Anpassbares Dashboard mit verschiebbaren Einstellungskacheln
- Bearbeitungsmodus per Fernbedienung mit explizitem Starten und Speichern einer Verschiebung
- Kachelgrößen `1x2` und `2x2`
- Theme-Auswahl mit Graphit-, Ozean- und Hell-Design
- Schwebendes Dashboard über dem Android-TV-Launcher
- Root-Prüfung und Shell-Fehlererkennung
- GitHub-Release-Prüfung, Update-Popup und Root-basierte OTA-Installation
- Zusätzliche Kachel zum Verwalten von Apps

### Changed

- Fernbedienungsfokus und Textkontrast für aktive Kacheln verbessert
- Deutsch- und Englisch-Ressourcen erweitert

## [1.0.0]

### Added

- Erste Version des FireOS8 Settings Hub
- Direkter Root-Zugriff auf die Fire-OS-8-Systemeinstellungen
- Kacheln für Anzeige und Ton, Netzwerk, Apps, Controller, Geräteinformationen, Konto und Barrierefreiheit
