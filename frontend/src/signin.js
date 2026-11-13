import {navigateTo} from './navigation-handler.js'

async function login(){
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    try {

        const response = await fetch("/auth/signin", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({username, password})
        });
        if(!response.ok) {
            const message = await response.text();
            throw new Error(message || "Credenciales invalidas");
        }
        const data  = await response.json();
        const token = data.token;
        localStorage.setItem("jwt", token);
        await navigateTo('/private');

    } catch (error) {
        alert("Error al iniciar sesión: " + error.message);
    }
}

const qs = (s) => document.querySelector(s);

function hideAndShowPassword() {
    const passwordInput = document.getElementById('password');
    const togglePasswordButton = document.getElementById('togglePassword'); // Añadir botón en HTML

    if (togglePasswordButton && passwordInput) {
        togglePasswordButton.addEventListener('click', function() {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            this.textContent = type === 'password' ? 'Mostrar' : 'Ocultar'; // Cambia el texto del botón
        });
    }
}

function forgotPassword() {
    navigateTo('/auth/forgot-password');
}


function bindEvents() {
    const loginBtn = qs('#loginBtn');
    if (loginBtn) {
        loginBtn.addEventListener('click', login);
    }
    const usernameInput = qs('#username');
    if (usernameInput) {
        usernameInput.focus();
    }
    const forgotPasswordLink = qs('#forgotPass');
    if (forgotPasswordLink) {
        forgotPasswordLink.addEventListener('click', forgotPassword);
    }
}

(function init(){
    bindEvents();
    hideAndShowPassword();
})();


