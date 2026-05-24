// src/main/resources/static/js/utils/date-utils.js

/**
 * Procesa, valida y ordena un rango de fechas para filtros de búsqueda,
 * incluyendo la zona horaria del navegador del cliente.
 * * @param {string} fromValue - Valor del input 'desde'
 * @param {string} toValue - Valor del input 'hasta'
 * @returns {Object} Objeto con { from, to, clientTz } procesados y corregidos
 */
function prepareDateRange(fromValue, toValue) {
    let from = fromValue || '';
    let to = toValue || '';

    // 🎯 Balanceo de fechas si una viene vacía
    if (!from && to) from = to;
    if (!to && from) to = from;

    // 🎯 Auto-corrección cronológica (Swap si están invertidas)
    try {
        const f = new Date(from);
        const t = new Date(to);
        if (!isNaN(f) && !isNaN(t) && f > t) {
            [from, to] = [to, from];
        }
    } catch (e) {
        console.warn("Error al parsear el orden cronológico de fechas en utilidad genérica", e);
    }

    // 🎯 Captura de la Zona Horaria nativa del navegador
    let clientTz = '';
    try {
        clientTz = Intl.DateTimeFormat().resolvedOptions().timeZone || '';
    } catch (e) {
        clientTz = '';
    }

    // Devolvemos un objeto limpio con los valores finales
    return { from, to, clientTz };
}