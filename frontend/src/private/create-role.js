import { navigateTo } from '../navigation-handler.js';
import { fetchWithAuth } from '../auth.js';

console.log("create.role.js cargado");

function createRole () {

    const form = document.getElementById("createRoleForm");
    if(!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const roleInput = document.getElementById("name");
        const roleName = roleInput?.value?.trim();

        if (!roleName) {
            alert("Debes ingresar un nombre de rol.");
            return;
        }

        try {
            const response = await fetchWithAuth("/api/roles/create", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({role: roleName})
            });

            if(!response.ok) {
                const err = await response.text();
                throw new Error(err || "Registro fallido");
            }

            await navigateTo("/private/admin/roles");

        } catch (error) {
            alert ("Error: " + error.message);
        }
    });

}

createRole();