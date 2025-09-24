console.log("signin.js cargado correctamente");

// Enfocar el primer campo (nombre de usuario) al cargar la página
function setupSigninForm() {
    const usernameInput = document.getElementById('username');
    if (usernameInput) {
        usernameInput.focus();
    }

    // Funcionalidad para mostrar/ocultar contraseña (opcional)
    const passwordInput = document.getElementById('password');
    const togglePasswordButton = document.getElementById('togglePassword'); // Añadir botón en HTML

    if (togglePasswordButton && passwordInput) {
        togglePasswordButton.addEventListener('click', function() {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            this.textContent = type === 'password' ? 'Mostrar' : 'Ocultar'; // Cambia el texto del botón
        });
    }

    const loginBtn = document.getElementById("loginBtn");
    if(loginBtn) {
        loginBtn.addEventListener("click", (e) => {
            e.preventDefault();
            login();
        });
    }
}

setupSigninForm();

//Login y manejo del token JWT:
async function login(){
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    try {
        /*Para realizar la solicitud fetch, se envía una solicitud POST
        a /auth/signin con los datos de inicio de sesión
        (usuario y contraseña) en formato JSON.*/
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

        //Guardar el token en local storage
        localStorage.setItem("jwt", token);

        // pide /private pero vía SPA (con fetch + Authorization)
        sessionStorage.setItem('postLoginTarget', '/private');

        // carga el shell privado
        window.location.replace = '/shell/private';

    } catch (error) {
        alert("Error al iniciar sesión: " + error.message);
    }
}