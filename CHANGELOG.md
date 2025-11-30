# 📋 Changelog

Tutte le modifiche notevoli a questo progetto saranno documentate in questo file.

## [1.1.1] - 2025-11-23

### 🐛 Risolto - HOTFIX CRITICO
- **FIXED: Lore dinamico non funzionante** - Il lore "Non hai i permessi" ora si nasconde correttamente per i player che hanno il permesso
- Migliorato sistema di confronto del lore: ora usa confronto basato su testo puro (senza color codes) invece di confronto esatto
- Aggiunto metodo `stripColorCodes()` per rimuovere tutti i color codes (legacy, hex, e nativi Minecraft) prima del confronto
- Risolto problema dove cambiare il messaggio nel file lingua causava il mancato riconoscimento del lore da rimuovere
- Il sistema ora funziona anche con items creati con versioni precedenti del plugin

### 📝 Note Tecniche
- Il confronto del lore ora usa `stripColorCodes()` che rimuove:
  - Codici legacy `&[0-9a-fk-or]`
  - Codici hex `&#RRGGBB`
  - Codici tradotti `§[0-9a-fk-or]`
  - Codici hex nativi Minecraft `§x§R§R§G§G§B§B`
- Questo garantisce un confronto affidabile indipendentemente dalla formattazione

### ⚠️ Istruzioni Aggiornamento
1. Sostituisci il JAR sul server
2. Esegui `/sip reload`
3. **Per items esistenti:** Rilogga o apri l'inventario per applicare l'aggiornamento del lore
4. **Opzionale:** Ricrea gli items vecchi con `/sip gui` per garantire la massima compatibilità

---

## [1.0.0] - 2024-11-21

### ✨ Aggiunto
- Sistema completo di permessi per items vanilla
- GUI interattiva per assegnare permessi multipli
- Sistema di conferma con messaggi cliccabili (ANNULLA / CONFERMA)
- **Sistema di token univoci per sicurezza massima**
- Prefisso automatico `simpleitemsperms.` ai permessi
- 10 listener per bloccare tutte le azioni (uso, equipaggiamento, consumo, drop, pickup, movimento inventario, rottura blocchi, piazzamento, attacco, arco)
- Comandi completi: `/sip gui`, `/sip give`, `/sip remove`, `/sip check`, `/sip info`, `/sip reload`
- Tab completer con suggerimenti intelligenti
- Supporto PersistentDataContainer per salvare permessi negli items
- Supporto colori HEX nei messaggi
- Lore personalizzabile "Non hai i permessi per usarlo"
- Sistema di suoni configurabile quando azioni vengono bloccate
- Opzione per nascondere il lore a chi ha il permesso
- File di configurazione completi (`config.yml` e `messages.yml`)
- Sistema di debug
- Compatibilità con qualsiasi plugin di permessi (LuckPerms, PermissionsEx, etc.)
- Task automatico di pulizia token scaduti (ogni 5 minuti)

### 🔧 Configurazione Default
- **Drop:** Permesso anche senza permesso (per evitare items bloccati nell'inventario)
- **Pickup:** Bloccato per chi non ha permesso (solo chi ha permesso può raccogliere)
- Tutti gli altri eventi bloccati per chi non ha permesso

### 📚 Documentazione
- README.md completo con esempi
- INTELLIJ_GUIDE.md per importazione in IntelliJ IDEA
- LICENSE (MIT)
- Commenti dettagliati nel codice

### 💡 Caratteristiche Speciali
- Sistema di conferma a 2 step per assegnare permessi
- Messaggi cliccabili in chat (SimpleBan style)
- **Token univoci con scadenza temporale (60 secondi)**
- **Validazione player-specific per ogni token**
- **Protezione contro riuso di comandi vecchi**
- Validazione input permessi
- Restituzione automatica items in caso di errore/annullamento
- ASCII art nel log del server

### 👨‍💻 Crediti
- Developed with ❤️ by **AlessioGTAII**
- Per **Simple Survival**
- Versione: 1.0.0
- Java: 21
- Minecraft: 1.21.3+

---

## Legenda

- ✨ **Aggiunto**: Nuove funzionalità
- 🔧 **Modificato**: Cambiamenti a funzionalità esistenti
- 🐛 **Risolto**: Bug fix
- 🗑️ **Rimosso**: Funzionalità rimosse
- 🔒 **Sicurezza**: Patch di sicurezza
- 📚 **Documentazione**: Modifiche alla documentazione
