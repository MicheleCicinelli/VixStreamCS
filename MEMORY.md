# Session Memory - VixStreamCS

## 📌 Stato del Progetto
Sviluppo di un'estensione CloudStream (TMDB + VixSrc).
- **Branch `main`**: Codice sorgente.
- **Branch `builds`**: Deployment (file `.cs3`, `repo.json`, `plugins.json`).

---

## ✅ Problemi Risolti

### 1. Repository Vuoto in CloudStream
Il repository veniva aggiunto ma non mostrava plugin. Cause e soluzioni:
- **Refuso URL**: Corretto `Cicinellli` in `Cicinelli` nel file `plugins.json`.
- **Encoding BOM**: I file JSON creati con PowerShell avevano il BOM (Byte Order Mark). Rimossi i byte `EF BB BF` per rendere i JSON leggibili dall'app.
- **Mismatch Metadati**: Il nome interno del plugin non coincideva con quello dichiarato nel repository.
- **Rinomina Modulo**: Il modulo Gradle è stato rinominato da `:app` a `:VixStreamCS` in `settings.gradle.kts` per forzare il campo `"name": "VixStreamCS"` nel `manifest.json` interno al file `.cs3`.

### 2. Campi Obbligatori Repository
Aggiunti campi critici nel `plugins.json` per compatibilità con le ultime versioni di CloudStream:
- `internalName`: "VixStreamCS"
- `apiVersion`: 1
- `status`: 1
- `repositoryUrl`: "https://github.com/MicheleCicinelli/VixStreamCS"

---

## 🛠 Problema Corrente: Errore Caricamento Dettagli
Dopo la ricerca, selezionando un titolo (Film o Serie TV) l'app va in errore (schermata di caricamento fallito).

### Ipotesi e Azioni Intraprese:
1.  **Offuscamento (R8/D8)**: Sospettiamo che la serializzazione dei dati tra ricerca e caricamento (`TmdbData`) fallisca a causa della rinomina dei campi durante la build.
    - **Azione**: Aggiunte annotazioni `@JsonProperty` a tutte le `data class` in `VixStreamCS.kt`.
2.  **Timeout Serie TV**: Il caricamento sequenziale delle stagioni era lento.
    - **Azione**: Ottimizzato il metodo `load` con `coroutineScope` e `async` per il caricamento parallelo.

---

## 🚀 Istruzioni per la Build e Deployment

### Comando di Build:
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat :VixStreamCS:make
```
Il file generato si trova in: `app/build/VixStreamCS.cs3`.

### Procedura di Aggiornamento Repository:
1. Build del plugin.
2. Copia del file `.cs3` nella root.
3. Verifica che `plugins.json` e `repo.json` non abbiano il BOM.
4. Push sul branch `builds`:
   ```powershell
   git checkout builds
   git add .
   git commit -m "Update plugin"
   git push origin builds --force
   ```

---

## 🔍 Prossimi Passi
- Verificare se le annotazioni `@JsonProperty` risolvono l'errore di caricamento (richiede build e test nell'app).
- Confronto con `ItaliaInStreaming` per verificare l'uso di `proguard-rules.pro` invece delle annotazioni manuali.
