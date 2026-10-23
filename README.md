# Weinregal

Eine Android-App (Kotlin + Jetpack Compose) für die eigene Weinsammlung:
Foto des Regals mit nummerierten Pfeilen auf alle 18 Fächer, ein durchsuchbares
Weinarchiv mit Rebsorten-Wissen und ein Bestand, in dem Flaschen per Fingertipp
entnommen, per QR-Code ergänzt und als CSV gesichert werden.

> **Status:** Version 2.2 – vollständig neu aufgebaut (Datenmodell, Regal-Tab,
> Archiv, Bestand, CSV-Sicherung, Preisabfrage, Tests).

---

## Inhalt

1. [Die drei Tabs](#die-drei-tabs)
2. [CSV-Sicherung](#csv-sicherung)
3. [Preisabfrage und Produktdaten](#preisabfrage-und-produktdaten)
4. [Rebsorten-Wissen](#rebsorten-wissen)
5. [Datenhaltung und Migration](#datenhaltung-und-migration)
6. [Projektstruktur](#projektstruktur)
7. [Bauen und testen](#bauen-und-testen)
8. [App aufs Smartphone bringen](#app-aufs-smartphone-bringen)
9. [Release über GitHub Actions](#release-über-github-actions)
10. [Bekannte Grenzen](#bekannte-grenzen)
11. [Datenschutz](#datenschutz)
12. [Änderungen in 2.0](#änderungen-in-20)

---

## Die drei Tabs

### 1. Weinregal

* Das Foto des Regals (`app/src/main/res/drawable/rack_photo.jpg`) bildet die
  Grundlage.
* Darüber liegen **18 nummerierte Pfeil-Marker** – 6 Spalten × 3 Reihen, von
  oben links nach unten rechts durchnummeriert.
* Belegte Fächer sind gold umrandet, freie Fächer dezent weiß.
* Ein Tipp auf ein Fach öffnet den dort stehenden Wein. Ist das Fach frei,
  öffnet sich direkt das Formular für einen neuen Wein mit diesem Fach als
  Lagerort.
* Unter dem Foto zeigt eine Liste „Fach N – Wein“, welcher Wein wo steht.
  Weine mit mehreren Flaschen werden mit `n×` gekennzeichnet; die Flaschen, die
  nicht im sichtbaren Fach stehen, zählen als „außerhalb des Regals“.
* **Ein Fach = eine Flasche.** Zusätzliche Flaschen desselben Weins stehen im
  Kühlschrank, in der Speisekammer oder auf einem freien Lagerplatz.

### 2. Weinarchiv

* Bibliothek **aller** Weine – im Bestand und ehemals vorhandene.
* Status-Badge „im Bestand“ bzw. „nicht lagernd“.
* Suche über Name, Winzer, Rebsorte, Region und Lagerort.
* Filter: Alle / Im Bestand / Nicht lagernd.
* Jede Karte zeigt zusätzlich eine kurze Rebsorten-Info (Charakter, Aromen),
  Lagerort, Flaschenzahl, Preis und Bewertung.
* Detaildialog mit allen Angaben, Rebsorten-Wissen, Preisabfrage und Aktionen
  (entnehmen, hinzufügen, bearbeiten, löschen mit Rückfrage).

### 3. Weinbestand

* Kopfzeile mit Kennzahlen: Flaschen, Anzahl Weine, Gesamtwert.
* **Flasche entnehmen** (`−`) und **Flasche hinzufügen** (`+`) pro Wein.
  Bei 0 Flaschen wechselt der Status automatisch auf „nicht lagernd“.
* **Neu**: Wein manuell anlegen.
* **QR / Barcode**: Kamera-Scanner (ML Kit) für EAN-13, EAN-8, UPC, QR und
  Code-128. Bekannter Code → Wein öffnen. Unbekannter Code → Produktdaten
  laden oder Formular vorausgefüllt öffnen.
* **Etikett fotografieren**: OCR (ML Kit Text Recognition) liest Name, Winzer,
  Jahrgang und Rebsorte aus dem Foto und füllt das Formular vor.
* **CSV sichern / CSV laden** (siehe unten).
* Suche nach Wein, Winzer, Rebsorte oder Lagerort.
* Bearbeiten und Löschen (mit Bestätigungsdialog) direkt aus der Liste.

### Lagerorte

| Art | Bedeutung |
| --- | --- |
| `Regal Fach 1` … `Regal Fach 18` | sichtbares Fach im Foto, eine Flasche |
| `Kühlschrank` | gekühlter Wein |
| `Speisekammer` | Vorrat |
| freier Text | z. B. „Kellerregal links“, „Lagerplatz (freie Fläche)“ |

Im Formular wird der Lagerort über vier Chips gewählt. Für das Regal gibt es
eine Pfeil-Navigation (`←` / `→`), mit der Fach für Fach durchgeschaltet wird –
inklusive Anzeige „Fach 7 von 18“.

---

## CSV-Sicherung

**Export:** Bestand → *CSV sichern* → Speicherort im System-Dialog wählen.
Dateiname: `weinregal-sicherung-JJJJ-MM-TT.csv`.

**Import:** Bestand → *CSV laden* → Datei wählen → bestätigen.
Der Import **führt zusammen**: Weine mit gleicher ID werden aktualisiert, neue
Weine kommen hinzu. Es wird nichts gelöscht.

**Format**

* Semikolon als Trennzeichen, UTF-8 **mit BOM** (Excel zeigt Umlaute korrekt).
* Erste Zeile = Spaltennamen.
* Anführungszeichen werden verdoppelt (`""`), Felder mit `;`, `"` oder
  Zeilenumbruch werden in Anführungszeichen gesetzt.
* Preise werden mit Punkt exportiert (`12.50`), beim Import sind Punkt und
  Komma erlaubt (`12,50`).

| Spalte | Inhalt |
| --- | --- |
| `id` | eindeutige ID (Schlüssel für den Zusammenführungs-Import) |
| `name` | Weinname |
| `winery` | Winzer / Weingut |
| `year` | Jahrgang, `0` = ohne Jahrgang |
| `grapeVariety` | Rebsorte |
| `wineType` | Rotwein, Weißwein, Rosé, Schaumwein |
| `region` | Region & Herkunft |
| `price` | Flaschenpreis in Euro |
| `quantity` | Flaschen im Bestand |
| `storageLocation` | Lagerort (siehe Tabelle oben) |
| `status` | `IN_STOCK` / `OUT_OF_STOCK` |
| `servingTemperature` | Trinktemperatur |
| `foodPairing` | Speiseempfehlung |
| `notes` | Notizen & Geschmack |
| `qrCode` | QR-/Barcode |
| `addedDate` | Zeitstempel (Millis) |
| `rating` | 0–5 Sterne |

Der Import ist tolerant: fehlende Spalten werden mit Standardwerten gefüllt,
Zeilen ohne Namen werden gezählt und übersprungen, „nicht lagernd“ ohne
Flaschenzahl wird als 0 Flaschen gelesen.

---

## Preisabfrage und Produktdaten

Die App erfindet **keine** Preise mehr. Es gibt zwei echte Wege:

1. **Preis online suchen** (im Formular und im Detaildialog)
   * Abfrage der öffentlichen Such-Endpunkte von **Vinocentral** und
     **Weinpaket** (Shopify `search/suggest.json`).
   * Angezeigt werden Händler, Produkttitel und Preis; ein Tipp übernimmt den
     Preis in das Feld.
   * Suche läuft über mehrere Varianten: Name, Winzer + Name, Name + Jahrgang.
   * Antwortet keine Quelle oder gibt es keinen passenden Treffer, sagt die App
     das offen („Kein Online-Preis gefunden“) und bietet eine Websuche im
     Browser an.

2. **Produktdaten per Barcode** (Open Food Facts / Open Products Facts)
   * Name, Marke, Kategorie, Land und ggf. Rebsorte werden übernommen.
   * Preis und Jahrgang werden **nicht** erfunden; fehlt der Jahrgang, steht
     „o. J.“.

> Preisangaben sind Momentaufnahmen der Händler und können sich ändern.

---

## Rebsorten-Wissen

`model/GrapeLibrary.kt` enthält 22 Rebsorten mit Charakter, Aromen, Säure,
Tannin, Anbaugebieten, Trinktemperatur und Speiseempfehlung. Erkannt werden
exakte Namen, gängige Synonyme (z. B. Pinot Noir → Spätburgunder,
Shiraz → Syrah) und Teiltreffer („Riesling trocken“, „Cabernet Sauvignon &
Merlot“). Die Info erscheint im Archiv, im Detaildialog und beim Ausfüllen des
Formulars – vollständig offline.

Enthalten: Riesling, Chardonnay, Sauvignon Blanc, Weißburgunder,
Grauburgunder, Silvaner, Gewürztraminer, Grüner Veltliner, Müller-Thurgau,
Cabernet Sauvignon, Merlot, Spätburgunder, Syrah, Tempranillo, Garnacha,
Sangiovese, Nebbiolo, Primitivo, Malbec, Dornfelder, Lemberger, Trollinger.

---

## Datenhaltung und Migration

* Speicherung in `SharedPreferences` als JSON (kotlinx.serialization).
* Neue Felder: `quantity`, `status` wird aus der Flaschenzahl abgeleitet,
  `wineType`, `region`, `servingTemperature`, `foodPairing`, `rating`.
* Beim Laden werden alte Datensätze migriert: „nicht lagernd“ ohne
  Flaschenzahl bedeutet 0 Flaschen; Regal-Fächer werden aus dem alten
  Freitext-Lagerort gelesen.
* Beim ersten Start werden 10 Demo-Weine angelegt.

---

## Projektstruktur

```
app/src/main/java/com/example/weinregal_neu/
├── MainActivity.kt                 Tabs, Dialoge, CSV-Dateidialoge, Snackbars
├── data/
│   ├── CsvService.kt               Export/Import inkl. Quoting-Parser
│   ├── OpenFoodFactsService.kt     Barcode-Lookup (echte Produktdaten)
│   ├── PriceLookupService.kt       Preisabfrage bei mehreren Shops
│   └── WineStorage.kt              Persistenz, Migration, Demo-Daten
├── model/
│   ├── GrapeLibrary.kt             Rebsorten-Wissen
│   ├── RackSlots.kt                18 Fächer, Lagerort-Logik
│   └── Wine.kt                     Datenmodell
├── ui/screens/
│   ├── RackScreen.kt               Foto + 18 Pfeil-Marker + Belegungsliste
│   ├── ArchiveScreen.kt            Bibliothek, Suche, Filter
│   ├── InventoryScreen.kt          Bestand, Entnehmen/Hinzufügen, CSV
│   ├── AddEditWineDialog.kt        Formular inkl. Fach-Pfeilen, Preisabfrage
│   ├── WineDetailDialog.kt         Detailansicht + Aktionen
│   ├── RealQrScannerDialog.kt      CameraX + ML Kit Barcode
│   └── LabelPhotoScannerDialog.kt  Kamera + ML Kit OCR
└── viewmodel/WineViewModel.kt      CRUD, Flaschenlogik, CSV-Zusammenführung
```

Tests: `app/src/test/java/...` (JVM) mit JUnit 4.

---

## Bauen und testen

Voraussetzungen: Android Studio (JDK 17+), Android SDK 37.

```powershell
# lokale SDK-Pfade eintragen (nicht im Git)
"SdkDir=C:\Users\<name>\AppData\Local\Android\Sdk" > local.properties

# Unit-Tests
.\gradlew.bat testDebugUnitTest

# Debug-APK bauen
.\gradlew.bat assembleDebug
# → app\build\outputs\apk\debug\app-debug.apk
```

Abgedeckt durch Unit-Tests:

* CSV-Export/Import: Roundtrip, Quoting, Semikolon, Zeilenumbrüche, alte
  Dateien ohne Header, „nicht lagernd“, übersprungene Zeilen, Preisformate.
* Regal-Logik: 18 Fächer, Fachnummer-Parsing, Lagerort-Erkennung, Labels.
* Rebsorten: exakte Treffer, Synonyme, Umlaute/Groß-Kleinschreibung,
  Teiltreffer, unbekannte Sorten, Vollständigkeit der Daten.
* Weinmodell: Flaschenzahl, Statuswechsel, negatives Entnehmen, abgeleiteter
  Lagerort, „außerhalb des Regals“, Jahrgangsanzeige.

---

## App aufs Smartphone bringen

### Variante A – Desktop-Verknüpfung (empfohlen)

1. Smartphone per USB anschließen, **USB-Debugging** aktivieren, entsperren.
2. Einmalig `scripts\Desktop-Verknuepfung-erstellen.ps1` ausführen
   (Rechtsklick → *Mit PowerShell ausführen*).
3. Auf dem Desktop liegt danach **„Weinregal aufs Handy“**. Doppelklick baut die
   APK, installiert sie per `adb install -r` und startet die App.

Ohne Verknüpfung funktioniert auch ein Doppelklick auf
`Auf-Handy-installieren.cmd` im Projektordner.

### Variante B – APK direkt

* APK aus dem GitHub-Release herunterladen, auf dem Smartphone öffnen und die
  Installation aus unbekannten Quellen erlauben.
* Oder die gebaute Datei `app-debug.apk` per USB/Cloud aufs Gerät kopieren.

### Variante C – Android Studio

Projekt öffnen und auf ▶ *Run* klicken.

---

## Release über GitHub Actions

`.github/workflows/build.yml`:

* läuft bei Push auf `main`, bei Pull Requests, bei Tags `v*` und manuell,
* führt die Unit-Tests aus,
* baut die Debug-APK und lädt sie als Artefakt hoch,
* veröffentlicht bei einem Tag `v*` automatisch ein GitHub-Release mit APK.

Neue Version veröffentlichen:

```powershell
git tag v2.0
git push origin v2.0
```

---

## Bekannte Grenzen

* **Preisquellen:** Nur zwei Shops liefern zuverlässig ohne API-Schlüssel.
  Große Portale (idealo, eBay, Vivino) blocken automatisierte Anfragen; die App
  zeigt in dem Fall ehrlich „kein Preis gefunden“ und verlinkt die Websuche.
* **Barcode-Datenbank:** Open Food Facts ist eine offene Datenbank; nicht jeder
  Wein ist enthalten. Dann öffnet sich das leere Formular mit dem gescannten
  Code.
* **Etikett-OCR:** Die Erkennung ist ein Vorschlag, kein geprüftes Ergebnis –
  die Angaben werden vor dem Speichern im Formular angezeigt.
* **APK-Signatur:** Die Release-APK ist mit dem Debug-Schlüssel signiert und
  damit direkt installierbar, aber nicht Play-Store-tauglich. Für eine
  Store-Version müsste ein eigener Keystore hinterlegt werden.
* **Lagerfoto:** Die Marker liegen über einem festen 6×3-Raster mit 5,5 %
  Rand. Bei einem stark anders zugeschnittenen Foto lassen sich die Ränder in
  `RackScreen.kt` (`insetX`, `insetY`) anpassen.

---

## Datenschutz

* Keine Konten, keine Analyse, keine Werbung.
* Netzwerkzugriff nur, wenn eine Preisabfrage, ein Barcode-Lookup oder ein
  Browser-Link ausgelöst wird.
* Alle Wein-, Preis- und Lagerdaten bleiben lokal auf dem Gerät.
* Kamera wird nur für Barcode-Scan und Etikettfoto genutzt.

---

## Änderungen in 2.2

* Neues Wein-Logo (Flaschenglas in Weinrot).
* Preisabfrage um Weinfreunde erweitert, mehr Treffer werden geprüft.
* Bessere Trefferanzeige: Falls keine stark passenden Angebote gefunden
  werden, werden gefundene Angebote trotzdem als Vorschlag gezeigt.

## Änderungen in 2.1

* Preisvorschläge werden auf den gesuchten Wein geprüft (Name, Winzer, Rebsorte);
  fremde Weine tauchen nicht mehr als Preisvorschlag auf.
* Jeder Preisvorschlag lässt sich direkt im Shop im Browser öffnen.
* Websuche-Link ohne irreführendes „Jahrgang 0“.
* Beim Anlegen eines Weins in ein belegtes Fach wird der bisherige Wein
  verschoben (statt den neuen Wein aus dem Fach zu nehmen).
* Doppelt belegte Fächer aus alten Daten werden beim Start automatisch
  aufgelöst, sodass keine Flasche im Regal unsichtbar bleibt.
* Klarere Beschriftung im Bestand: „Entnehmen“ und „Hinzufügen“.

## Änderungen in 2.0

* Neuer Regal-Tab: Foto als Vorlage, 18 nummerierte Pfeil-Marker, Belegungsliste.
* Datenmodell mit Flaschenzahl, Lagerort-Typ, Weinart, Region, Trinktemperatur,
  Speiseempfehlung und Bewertung; Migration alter Daten.
* Archiv als vollständige Bibliothek mit Suche, Filtern und Rebsorten-Info.
* Bestand mit Entnehmen/Hinzufügen, Kennzahlen und Suche.
* CSV-Export/Import über den System-Dateidialog (sicher, ohne
  Speicherberechtigungen), Zusammenführung statt Überschreiben.
* Echte Preisabfrage bei mehreren Shops statt erfundener Schätzwerte.
* Barcode- und OCR-Import ohne erfundene Preise/Jahrgänge.
* Löschen nur mit Bestätigung; Validierung im Formular.
* 30+ Unit-Tests, GitHub-Actions-Build, Desktop-Installationsskript.
