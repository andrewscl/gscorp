/*Función reutilizable para rutas protegidas.
agregar token JWT a las solicitudes protegidas*/
export async function fetchWithAuth(url, options = {}) {

    const token = localStorage.getItem("jwt");
    console.log("Enviando solicitud protegida con token:", token);

    if(!token) {
        throw new Error("No token found");
    }

    /*Se agregan los encabezados necesarios para la solicitud.
    En el caso de las solicitudes autenticadas, se añade el
    token en el campo Authorization con el formato Bearer 
    <token>*/

    const headers = {
        ...options.headers,
        "Authorization": `Bearer ${token}`,
    };

    /*Se realiza la solicitud fetch con los encabezados que
    incluyen el token.*/ 
    const response = await fetch (url, {
        ...options,
        headers,
    });

    return response;
}