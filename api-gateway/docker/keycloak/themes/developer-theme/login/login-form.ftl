<#-- developer-theme/login/login-form.ftl -->

<form id="kc-form-login" onsubmit="login.disabled = true;" action="${url.loginAction}" method="post">
    <div class="form-group">
        <label for="username">Username</label>
        <input tabindex="1" id="username" name="username" value="${username!}" type="text" autofocus/>
    </div>
    
    <div class="form-group">
        <label for="password">Password</label>
        <input tabindex="2" id="password" name="password" type="password"/>
    </div>
    
    <div class="form-buttons">
        <input tabindex="3" type="submit" id="kc-login" name="login" value="Log In"/>
    </div>
    
    <#-- Optional: forgot password link -->
    <#if url.loginResetCredentials>
        <p><a href="${url.loginResetCredentials}">Forgot Password?</a></p>
    </#if>
</form>
