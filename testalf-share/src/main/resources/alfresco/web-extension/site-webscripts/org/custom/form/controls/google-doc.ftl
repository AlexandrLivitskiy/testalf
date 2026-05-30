<#assign jsonString = field.value!"" />
<#assign iconUrl = "" />
<#assign docName = "" />
<#assign docUrl = "" />
<!--<#assign iconUrl = field.control.params.iconUrl?default("")>-->

<#if jsonString?has_content>

<#-- Simple JSON parsing without eval_json -->
<#assign docName = jsonString?replace('.*"name":"([^"]*)".*', '$1', 'r') />
<#assign docUrl = jsonString?replace('.*"url":"([^"]*)".*', '$1', 'r') />
<#assign iconUrl = jsonString?replace('.*"icon":"([^"]*)".*', '$1', 'r') />

<div style="display:flex;align-items:center;gap:10px;padding:10px;border:1px solid #ddd;border-radius:6px;">
    <img src="${iconUrl}" style="width:40px;">
    <div>
        <div style="font-weight:bold">${docName}</div>
        <a href="${docUrl}" target="_blank">${docUrl}</a>
    </div>
</div>

<#else>

<div style="color:#999;">
<!--    Документ ще не створено-->
</div>

</#if>
