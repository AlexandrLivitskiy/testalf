<html>
<head>
    <title>Google Login Redirect</title>
</head>
<body>
<#if password??>
<form id="loginForm" method="POST" action="https://testalf.onu.edu.ua/share/page/dologin">
    <input type="hidden" name="username" value="${user}"/>
    <input type="hidden" name="password" value="${password}"/>
    <input type="hidden" name="success" value="/share/page/"/>
    <input type="hidden" name="failure" value="/share/page/?error=true"/>
</form>
<script>
    document.getElementById("loginForm").submit();
</script>
<#else>
<p>Login failed: </p>
<#if error??>
<p>Error: ${error}</p>
</#if>
</#if>
</body>
</html>
