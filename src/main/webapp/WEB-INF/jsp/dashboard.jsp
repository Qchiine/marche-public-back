<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>VeilleMarché.ma - Tableau de Bord</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f5f7fa;
        }

        header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px 40px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
        }

        header h1 {
            font-size: 24px;
        }

        .user-info {
            display: flex;
            gap: 20px;
            align-items: center;
        }

        .user-info button {
            background: rgba(255, 255, 255, 0.2);
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 5px;
            cursor: pointer;
            transition: background 0.3s;
        }

        .user-info button:hover {
            background: rgba(255, 255, 255, 0.3);
        }

        .container {
            max-width: 1200px;
            margin: 30px auto;
            padding: 0 20px;
        }

        .section {
            background: white;
            border-radius: 10px;
            padding: 30px;
            margin-bottom: 30px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .section h2 {
            color: #333;
            margin-bottom: 20px;
            font-size: 22px;
        }

        .search-form {
            display: grid;
            grid-template-columns: 1fr 1fr 1fr;
            gap: 15px;
            margin-bottom: 20px;
        }

        .search-form input,
        .search-form select {
            padding: 12px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 14px;
        }

        .search-form input:focus,
        .search-form select:focus {
            outline: none;
            border-color: #667eea;
        }

        .btn-search {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 12px 20px;
            border-radius: 5px;
            cursor: pointer;
            font-weight: 600;
            transition: transform 0.2s;
        }

        .btn-search:hover {
            transform: translateY(-2px);
        }

        .btn-search:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }

        .results {
            margin-top: 20px;
        }

        .result-item {
            background: #f9f9f9;
            padding: 20px;
            margin-bottom: 15px;
            border-left: 4px solid #667eea;
            border-radius: 5px;
        }

        .result-item h3 {
            color: #333;
            margin-bottom: 10px;
        }

        .result-item p {
            color: #666;
            line-height: 1.6;
            margin-bottom: 8px;
        }

        .result-item .score {
            display: inline-block;
            background: #667eea;
            color: white;
            padding: 5px 15px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
        }

        .loading {
            text-align: center;
            padding: 20px;
            color: #999;
        }

        .spinner {
            border: 4px solid #f3f3f3;
            border-top: 4px solid #667eea;
            border-radius: 50%;
            width: 30px;
            height: 30px;
            animation: spin 1s linear infinite;
            margin: 0 auto 10px;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        .error {
            background: #fadbd8;
            color: #e74c3c;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }

        .success {
            background: #d5f4e6;
            color: #27ae60;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }

        .stats {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 20px;
            margin-bottom: 30px;
        }

        .stat-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 10px;
            text-align: center;
        }

        .stat-card h3 {
            font-size: 32px;
            margin-bottom: 10px;
        }

        .stat-card p {
            font-size: 14px;
            opacity: 0.9;
        }
    </style>
</head>
<body>
    <header>
        <h1>VeilleMarché.ma</h1>
        <div class="user-info">
            <span>Bienvenue, <span id="userEmail">Utilisateur</span></span>
            <button onclick="logout()">Déconnexion</button>
        </div>
    </header>

    <div class="container">
        <div class="stats" id="stats">
            <div class="stat-card">
                <h3 id="totalOffres">0</h3>
                <p>Offres totales</p>
            </div>
            <div class="stat-card">
                <h3 id="totalNotifications">0</h3>
                <p>Notifications</p>
            </div>
            <div class="stat-card">
                <h3 id="totalRecherches">0</h3>
                <p>Recherches IA</p>
            </div>
        </div>

        <div class="section">
            <h2>🔍 Recherche - Marchés Publics</h2>

            <div id="errorMessage" class="error" style="display: none;"></div>
            <div id="successMessage" class="success" style="display: none;"></div>

            <div class="search-form">
                <input type="text" id="motsCles" placeholder="Mots-clés (ex: informatique)">
                <select id="secteur">
                    <option value="">-- Secteur --</option>
                    <option value="Travaux">Travaux</option>
                    <option value="Fournitures">Fournitures</option>
                    <option value="Services">Services</option>
                </select>
                <select id="localisation">
                    <option value="">-- Localisation --</option>
                    <option value="Casablanca">Casablanca</option>
                    <option value="Rabat">Rabat</option>
                    <option value="Marrakech">Marrakech</option>
                    <option value="Fès">Fès</option>
                </select>
            </div>

            <button class="btn-search" id="searchBtn" onclick="performSearch()">Rechercher</button>

            <div id="loadingDiv" class="loading" style="display: none;">
                <div class="spinner"></div>
                <p>Recherche en cours...</p>
            </div>

            <div class="results" id="results"></div>
        </div>
    </div>

    <script>
        // Initialisation
        document.addEventListener('DOMContentLoaded', function() {
            checkAuth();
            loadStats();
        });

        function checkAuth() {
            const token = localStorage.getItem('token');
            if (!token) {
                window.location.href = '/login';
                return;
            }

            // Décoder le token JWT pour extraire l'email (simple parsing)
            try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                document.getElementById('userEmail').textContent = payload.sub || 'Utilisateur';
            } catch (e) {
                console.error('Erreur parsing token');
            }
        }

        function getAuthHeaders() {
            return {
                'Authorization': 'Bearer ' + localStorage.getItem('token'),
                'Content-Type': 'application/json'
            };
        }

        async function loadStats() {
            try {
                // Charger les statistiques (optionnel)
                document.getElementById('totalOffres').textContent = '0';
                document.getElementById('totalNotifications').textContent = '0';
                document.getElementById('totalRecherches').textContent = '0';
            } catch (error) {
                console.error('Erreur chargement stats:', error);
            }
        }

        async function performSearch() {
            const motsCles = document.getElementById('motsCles').value;
            const secteur = document.getElementById('secteur').value;
            const localisation = document.getElementById('localisation').value;

            if (!motsCles.trim()) {
                showError('Veuillez entrer des mots-clés');
                return;
            }

            document.getElementById('searchBtn').disabled = true;
            document.getElementById('loadingDiv').style.display = 'block';
            document.getElementById('results').innerHTML = '';
            hideMessages();

            try {
                // Fetch all offers and filter client-side
                const response = await fetch('/api/offres', {
                    method: 'GET',
                    headers: getAuthHeaders()
                });

                if (response.ok) {
                    const offres = await response.json();

                    // Filter based on search criteria
                    const filtered = offres.filter(offre => {
                        const motMatch = offre.intitule?.toLowerCase().includes(motsCles.toLowerCase()) ||
                                        offre.description?.toLowerCase().includes(motsCles.toLowerCase());
                        const secteurMatch = !secteur || offre.secteur === secteur;
                        const locMatch = !localisation || offre.localisation === localisation;
                        return motMatch && secteurMatch && locMatch;
                    });

                    displayResults(filtered);
                    showSuccess(`${filtered.length} offre(s) trouvée(s)`);
                } else if (response.status === 401) {
                    logout();
                } else {
                    showError('Erreur lors de la recherche');
                }
            } catch (error) {
                showError('Erreur: ' + error.message);
            } finally {
                document.getElementById('searchBtn').disabled = false;
                document.getElementById('loadingDiv').style.display = 'none';
            }
        }

        function displayResults(offres) {
            const resultsDiv = document.getElementById('results');
            if (!offres || offres.length === 0) {
                resultsDiv.innerHTML = '<p style="text-align: center; color: #999;">Aucun résultat</p>';
                return;
            }

            resultsDiv.innerHTML = offres.map(offre => `
                <div class="result-item">
                    <h3>${offre.intitule || 'Sans titre'}</h3>
                    <p><strong>Organisme:</strong> ${offre.organisme || 'N/A'}</p>
                    <p><strong>Secteur:</strong> ${offre.secteur || 'N/A'}</p>
                    <p><strong>Localisation:</strong> ${offre.localisation || 'N/A'}</p>
                    <p><strong>Description:</strong> ${(offre.description || 'N/A').substring(0, 150)}...</p>
                    <p><strong>Publication:</strong> ${offre.datePublication || 'N/A'}</p>
                    <p><strong>Clôture:</strong> ${offre.dateCloture || 'N/A'}</p>
                    ${offre.urlOfficielle ? `<p><a href="${offre.urlOfficielle}" target="_blank">Voir l'offre</a></p>` : ''}
                </div>
            `).join('');
        }

        function showError(message) {
            const errorDiv = document.getElementById('errorMessage');
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
        }

        function showSuccess(message) {
            const successDiv = document.getElementById('successMessage');
            successDiv.textContent = message;
            successDiv.style.display = 'block';
        }

        function hideMessages() {
            document.getElementById('errorMessage').style.display = 'none';
            document.getElementById('successMessage').style.display = 'none';
        }

        function logout() {
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
    </script>
</body>
</html>
