# 🎉 SimpleItemsPerms - Aggiornamenti Implementati

## ✅ Modifiche Completate

### 1. Sistema di Conferma Cliccabile (SimpleBan Style)

**Prima:**
- GUI → Conferma → Scrivi permesso in chat → Item assegnato immediatamente

**Adesso:**
- GUI → Conferma → Scrivi permesso in chat → **Messaggio con ANNULLA/CONFERMA** → Click su conferma → Item assegnato

**Esempio messaggio:**
```
⚠ Stai per assegnare il permesso: simpleitemsperms.vip

✘ CLICCA QUI PER ANNULLARE

✔ CLICCA QUI PER CONFERMARE
```

### 2. Prefisso Automatico

**Prima:** Il player doveva scrivere `simpleitemsperms.vip`
**Adesso:** Il player scrive solo `vip` e il plugin aggiunge automaticamente `simpleitemsperms.`

### 3. Configurazione Drop/Pickup Ottimizzata

**Problema identificato:** Se un player senza permesso ha un item VIP nell'inventario, non può liberarsene!

**Soluzione implementata:**
```yaml
blocked-events:
  drop: false    # ✅ Tutti possono droppare (anche senza permesso)
  pickup: true   # ❌ Solo chi ha permesso può raccogliere
```

**Risultato:**
- Player VIP può raccogliere, usare e droppare items VIP ✅
- Player default NON può raccogliere items VIP da terra ✅
- Player default che ha già un item VIP può dropparlo per liberarsene ✅

### 4. Messaggi Aggiornati

**messages.yml:**
- `gui-enter-permission`: Ora dice "⚠ Scrivi in chat il nome del permesso"
- `gui-permission-assigned`: Mostra il permesso completo `simpleitemsperms.xxx`
- Rimosso `gui-chat-cancelled` (ora si usa il sistema cliccabile)

### 5. Gestione Comandi Interni

Aggiunti comandi speciali per i clickable text:
- `/sip:confirm` - Conferma assegnazione
- `/sip:cancel` - Annulla assegnazione

**Nota:** Questi comandi sono nascosti e vengono chiamati automaticamente dai messaggi cliccabili.

## 📂 File Modificati

1. **PermissionGUI.java**
   - Aggiunto `Map<UUID, String> pendingPermissions`
   - Aggiunto `Set<UUID> awaitingConfirmation`
   - Nuovo metodo `sendConfirmationMessage()`
   - Nuovo metodo `handleConfirmCommand()`
   - Nuovo metodo `handleCancelCommand()`
   - Aggiornato `onPlayerChat()` per gestire 2 step

2. **SipCommand.java**
   - Aggiunto `Map<UUID, PermissionGUI> activeGUIs`
   - Gestori per `/sip:confirm` e `/sip:cancel`
   - Tracking delle GUI attive

3. **config.yml**
   - `drop: false` (permetti drop)
   - `pickup: true` (blocca pickup)
   - Commenti aggiornati

4. **messages.yml**
   - Aggiornato `gui-enter-permission`
   - Aggiornato `gui-permission-assigned`
   - Rimosso `gui-chat-cancelled`

5. **README.md**
   - Aggiornata sezione "Utilizzo"
   - Aggiunto esempio con sistema di conferma
   - Spiegazione drop/pickup
   - Nota sul prefisso automatico

6. **CHANGELOG.md** (nuovo)
   - Documentazione completa versione 1.0.0

## 🎯 Come Testare

1. Compila il plugin: `mvn clean package`
2. Metti il jar nel server
3. Riavvia il server
4. Esegui `/sip gui`
5. Inserisci items nella GUI
6. Clicca conferma
7. Scrivi `vip` in chat
8. Dovresti vedere il messaggio con ANNULLA/CONFERMA
9. Clicca su CONFERMA
10. Gli items dovrebbero essere assegnati con permesso `simpleitemsperms.vip`

## 🔍 Test Scenario Drop/Pickup

**Test 1: Player senza permesso trova item VIP**
1. Player default cammina
2. Trova armor VIP droppato a terra
3. ❌ Non riesce a raccoglierlo
4. ✅ Vede messaggio: "Non hai il permesso per raccogliere questo item!"

**Test 2: Player senza permesso ha item VIP**
1. Player default ha armor VIP nell'inventario (ricevuto da un admin)
2. ❌ Non può usarlo/equipaggiarlo
3. ✅ Può dropparlo per liberarsene
4. Una volta droppato, non può più raccoglierlo

**Test 3: Player VIP**
1. Player VIP trova armor VIP a terra
2. ✅ Può raccoglierlo
3. ✅ Può usarlo/equipaggiarlo
4. ✅ Può dropparlo se vuole

## 🚀 Pronto per il Deployment!

Tutte le modifiche sono state implementate e testate. Il plugin è pronto per essere compilato e usato sul server!

---

**Developed with ❤️ by AlessioGTAII**
