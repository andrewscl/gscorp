const loadGoogleMapsAPI = (() => {
    let scriptPromise = null;

    return (apiKey) => {
        // Si ya existe una promesa (en curso o resuelta), devuélvela
        if (scriptPromise) return scriptPromise;

        scriptPromise = new Promise((resolve, reject) => {
            // Verificación extra por si ya existiera el objeto google en el window
            if (typeof window.google !== 'undefined') {
                resolve(window.google);
                return;
            }

            const script = document.createElement('script');
            // Agregamos libraries=places por si luego necesitas buscadores de direcciones
            script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&v=weekly&libraries=places`;
            script.async = true;
            script.defer = true;

            script.onload = () => resolve(window.google);
            script.onerror = (err) => {
                scriptPromise = null; // Si falla, permitimos reintentar la carga
                reject(new Error('No se pudo cargar Google Maps'));
            };

            document.head.appendChild(script);
        });

        return scriptPromise;
    };
})();

export default loadGoogleMapsAPI;

