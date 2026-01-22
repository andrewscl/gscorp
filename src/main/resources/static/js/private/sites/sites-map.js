import{f as u}from"../../auth.js";let r,s=[],c=[];function f(){const e=document.getElementById("site-map");if(console.log("Google Maps Config:",googleMapsConfig),!e||!window.google||!google.maps){a("No se pudo cargar Google Maps.");return}r=new google.maps.Map(e,{center:{lat:-33.45,lng:-70.65},zoom:8,mapTypeId:"roadmap",mapId:googleMapsConfig.mapId}),document.getElementById("site-select")?.addEventListener("change",y),E()}function a(e){const o=document.getElementById("site-map-error");o&&(o.textContent=e,o.style.display="block")}function v(){const e=document.getElementById("site-map-error");e&&(e.style.display="none")}function h(e){s.forEach(n=>n.setMap(null)),s=[],c=e;const o=document.getElementById("site-select");o&&(o.innerHTML='<option value="">Seleccionar sitio</option>');const t=new google.maps.LatLngBounds;e.forEach(n=>{if(typeof n.lat=="number"&&typeof n.lon=="number"){const i={lat:n.lat,lng:n.lon},g=document.createElement("div");g.innerHTML=`
        <div style="background-color: white; border: 1px solid black; padding: 5px; border-radius: 3px;">
          <strong>${n.name}</strong>
        </div>
      `;const l=new google.maps.marker.AdvancedMarkerElement({map:r,position:i,title:n.name,content:g}),m=new google.maps.InfoWindow({content:`<h4>${n.name}</h4><p>Dirección: ${n.address}</p><p>Zona horaria: ${n.timeZone}</p>`});if(l.addListener("click",()=>m.open(r,l)),s.push(l),t.extend(i),o){const d=document.createElement("option");d.value=n.id,d.textContent=n.name,o.appendChild(d)}}}),e.length>0?r.fitBounds(t):(r.setCenter({lat:-33.45,lng:-70.65}),r.setZoom(8),a("No hay sitios para mostrar.")),w()}function y(){const e=document.getElementById("site-select");if(!e)return;const o=e.value,t=c.find(n=>String(n.id)===o);t&&(r.setCenter({lat:t.lat,lng:t.lon}),r.setZoom(15))}async function E(){try{const e=await u("/api/sites/projections-by-user",{method:"GET",headers:{Accept:"application/json"}});if(!e.ok){a("Error al cargar sitios. Intente nuevamente.");return}const o=await e.json();if(v(),!Array.isArray(o)||!o.every(t=>t.id&&t.lat&&t.lon)){a("Datos de sitios inválidos.");return}h(o)}catch(e){console.error("Fallo al obtener sitios:",e),a("Error al cargar sitios. Intente nuevamente.")}}function p(e=0){window.google&&window.google.maps?f():e<10?setTimeout(()=>p(e+1),300+150*e):a("No se pudo cargar Google Maps. Intente más tarde.")}globalThis.waitForGoogleMapsAndInit=p;function w(){const e=document.getElementById("site-select");e&&(e.addEventListener("mouseover",o=>{const t=o.target.value;if(!t)return;const n=c.find(i=>String(i.id)===t);n&&s.forEach(i=>{i.title===n.name?i.content.innerHTML=`
            <div style="background-color: #0000FF; color: white; border-radius: 8px; padding: 5px;">
              <strong>${n.name}</strong>
            </div>
          `:i.content.innerHTML=`
            <div style="background-color: #FF0000; color: white; border-radius: 8px; padding: 5px;">
              <strong>${i.title}</strong>
            </div>
          `})}),e.addEventListener("mouseleave",()=>{s.forEach(o=>{o.content.innerHTML=`
        <div style="background-color: #FF0000; color: white; border-radius: 8px; padding: 5px;">
          <strong>${o.title}</strong>
        </div>
      `})}))}(function(){p()})();
