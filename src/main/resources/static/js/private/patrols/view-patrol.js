import{f}from"../../auth.js";import{n as u}from"../../navigation-handler.js";import{l as y}from"../../shared/maps/googlemaps-loader.js";import{i as w}from"../../shared/maps/init-map.js";import{a as h}from"../../shared/maps/advanced-marker.js";let c=[],b=[],r=null,i=null;const d=e=>document.querySelector(e),k=()=>{setTimeout(()=>u("/private/patrols/table-view",!0),1e3)},v=async()=>{const e=googleMapsConfig.apiKey,a=d("#siteId").value;try{console.log("Loading Google Maps API..."),await y(e);const o=await w("map",{mapTypeId:"hybrid",zoom:10});window.mapInstance=o,mapInstance=o,o.addListener("click",()=>{i&&i.close()});const n=await(await f(`/api/sites/${a}`,{method:"GET",headers:{Accept:"application/json"}})).json();console.log("Site data:",n);const t=await h(o,n.name,n.lat,n.lon),s=new google.maps.LatLngBounds;return s.extend({lat:parseFloat(n.lat),lng:parseFloat(n.lon)}),o.fitBounds(s),o.setZoom(15),await I(),{map:o,siteData:n,initialMarker:t}}catch(o){console.error("[site-map.js] Error al cargar la API de Google Maps:",o)}},I=async()=>{const e=document.getElementById("checkpoints-data");if(!e||!e.value){console.warn("No se encontraron puntos pre-cargados en el DOM.");return}try{const a=JSON.parse(e.value);if(a.length===0)return;const{AdvancedMarkerElement:o,PinElement:p}=await google.maps.importLibrary("marker");i||(i=new google.maps.InfoWindow);const n=new google.maps.LatLngBounds;for(const t of a){const s={lat:parseFloat(t.latitude),lng:parseFloat(t.longitude)};n.extend(s);const m=new p({glyph:t.checkpointOrder.toString(),background:"#FBBC04",borderColor:"#137333",glyphColor:"white"}),l=new o({map:window.mapInstance,position:s,content:m.element,title:t.name,gmpDraggable:!1});l.addListener("click",()=>{i.close();const g=`
                    <div style="font-family: 'Segoe UI', Roboto, sans-serif; padding: 6px; min-width: 170px; color: #1e293b;">
                        <div style="border-bottom: 2px solid #3b82f6; padding-bottom: 4px; margin-bottom: 6px;">
                            <strong style="font-size: 13px; display: block; color: #1e3a8a;">${t.name}</strong>
                        </div>
                        <div style="font-size: 11px; line-height: 1.5;">
                            <div style="display: flex; justify-content: space-between; margin-bottom: 2px;">
                                <span style="color: #64748b;">Orden:</span>
                                <span style="font-weight: 600; color: #0f172a;">N° ${t.checkpointOrder}</span>
                            </div>
                            <div style="display: flex; justify-content: space-between; margin-bottom: 2px;">
                                <span style="color: #64748b;">Estadía:</span>
                                <span style="font-weight: 600; color: #0f172a;">${t.stayTime} min</span>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span style="color: #64748b;">Tránsito:</span>
                                <span style="font-weight: 600; color: #0f172a;">${t.minutesToReach||0} min</span>
                            </div>
                        </div>
                    </div>
                `;i.setContent(g),i.open({map:window.mapInstance,anchor:l})}),c.push({externalId:t.externalId,latitude:s.lat,longitude:s.lng,checkpointOrder:t.checkpointOrder,name:t.name,stayTime:t.stayTime,transitTime:t.minutesToReach}),b.push(l)}x(),window.mapInstance.fitBounds(n)}catch(a){console.error("Error al parsear los puntos ocultos:",a)}};function x(){const e=c.map(a=>({lat:a.latitude,lng:a.longitude}));r?r.setPath(e):r=new google.maps.Polyline({path:e,geodesic:!0,strokeColor:"#FF0000",strokeOpacity:1,strokeWeight:3,map:window.mapInstance})}function L(){const e=d(".btn-secondary");e&&e.addEventListener("click",k)}(async function(){console.log("[view-patrol init] IIFE iniciado"),googleMapsConfig.apiKey,L(),await v(),console.log("View patrol page initialized.")})();
