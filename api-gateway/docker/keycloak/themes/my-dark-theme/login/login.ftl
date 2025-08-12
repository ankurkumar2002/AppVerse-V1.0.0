<#-- Minimal safe login.ftl -->
<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo; section>
    <#assign mode = request.getParameter("mode")!"user">
    <#if section = "title">
        Login - ${mode?capitalize}
    <#elseif section = "head">
        <link rel="stylesheet" href="${url.resourcesPath}/css/${mode}.css">
    <#elseif section = "form">
        <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <label for="username">Username</label>
                <input id="username" name="username" type="text" autofocus>
            </div>
            <div class="${properties.kcFormGroupClass!}">
                <label for="password">Password</label>
                <input id="password" name="password" type="password">
            </div>
            <div class="${properties.kcFormGroupClass!}">
                <input type="submit" name="login" value="Log in as ${mode}">
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
