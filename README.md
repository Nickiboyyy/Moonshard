# Moonshard Plugin

Ein umfassendes Shard-System für deinen Minecraft-Server, das eine eigene Währung, ein Wirtschaftssystem, Glücksspiel, tägliche Belohnungen, Admin-Tools und vieles mehr bietet.

## Inhaltsverzeichnis
- [Features](#features)
- [Befehle](#befehle)
- [Permissions](#permissions)
- [Konfiguration (config.yml)](#konfiguration-configyml)
- [PlaceholderAPI](#placeholderapi)
- [Installation](#installation)
- [Autor](#autor)

## Features
*   **Eigene Währung:** Implementiert ein "Shard"-System als In-Game-Währung mit anpassbarem Namen und Hex-Farben.
*   **GUI-Menüs:** Interaktive Menüs für Shard-Übersicht, Leaderboard und Shop.
*   **Shard-Ökonomie:**
    *   **Pay-System:** Spieler können Shards an andere Spieler senden.
    *   **Gamble (Coinflip):** Spieler können Shards auf "Schwarz" oder "Weiß" setzen, mit konfigurierbaren Einsätzen und Steuern.
    *   **Tägliche Belohnungen:** Spieler können einmal pro konfigurierbarem Zeitraum Shards abholen.
    *   **Passives Einkommen:** Spieler mit der entsprechenden Permission erhalten regelmäßig Shards.
*   **Leaderboard:** Zeigt die Top-Spieler mit den meisten Shards an, sowohl im Chat als auch in einem GUI.
*   **Shop-System:** Ein grundlegendes Shop-Menü, das über die `config.yml` erweitert werden kann. Admin können Items direkt aus der Hand hinzufügen.
*   **Admin-Tools:** Umfassende Befehle zur Verwaltung von Shards, zum Zurücksetzen von Guthaben und zur Shop-Konfiguration.
*   **Log-System:** Alle wichtigen Shard-Transaktionen und Admin-Aktionen werden pro Spieler in Log-Dateien gespeichert und können im Spiel eingesehen werden.
*   **PlaceholderAPI-Integration:** Bietet Platzhalter für die Anzeige von Shards in anderen Plugins.
*   **Anpassbare Nachrichten:** Alle Plugin-Nachrichten sind in der `config.yml` konfigurierbar und unterstützen Farbcodes sowie Hex-Farben.
*   **Tab-Completion:** Intelligente Autovervollständigung für alle Befehle, die auch Permissions berücksichtigt.
*   **Klickbarer Info-Link:** Im `/shard info` Befehl kann ein konfigurierbarer Link hinterlegt werden, der im Browser geöffnet werden kann.

## Befehle

| Befehl                       | Beschreibung                                                              | Permissions              |
| :--------------------------- | :------------------------------------------------------------------------ | :----------------------- |
| `/shard`                     | Öffnet das Hauptmenü für Shards.                                          | `moonshard.use`          |
| `/shard shop`                | Öffnet das Shard-Shop-Menü.                                               | `moonshard.shop`         |
| `/shard gamble <amount> <black|white>` | Setzt Shards auf Schwarz oder Weiß.                                       | `moonshard.gamble`       |
| `/shard pay <player> <amount>` | Sendet Shards an einen anderen Spieler.                                   | `moonshard.pay`          |
| `/shard baltop`              | Zeigt die Top-Spieler mit den meisten Shards an (Alias: `/shard leaderboard`). | `moonshard.leaderboard`  |
| `/shard daily`               | Fordert die tägliche Shard-Belohnung an.                                  | `moonshard.daily`        |
| `/shard info`                | Zeigt Informationen über das Plugin an.                                   | `moonshard.info`         |
| `/shardadmin set <player> <amount>` | Setzt die Shards eines Spielers auf einen bestimmten Betrag.              | `moonshard.admin`        |
| `/shardadmin add <player> <amount>` | Fügt einem Spieler Shards hinzu.                                          | `moonshard.admin`        |
| `/shardadmin remove <player> <amount>` | Entfernt Shards von einem Spieler.                                        | `moonshard.admin`        |
| `/shardadmin reset <allonline|all>` | Setzt Shards zurück. `allonline` für Online-Spieler, `all` für alle Spieler. | `moonshard.admin`        |
| `/shardadmin addshopitem <id> <price>` | Fügt das Item in der Hand zum Shop hinzu.                                 | `moonshard.admin`        |
| `/shardadmin logs <player> [page]` | Zeigt die Shard-Transaktionslogs eines Spielers an.                       | `moonshard.admin`        |

## Permissions

| Permission               | Beschreibung                                                | Standard |
| :----------------------- | :---------------------------------------------------------- | :------- |
| `moonshard.admin`        | Ermöglicht den Zugriff auf alle Admin-Befehle.              | `op`     |
| `moonshard.use`          | Basis-Zugriff auf den `/shard`-Befehl.                      | `true`   |
| `moonshard.shop`         | Ermöglicht den Zugriff auf `/shard shop`.                   | `true`   |
| `moonshard.daily`        | Ermöglicht den Zugriff auf `/shard daily`.                  | `true`   |
| `moonshard.gamble`       | Ermöglicht den Zugriff auf `/shard gamble`.                 | `true`   |
| `moonshard.pay`          | Ermöglicht den Zugriff auf `/shard pay`.                    | `true`   |
| `moonshard.leaderboard`  | Ermöglicht den Zugriff auf `/shard leaderboard`.            | `true`   |
| `moonshard.info`         | Ermöglicht den Zugriff auf `/shard info`.                   | `true`   |
| `moonshard.income`       | Ermöglicht dem Spieler, passives Shard-Einkommen zu erhalten. | `true`   |

## Konfiguration (config.yml)

Die `config.yml` befindet sich im Ordner `plugins/Moonshard/` und ermöglicht die Anpassung fast aller Aspekte des Plugins.
