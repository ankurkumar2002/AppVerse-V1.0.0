<#-- developer-theme/login/register.ftl -->
<#import "template.ftl" as layout>

<@layout.registrationLayout; section>
    <#if section == "title">
        Developer Portal 👨‍💻 - Register
    <#elseif section == "form">
        <h2>Create Your Developer Account</h2>
        <form id="kc-register-form" action="${url.registrationAction}" method="post">

            <div class="form-group">
                <label for="username">Username</label>
                <input tabindex="1" id="username" name="username"
                       value="${(register.formData.username!'')}" type="text" autofocus required />
            </div>

            <div class="form-group">
                <label for="firstName">First Name</label>
                <input tabindex="2" id="firstName" name="firstName"
                       value="${(register.formData.firstName!'')}" type="text" required />
            </div>

            <div class="form-group">
                <label for="lastName">Last Name</label>
                <input tabindex="3" id="lastName" name="lastName"
                       value="${(register.formData.lastName!'')}" type="text" required />
            </div>

            <div class="form-group">
                <label for="email">Email</label>
                <input tabindex="4" id="email" name="email"
                       value="${(register.formData.email!'')}" type="email" required />
            </div>

            <div class="form-group">
                <label for="password">Password</label>
                <input tabindex="5" id="password" name="password" type="password" required />
            </div>

            <div class="form-group">
                <label for="password-confirm">Confirm Password</label>
                <input tabindex="6" id="password-confirm" name="password-confirm" type="password" required />
            </div>

            <div class="form-buttons">
                <input tabindex="7" type="submit" value="Register" />
            </div>

            <#-- Display error messages -->
            <#if messagesPerField.existsError('username','firstName','lastName','email','password','password-confirm')>
                <div class="alert alert-danger" style="margin-top:15px; color:#f85149;">
                    <#list ['username','firstName','lastName','email','password','password-confirm'] as field>
                        <#if messagesPerField.existsError(field)>
                            <div>${kcSanitize(messagesPerField.getFirstError(field))?no_esc}</div>
                        </#if>
                    </#list>
                </div>
            </#if>

        </form>
    </#if>
</@layout.registrationLayout>
