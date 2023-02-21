# Suggerimenti per il setup dei tools per gli sviluppatori.

## Flyway

Installare il flyway in caso di manutenzioni non ordinare alle versioni del db.

Per inizializzare il flyway su un db di ePAS esiste utilizzare la funzione di _baseline_

Esempio:
```
$ flyway baseline -url=jdbc:postgresql://localhost:5432/epas-devel -user=epas
```

## Oauth 2

Per prelevare l'accesso token di un utente dal KeyCloak IIT si può usare una curl tipo questa:

$ ACCESS_TOKEN=`curl -d "client_id=epas-service" -d "username=cristian.lucchesi" -d "password=*****" -d "grant_type=password" -X POST "https://auth.iit.cnr.it/auth/realms/testing/protocol/openid-connect/token" | jq -r .access_token`

Fatto salvo che l'utente deve essere presente nel realm "testing".
Inserendo il token precedentemente ottenuto nella variabile ACCESS_TOKEN è possibile utilizzarlo per ottenere le informazioni dell'utente tramite l'endpoint userinfo.

$ curl https://auth.iit.cnr.it/auth/realms/testing/protocol/openid-connect/userinfo --header "Authorization: Bearer $ACCESS_TOKEN"

{"sub":"0c0fef69-0b23-4532-8cd3-f8ea6929e659","email_verified":true,"name":"Cristian Lucchesi","preferred_username":"cristian.lucchesi","locale":"it","given_name":"Cristian","family_name":"Lucchesi","email":"cristian.lucchesi@iit.cnr.it"}

Per utilizzare l'access token con httpie su epas-service è possibile utilizzare
la sintassi tipo:

$ http GET :8080/rest/v4/userinfo "Authorization: Bearer $ACCESS_TOKEN"


## OpenAPI e Swagger

I metodi REST sono documentati tramite Swagger e la descrizione openapi è generata automatica
al build del progetto.
L'interfaccia Swagger è disponibile in locale all'indirizzo :8080/swagger-ui/index.html

