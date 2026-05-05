<#-- developer-theme/login/template.ftl -->

<#macro registrationLayout bodyClass="" displayInfo=false displayWide=false>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title><#nested "title"></title>

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
        .form-group label {
            display: block;
            margin-bottom: 5px;
        }
        .form-group input {
            width: 100%;
            padding: 8px;
            border-radius: 4px;
            border: 1px solid #30363d;
            background: #0d1117;
            color: #e6edf3;
        }
        .form-actions {
            text-align: center;
            margin-top: 20px;
        }
    </style>
</head>
<body class="${bodyClass}">
    <div class="container">
        <#nested "form">
    </div>
</body>
</html>
</#macro>