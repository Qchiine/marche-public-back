# 🎓 Patterns Système Distribué - VeilleMarché.ma

Ce document décrit les patterns de système distribué implémentés dans le projet.

---

## 1. 🔄 Transactions Distribuées (JTA Pattern)

**Fichiers:**
- `service/TransactionService.java`
- `service/UtilisateurService.java` (avec `@Transactional`)

### Concepts Implémentés:

#### A. Propagation.REQUIRED
```java
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public void operationCritique(Runnable action)
```
- Utilise la transaction actuelle ou en crée une nouvelle
- **Cas d'usage:** Opérations critiques nécessitant atomicité

#### B. Propagation.REQUIRES_NEW
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void operationIndependante(Runnable action)
```
- Crée toujours une nouvelle transaction isolée
- **Cas d'usage:** Opérations qui doivent être indépendantes

#### C. Propagation.SUPPORTS
```java
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public void operationLecture(Runnable action)
```
- Utilise la transaction si elle existe, sinon sans transaction
- **Cas d'usage:** Opérations de lecture

#### D. Propagation.NESTED
```java
@Transactional(propagation = Propagation.NESTED)
public void operationImbriquee(Runnable action)
```
- Crée un savepoint dans la transaction parent
- **Cas d'usage:** Opérations partielles avec rollback sélectif

### Avantages:
✅ Atomicité des opérations
✅ Isolation entre transactions
✅ Durabilité des données
✅ Gestion automatique des rollback

---

## 2. 🌐 Services Distribués (RMI-like Pattern)

**Fichiers:**
- `service/DistributedServiceClient.java`
- `config/DistributedConfig.java`

### Implémentation:

#### A. Appels RPC Synchrones
```java
<T> T callRemoteService(String serviceUrl, Object requestBody, Class<T> responseType)
```
- Communication synchrone avec services distants
- Simule Java RMI (Remote Method Invocation)
- **Avantage:** Appelé comme une méthode locale

#### B. Appels GET Distribués
```java
<T> T getFromRemoteService(String serviceUrl, Class<T> responseType)
```
- Récupère des données depuis un service distant
- Gestion des erreurs automatique

#### C. Retry Automatique (Fault Tolerance)
```java
<T> T callWithRetry(String serviceUrl, Object requestBody, Class<T> responseType, int maxRetries)
```
- Retry automatique avec backoff exponentiel
- **Pattern:** Circuit Breaker (simplifié)
- **Délais:** 1s → 2s → 4s → ...

### Configuration RestTemplate:
```java
@Bean
public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofSeconds(5))  // Timeout connexion
        .setReadTimeout(Duration.ofSeconds(10))     // Timeout lecture
        .build();
}
```

### Architecture:
```
Service A (localhost:8080)
    ↓
DistributedServiceClient (RestTemplate)
    ↓ HTTP/HTTPS
Service B (remote:8081)
    ↓
Réponse JSON/XML
```

---

## 3. 🔐 Web Services SOAP (JAXWS Pattern)

**Fichiers:**
- `ws/OffreWebService.java` (Endpoint)
- `ws/dto/GetOffreRequest.java` (Request)
- `ws/dto/GetOffreResponse.java` (Response)
- `config/WebServiceConfig.java` (Configuration)

### Endpoint SOAP:

#### A. Opération: getOffre
```
POST /ws/offre
Content-Type: application/soap+xml

<soap:Envelope>
  <soap:Body>
    <getOffreRequest>
      <reference>AO-2025-001</reference>
    </getOffreRequest>
  </soap:Body>
</soap:Envelope>
```

**Réponse:**
```xml
<soap:Envelope>
  <soap:Body>
    <getOffreResponse>
      <reference>AO-2025-001</reference>
      <intitule>Fourniture informatique</intitule>
      <organisme>Ministère X</organisme>
      <secteur>Fournitures</secteur>
      <success>true</success>
    </getOffreResponse>
  </soap:Body>
</soap:Envelope>
```

#### B. Opération: searchOffres
```
POST /ws/offre
<searchOffresRequest>
  <secteur>Services</secteur>
</searchOffresRequest>
```

### Accès WSDL:
```
http://localhost:8080/ws/offre.wsdl
```

### Avantages SOAP:
✅ Contrats stricts (WSDL)
✅ Validation XML
✅ Interopérabilité (Java, .NET, Python, etc.)
✅ Support des transactions SOAP

---

## 4. 🗄️ Clustering MongoDB (Distribution de Données)

### Configuration Replica Set:

#### A. Démarrer un Replica Set (3 instances):

```bash
# Terminal 1 - Node 1 (Primary)
mongod --replSet rs0 --port 27017 --dbpath /data/db1

