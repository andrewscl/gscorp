function a({containerEl:e,items:n,emptyLabel:o="Sin ítems disponibles",valueKey:l="externalId",inputName:r="projectIds",preservedValues:i=[]}){if(e){if(!n||n.length===0){e.innerHTML=`<span class="empty-msg" style="font-size:0.85rem; color:#64748b;">${o}</span>`;return}e.innerHTML=n.map(s=>{const c=s[l]||s.id||"",p=i.includes(String(c))?"checked":"",t=`project_${c}`;return`
            <div class="checkbox-item">
                <input type="checkbox" 
                       name="${r}" 
                       id="${t}" 
                       value="${c}" 
                       ${p} />
                <label for="${t}">${s.name||""}</label>
            </div>
        `}).join("")}}export{a as p};
