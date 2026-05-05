<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Developer Register</title>

    <style>
        body {
            margin: 0;
            font-family: Inter, sans-serif;
            background: radial-gradient(circle at top left, #1f2937, #0f172a);
            color: white;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }

        .card {
            width: 420px;
            padding: 40px;
            background: rgba(17,24,39,0.92);
            border: 1px solid #334155;
            border-radius: 18px;
            box-shadow: 0 20px 50px rgba(0,0,0,0.45);
        }

        h2 {
            text-align: center;
            color: #60a5fa;
            margin-bottom: 8px;
        }

        p {
            text-align: center;
            color: #94a3b8;
            margin-bottom: 24px;
        }

        input {
            width: 100%;
            padding: 14px;
            margin-bottom: 14px;
            border-radius: 10px;
            border: 1px solid #334155;
            background: #0f172a;
            color: white;
            box-sizing: border-box;
        }

        input[type="submit"] {
            background: #2563eb;
            border: none;
            cursor: pointer;
            font-weight: 600;
        }

        a {
            color: #60a5fa;
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
            background: #2d333b;
            border: 1px solid #da3633;
            color: #f85149;
        }
    </style>
</head>
<body>

<div class="card">
    <h2>Create Developer Account</h2>
    <p>Start building and publishing your apps.</p>

    <form action="${url.registrationAction}" method="post">

        <input name="username" placeholder="Username" value="${(register.formData.username!'')}" required />
        <input name="firstName" placeholder="First Name" value="${(register.formData.firstName!'')}" required />
        <input name="lastName" placeholder="Last Name" value="${(register.formData.lastName!'')}" required />
        <input type="email" name="email" placeholder="Email" value="${(register.formData.email!'')}" required />
        <input type="password" name="password" placeholder="Password" required />
        <input type="password" name="password-confirm" placeholder="Confirm Password" required />

        <input type="submit" value="Create Developer Account" />
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