<html>
<head>
    <title>Google Login Debug Redirect</title>
</head>
<body>
<#if password??>
<form id="loginForm" method="POST" action="http://localhost/share/page/dologin">
    <input name="username" value="${user}"/>
    <input name="password" value="${password}"/>
    <input name="success" value="/share/page/"/>
    <input name="failure" value="/share/page/?error=true"/>
</form>
<#if error??>
<p>Error: ${error}</p>
</#if>
<#else>
<p>Login failed: </p>
<#if error??>
<p>Error: ${error}</p>
</#if>
</#if>
</body>
</html>
