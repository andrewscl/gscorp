import{f as i}from"../../../auth.js";import"../../../signup.js";import"../../../navigation-handler.js";const a=s=>document.querySelector(s);let n=[];const o=async()=>{try{const s=await i("/api/users/admin-dashboard-metrics",{credentials:"same-origin"});if(!s.ok)throw new Error(`Error cargando dashboard metrics: ${s.status}`);n=await s.json(),d(n),u(n)}catch(s){console.error("No se pudo cargar la lista de metrics:",s),n=[]}};function u(s){const t=s.userStatusSummary[0];t&&(a("#kpi-invited").innerText=t.invitedCount,a("#kpi-active").innerText=t.activeCount,a("#kpi-inactive").innerText=t.inactiveCount,a("#kpi-expired").innerText=t.expiredCount,a("#kpi-suspended").innerText=t.suspendedCount)}const d=s=>{const t=a("#userStatsContainer");t&&s.userStatusSummary&&(s.userStatusSummary.length===0?t.innerHTML='<p class="text-muted text-center py-2">No hay datos de usuarios</p>':t.innerHTML=s.userStatusSummary.map((e,r)=>`
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${e.userStatus}</strong></span>
                        <span class="stat-badge user-active" style="background-color: var(--bs-success-soft); color: var(--bs-success);">${e.stats.activeUsersCount} En Línea</span>
                    </div>
                    <div class="stat-details">
                        <small>✉️ ${e.userStatusSummary.invitedUsersCount} Invitados</small> | <small>🔴 ${e.stats.inactiveUsersCount} Inactivos</small> | <small>⏳ ${e.stats.expiredUsersCount} Expirados</small>
                    </div>
                </div>
                ${r<s.userStatusSummary.length-1?"<hr>":""}
            `).join(""))};(async function(){console.log("🚀 Inicializando Dashboard de Administración..."),await o()})();
