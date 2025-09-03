<#-- user-theme/login/register-form.ftl -->
<form id="kc-register-form" class="form-container" action="${url.registrationAction}" method="post">

    <div class="form-field">
        <label for="firstName">First Name</label>
        <input type="text" id="firstName" name="firstName" value="${(register.formData.firstName!'')}" required autofocus>
    </div>

    <div class="form-field">
        <label for="lastName">Last Name</label>
        <input type="text" id="lastName" name="lastName" value="${(register.formData.lastName!'')}" required>
    </div>

    <div class="form-field">
        <label for="username">Username</label>
        <input type="text" id="username" name="username" value="${(register.formData.username!'')}" autocomplete="username" required>
    </div>

    <div class="form-field">
        <label for="email">Email</label>
        <input type="email" id="email" name="email" value="${(register.formData.email!'')}" autocomplete="email" required>
    </div>

    <div class="form-field">
        <label for="password">Password</label>
        <input type="password" id="password" name="password" autocomplete="new-password" required>
    </div>

    <div class="form-field">
        <label for="password-confirm">Confirm Password</label>
        <input type="password" id="password-confirm" name="password-confirm" autocomplete="new-password" required>
    </div>

    <div class="form-field">
        <button type="submit" class="btn-primary">Register</button>
    </div>

    <div class="form-footer">
        <p>Already have an account? <a href="${url.loginUrl}">Login here</a></p>
    </div>
</form>
