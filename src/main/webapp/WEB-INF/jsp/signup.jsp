<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>VeilleMarché.ma - Inscription</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding: 20px;
        }

        .signup-container {
            background: white;
            border-radius: 10px;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
            width: 100%;
            max-width: 500px;
            padding: 40px;
        }

        .logo {
            text-align: center;
            margin-bottom: 30px;
        }

        .logo h1 {
            color: #667eea;
            font-size: 28px;
            margin-bottom: 10px;
        }

        .logo p {
            color: #999;
            font-size: 14px;
        }

        .form-group {
            margin-bottom: 20px;
        }

        label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 500;
        }

        input[type="text"],
        input[type="email"],
        input[type="password"],
        select {
            width: 100%;
            padding: 12px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 14px;
            transition: border-color 0.3s;
        }

        input:focus,
        select:focus {
            outline: none;
            border-color: #667eea;
        }

        .btn-signup {
            width: 100%;
            padding: 12px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 5px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s;
            margin-top: 10px;
        }

        .btn-signup:hover {
            transform: translateY(-2px);
        }

        .btn-signup:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }

        .login-link {
            text-align: center;
            margin-top: 20px;
            font-size: 14px;
            color: #999;
        }

        .login-link a {
            color: #667eea;
            text-decoration: none;
            font-weight: 600;
        }

        .message {
            font-size: 14px;
            margin-bottom: 15px;
            padding: 10px;
            border-radius: 5px;
            display: none;
        }

        .error-message {
            color: #e74c3c;
            background: #fadbd8;
        }

        .success-message {
            color: #27ae60;
            background: #d5f4e6;
        }

        .message.show {
            display: block;
        }
    </style>
</head>
<body>
    <div class="signup-container">
        <div class="logo">
            <h1>VeilleMarché.ma</h1>
            <p>Créer un nouveau compte</p>
        </div>

        <div id="errorMessage" class="message error-message"></div>
        <div id="successMessage" class="message success-message"></div>

        <form id="signupForm">
            <div class="form-group">
                <label for="nom">Nom complet</label>
                <input type="text" id="nom" name="nom" required>
            </div>

            <div class="form-group">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" required>
            </div>

            <div class="form-group">
                <label for="password">Mot de passe</label>
                <input type="password" id="password" name="password" required minlength="6">
            </div>

            <div class="form-group">
                <label for="confirmPassword">Confirmer le mot de passe</label>
                <input type="password" id="confirmPassword" name="confirmPassword" required minlength="6">
            </div>

            <div class="form-group">
                <label for="typeEntreprise">Type d'entreprise</label>
                <select id="typeEntreprise" name="typeEntreprise">
                    <option value="">-- Sélectionner --</option>
                    <option value="PME">PME</option>
                    <option value="ETI">ETI</option>
                    <option value="GRAND_ENTREPRISE">Grande entreprise</option>
                    <option value="COLLECTIVITE">Collectivité</option>
                </select>
            </div>

            <button type="submit" class="btn-signup">S'inscrire</button>
        </form>

        <div class="login-link">
            Déjà inscrit? <a href="/login">Se connecter</a>
        </div>
    </div>

    <script>
        document.getElementById('signupForm').addEventListener('submit', async (e) => {
            e.preventDefault();

            const nom = document.getElementById('nom').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const typeEntreprise = document.getElementById('typeEntreprise').value;

            // Validation
            if (password !== confirmPassword) {
                showError('Les mots de passe ne correspondent pas');
                return;
            }

            if (password.length < 6) {
                showError('Le mot de passe doit contenir au moins 6 caractères');
                return;
            }

            try {
                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        nom: nom,
                        email: email,
                        motDePasse: password,
                        typeEntreprise: typeEntreprise || null
                    })
                });

                if (response.ok) {
                    showSuccess('Compte créé avec succès! Redirection...');
                    setTimeout(() => {
                        window.location.href = '/login';
                    }, 2000);
                } else if (response.status === 400) {
                    const error = await response.json();
                    showError(error.message || 'Erreur lors de l\'inscription');
                } else {
                    showError('Erreur lors de l\'inscription');
                }
            } catch (error) {
                showError('Erreur: ' + error.message);
            }
        });

        function showError(message) {
            const errorDiv = document.getElementById('errorMessage');
            errorDiv.textContent = message;
            errorDiv.classList.add('show');
            document.getElementById('successMessage').classList.remove('show');
        }

        function showSuccess(message) {
            const successDiv = document.getElementById('successMessage');
            successDiv.textContent = message;
            successDiv.classList.add('show');
            document.getElementById('errorMessage').classList.remove('show');
        }
    </script>
</body>
</html>
