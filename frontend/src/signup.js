import { navigateTo } from './navigation-handler.js';

console.log("signup.js cargado correctamente");

function setupSignupForm() {
    const usernameInput = document.getElementById('username');
    if (usernameInput) {
        usernameInput.focus();
    }

    const passwordInput = document.getElementById('password');
    const togglePasswordButton = document.getElementById('togglePassword');

    if (togglePasswordButton && passwordInput) {
        togglePasswordButton.addEventListener('click', () => {
            const type = passwordInput.type === 'password' ? 'text' : 'password';
            passwordInput.type = type;
            togglePasswordButton.textContent = type === 'password' ? 'Mostrar' : 'Ocultar';
        });
    }

    const form = document.getElementById("signupForm");
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();

            const username = document.getElementById("username").value;
            const password = document.getElementById("password").value;

            try {
                console.log("fetch a /auth/signup");
                const response = await fetch("/auth/signup", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ username, password })
                });

                if (!response.ok) {
                    const err = await response.text();
                    throw new Error(err || "Registro fallido");
                }

                await navigateTo("/public/home");

            } catch (error) {
                const errorDiv = document.getElementById("signupError");
                if (errorDiv) {
                    errorDiv.textContent = "Error al registrar: " + error.message;
                } else {
                    alert("Error: " + error.message);
                }
            }
        });
    }
}

// Ejecutar de inmediato como signin.js
setupSignupForm();
