# VixStreamCS - Estensione CloudStream

Estensione per l'app Android [CloudStream](https://github.com/recloudstream/cloudstream) che utilizza il catalogo di **TMDB (The Movie Database)** per la navigazione e il sito **vixsrc.to** come sorgente per i contenuti streaming (Film e Serie TV).

## Caratteristiche
- **Versione 5**: Fix critico per l'estrattore VixSrc (supporto Next.js API).
- **Catalogo TMDB**: Utilizza le API ufficiali di TMDB per fornire locandine, trame, voti e metadati accurati.
- **Sorgente VixSrc**: Integra l'estrattore per `vixsrc.to` che fornisce flussi video in formato M3U8 tramite risoluzione dinamica degli embed.
- **Supporto Multiplo**: Gestione completa di Film e Serie TV con selezione di stagioni ed episodi.
- **Lingua**: Interfaccia e metadati configurati per la lingua italiana.

## Requisiti per lo Sviluppo
- **Java 17 o superiore**: Necessario per eseguire Gradle 9.0.0.
- **Android Studio (JBR)**: Consigliato per la gestione della JDK.
- **Kotlin 2.3.0**: Per la compatibilità dei metadati del plugin.

## Istruzioni per la Compilazione

Per generare il file del plugin (`.cs3`) da caricare su CloudStream, segui questi passaggi:

1. Apri un terminale (PowerShell consigliato su Windows).
2. Assicurati che `JAVA_HOME` punti alla JDK corretta:
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
   ```
3. Esegui il comando di build usando il modulo corretto:
   ```powershell
   .\gradlew.bat :VixStreamCS:make
   ```

### Output
Al termine della compilazione, troverai il file del plugin al seguente percorso:
`app/build/VixStreamCS.cs3`

## Installazione in CloudStream
Puoi aggiungere l'estensione tramite il repository ufficiale:
`https://raw.githubusercontent.com/MicheleCicinelli/VixStreamCS/builds/repo.json`

## Note Tecniche
- **Metodo Load**: Gestisce sia input JSON (`TmdbData`) che URL di TMDB.
- **Estrattore VixSrc**: Risolve i link video interrogando prima le API interne del sito per ottenere l'iframe di embed, garantendo la compatibilità con le protezioni lato client.
- **Offuscamento**: Configurato per proteggere i DTO tramite annotazioni Jackson e compiler flags specifiche.

## Note Legali
Questa estensione è fornita solo a scopo di studio e ricerca. Non ospita alcun contenuto multimediale.
