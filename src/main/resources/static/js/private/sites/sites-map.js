import{f as m}from"../../auth.js";let i,a=[],c=[];function u(){const e=document.getElementById("site-map");if(console.log("mapDiv:",e),console.log("Google Maps Config:",googleMapsConfig),!e||!window.google||!google.maps){console.log("google.maps:",typeof google<"u"&&typeof google.maps<"u"),s("No se pudo cargar Google Maps.");return}i=new google.maps.Map(e,{center:{lat:-33.45,lng:-70.65},zoom:8,mapTypeId:"roadmap",mapId:googleMapsConfig.mapId}),document.getElementById("site-select")?.addEventListener("change",y),h()}function s(e){const o=document.getElementById("site-map-error");o&&(o.textContent=e,o.style.display="block")}function f(){const e=document.getElementById("site-map-error");e&&(e.style.display="none")}function v(e){a.forEach(n=>n.setMap(null)),a=[],c=e;const o=document.getElementById("site-select");o&&(o.innerHTML='<option value="">Seleccionar sitio</option>');const t=new google.maps.LatLngBounds;e.forEach(n=>{if(typeof n.lat=="number"&&typeof n.lon=="number"){const r={lat:n.lat,lng:n.lon},p=document.createElement("div");p.innerHTML=`
        <div style="background-color: white; border: 1px solid black; padding: 5px; border-radius: 3px;">
          <strong>${n.name}</strong>
        </div>
      `;const l=new google.maps.marker.AdvancedMarkerElement({map:i,position:r,title:n.name,content:p}),g=new google.maps.InfoWindow({content:`<h4>${n.name}</h4><p>Dirección: ${n.address}</p><p>Zona horaria: ${n.timeZone}</p>`});if(l.addListener("click",()=>g.open(i,l)),a.push(l),t.extend(r),o){const d=document.createElement("option");d.value=n.id,d.textContent=n.name,o.appendChild(d)}}}),e.length>0?i.fitBounds(t):(i.setCenter({lat:-33.45,lng:-70.65}),i.setZoom(8),s("No hay sitios para mostrar.")),E()}function y(){const e=document.getElementById("site-select");if(!e)return;const o=e.value,t=c.find(n=>String(n.id)===o);t&&(i.setCenter({lat:t.lat,lng:t.lon}),i.setZoom(15))}async function h(){try{const e=await m("/api/sites/projections-by-user",{method:"GET",headers:{Accept:"application/json"}});if(!e.ok){s("Error al cargar sitios. Intente nuevamente.");return}const o=await e.json();if(f(),!Array.isArray(o)||!o.every(t=>t.id&&t.lat&&t.lon)){s("Datos de sitios inválidos.");return}v(o)}catch(e){console.error("Fallo al obtener sitios:",e),s("Error al cargar sitios. Intente nuevamente.")}}function E(){const e=document.getElementById("site-select");e&&(e.addEventListener("mouseover",o=>{const t=o.target.value;if(!t)return;const n=c.find(r=>String(r.id)===t);n&&a.forEach(r=>{r.title===n.name?r.content.innerHTML=`
            <div style="background-color: #0000FF; color: white; border-radius: 8px; padding: 5px;">
              <strong>${n.name}</strong>
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
