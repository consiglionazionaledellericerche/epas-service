# ePAS Service
## Electronic Personnel Attendance System - REST Services

[![license](https://img.shields.io/badge/License-AGPL%20v3-blue.svg?logo=gnu&style=for-the-badge)](https://github.com/consiglionazionaledellericerche/epas-service/blob/master/LICENSE)
[![Supported JVM Versions](https://img.shields.io/badge/JVM-11-brightgreen.svg?style=for-the-badge&logo=Java)](https://openjdk.java.net/install/)
[![contributors](https://img.shields.io/github/contributors/consiglionazionaledellericerche/epas-service.svg?logo=github&style=for-the-badge)](https://github.com/consiglionazionaledellericerche/epas-service/contributors/)
[![Downloads](https://img.shields.io/github/downloads/consiglionazionaledellericerche/epas-service/total?logo=github&style=for-the-badge)](https://github.com/consiglionazionaledellericerche/epas-service/pkgs/container/epas-service)
[![ePAS on developers.italia.it](https://img.shields.io/badge/Italia-blue.svg?label=ePAS&style=for-the-badge&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAMAAABEpIrGAAABd1BMVEUAZM0AYswAYcwAYs0Ea88Fa88Das8GbNACaM4Gac/B3/Xg8Pra7fnn9fx8suYAXMoHaM/u9fz///+YwOsAWcoFaM7m8fqTvuoAXMsAWsoAW8oAX8sAX8yOu+kAU8gAXssAYMwAY81Cj9t5seZUmt4EaM4LbNAhetQcd9Mdd9MgedShx+0VctIjetQle9V3sea01PFoqeP8/v/9//88jdoAXcv9/v/e7PkJa8/u9vw/jNprq+Tj7/oCZs4ed9NlouFpquM7jNrl8Prs9fxCjttpquT6/f7v9/3y+f36/v/S5vcFZ85anN/1+v2fx+2QvuoAZs4Nb9EOcNEOb9ERcNE8jNolfdWHt+gAVciKuegBZs7g7fmSv+rR5ffl8vs5i9kScdEfeNQZdNK00/H3+/7D2/RopuLR5vdwruVAj9sMbtDn8/vl8fsAZc1HlN251/Ku0PCv0PAogNYoftWexu3h7vn2/P7t9fzW5/eXwusBZc0PcNESc9L054SXAAABXklEQVQ4y72TZ1/CMBDG29SFqxpBu6SJigMXKlUcuLfinrj33nt8eC/Q8qssX8m9epL8c8lzl3DcvwSPEBLSrAtZ2Tm5eXzq/Y78gsKiYjElgEpKMcZlztSAqxyAihjAS7KCIkJVNU2wgEq3rhPKeFpVXeMhsF5bV9/g9Qgm0NjU3OKlQCitMPKBK6UNt3dgv2ECuBPjLhdYCXSzUY/Byb1M+EULgOgLAtAfkQMy5xxkYkiyAcNBuJJjhMlRiZPHfgHjE5NT0zOQQZoNYTxHeQuwjphfWFxaZvWmxsrqmgo+4zKsbxCCooUghs78xgGbCZWMOyIRMF2If2WQMgHw4S0Q225VI8kBinZA7O7tHxyipAAnHkU6cYxPNGq3GQPkU7NZZ8QOyOds7gIAcnkVBa5v7E/buL27f/A9qkw+Pb+EXt/ePz7tGSiRFMWIXktXwl/fAWTwaT4X9JJymYkfJHxA0uanFlQAAAAASUVORK5CYII=)](https://developers.italia.it/it/software/cnr-consiglionazionaledellericerche-epas)
[![Version](https://img.shields.io/github/v/release/consiglionazionaledellericerche/epas-service.svg)](https://github.com/consiglionazionaledellericerche/epas-service/blob/main/VERSION)

ePAS Service è la nuova versione della parte di backend di ePAS, fornirà solo endopoint REST ed è
pensato per essere compatibile al 100% con il database di ePAS.

ePAS Service deve essere affiancato ad un'istanza funzionante di ePAS, condividendo lo stesso database.
Al momento non sono ancora presenti le procedure ed i dati per utilizzare ePAS Service senza un 
database configurato di ePAS.

Il progetto è attualmente in fase avanzata di sviluppo e fornirà tutte le funzionalità necessarie
alla nuova interfaccia web di ePAS che sarà parte di un nuovo progetto e sarà rilasciata a
breve in versione preliminare.

Per maggiori informazioni su ePAS è possibile consultare la documentazione completa all'indirizzo:

- [https://consiglionazionaledellericerche.github.io/epas/](https://consiglionazionaledellericerche.github.io/epas/)

## ePAS Service

ePAS Service fornisce già alcuni servizi REST utilizzabili in produzione per:

 - la ricezione delle timbrature
 - la gestione degli uffici
 - la gestione dei dipendenti
 - la gestione dei contratti
 - l'esportazione della situazione mensile di un dipendente

I servizi saranno estesi per coprire tutte le attuali funzionalità di ePAS lato server.

Al momento è possibil utilizzare l'autenticazione tramite Bearer Token OAUTH2 oppure Basic Auth.

ePAS Service fornisce la documentazione di utilizzo degli endpoint REST in formato *openapi* ed
integrare lo *Swagger* per la visualizzazione della documentazione e l'interfaccia di prova dei 
servizi.

L'interfaccia *Swagger* è disponibile nella url relativa alla propria installazione di ePAS Service
con il path /swagger-ui/index.html. Es.: https://epas-service.amministrazione.cnr.it/swagger-ui/index.html.


## 👏 Come Contribuire 

Lo scopo principale di questo repository è continuare ad evolvere ePAS. 
Vogliamo contribuire a questo progetto nel modo più semplice e trasparente possibile e siamo grati
alla comunità per ogni contribuito a correggere bug e miglioramenti.

## 📄 Licenza

ePAS Service è concesso in licenza GNU AFFERO GENERAL PUBLIC LICENSE, come si trova nel file
[LICENSE][l].

[l]: https://github.com/consiglionazionaledellericerche/epas-service/blob/master/LICENSE

# <img src="https://www.docker.com/wp-content/uploads/2021/10/Moby-logo-sm.png" width=80> Startup

#### _Per avviare una istanza di ePAS con postgres locale_

ePAS può essere facilmente installato via docker-compose su server Linux utilizzando il file 
docker-compose.yml presente in questo repository.

Accertati di aver installato docker e docker-compose dove vuoi installare ePAS ed in seguito
esegui il comando successivo per un setup di esempio.

```
docker-compose up -d
```

## Credits

[Istituto di Informatica e Telematica del CNR](https://www.iit.cnr.it)

  - Cristian Lucchesi <cristian.lucchesi@iit.cnr.it>
  - Maurizio Martinelli <maurizio.martinelli@iit.cnr.it>
  - Dario Tagliaferri <dario.tagliaferri@iit.cnr.it>

## Vedi anche

  - [Documentazione completa di ePAS ](https://consiglionazionaledellericerche.github.io/epas/)
  - [ePAS client - file locali / ftp /sftp e lettori smartclock](https://github.com/consiglionazionaledellericerche/epas-client)
  - [ePAS client - timbratura da database SQL](https://github.com/consiglionazionaledellericerche/epas-client-sql)
