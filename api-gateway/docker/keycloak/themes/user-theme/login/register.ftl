<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>User Portal 👤 - Register</title>
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
            width: 400px;
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
    </style>
</head>
<body>
<div class="container">
    <h2>Create Your User Account</h2>
    <form id="kc-register-form" action="${url.registrationAction}" method="post">

        <label for="firstName">First Name</label>
        <input id="firstName" name="firstName" type="text" value="${(register.formData.firstName!'')}" required>

        <label for="lastName">Last Name</label>
        <input id="lastName" name="lastName" type="text" value="${(register.formData.lastName!'')}" required>

        <label for="email">Email</label>
        <input id="email" name="email" type="email" value="${(register.formData.email!'')}" required>

        <label for="username">Username</label>
        <input id="username" name="username" type="text" value="${(register.formData.username!'')}" required>

        <label for="password">Password</label>
        <input id="password" name="password" type="password" required>

        <label for="password-confirm">Confirm Password</label>
        <input id="password-confirm" name="password-confirm" type="password" required>

        <button type="submit">Register</button>
    </form>
</div>
</body>
</html>
