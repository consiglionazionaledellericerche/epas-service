# Suggerimenti per il setup dei tools per gli sviluppatori.

## Flyway

Installare il flyway in caso di manutenzioni non ordinare alle versioni del db.

Per inizializzare il flyway su un db di ePAS esiste utilizzare la funzione di _baseline_

Esempio:
```
$ flyway baseline -url=jdbc:postgresql://localhost:5432/epas-devel -user=epas
```