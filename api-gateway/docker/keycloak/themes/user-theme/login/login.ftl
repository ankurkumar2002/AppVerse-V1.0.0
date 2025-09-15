<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>User Portal 👤 - Login</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #1d3557, #457b9d);
            color: #f1faee;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }
        .container {
            background: #ffffff;
            color: #333;
            padding: 2rem;
            border-radius: 10px;
            width: 350px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.2);
        }
        h2 {
            margin-bottom: 1rem;
            text-align: center;
            color: #1d3557;
        }
        label {
            display: block;
            margin: 0.5rem 0 0.2rem;
            font-size: 0.9rem;
            color: #555;
        }
        input {
            width: 100%;
            padding: 0.6rem;
            margin-bottom: 1rem;
            border: 1px solid #bbb;
            border-radius: 6px;
            font-size: 0.95rem;
        }
        button {
            width: 100%;
            padding: 0.7rem;
            background: #457b9d;
            color: white;
            font-size: 1rem;
            border: none;
            border-radius: 6px;
            cursor: pointer;
        }
        button:hover {
            background: #1d3557;
        }
        .extra-links {
            text-align: center;
            margin-top: 1rem;
        }
        .extra-links a {
            color: #457b9d;
            text-decoration: none;
            font-size: 0.85rem;
        }
    </style>
</head>
<body>
<div class="container">
    <h2>User Login</h2>
    <#if message?has_content>
      <div class="alert" style="
            background: #2d333b;
            border: 1px solid #da3633;
            color: #f85149;
            padding: 10px;
            margin-bottom: 15px;
            border-radius: 4px;
            text-align: center;">
        ${message.summary}
      </div>
    </#if>
    <form action="${url.loginAction}" method="post">
        <label for="username">Username or Email</label>
        <input id="username" name="username" type="text" autofocus required>

        <label for="password">Password</label>
        <input id="password" name="password" type="password" required>

        <button type="submit">Login</button>
    </form>

    <div class="extra-links">
        <a href="${url.registrationUrl}">New user? Register here</a>
    </div>
</div>
</body>
</html>
