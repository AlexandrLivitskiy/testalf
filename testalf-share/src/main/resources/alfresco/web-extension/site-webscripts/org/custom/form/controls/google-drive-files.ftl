<#assign raw = field.value!"" />

<#if raw?has_content>

<#list raw?split(";") as item>

<#assign parts = item?split("|")>

<#assign name = parts[0]!"" />
<#assign docURL = parts[1]!"" />

<div style="display:flex;align-items:center;gap:10px;
                    padding:8px 10px;
                    border:1px solid #ddd;
                    border-radius:6px;
                    background:#fafafa;
                    margin-bottom:6px;">

    <img src="https://ssl.gstatic.com/docs/doclist/images/drive_2022q3_32dp.png"
         style="width:24px;height:24px;" />

    <div>
        <div style="font-weight:bold">${name}</div>
        <a href="${docURL}" target="_blank"
           style="color:#0b79d0;text-decoration:none;">
            Відкрити файл
        </a>
    </div>
</div>

</#list>

</#if>
