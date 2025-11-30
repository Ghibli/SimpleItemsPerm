# SimpleItemsPerms

![Version](https://img.shields.io/badge/version-1.1.1-blue)
![Minecraft](https://img.shields.io/badge/minecraft-1.21.3+-green)
![License](https://img.shields.io/badge/license-MIT-orange)

**Sistema di permessi per items specifici** - Perfetto per kit VIP, MVIP ed Elite!

Developed with ❤️ by **AlessioGTAII** for Simple Survival

---

## 📋 Descrizione

SimpleItemsPerms è un plugin Spigot/Paper che permette di assegnare permessi specifici agli items, anche vanilla. Ideale per server con sistemi di rank dove vuoi che solo i giocatori con determinati permessi possano utilizzare certi items dai kit.

## ✨ Caratteristiche

- ✅ Funziona con qualsiasi item vanilla (non servono custom items)
- ✅ Utilizza PersistentDataContainer (i permessi rimangono anche dopo il riavvio)
- ✅ GUI intuitiva per assegnare permessi a più items contemporaneamente
- ✅ Lore personalizzabile che mostra chi non ha i permessi
- ✅ Blocca tutte le azioni: uso, equipaggiamento, consumo, drop, etc.
- ✅ Compatibile con qualsiasi plugin di permessi (LuckPerms, PermissionsEx, etc.)
- ✅ Suoni e messaggi completamente personalizzabili
- ✅ Supporto per colori hex nei messaggi
- ✅ Sistema di debug integrato

## 📦 Installazione

1. Scarica l'ultimo file `.jar` dalla sezione [Releases](https://github.com/tuousername/SimpleItemsPerms/releases)
2. Inserisci il file nella cartella `plugins` del tuo server
3. Riavvia il server
4. Configura i file `config.yml` e `messages.yml` a tuo piacimento
5. Usa `/sip reload` per ricaricare le configurazioni

## 🎮 Comandi

| Comando | Descrizione | Permesso |
|---------|-------------|----------|
| `/sip gui` | Apri la GUI per assegnare permessi agli items | `simpleitemsperms.admin` |
| `/sip give <player> <permesso>` | Dai l'item in mano con un permesso specifico | `simpleitemsperms.admin` |
| `/sip remove` | Rimuovi il permesso dall'item in mano | `simpleitemsperms.admin` |
| `/sip check` | Controlla che permesso ha l'item in mano | `simpleitemsperms.admin` |
| `/sip info` | Mostra informazioni sul plugin | Nessuno |
| `/sip reload` | Ricarica le configurazioni | `simpleitemsperms.admin` |

**Alias:** `/simpleitemsperms`, `/itemsperms`

## 🔧 Utilizzo

### Assegnare permessi tramite GUI

1. Esegui `/sip gui`
2. Inserisci gli items nella GUI
3. Clicca sul pulsante di conferma (calcestruzzo verde)
4. Scrivi in chat il nome del permesso:
   - **Modo semplice:** Scrivi solo `vip` → Diventa `simpleitemsperms.vip`
   - **Modo completo:** Scrivi `simpleitemsperms.elite` → Rimane `simpleitemsperms.elite`
   - **Permessi custom:** Scrivi `custom.test` → Diventa `simpleitemsperms.custom.test`
5. Clicca sul messaggio **"CLICCA QUI PER CONFERMARE"** in giallo
6. Gli items ti verranno restituiti con il permesso assegnato!

**Nota:** Il plugin aggiunge automaticamente il prefisso `simpleitemsperms.` **solo se non è già presente**. Questo permette sia l'uso semplice (`vip`) che quello completo (`simpleitemsperms.vip`).

### Assegnare permessi direttamente

1. Tieni l'item in mano
2. Esegui `/sip give <player> simpleitemsperms.vip`
3. Il giocatore riceverà l'item con il permesso richiesto

### Dare i permessi ai giocatori

Usa il tuo plugin di permessi preferito (es: LuckPerms):
```
/lp user AlessioGTAII permission set simpleitemsperms.vip true
/lp group vip permission set simpleitemsperms.vip true
```

## ⚙️ Configurazione

### config.yml

```yaml
# Prefix dei messaggi
prefix: "&8[&6SimpleItemsPerms&8]&r"

# Configurazione GUI
gui:
  title: "&6Assegna Permesso agli Items"
  size: 54

# Configurazione Lore
lore:
  no-permission-text: "&c✘ Non hai i permessi per usarlo"
  hide-for-permitted-players: true  # Nascondi lore a chi ha il permesso
  position: "end"  # Posizione: "start" o "end"

# Eventi da bloccare
blocked-events:
  use-item: true
  equip-armor: true
  consume: true
  drop: false      # ✅ Permetti drop (anche senza permesso - per liberarsene!)
  pickup: true     # ❌ Blocca pickup (solo chi ha permesso può raccogliere)
  move-inventory: true
  break-block: true
  place-block: true
  attack: true
  shoot-bow: true

# Suono quando un'azione viene bloccata
block-sound:
  enabled: true
  sound: "ENTITY_VILLAGER_NO"
  volume: 1.0
  pitch: 1.0
```

### messages.yml

Tutti i messaggi sono completamente personalizzabili! Supporto per:
- Colori legacy (`&a`, `&c`, etc.)
- Colori hex (`&#FF5733`)
- Placeholder: `{prefix}`, `{player}`, `{permission}`, `{item}`, `{amount}`

## 🎯 Esempio d'uso

**Scenario:** Vuoi che solo i VIP possano usare l'armor di netherite dal kit VIP.

1. Crea l'armor di netherite con i tuoi incantamenti
2. Esegui `/sip gui`
3. Inserisci tutti i pezzi dell'armor nella GUI
4. Conferma e scrivi: `vip` (il plugin aggiungerà automaticamente `simpleitemsperms.`)
5. Clicca su **"CLICCA QUI PER CONFERMARE"**
6. Dai il permesso `simpleitemsperms.vip` al gruppo VIP con LuckPerms
7. Inserisci gli items nel kit VIP

**Risultato:** 
- ✅ Solo i giocatori VIP potranno equipaggiare e usare quell'armor!
- ✅ I player senza permesso NON potranno raccogliere l'armor da terra
- ✅ Se un player senza permesso ha già l'armor, può dropparlo per liberarsene

**Nota importante:** La configurazione di default permette il **drop** anche senza permesso (altrimenti l'item rimarrebbe bloccato nell'inventario per sempre!) ma blocca il **pickup** per evitare che player non autorizzati raccolgano items VIP.

## 🛠️ Compilazione

Requisiti:
- Java 21
- Maven 3.6+
- Spigot API 1.21.3+

Comandi:
```bash
git clone https://github.com/tuousername/SimpleItemsPerms.git
cd SimpleItemsPerms
mvn clean package
```

Il file `.jar` sarà generato in `target/SimpleItemsPerms-1.0.0.jar`

## 🔒 Sicurezza

### Sistema di Token Univoci

Il plugin utilizza un sistema di token univoci per i comandi cliccabili, garantendo massima sicurezza:

- **Token casuali**: Ogni messaggio cliccabile genera un token unico (es: `a8f3x92b`)
- **Scadenza temporale**: I token scadono dopo 60 secondi
- **Validazione player**: Un token può essere usato solo dal player che l'ha generato
- **Uso singolo**: Dopo l'uso, il token viene invalidato
- **Pulizia automatica**: I token scaduti vengono rimossi ogni 5 minuti

**Esempio di comandi generati:**
```
/sip confirm-a8f3x92b
/sip cancel-b4c7d91e
/sip cancelinput-f2e9a5b3
```

Se qualcuno prova ad usare un token scaduto o invalido, riceve: *"Il link è scaduto o non valido. Riprova."*

Questo previene:
- ✅ Uso di comandi fuori contesto
- ✅ Riutilizzo di link vecchi
- ✅ Esecuzione da parte di altri player

## 🐛 Bug e Suggerimenti

Hai trovato un bug o hai un suggerimento? Apri una [Issue](https://github.com/tuousername/SimpleItemsPerms/issues)!

## 📝 Licenza

Questo progetto è rilasciato sotto licenza MIT. Vedi il file [LICENSE](LICENSE) per maggiori dettagli.

## 💖 Crediti

**Sviluppatore:** AlessioGTAII  
**Server:** Simple Survival  
**Versione:** 1.0.0

---

<div align="center">
  <p>Developed with ❤️ by AlessioGTAII</p>
  <p>Se ti piace il plugin, lascia una ⭐ su GitHub!</p>
</div>
