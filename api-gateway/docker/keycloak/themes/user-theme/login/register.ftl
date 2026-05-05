<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>User Register</title>

    <style>
        body {
            margin: 0;
            font-family: Inter, sans-serif;
            background: linear-gradient(135deg, #dbeafe, #bfdbfe);
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }

        .card {
            width: 400px;
            padding: 36px;
            background: white;
            border-radius: 18px;
            box-shadow: 0 15px 40px rgba(0,0,0,0.12);
        }

        h2 {
            text-align: center;
            color: #1e3a8a;
            margin-bottom: 8px;
        }

        p {
            text-align: center;
            color: #64748b;
            margin-bottom: 24px;
        }

        input {
            width: 100%;
            padding: 14px;
            margin-bottom: 14px;
            border-radius: 10px;
            border: 1px solid #cbd5e1;
            box-sizing: border-box;
        }

        input[type="submit"] {
            background: #2563eb;
            color: white;
            border: none;
            font-weight: 600;
            cursor: pointer;
        }

        a {
            color: #2563eb;
            text-decoration: none;
        }

        .footer {
            text-align: center;
            margin-top: 14px;
        }

        .error-box {
            margin-top: 16px;
            padding: 12px;
            border-radius: 8px;
            background: #ffe5e5;
            border: 1px solid #ff4d4d;
            color: #cc0000;
        }
    </style>
</head>
<body>

<div class="card">
    <h2>Create User Account</h2>
    <p>Join AppVerse and explore applications.</p>

    <form action="${url.registrationAction}" method="post">

        <input name="firstName" placeholder="First Name" value="${(register.formData.firstName!'')}" required />
        <input name="lastName" placeholder="Last Name" value="${(register.formData.lastName!'')}" required />
        <input name="username" placeholder="Username" value="${(register.formData.username!'')}" required />
        <input type="email" name="email" placeholder="Email" value="${(register.formData.email!'')}" required />
        <input type="password" name="password" placeholder="Password" required />
        <input type="password" name="password-confirm" placeholder="Confirm Password" required />

        <input type="submit" value="Create User Account" />
    </form>

    <#if messagesPerField.existsError('username','firstName','lastName','email','password','password-confirm')>
        <div class="error-box">
            <#list ['username','firstName','lastName','email','password','password-confirm'] as field>
                <#if messagesPerField.existsError(field)>
                    <div>${kcSanitize(messagesPerField.getFirstError(field))?no_esc}</div>
                </#if>
            </#list>
        </div>
    </#if>

    <div class="footer">
        <a href="${url.loginUrl}">Already have an account? Sign In</a>
    </div>
</div>

</body>
</html>