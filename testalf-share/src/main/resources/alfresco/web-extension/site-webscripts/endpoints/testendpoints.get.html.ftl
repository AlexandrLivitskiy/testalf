<html>
<head>
    <title>Share Remote Endpoints</title>
</head>
<body>
<h2>Available Remote Endpoints</h2>
<ul>
    <#if (endpoints??) && (endpoints?size > 0)>
        <#list endpoints as e>
        <li>
        ${e.id!"(unknown)"} : <b>${e.exists?string("Yes", "No")}</b>
        <#if e.error??> (Error: ${e.error})</#if>
        </li>
        </#list>
    <#else>
        <li>No endpoints found</li>
    </#if>
</ul>
</body>
</html>
