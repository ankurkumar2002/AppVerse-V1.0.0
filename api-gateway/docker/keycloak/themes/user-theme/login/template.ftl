<#-- developer-theme/template.ftl -->
<#macro registrationLayout>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>${(realm.displayName)!'AppVerse'} - ${section?cap_first}</title>
    <style>
        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #2e3b55, #1e2a38);
            color: #fff;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }
        .container {
            background: rgba(255, 255, 255, 0.08);
            padding: 2rem;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.4);
            width: 100%;
            max-width: 420px;
        }
        h2 {
            margin-bottom: 1rem;
            font-size: 1.5rem;
            text-align: center;
        }
        form {
            display: flex;
            flex-direction: column;
            gap: 1rem;
        }
        label {
            font-size: 0.9rem;
            margin-bottom: 0.2rem;
        }
        input {
            padding: 0.6rem;
            border: none;
            border-radius: 6px;
            outline: none;
            font-size: 1rem;
        }
        input:focus {
            box-shadow: 0 0 0 2px #3a9fff;
        }
        button {
            padding: 0.7rem;
            background: #3a9fff;
            border: none;
            border-radius: 6px;
            font-size: 1rem;
            font-weight: bold;
            cursor: pointer;
            transition: background 0.2s ease;
        }
        button:hover {
            background: #2c82e0;
        }
        .link {
            margin-top: 1rem;
            text-align: center;
        }
        .link a {
            color: #3a9fff;
            text-decoration: none;
        }
        .link a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="container">
        <#nested "form">
    </div>
</body>
</html>
</#macro>
