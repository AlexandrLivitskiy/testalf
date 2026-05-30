<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link href="${url.context}/res/components/guest/login.css" group="login"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script src="${url.context}/res/components/guest/login.js" group="login"/>
</@>

<@markup id="widgets">
   <@createWidgets group="login"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
      <#assign el=args.htmlid?html>
      <div id="${el}-body" class="theme-overlay login hidden">
      
      <@markup id="header">
         <div style="text-align: center;">
         <div class="theme-company-logo"></div>
         <div class="product-name">${msg("app.name")} ONU</div>
         <div class="product-tagline">${msg("app.tagline")}</div>
         <div class="product-community">${msg("app.community")}</div>
         </div>
      </@markup>
      
      <#if errorDisplay == "container">
      <@markup id="error">
         <#if error>
         <div class="error">${msg("message.loginautherror")}</div>
         <#else>
         <script type="text/javascript">//<![CDATA[
            <#assign cookieHeadersConfig = config.scoped["COOKIES"] />
            <#if cookieHeadersConfig?? && (cookieHeadersConfig.secure.getValue() == "true" || cookieHeadersConfig.secure.getValue() == "false")>
               Alfresco.constants.secureCookie = ${cookieHeadersConfig.secure.getValue()};
               Alfresco.constants.sameSite = "${cookieHeadersConfig.sameSite.getValue()}";
            </#if>

            var cookieDefinition = "_alfTest=_alfTest; Path=/;";
            if(Alfresco.constants.secureCookie)
            {
               cookieDefinition += " Secure;";
            }
            if(Alfresco.constants.sameSite)
            {
               cookieDefinition += " SameSite="+Alfresco.constants.sameSite+";";
            }
            document.cookie = cookieDefinition;

            var cookieEnabled = (document.cookie.indexOf("_alfTest") !== -1);
            if (!cookieEnabled)
            {
               document.write('<div class="error">${msg("message.cookieserror")}</div>');
            }
         //]]></script>
         </#if>
      </@markup>
      </#if>
      
      <@markup id="form">
         <form id="${el}-form" accept-charset="UTF-8" method="post" action="${loginUrl}" class="form-fields login" hidden>
            <@markup id="fields">
            <input type="hidden" id="${el}-success" name="success" value="${successUrl?replace("@","%40")?html}"/>
            <input type="hidden" name="failure" value="${failureUrl?replace("@","%40")?html}"/>
            <div class="product-community">1.2.1</div>
            <div class="form-field">
               <input type="text" id="${el}-username" name="username" maxlength="255" value="<#if lastUsername??>${lastUsername?html}</#if>" placeholder="${msg("label.username")}" />
            </div>
            <div class="form-field">
               <input type="password" id="${el}-password" name="password" maxlength="255" placeholder="${msg("label.password")}" />
            </div>
            </@markup>
            <@markup id="buttons">
            <div class="form-field">
               <input type="submit" id="${el}-submit" class="login-button" value="${msg("button.login")}"/>
            </div>
            <div style="text-align: center;">
               <a href="https://accounts.google.com/o/oauth2/v2/auth?client_id=429716899671-3frpeh79koph1r4dt57bo88ntna467pl.apps.googleusercontent.com&redirect_uri=https://testalf.onu.edu.ua/share/service/google/callback&response_type=code&scope=openid%20email%20profile%20https://www.googleapis.com/auth/drive&access_type=offline&prompt=consent"
                  style="display:inline-block; background:#42F485; color:white; padding:10px 20px; border-radius:4px; font-family:Arial, sans-serif; text-decoration:none; font-size:14px;">
                  <img src="https://developers.google.com/identity/images/g-logo.png"
                       alt="Google logo"
                       style="height:18px; vertical-align:middle; margin-right:8px;">
                  Google + Drive
               </a>
            </div>
            </@markup>
         </form>
<BR>
<div style="text-align: center;">
<a href="https://accounts.google.com/o/oauth2/v2/auth?client_id=429716899671-3frpeh79koph1r4dt57bo88ntna467pl.apps.googleusercontent.com&redirect_uri=https://testalf.onu.edu.ua/share/service/google/callback&response_type=code&scope=openid%20email%20profile"   style="display:inline-block; background:#4285F4; color:white; padding:10px 20px; border-radius:4px; font-family:Arial, sans-serif; text-decoration:none; font-size:14px;">
   <img src="https://developers.google.com/identity/images/g-logo.png"
        alt="Google logo"
        style="height:18px; vertical-align:middle; margin-right:8px;">
   Google Авторизація
</a>
</div>
      </@markup>
      
      <@markup id="preloader">
         <script type="text/javascript">//<![CDATA[
            window.onload = function() 
            {
                setTimeout(function()
                {
                    var xhr;
                    <#list dependencies as dependency>
                       xhr = new XMLHttpRequest();
                       xhr.open('GET', '<@checksumResource src="${url.context}/res/${dependency}"/>');
                       xhr.send('');
                    </#list>
                    <#list images as image>
                       new Image().src = "${url.context?js_string}/res/${image}";
                    </#list>
                }, 1000);
            };
         //]]></script>
      </@markup>

      </div>
      
      <@markup id="footer">
      <div class="login-copy">${msg("label.copyright")}</div>
      </@markup>
   </@>
</@>