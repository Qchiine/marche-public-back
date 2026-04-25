# 🐳 Docker Setup - VeilleMarché

## 📋 Prérequis

- Docker Desktop installé
- Docker Compose (inclus dans Docker Desktop)

---

## 🚀 Démarrage rapide

### **1️⃣ Depuis la racine du backend** (`C:\Users\DELL\Desktop\marche-public-backo`)

```bash
docker-compose up --build
```

Cela va:
- ✅ Construire et lancer **MongoDB** (port 27017)
- ✅ Construire et lancer **Backend Spring Boot** (port 8080)
- ✅ Construire et lancer **Frontend Next.js** (port 3000)

### **2️⃣ Accès**

```
Frontend:  http://localhost:3000
Backend:   http://localhost:8080
MongoDB:   localhost:27017
```

### **3️⃣ Arrêter les conteneurs**

```bash
docker-compose down
```

Pour supprimer aussi les volumes MongoDB:
```bash
docker-compose down -v
```

---

## 🔧 Configuration

### **Variables d'environnement** (`.env`)

```env
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_USERNAME=votre-email@gmail.com
SPRING_MAIL_PASSWORD=votre-mot-passe-app
```

### **Frontend** (`.env.local`)

Pour Docker:
```env
NEXT_PUBLIC_API_URL=http://backend:8080
```

Pour développement local (sans Docker):
```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

## 🐛 Dépannage

### **Le frontend ne se connecte pas au backend**

**Vérifier:**
```bash
# Dans le conteneur frontend
docker exec marches-frontend curl http://backend:8080
```

**Solution:**
- Assurer que `NEXT_PUBLIC_API_URL=http://backend:8080` dans le conteneur

### **MongoDB ne démarre pas**

```bash
docker logs mongo-marches
```

### **Voir les logs en temps réel**

```bash
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mongo
```

---

## 📱 Endpoints API disponibles

Voir `lib/api.ts` pour la liste complète.

Exemple:
```typescript
import { apiClient } from '@/lib/api';

// Login
const { accessToken } = await apiClient.login('user@example.com', 'password');
localStorage.setItem('token', accessToken);

// Notifications
const notifs = await apiClient.getNotifications();
```

---

#