import{f as c}from"../../../auth.js";import"../../../signup.js";import"../../../navigation-handler.js";const n=s=>document.querySelector(s);let i=[];const d=async()=>{try{const s=await c("/api/users/admin-dashboard-metrics",{credentials:"same-origin"});if(!s.ok)throw new Error(`Error cargando dashboard metrics: ${s.status}`);i=await s.json(),p(i),l(i)}catch(s){console.error("No se pudo cargar la lista de metrics:",s),i=[]}};function l(s){const t=s.usersStatusSummary[0];t&&(n("#kpi-invited").innerText=t.invitedCount,n("#kpi-active").innerText=t.activeCount,n("#kpi-inactive").innerText=t.inactiveCount,n("#kpi-expired").innerText=t.expiredCount,n("#kpi-suspended").innerText=t.suspendedCount)}const p=s=>{const t=n("#companyStatsContainer"),o=n("#roleStatsContainer");if(t){const a=s?.companyUsersSummary||[];a.length===0?t.innerHTML='<p class="text-muted text-center py-2">No hay datos de empresas</p>':t.innerHTML=a.map((e,r)=>`
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${e.companyName||"Empresa sin Nombre"}</strong></span>
                        <span class="stat-badge user-active" style="background-color: var(--bs-success-soft); color: var(--bs-success);">${e.activeCount??0} Activos</span>
                    </div>
                    <div class="stat-details">
                        <small>✉️ ${e.invitedCount??0} Invitados</small> | <small>🔴 ${e.inactiveCount??0} Inactivos</small>
                    </div>
                </div>
                ${r<a.length-1?"<hr>":""}
            `).join("")}if(o){const a=s?.roleUsersSummary||[];a.length===0?o.innerHTML='<p class="text-muted text-center py-2">No hay datos de roles</p>':o.innerHTML=a.map((e,r)=>`
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name">💼 <strong>${e.roleName||"Rol del Sistema"}</strong></span>
                        <span class="badge bg-primary px-3 py-1">${e.totalUsers??0} Usuarios</span>
                    </div>
                </div>
                ${r<a.length-1?"<hr>":""}
            `).join("")}};(async function(){console.log("🚀 Inicializando Dashboard de Administración..."),await d()})();
