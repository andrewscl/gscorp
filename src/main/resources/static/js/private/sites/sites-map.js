import{f as m}from"../../auth.js";let i,a=[],l=[];function u(){const e=document.getElementById("site-map");if(console.log("Google Maps Config:",googleMapsConfig),!e||!window.google||!google.maps){s("No se pudo cargar Google Maps.");return}i=new google.maps.Map(e,{center:{lat:-33.45,lng:-70.65},zoom:8,mapTypeId:"roadmap",mapId:googleMapsConfig.mapId}),document.getElementById("site-select")?.addEventListener("change",h),y()}function s(e){const o=document.getElementById("site-map-error");o&&(o.textContent=e,o.style.display="block")}function f(){const e=document.getElementById("site-map-error");e&&(e.style.display="none")}function v(e){a.forEach(t=>t.setMap(null)),a=[],l=e;const o=document.getElementById("site-select");o&&(o.innerHTML='<option value="">Seleccionar sitio</option>');const n=new google.maps.LatLngBounds;e.forEach(t=>{if(typeof t.lat=="number"&&typeof t.lon=="number"){const r={lat:t.lat,lng:t.lon},p=document.createElement("div");p.innerHTML=`
        <div style="background-color: white; border: 1px solid black; padding: 5px; border-radius: 3px;">
          <strong>${t.name}</strong>
        </div>
      `;const c=new google.maps.marker.AdvancedMarkerElement({map:i,position:r,title:t.name,content:p}),g=new google.maps.InfoWindow({content:`<h4>${t.name}</h4><p>Dirección: ${t.address}</p><p>Zona horaria: ${t.timeZone}</p>`});if(c.addListener("click",()=>g.open(i,c)),a.push(c),n.extend(r),o){const d=document.createElement("option");d.value=t.id,d.textContent=t.name,o.appendChild(d)}}}),e.length>0?i.fitBounds(n):(i.setCenter({lat:-33.45,lng:-70.65}),i.setZoom(8),s("No hay sitios para mostrar.")),E()}function h(){const e=document.getElementById("site-select");if(!e)return;const o=e.value,n=l.find(t=>String(t.id)===o);n&&(i.setCenter({lat:n.lat,lng:n.lon}),i.setZoom(15))}async function y(){try{const e=await m("/api/sites/projections-by-user",{method:"GET",headers:{Accept:"application/json"}});if(!e.ok){s("Error al cargar sitios. Intente nuevamente.");return}const o=await e.json();if(f(),!Array.isArray(o)||!o.every(n=>n.id&&n.lat&&n.lon)){s("Datos de sitios inválidos.");return}v(o)}catch(e){console.error("Fallo al obtener sitios:",e),s("Error al cargar sitios. Intente nuevamente.")}}function E(){const e=document.getElementById("site-select");e&&(e.addEventListener("mouseover",o=>{const n=o.target.value;if(!n)return;const t=l.find(r=>String(r.id)===n);t&&a.forEach(r=>{r.title===t.name?r.content.innerHTML=`
            <div style="background-color: #0000FF; color: white; border-radius: 8px; padding: 5px;">
              <strong>${t.name}</strong>
            </div>
          `:r.content.innerHTML=`
            <div style="background-color: #FF0000; color: white; border-radius: 8px; padding: 5px;">
              <strong>${r.title}</strong>
            </div>
          `})}),e.addEventListener("mouseleave",()=>{a.forEach(o=>{o.content.innerHTML=`
        <div style="background-color: #FF0000; color: white; border-radius: 8px; padding: 5px;">
          <strong>${o.title}</strong>
        </div>
      `})}))}(function(){u()})();
