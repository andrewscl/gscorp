import{f as c}from"../../auth.js";const s=a=>document.querySelector(a);let e=[];const l=async()=>{try{const a=await c("/api/employees/hr-dashboard-metrics",{credentials:"same-origin"});if(!a.ok)throw new Error(`Error cargando dashboard metrics: ${a.status}`);e=await a.json(),d(e),m(e)}catch(a){console.error("No se pudo cargar la lista de metrics:",a),e=[]}};function m(a){const n=a.employeeStatusSummary[0];n&&(s("#kpi-hired").innerText=n.hiredCount,s("#kpi-active").innerText=n.activeCount,s("#kpi-notice").innerText=n.noticeGivenCount,s("#kpi-inactive").innerText=n.inactiveCount,s("#kpi-settled").innerText=n.settledCount)}const d=a=>{const n=s("#companyStatsContainer");n&&a.companyEmployeesStatusSummary&&(a.companyEmployeesStatusSummary.length===0?n.innerHTML='<p class="text-muted text-center py-2">No hay datos de empresas</p>':n.innerHTML=a.companyEmployeesStatusSummary.map((t,i)=>`
                <div class="stat-item">
                    <div class="stat-main-info">
                        <span class="stat-name"><strong>${t.companyName}</strong></span>
                        <span class="stat-badge">${t.activeCount} Activos</span>
                    </div>
                    <div class="stat-details">
                        <small>⏱️ ${t.hiredCount} Por Ingresar</small> | <small>⚠️ ${t.noticegivenCount} En Aviso</small>
                    </div>
                </div>
                ${i<a.companyEmployeesStatusSummary.length-1?"<hr>":""}
            `).join(""));const o=s("#clientStatsContainer");o&&a.companyEmployeesStatusSummary&&(a.companyEmployeesStatusSummary.length===0?o.innerHTML='<p class="text-muted text-center py-2">No hay datos de clientes</p>':o.innerHTML=a.companyEmployeesStatusSummary.map((t,i)=>{const r=t.activeCount+t.hiredCount+t.noticegivenCount;return`
                    <div class="stat-item">
                        <div class="stat-main-info">
                            <span class="stat-name"><strong>${t.clientName}</strong></span>
                            <span class="stat-badge">${r} Asignados</span>
                        </div>
                        <div class="stat-details">
                            <small>🟢 ${t.activeCount} Activos</small> | <small>⏱️ ${t.hiredCount} Próximos</small>
                        </div>
                    </div>
                    ${i<a.companyEmployeesStatusSummary.length-1?"<hr>":""}
                `}).join(""))};(async function(){console.log("🚀 Inicializando Dashboard de Recursos Humanos..."),await l()})();
