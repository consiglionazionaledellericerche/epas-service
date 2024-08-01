# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.5.0] - UNRELEASED
### Added
 - Aggiunti historyDao mancanti
 - API Rest per inserimento timbrature via interfaccia web

## [0.4.1] - 2024-07-05
### Added
- API Rest per il controllo della secure.check

## [0.4.0] - 2024-06-04
### Added
 - API Rest per inserimento delle assenze

## [0.3.0] - 2024-04-09
### Added
 - Aggiunta gestione e visualizzazione data di nascita e residenza delle persone
 - Inserita la possibilità di disattivare il calcolo automatica del permesso breve in un giorno
 - Introdotta gestione missioni nel comune di residenza già presente su ePAS
 - Aggiunti parametri di configurazione per flussi straordinari
 - Implementate api per gestione calendario reperibilità
 
### Changed
 - Corretta gestione missioni ricevute via REST con date sovrapposte
 - Evitati di caricare tutti i contratti presenti nel sistema nel dao che fa la fetch
   dei contratti
 - Corretto HistoricalDao e sistemato dto per aggiungre eventi di reperibilità e modificarli
 - Aggiunti campi subDayPostPartumProgression e accruedDay ai dto delle api di vacationsummary
 - Corretta svista in API doc dei contratti

## [0.2.0] - 2023-09-04
### Added
 - Completata la configurabilità tramite docker-compose e la relativa documentazione

### Changed
 - passaggio allo spring 2.7.15
 - REST per assenze valorizzato justifiedTime anche per giornaliere.


## [0.1] - 2023-07-06
### Added
 - Aggiunti i principali metodi REST utilizzabili in produzione per:
    - la ricezione delle timbrature
    - la gestione degli uffici
    - la gestione dei dipendenti
    - la gestione dei contratti
    - l'esportazione della situazione mensile di un dipendente
