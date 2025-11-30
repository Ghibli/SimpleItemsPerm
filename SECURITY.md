# 🔒 Sistema di Sicurezza - SimpleItemsPerms

## 🎯 Problema Risolto

Nei plugin con comandi cliccabili, c'è sempre il rischio che:
1. Un player scopra il comando e lo usi manualmente fuori contesto
2. Un player copi un link vecchio e lo riusi
3. Un player usi un comando destinato ad un altro player

**SimpleItemsPerms risolve tutti questi problemi con un sistema di token univoci!**

---

## 🔐 Sistema di Token Univoci

### Come Funziona

Quando un player interagisce con la GUI e deve confermare un'azione, il plugin:

1. **Genera un token casuale univoco** (es: `a8f3x92b`)
2. **Salva il token** con queste informazioni:
   - UUID del player
   - Timestamp di creazione
   - Tipo di operazione ("input" o "confirm")
3. **Crea il comando cliccabile** con il token: `/sip confirm-a8f3x92b`

### Esempio Pratico

**Step 1:** Player apre GUI e conferma
```
Token generato: f7d3e9a2
Salvato: {
  playerUUID: 123e4567-e89b-12d3-a456-426614174000
  createdAt: 1700000000000
  type: "input"
}
Comando: /sip cancelinput-f7d3e9a2
```

**Step 2:** Player clicca sul messaggio
```
1. Plugin riceve: /sip cancelinput-f7d3e9a2
2. Estrae token: f7d3e9a2
3. Valida token:
   ✓ Esiste?
   ✓ È scaduto? (60 secondi)
   ✓ Appartiene a questo player?
   ✓ È del tipo corretto?
4. Se tutto OK → Esegue azione
5. Consuma il token (lo rimuove)
```

---

## ✅ Validazioni Implementate

### 1. Validazione Esistenza
```java
TokenData data = activeTokens.get(token);
if (data == null) return false;
```
Se il token non esiste, l'azione viene bloccata.

### 2. Validazione Scadenza
```java
if (System.currentTimeMillis() - createdAt > 60000) {
    activeTokens.remove(token);
    return false;
}
```
I token scadono dopo **60 secondi**. Perfetto per prevenire link vecchi.

### 3. Validazione Player
```java
if (!data.playerUUID.equals(playerUUID)) return false;
```
Un token può essere usato **solo dal player che l'ha generato**.

### 4. Validazione Tipo
```java
if (!data.type.equals(expectedType)) return false;
```
Un token "input" non può essere usato per "confirm" e viceversa.

### 5. Uso Singolo
```java
consumeToken(token); // Rimuove il token dopo l'uso
```
Ogni token può essere usato **una sola volta**.

---

## 🧹 Pulizia Automatica

Un task automatico gira ogni **5 minuti** per rimuovere token scaduti:

```java
getServer().getScheduler().runTaskTimer(this, () -> {
    PermissionGUI.cleanupExpiredTokens();
}, 6000L, 6000L);
```

Questo previene accumulo di memoria e mantiene il sistema pulito.

---

## 🎭 Scenari di Test

### ✅ Scenario 1: Uso Normale
1. Player apre GUI
2. Conferma items
3. Riceve messaggio con token `abc123`
4. Clicca entro 60 secondi
5. **Risultato:** Azione eseguita con successo ✓

### ❌ Scenario 2: Token Scaduto
1. Player apre GUI
2. Conferma items
3. Riceve messaggio con token `xyz789`
4. Aspetta 2 minuti
5. Clicca sul link
6. **Risultato:** "Il link è scaduto o non valido. Riprova." ✗

### ❌ Scenario 3: Token di Altro Player
1. Player A apre GUI → token `aaa111`
2. Player B scopre il comando `/sip confirm-aaa111`
3. Player B prova ad eseguirlo
4. **Risultato:** "Il link è scaduto o non valido. Riprova." ✗

### ❌ Scenario 4: Token Riutilizzato
1. Player conferma items → token `bbb222`
2. Clicca sul link → Azione eseguita
3. Token `bbb222` viene consumato
4. Player prova a cliccare di nuovo
5. **Risultato:** "Il link è scaduto o non valido. Riprova." ✗

### ❌ Scenario 5: Token Manuale
1. Player prova a indovinare: `/sip confirm-test123`
2. **Risultato:** "Il link è scaduto o non valido. Riprova." ✗

---

## 📊 Vantaggi del Sistema

| Caratteristica | Beneficio |
|----------------|-----------|
| **Token Casuali** | Impossibili da indovinare (UUID 8 caratteri) |
| **Scadenza 60s** | Previene riuso di link vecchi |
| **Player-Specific** | Solo il creatore può usarlo |
| **Tipo-Specific** | Previene uso improprio |
| **Uso Singolo** | Non riutilizzabile |
| **Pulizia Auto** | Nessun memory leak |
| **Messaggi Chiari** | UX friendly |

---

## 🛡️ Confronto con Altri Sistemi

### Sistema Senza Token (INSICURO)
```
/sip:confirm  ← Chiunque può usarlo!
```
**Problemi:**
- ❌ Riutilizzabile infinite volte
- ❌ Nessuna scadenza
- ❌ Nessuna validazione player
- ❌ Può essere eseguito manualmente

### Sistema con Token (SICURO)
```
/sip confirm-a8f3x92b  ← Token univoco!
```
**Vantaggi:**
- ✅ Usa una sola volta
- ✅ Scade dopo 60 secondi
- ✅ Solo per il player specifico
- ✅ Validazione completa

---

## 💻 Implementazione Tecnica

### Classe TokenData
```java
private static class TokenData {
    final UUID playerUUID;
    final long createdAt;
    final String type;
    
    TokenData(UUID playerUUID, String type) {
        this.playerUUID = playerUUID;
        this.createdAt = System.currentTimeMillis();
        this.type = type;
    }
    
    boolean isExpired() {
        return System.currentTimeMillis() - createdAt > 60000;
    }
}
```

### Storage
```java
private static final Map<String, TokenData> activeTokens = new HashMap<>();
```

### Generazione Token
```java
private static String generateToken() {
    return UUID.randomUUID().toString().substring(0, 8);
}
```

### Validazione
```java
public static boolean validateToken(String token, UUID playerUUID, String expectedType) {
    TokenData data = activeTokens.get(token);
    if (data == null) return false;
    if (data.isExpired()) {
        activeTokens.remove(token);
        return false;
    }
    if (!data.playerUUID.equals(playerUUID)) return false;
    if (!data.type.equals(expectedType)) return false;
    return true;
}
```

---

## 🎓 Best Practices

1. **Mai riutilizzare token**: Ogni azione genera un nuovo token
2. **Scadenza breve**: 60 secondi è perfetto per UX e sicurezza
3. **Pulizia regolare**: Task ogni 5 minuti previene memory leak
4. **Messaggi chiari**: Se il token è invalido, spiega perché
5. **Log in debug**: Utile per troubleshooting

---

## 🔍 Debug

Per abilitare il debug dei token, aggiungi nel `config.yml`:
```yaml
debug: true
```

Il plugin loggerà:
- Token generati
- Token validati
- Token consumati
- Token scaduti rimossi

---

**Developed with ❤️ by AlessioGTAII**

*Sistema di sicurezza ispirato alle best practices di autenticazione web moderne (JWT tokens)*
