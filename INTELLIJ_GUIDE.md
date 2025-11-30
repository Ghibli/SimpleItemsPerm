# 🚀 Guida Rapida - Importazione in IntelliJ IDEA

## 📥 Importare il Progetto

1. Apri **IntelliJ IDEA**
2. Clicca su **File** → **Open**
3. Seleziona la cartella `SimpleItemsPerms`
4. IntelliJ riconoscerà automaticamente il progetto Maven
5. Aspetta che IntelliJ scarichi tutte le dipendenze (controlla la barra in basso)

## ⚙️ Configurazione Maven

IntelliJ dovrebbe configurare Maven automaticamente. Se non succede:

1. Apri il pannello **Maven** (a destra)
2. Clicca sull'icona di refresh (reload) 🔄
3. Maven scaricherà tutte le dipendenze

## 🔨 Compilare il Plugin

### Da IntelliJ:
1. Apri il pannello **Maven** (View → Tool Windows → Maven)
2. Espandi **SimpleItemsPerms** → **Lifecycle**
3. Doppio click su **package**
4. Il file `.jar` sarà in `target/SimpleItemsPerms-1.0.0.jar`

### Da Terminale:
```bash
mvn clean package
```

## 📁 Struttura del Progetto

```
SimpleItemsPerms/
├── src/main/java/it/alessiogta/simpleItemsPerms/
│   ├── SimpleItemsPerms.java          # Classe principale
│   ├── commands/
│   │   ├── SipCommand.java            # Gestore comandi
│   │   └── SipTabCompleter.java       # Tab completer
│   ├── gui/
│   │   └── PermissionGUI.java         # GUI per assegnare permessi
│   ├── listeners/
│   │   ├── ArmorEquipListener.java    # Blocco equipaggiamento
│   │   ├── BlockBreakListener.java    # Blocco rottura blocchi
│   │   ├── BlockPlaceListener.java    # Blocco piazzamento
│   │   ├── EntityDamageListener.java  # Blocco attacco
│   │   ├── InventoryMoveListener.java # Blocco spostamento
│   │   ├── ItemConsumeListener.java   # Blocco consumo
│   │   ├── ItemDropListener.java      # Blocco drop
│   │   ├── ItemPickupListener.java    # Blocco pickup
│   │   ├── ItemUseListener.java       # Blocco uso
│   │   └── ProjectileShootListener.java # Blocco arco
│   └── utils/
│       ├── ColorUtil.java             # Gestione colori
│       ├── ConfigManager.java         # Gestione config.yml
│       ├── ItemPermissionUtil.java    # Utility permessi items
│       └── MessageManager.java        # Gestione messaggi
├── src/main/resources/
│   ├── plugin.yml                     # Configurazione plugin
│   ├── config.yml                     # Configurazione utente
│   └── messages.yml                   # Messaggi personalizzabili
├── pom.xml                            # Configurazione Maven
├── README.md                          # Documentazione
└── .gitignore                         # File da ignorare in Git
```

## 🧪 Testing

Per testare il plugin:

1. Compila il progetto (`mvn clean package`)
2. Copia `target/SimpleItemsPerms-1.0.0.jar` nella cartella `plugins/` del tuo server di test
3. Avvia il server
4. Controlla la console per eventuali errori
5. Testa i comandi in-game!

## 🔍 Debug

Per abilitare il debug:

1. Apri `plugins/SimpleItemsPerms/config.yml`
2. Cambia `debug: false` in `debug: true`
3. Esegui `/sip reload`
4. I messaggi di debug appariranno nella console

## 📝 Note Importanti

- **Java 21** è richiesto (configurato nel `pom.xml`)
- Compatibile con **Spigot/Paper 1.21.3+**
- Il plugin usa **PersistentDataContainer** per salvare i dati negli items
- Compatibile con qualsiasi plugin di permessi (LuckPerms, PermissionsEx, etc.)

## 🆘 Risoluzione Problemi

### Maven non scarica le dipendenze
- Controlla la tua connessione internet
- Verifica che Maven sia installato correttamente
- Prova: `mvn clean install -U`

### Errori di compilazione
- Assicurati di usare Java 21
- Verifica che il `pom.xml` non sia stato modificato
- Prova a invalidare la cache: File → Invalidate Caches → Invalidate and Restart

### Il plugin non si carica sul server
- Controlla la versione di Spigot/Paper (minimo 1.21.3)
- Verifica che Java 21 sia installato sul server
- Controlla la console per errori specifici

## 🎨 Personalizzazione

Puoi personalizzare:
- **Messaggi**: Modifica `messages.yml`
- **Configurazioni**: Modifica `config.yml`
- **Codice**: Modifica i file Java e ricompila

## 🚀 Prossimi Passi

1. Importa il progetto in IntelliJ
2. Esplora il codice
3. Compila e testa sul tuo server
4. Personalizza a tuo piacimento!

---

**Buon coding! 💻**

*Developed with ❤️ by AlessioGTAII*
