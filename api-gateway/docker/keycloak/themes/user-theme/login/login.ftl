<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>User Login</title>

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
            margin-bottom: 16px;
            padding: 12px;
            border-radius: 8px;
            background: #ffe5e5;
            border: 1px solid #ff4d4d;
            color: #cc0000;
        }
    </style>
</head>

<body style="visibility:hidden;">

<div class="card">
    <h2>User Portal</h2>
    <p>Discover and use amazing applications.</p>

    <#if message?has_content>
        <div class="error-box">
            ${kcSanitize(message.summary)?no_esc}
        </div>
    </#if>

    <form action="${url.loginAction}" method="post">
        <input name="username"
               placeholder="Username or Email"
               value="${(login.username!'')}"
               required />

        <input name="password"
               type="password"
               placeholder="Password"
               required />

        <input type="submit" value="Sign In" />
    </form>

    <div class="footer">
        <a id="register-link" href="${url.registrationUrl}">
            New user? Register here
        </a>
    </div>
</div>

<script>
    const params = new URLSearchParams(window.location.search);

    if (params.get('register') === 'true') {
        window.onload = () => {
            document.getElementById('register-link')?.click();
        };
    } else {
        document.body.style.visibility = 'visible';
    }
</script>

</body>
</html>