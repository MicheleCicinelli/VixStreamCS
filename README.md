# VixStreamCS - Estensione CloudStream

Estensione per l'app Android [CloudStream](https://github.com/recloudstream/cloudstream) che utilizza il catalogo di **TMDB (The Movie Database)** per la navigazione e il sito **vixsrc.to** come sorgente per i contenuti streaming (Film e Serie TV).

## Caratteristiche
- **Catalogo TMDB**: Utilizza le API ufficiali di TMDB per fornire locandine, trame, voti e metadati accurati.
- **Sorgente VixSrc**: Integra l'estrattore per `vixsrc.to` che fornisce flussi video in formato M3U8.
- **Supporto Multiplo**: Gestione completa di Film e Serie TV con selezione di stagioni ed episodi.
- **Lingua**: Interfaccia e metadati configurati per la lingua italiana.

## Requisiti per lo Sviluppo
- **Java 17 o superiore**: Necessario per eseguire Gradle 9.0.0.
- **Android SDK**: Installato e configurato (il percorso deve essere presente in `local.properties`).
- **Gradle Wrapper**: Incluso nel progetto.

## Istruzioni per la Compilazione

Per generare il file del plugin (`.cs3`) da caricare su CloudStream, segui questi passaggi:

1. Apri un terminale (PowerShell consigliato su Windows).
2. Assicurati che `JAVA_HOME` punti a una versione compatibile (es. quella fornita da Android Studio):
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
   ```
3. Esegui il comando di build:
   ```powershell
   .\gradlew.bat :app:make
   ```

### Output
Al termine della compilazione, troverai il file del plugin al seguente percorso:
`app/build/app.cs3`

Questo file può essere installato direttamente nell'app CloudStream.

## Struttura del Progetto
- `app/src/main/kotlin/it/vixstreamcs/VixStreamProvider.kt`: Logica principale del provider TMDB.
- `app/src/main/kotlin/it/vixstreamcs/VixSrcExtractor.kt`: Estrattore specifico per vixsrc.to.
- `app/src/main/kotlin/it/vixstreamcs/VixStreamPlugin.kt`: Entry point del plugin.

## Note Legali
Questa estensione è fornita solo a scopo di studio e ricerca. Non ospita alcun contenuto multimediale.
