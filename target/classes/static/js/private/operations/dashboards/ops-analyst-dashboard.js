import{f as p}from"../../../auth.js";import"../../../signup.js";import"../../../navigation-handler.js";const l=e=>document.querySelector(e),m=async()=>{try{const e=await p("/api/operations/ops-dashboard-metrics",{credentials:"same-origin"});if(!e.ok)throw new Error(`Error cargando dashboard metrics: ${e.status}`);metricList=await e.json(),u(metricList)}catch(e){console.error("No se pudo cargar la lista de attendance metrics:",e),metricList=[]}},u=e=>{const i=l("#operationStatsContainer");if(!i)return;const o=e?.projectSiteShiftsSummary||[],c=e?.projectSiteAttendancesSummary||[];if(o.length===0&&c.length===0){i.innerHTML='<p class="text-muted text-center py-2">No hay datos de asistencia</p>';return}const s={},d=(t,a,r,n)=>(s[t]||(s[t]={projectName:a||"Proyecto General",sites:{}}),s[t].sites[r]||(s[t].sites[r]={siteName:n||"Instalación",attendanceCount:0,pendingShiftsCount:0}),s[t].sites[r]);c.forEach(t=>{const a=d(t.projectId||0,t.projectName,t.siteId||0,t.siteName);a.attendanceCount=t.attendanceCount??0}),o.forEach(t=>{const a=d(t.projectId||0,t.projectName,t.siteId||0,t.siteName);a.pendingShiftsCount=t.totalShiftsToday??0}),attendanceContainer.innerHTML=Object.values(s).map((t,a,r)=>`
        <div class="project-group mb-3">
            <div class="project-header d-flex align-items-center gap-2 mb-2">
                <span class="fs-6 fw-bold text-secondary">📂 ${t.projectName}</span>
            </div>
            
            <div class="project-sites-list ps-3 border-start">
                ${Object.values(t.sites).map(n=>`
                    <div class="stat-item py-2 border-bottom border-light">
                        <div class="stat-main-info d-flex justify-content-between align-items-center flex-wrap gap-2">
                            <span class="stat-name text-dark fw-medium">🏢 ${n.siteName}</span>
                            
                            <div class="d-flex gap-2">
                                <span class="badge bg-light text-primary border border-primary-subtle px-2 py-1">
                                    👤 ${n.attendanceCount} Asistencias
                                </span>
                                <span class="badge ${n.pendingShiftsCount>0?"bg-danger-subtle text-danger":"bg-light text-muted"} px-2 py-1">
                                    🚨 ${n.pendingShiftsCount} Por Cubrir
                                </span>
                            </div>
                        </div>
                    </div>
                `).join("")}
            </div>
        </div>
        ${a<r.length-1?'<hr class="my-3 opacity-25">':""}
    `).join("")};(async function(){console.log("🚀 Inicializando Dashboard de Operaciones..."),await m()})();
