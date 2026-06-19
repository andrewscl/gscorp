import{f as o}from"../../../auth.js";import"../../../signup.js";import"../../../navigation-handler.js";const n=e=>document.querySelector(e);let i=[];const d=async()=>{try{const e=await o("/api/users/admin-dashboard-metrics",{credentials:"same-origin"});if(!e.ok)throw new Error(`Error cargando dashboard metrics: ${e.status}`);i=await e.json(),l(i),c(i)}catch(e){console.error("No se pudo cargar la lista de metrics:",e),i=[]}};function c(e){const t=e.usersStatusSummary[0];t&&(n("#kpi-invited").innerText=t.invitedCount,n("#kpi-active").innerText=t.activeCount,n("#kpi-inactive").innerText=t.inactiveCount,n("#kpi-expired").innerText=t.expiredCount,n("#kpi-suspended").innerText=t.suspendedCount)}const l=e=>{const t=n("#roleStatsContainer");if(t){const s=e?.roleUsersSummary||[];s.length===0?t.innerHTML='<p class="text-muted text-center py-2">No hay datos de roles</p>':t.innerHTML=s.map((a,r)=>`
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name">💼 <strong>${a.roleName||"Rol del Sistema"}</strong></span>
                        <span class="badge bg-primary px-3 py-1">${a.totalUsers??0} Usuarios</span>
                    </div>
                </div>
                ${r<s.length-1?"<hr>":""}
            `).join("")}};(async function(){console.log("🚀 Inicializando Dashboard de Administración..."),await d()})();
