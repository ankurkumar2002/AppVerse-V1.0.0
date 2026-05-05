<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Developer Login</title>

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
            margin-bottom: 16px;
            padding: 12px;
            border-radius: 8px;
            background: #2d333b;
            border: 1px solid #da3633;
            color: #f85149;
        }
    </style>
</head>

<body style="visibility:hidden;">

<div class="card">
    <h2>Developer Portal</h2>
    <p>Build. Publish. Manage your apps.</p>

    <#if message?has_content>
        <div class="error-box">
            ${kcSanitize(message.summary)?no_esc}
        </div>
    </#if>

    <form action="${url.loginAction}" method="post">
        <input name="username"
               placeholder="Username"
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
            New developer? Create account
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