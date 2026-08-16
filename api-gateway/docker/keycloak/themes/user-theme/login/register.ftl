<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta
        name="viewport"
        content="width=device-width, initial-scale=1.0"
    >

    <title>User Registration | AppVerse</title>

    <style>

        /* =====================================================
           APPVERSE THEME
           ===================================================== */

        :root {
            --background: #08090d;
            --card: #111319;

            --border: rgba(255, 255, 255, 0.09);

            --text-primary: #f4f5f7;
            --text-secondary: #9da3ae;
            --text-muted: #69717d;

            --accent: #7557e8;
            --accent-hover: #896ff0;

            --input-background: #0d0f14;

            --error-background: rgba(239, 68, 68, 0.08);
            --error-border: rgba(239, 68, 68, 0.30);
            --error-text: #fca5a5;
        }


        /* =====================================================
           RESET
           ===================================================== */

        * {
            box-sizing: border-box;
        }


        html,
        body {
            margin: 0;
            padding: 0;
            min-height: 100%;
        }


        /* =====================================================
           BODY
           ===================================================== */

        body {

            font-family:
                Inter,
                -apple-system,
                BlinkMacSystemFont,
                "Segoe UI",
                sans-serif;

            background:

                radial-gradient(
                    circle at 50% 15%,
                    rgba(117, 87, 232, 0.10),
                    transparent 35%
                ),

                #08090d;

            color: var(--text-primary);

            min-height: 100vh;

            display: flex;

            justify-content: center;

            align-items: center;

            padding: 30px 20px;

            position: relative;

            overflow-x: hidden;
        }


        /* =====================================================
           SUBTLE BACKGROUND GRID
           ===================================================== */

        body::before {

            content: "";

            position: fixed;

            inset: 0;

            pointer-events: none;

            opacity: 0.12;

            background-image:

                linear-gradient(
                    rgba(255, 255, 255, 0.025) 1px,
                    transparent 1px
                ),

                linear-gradient(
                    90deg,
                    rgba(255, 255, 255, 0.025) 1px,
                    transparent 1px
                );

            background-size: 64px 64px;
        }


        /* =====================================================
           MAIN WRAPPER
           ===================================================== */

        .register-container {

            width: 100%;

            max-width: 470px;

            position: relative;

            z-index: 2;

            animation:
                fadeIn 0.45s ease-out;
        }


        /* =====================================================
           APPVERSE BRAND
           ===================================================== */

        .brand {

            text-align: center;

            margin-bottom: 22px;
        }


        .brand-name {

            font-size: 1.25rem;

            font-weight: 700;

            letter-spacing: -0.5px;

            color: var(--text-primary);
        }


        .brand-subtitle {

            margin-top: 5px;

            font-size: 0.68rem;

            color: var(--text-muted);

            letter-spacing: 0.08em;

            text-transform: uppercase;
        }


        /* =====================================================
           USER BADGE
           ===================================================== */

        .portal-badge {

            width: fit-content;

            margin: 0 auto 15px;

            padding: 6px 11px;

            display: flex;

            align-items: center;

            gap: 7px;

            border-radius: 999px;

            background:
                rgba(117, 87, 232, 0.08);

            border:
                1px solid rgba(117, 87, 232, 0.18);

            color: #9a87ef;

            font-size: 0.66rem;

            font-weight: 700;

            letter-spacing: 0.08em;

            text-transform: uppercase;
        }


        .portal-dot {

            width: 6px;
            height: 6px;

            border-radius: 50%;

            background: #9279f5;

            box-shadow:
                0 0 8px rgba(146, 121, 245, 0.6);
        }


        /* =====================================================
           CARD
           ===================================================== */

        .card {

            width: 100%;

            padding: 32px 34px;

            background:

                linear-gradient(
                    180deg,
                    rgba(255, 255, 255, 0.035),
                    rgba(255, 255, 255, 0.015)
                ),

                var(--card);

            border:
                1px solid var(--border);

            border-radius: 14px;

            box-shadow:
                0 25px 70px rgba(0, 0, 0, 0.40);

            backdrop-filter:
                blur(16px);

            -webkit-backdrop-filter:
                blur(16px);
        }


        /* =====================================================
           HEADER
           ===================================================== */

        h2 {

            margin: 0;

            text-align: center;

            color: var(--text-primary);

            font-size: 1.55rem;

            font-weight: 700;

            letter-spacing: -0.6px;
        }


        .description {

            margin: 8px 0 26px;

            text-align: center;

            color: var(--text-secondary);

            font-size: 0.86rem;

            line-height: 1.5;
        }


        /* =====================================================
           FORM
           ===================================================== */

        form {

            width: 100%;
        }


        /* =====================================================
           INPUT GROUP
           ===================================================== */

        .field {

            margin-bottom: 15px;
        }


        .field-label {

            display: block;

            margin-bottom: 7px;

            color: var(--text-secondary);

            font-size: 0.74rem;

            font-weight: 600;
        }


        /* =====================================================
           FIRST + LAST NAME
           ===================================================== */

        .name-row {

            display: grid;

            grid-template-columns: 1fr 1fr;

            gap: 12px;
        }


        /* =====================================================
           INPUTS
           ===================================================== */

        input[type="text"],
        input[type="email"],
        input[type="password"] {

            width: 100%;

            height: 46px;

            padding: 0 13px;

            border:
                1px solid var(--border);

            border-radius: 8px;

            background:
                var(--input-background);

            color:
                var(--text-primary);

            outline: none;

            font-family: inherit;

            font-size: 0.85rem;

            transition:
                border-color 0.2s ease,
                background 0.2s ease,
                box-shadow 0.2s ease;
        }


        input::placeholder {

            color:
                var(--text-muted);
        }


        input[type="text"]:focus,
        input[type="email"]:focus,
        input[type="password"]:focus {

            border-color:
                var(--accent);

            background:
                #0f1117;

            box-shadow:
                0 0 0 3px
                rgba(117, 87, 232, 0.10);
        }


        /* =====================================================
           SUBMIT BUTTON
           ===================================================== */

        input[type="submit"] {

            width: 100%;

            height: 48px;

            margin-top: 5px;

            padding: 0;

            border: none;

            border-radius: 8px;

            background:
                var(--accent);

            color: #ffffff;

            font-family: inherit;

            font-size: 0.87rem;

            font-weight: 650;

            cursor: pointer;

            transition:
                background 0.2s ease,
                transform 0.2s ease,
                box-shadow 0.2s ease;
        }


        input[type="submit"]:hover {

            background:
                var(--accent-hover);

            transform:
                translateY(-1px);

            box-shadow:
                0 10px 25px
                rgba(117, 87, 232, 0.22);
        }


        input[type="submit"]:active {

            transform:
                translateY(0);
        }


        /* =====================================================
           ERROR BOX
           ===================================================== */

        .error-box {

            margin-top: 18px;

            padding: 12px 14px;

            border-radius: 8px;

            background:
                var(--error-background);

            border:
                1px solid var(--error-border);

            color:
                var(--error-text);

            font-size: 0.78rem;

            line-height: 1.45;
        }


        .error-box div + div {

            margin-top: 5px;
        }


        /* =====================================================
           FOOTER
           ===================================================== */

        .footer {

            margin-top: 22px;

            padding-top: 19px;

            border-top:
                1px solid var(--border);

            text-align: center;

            color:
                var(--text-muted);

            font-size: 0.76rem;
        }


        .footer a {

            margin-left: 4px;

            color:
                #9a87ef;

            text-decoration: none;

            font-weight: 600;

            transition:
                color 0.2s ease;
        }


        .footer a:hover {

            color:
                #b0a1f7;

            text-decoration:
                underline;
        }


        /* =====================================================
           BOTTOM TEXT
           ===================================================== */

        .bottom-text {

            margin-top: 15px;

            text-align: center;

            color:
                #4f5661;

            font-size: 0.65rem;
        }


        /* =====================================================
           ANIMATION
           ===================================================== */

        @keyframes fadeIn {

            from {

                opacity: 0;

                transform:
                    translateY(10px);
            }

            to {

                opacity: 1;

                transform:
                    translateY(0);
            }
        }


        /* =====================================================
           MOBILE
           ===================================================== */

        @media (max-width: 520px) {

            body {

                padding:
                    22px 16px;
            }


            .card {

                padding:
                    28px 22px;
            }


            .name-row {

                grid-template-columns:
                    1fr;

                gap: 0;
            }


            h2 {

                font-size:
                    1.4rem;
            }
        }

    </style>

