const BASE_URL = 'https://api.mercadopublico.cl/servicios/v1/licitaciones.json';
const TICKET = 'TU_TICKET_AQUI'; // O toma desde variable de entorno/config

// Palabras clave para filtrar servicios de seguridad y aseo
const KEYWORDS = [
  'seguridad', 'guardia', 'vigilancia', 'aseo', 'limpieza', 'sanitización', 'conserje'
];

// Función para consultar y filtrar
export async function getFilteredLicitaciones({ fechaDesde, fechaHasta }) {
  const url = `${BASE_URL}?fechaDesde=${fechaDesde}&fechaHasta=${fechaHasta}&ticket=${TICKET}`;
  try {
    const response = await fetch(url);
    if (!response.ok) throw new Error('Error al consultar Mercado Público');
    const data = await response.json();

    // Asumiendo que el array de licitaciones está en data['Listado']
    const results = data.Listado || [];
    // Filtrar por palabras clave en nombre o descripcion
    const filtradas = results.filter(item => {
      const texto = `${item.Nombre} ${item.Descripcion}`.toLowerCase();
      return KEYWORDS.some(kw => texto.includes(kw));
    });

    return filtradas;
  } catch (error) {
    console.error('Error al obtener licitaciones:', error);
    return [];
  }
}

// Ejemplo de uso
getFilteredLicitaciones({
  fechaDesde: '2024-10-01',
  fechaHasta: '2024-10-14'
}).then(licitaciones => {
  console.log('Licitaciones de seguridad y aseo:', licitaciones);
});