# Terminal 2 - Node 2 (Secondary)
mongod --replSet rs0 --port 27018 --dbpath /data/db2

# Terminal 3 - Node 3 (Secondary/Arbiter)
mongod --replSet rs0 --port 27019 --dbpath /data/db3
```

#### B. Initialiser le Replica Set:

```javascript
rs.initiate({
  _id: "rs0",
  members: [
    { _id: 0, host: "localhost:27017" },  // Primary
    { _id: 1, host: "localhost:27018" },  // Secondary
    { _id: 2, host: "localhost:27019" }   // Secondary
  ]
})
```

#### C. Vérifier le statut:
```javascript
rs.status()  // Affiche l'état du cluster
rs.conf()    // Configuration actuelle
```

### Configuration Spring (application.properties):

```properties
spring.data.mongodb.uri=mongodb://localhost:27017,localhost:27018,localhost:27019/marchesdb?replicaSet=rs0
spring.data.mongodb.auto-index-creation=true
```

### Avantages MongoDB Replica Set:
✅ **Haute Disponibilité:** 1 Primary + 2 Secondaries
✅ **Redondance Automatique:** Failover automatique
✅ **Read Scaling:** Lectures sur secondaries possibles
✅ **Sauvegarde Distribuée:** Données répliquées

### Failover Automatique:
```
Primary échoue
    ↓
Election parmi Secondaries
    ↓
Nouveau Primary choisi (en 10-30s)
    ↓
Services reconnus à nouveau
```

---

## 🏗️ Architecture Complète - Système Distribué

```
┌─────────────────────────────────────────────────────┐
│         CLIENT (Frontend JSP)                        │
│    Login → Authentification JWT                      │
└──────────────────────────┬──────────────────────────┘
                           │
                           ↓
┌─────────────────────────────────────────────────────┐
│      REST API / SOAP Endpoints                       │
│      /api/* (REST) | /ws/* (SOAP)                   │
└──────────────────────────┬──────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ↓                  ↓                  ↓
    ┌────────┐        ┌────────┐       ┌──────────┐
    │REST    │        │SOAP    │       │Async Svc │
    │Handler │        │Handler │       │Calls     │
    └───┬────┘        └───┬────┘       └─────┬────┘
        │                 │                  │
        │    ┌────────────┼──────────────┐  │
        │    │   @Transactional          │  │
        │    │   (JTA Pattern)           │  │
        │    └────────────┼──────────────┘  │
        │                 │                  │
        └─────────────────┼──────────────────┘
                          │
                          ↓
        ┌────────────────────────────────────┐
        │  DistributedServiceClient          │
        │  RestTemplate + Retry Logic        │
        └────────────┬───────────────────────┘
                     │ HTTP Calls to
                     │ Remote Services
        ┌────────────┴────────┐
        │                     │
        ↓                     ↓
   Service B            Service C
  (Optional)           (Optional)

                          │
        ┌─────────────────┘
        │
        ↓
┌──────────────────────────────────────┐
│  MongoDB Replica Set (Clustering)    │
│                                      │
│  Primary (27017)  ← Write Operations │
│    ↓         ↓                       │
│  Replica1  Replica2  ← Read Replicas│
│  (27018)   (27019)                  │
│                                      │
│  Automatic Failover & Replication   │
└──────────────────────────────────────┘
```

---

## 📊 Résumé des Concepts

| Pattern | Jakarta EE | Spring Boot | Bénéfice |
|---------|-----------|-----------|----------|
| JTA | @TransactionAttribute | @Transactional | Atomicité |
| RMI | EJB Remote | RestTemplate | Distribution |
| JAXWS | @WebService | @Endpoint + SOAP | Interop |
| Clustering | EJB Cluster | Mongo Replica | HA |

---

## 🚀 Points Clés

1. **Transactions:** Gèrent l'atomicité des opérations distribuées
2. **RMI:** Communique avec d'autres services comme des appels locaux
3. **SOAP:** Interface standardisée pour clients externes
4. **Clustering:** Assure la disponibilité et la durabilité des données

---

## 📚 Ressources

- Spring Transaction Documentation: https://spring.io/guides/gs/handling-form-submission/
- RestTemplate Guide: https://www.baeldung.com/rest-template
- Spring WS (SOAP): https://spring.io/projects/spring-ws
- MongoDB Replica Set: https://docs.mongodb.com/manual/replication/