</head>


<body>

<div class="register-container">


    <!-- =====================================================
         BRAND
         ===================================================== -->

    <div class="brand">

        <div class="brand-name">
            AppVerse
        </div>

        <div class="brand-subtitle">
            Application Marketplace
        </div>

    </div>


    <!-- =====================================================
         USER PORTAL
         ===================================================== -->

    <div class="portal-badge">

        <span class="portal-dot"></span>

        User Portal

    </div>


    <!-- =====================================================
         CARD
         ===================================================== -->

    <div class="card">


        <!-- Header -->

        <h2>
            Create User Account
        </h2>

        <p class="description">
            Join AppVerse and explore applications.
        </p>


        <!-- =================================================
             FORM
             ================================================= -->

        <form
            action="${url.registrationAction}"
            method="post"
        >


            <!-- First + Last Name -->

            <div class="name-row">


                <!-- First Name -->

                <div class="field">

                    <label
                        class="field-label"
                        for="firstName"
                    >
                        First Name
                    </label>

                    <input
                        id="firstName"
                        name="firstName"
                        type="text"
                        placeholder="First name"
                        value="${(register.formData.firstName!'')}"
                        autocomplete="given-name"
                        required
                    />

                </div>


                <!-- Last Name -->

                <div class="field">

                    <label
                        class="field-label"
                        for="lastName"
                    >
                        Last Name
                    </label>

                    <input
                        id="lastName"
                        name="lastName"
                        type="text"
                        placeholder="Last name"
                        value="${(register.formData.lastName!'')}"
                        autocomplete="family-name"
                        required
                    />

                </div>

            </div>


            <!-- Username -->

            <div class="field">

                <label
                    class="field-label"
                    for="username"
                >
                    Username
                </label>

                <input
                    id="username"
                    name="username"
                    type="text"
                    placeholder="Choose a username"
                    value="${(register.formData.username!'')}"
                    autocomplete="username"
                    required
                />

            </div>


            <!-- Email -->

            <div class="field">

                <label
                    class="field-label"
                    for="email"
                >
                    Email Address
                </label>

                <input
                    id="email"
                    name="email"
                    type="email"
                    placeholder="you@example.com"
                    value="${(register.formData.email!'')}"
                    autocomplete="email"
                    required
                />

            </div>


            <!-- Password -->

            <div class="field">

                <label
                    class="field-label"
                    for="password"
                >
                    Password
                </label>

                <input
                    id="password"
                    name="password"
                    type="password"
                    placeholder="Create a password"
                    autocomplete="new-password"
                    required
                />

            </div>


            <!-- Confirm Password -->

            <div class="field">

                <label
                    class="field-label"
                    for="password-confirm"
                >
                    Confirm Password
                </label>

                <input
                    id="password-confirm"
                    name="password-confirm"
                    type="password"
                    placeholder="Confirm your password"
                    autocomplete="new-password"
                    required
                />

            </div>


            <!-- Submit -->

            <input
                type="submit"
                value="Create User Account"
            />

        </form>


        <!-- =================================================
             KEYCLOAK ERRORS
             ================================================= -->

        <#if messagesPerField.existsError(
            'username',
            'firstName',
            'lastName',
            'email',
            'password',
            'password-confirm'
        )>

            <div class="error-box">

                <#list
                    [
                        'username',
                        'firstName',
                        'lastName',
                        'email',
                        'password',
                        'password-confirm'
                    ]
                    as field
                >

                    <#if messagesPerField.existsError(field)>

                        <div>
                            ${kcSanitize(
                                messagesPerField.getFirstError(field)
                            )?no_esc}
                        </div>

                    </#if>

                </#list>

            </div>

        </#if>


        <!-- =================================================
             LOGIN LINK
             ================================================= -->

        <div class="footer">

            Already have an account?

            <a href="${url.loginUrl}">
                Sign In
            </a>

        </div>

    </div>


    <!-- Bottom -->

    <div class="bottom-text">
        Discover. Connect. Explore.
    </div>


</div>

</body>

</html>