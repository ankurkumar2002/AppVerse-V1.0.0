<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Login</title>

    <style>
        body {
            background: #0d1117;
            color: #e6edf3;
            font-family: 'Segoe UI', sans-serif;
        }
        h2 {
            color: #58a6ff;
            text-align: center;
            margin-bottom: 20px;
        }
        .container {
            width: 400px;
            margin: 100px auto;
            padding: 30px;
            background: #161b22;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.5);
        }
        .form-group {
            margin-bottom: 15px;
        }
        .form-group input {
            width: 100%;
            padding: 8px;
            border-radius: 4px;
            border: 1px solid #30363d;
            background: #0d1117;
            color: #e6edf3;
        }
        .form-buttons {
            text-align: center;
            margin-top: 20px;
        }
        .form-buttons input[type="submit"] {
            background-color: #238636;
            border: none;
            padding: 10px 20px;
            color: #ffffff;
            font-weight: bold;
            border-radius: 4px;
            cursor: pointer;
        }
        .form-buttons input[type="submit"]:hover {
            background-color: #2ea043;
        }
        a {
            color: #58a6ff;
            text-decoration: none;
        }
        a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="container">
        <h2>Developer Login</h2>

        <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <div class="form-group">
                <input tabindex="1" id="username" name="username" type="text" placeholder="Username" value="${(login.username!'')}" autofocus autocomplete="off"/>
            </div>
            <div class="form-group">
                <input tabindex="2" id="password" name="password" type="password" placeholder="Password" autocomplete="off"/>
            </div>
            <div class="form-buttons">
                <input tabindex="3" name="login" id="kc-login" type="submit" value="Log In"/>
            </div>
        </form>

        <#if realm.resetPasswordAllowed>
            <div style="text-align:center; margin-top:10px;">
                <a href="${url.loginResetCredentialsUrl}">Forgot Password?</a>
            </div>
        </#if>
    </div>
</body>
</html>
