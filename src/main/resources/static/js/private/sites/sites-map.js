import{f as m}from"../../auth.js";let i,a=[],l=[];function u(){const e=document.getElementById("site-map");console.log("Google Maps Config:",googleMapsConfig),i=new google.maps.Map(e,{center:{lat:-33.45,lng:-70.65},zoom:8,mapTypeId:"roadmap",mapId:googleMapsConfig.mapId}),document.getElementById("site-select")?.addEventListener("change",h),y()}function s(e){const t=document.getElementById("site-map-error");t&&(t.textContent=e,t.style.display="block")}function f(){const e=document.getElementById("site-map-error");e&&(e.style.display="none")}function v(e){a.forEach(n=>n.setMap(null)),a=[],l=e;const t=document.getElementById("site-select");t&&(t.innerHTML='<option value="">Seleccionar sitio</option>');const o=new google.maps.LatLngBounds;e.forEach(n=>{if(typeof n.lat=="number"&&typeof n.lon=="number"){const r={lat:n.lat,lng:n.lon},p=document.createElement("div");p.innerHTML=`
        <div style="background-color: white; border: 1px solid black; padding: 5px; border-radius: 3px;">
          <strong>${n.name}</strong>
        </div>
      `;const c=new google.maps.marker.AdvancedMarkerElement({map:i,position:r,title:n.name,content:p}),g=new google.maps.InfoWindow({content:`<h4>${n.name}</h4><p>Dirección: ${n.address}</p><p>Zona horaria: ${n.timeZone}</p>`});if(c.addListener("click",()=>g.open(i,c)),a.push(c),o.extend(r),t){const d=document.createElement("option");d.value=n.id,d.textContent=n.name,t.appendChild(d)}}}),e.length>0?i.fitBounds(o):(i.setCenter({lat:-33.45,lng:-70.65}),i.setZoom(8),s("No hay sitios para mostrar.")),E()}function h(){const e=document.getElementById("site-select");if(!e)return;const t=e.value,o=l.find(n=>String(n.id)===t);o&&(i.setCenter({lat:o.lat,lng:o.lon}),i.setZoom(15))}async function y(){try{const e=await m("/api/sites/projections-by-user",{method:"GET",headers:{Accept:"application/json"}});if(!e.ok){s("Error al cargar sitios. Intente nuevamente.");return}const t=await e.json();if(f(),!Array.isArray(t)||!t.every(o=>o.id&&o.lat&&o.lon)){s("Datos de sitios inválidos.");return}v(t)}catch(e){console.error("Fallo al obtener sitios:",e),s("Error al cargar sitios. Intente nuevamente.")}}function E(){const e=document.getElementById("site-select");e&&(e.addEventListener("mouseover",t=>{const o=t.target.value;if(!o)return;const n=l.find(r=>String(r.id)===o);n&&a.forEach(r=>{r.title===n.name?r.content.innerHTML=`
            <div style="background-color: #0000FF; color: white; border-radius: 8px; padding: 5px;">
              <strong>${n.name}</strong>
            </div>
          `:r.content.innerHTML=`
            <div style="background-color: #FF0000; color: white; border-radius: 8px; padding: 5px;">
              <strong>${r.title}</strong>
            </div>
          `})}),e.addEventListener("mouseleave",()=>{a.forEach(t=>{t.content.innerHTML=`
        <div style="background-color: #FF0000; color: white; border-radius: 8px; padding: 5px;">
          <strong>${t.title}</strong>
        </div>
      `})}))}(function(){u()})();
