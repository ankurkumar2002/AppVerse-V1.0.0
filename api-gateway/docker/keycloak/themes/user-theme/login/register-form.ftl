<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta
        name="viewport"
        content="width=device-width, initial-scale=1.0"
    >

    <title>Create User Account | AppVerse</title>

    <style>

        /* =====================================================
           VARIABLES
           ===================================================== */

        :root {

            --bg: #08090d;
            --card: rgba(17, 19, 25, 0.94);

            --border: rgba(255, 255, 255, 0.09);

            --text-primary: #f4f5f7;
            --text-secondary: #9da3ae;
            --text-muted: #69717d;

            --accent: #7557e8;
            --accent-light: #9279f5;

            --input-bg: #0d0f14;

            --error-bg: rgba(239, 68, 68, 0.08);
            --error-border: rgba(239, 68, 68, 0.3);
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

            width: 100%;
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
                    circle at 50% 20%,
                    rgba(117, 87, 232, 0.08),
                    transparent 34%
                ),

                var(--bg);

            color: var(--text-primary);

            display: flex;

            justify-content: center;

            align-items: center;

            min-height: 100vh;

            padding: 35px 20px;

            position: relative;

            overflow-x: hidden;
        }


        /* =====================================================
           BACKGROUND GRID
           ===================================================== */

        body::before {

            content: "";

            position: fixed;

            inset: 0;

            pointer-events: none;

            opacity: 0.13;

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
           WRAPPER
           ===================================================== */

        .register-wrapper {

            width: 100%;

            max-width: 470px;

            position: relative;

            z-index: 2;

            animation:
                fadeIn
                0.5s
                ease-out;
        }


        /* =====================================================
           BRAND
           ===================================================== */

        .brand {

            display: flex;

            flex-direction: column;

            align-items: center;

            margin-bottom: 24px;
        }


        .brand-mark {

            width: 42px;
            height: 42px;

            display: flex;

            align-items: center;
            justify-content: center;

            margin-bottom: 11px;

            border-radius: 11px;

            background:
                rgba(117, 87, 232, 0.1);

            border:
                1px solid rgba(117, 87, 232, 0.2);

            color:
                var(--accent-light);

            font-size: 17px;

            font-weight: 700;

            letter-spacing: -1px;

            box-shadow:
                0 0 30px rgba(117, 87, 232, 0.08);
        }


        .brand-name {

            font-size: 1.2rem;

            font-weight: 700;

            letter-spacing: -0.5px;

            color:
                var(--text-primary);
        }


        /* =====================================================
           USER BADGE
           ===================================================== */

        .user-badge {

            width: fit-content;

            display: flex;

            align-items: center;

            gap: 7px;

            margin: 0 auto 16px;

            padding: 6px 10px;

            border-radius: 999px;

            background:
                rgba(117, 87, 232, 0.08);

            border:
                1px solid rgba(117, 87, 232, 0.18);

            color:
                var(--accent-light);

            font-size: 0.67rem;

            font-weight: 700;

            letter-spacing: 0.08em;

            text-transform: uppercase;
        }


        .user-dot {

            width: 6px;
            height: 6px;

            border-radius: 50%;

            background:
                var(--accent-light);

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
                    rgba(255, 255, 255, 0.018)
                ),

                var(--card);

            border:
                1px solid var(--border);

            border-radius: 14px;

            box-shadow:
                0 25px 70px rgba(0, 0, 0, 0.35);

            backdrop-filter:
                blur(18px);

            -webkit-backdrop-filter:
                blur(18px);
        }


        /* =====================================================
           HEADING
           ===================================================== */

        .card h2 {

            margin: 0;

            text-align: center;

            color:
                var(--text-primary);

            font-size: 1.55rem;

            font-weight: 700;

            letter-spacing: -0.6px;
        }


        .subtitle {

            margin: 8px 0 25px;

            text-align: center;

            color:
                var(--text-secondary);

            font-size: 0.86rem;

            line-height: 1.5;
        }


        /* =====================================================
           FORM
           ===================================================== */

        form {

            width: 100%;
        }


        .field {

            margin-bottom: 15px;
        }


        .field label {

            display: block;

            margin-bottom: 7px;

            color:
                var(--text-secondary);

            font-size: 0.75rem;

            font-weight: 600;
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

            outline: none;

            background:
                var(--input-bg);

            color:
                var(--text-primary);

            font-family: inherit;

            font-size: 0.86rem;

            transition:

                border-color 0.2s ease,

                background 0.2s ease,

                box-shadow 0.2s ease;
        }


        input[type="text"]::placeholder,
        input[type="email"]::placeholder,
        input[type="password"]::placeholder {

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
                0 0 0 3px rgba(117, 87, 232, 0.1);
        }


        /* =====================================================
           FIRST + LAST NAME
           ===================================================== */

        .name-row {

            display: grid;

            grid-template-columns:
                1fr 1fr;

            gap: 12px;
        }


        /* =====================================================
           SUBMIT
           ===================================================== */

        input[type="submit"] {

            width: 100%;

            height: 48px;

            margin-top: 5px;

            border: none;

            border-radius: 8px;

            background:
                var(--accent);

            color: white;

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
                var(--accent-light);

            transform:
                translateY(-1px);

            box-shadow:
                0 10px 25px rgba(117, 87, 232, 0.22);
        }


        input[type="submit"]:active {

            transform:
                translateY(0);
        }


        /* =====================================================
           ERROR
           ===================================================== */

        .error-box {

            margin-top: 20px;

            padding: 12px 14px;

            border-radius: 8px;

            background:
                var(--error-bg);

            border:
                1px solid var(--error-border);

            color:
                var(--error-text);

            font-size: 0.79rem;

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

            padding-top: 20px;

            border-top:
                1px solid var(--border);

            text-align: center;

            color:
                var(--text-muted);

            font-size: 0.77rem;
        }


        .footer a {

            color:
                var(--accent-light);

            text-decoration: none;

            font-weight: 600;

            transition:
                color 0.2s ease;
        }


        .footer a:hover {

            color:
                #a895f7;

            text-decoration:
                underline;
        }


        /* =====================================================
           BOTTOM TEXT
           ===================================================== */

        .bottom-text {

            margin-top: 16px;

            text-align: center;

            color:
                var(--text-muted);

            font-size: 0.67rem;
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
                    25px 16px;
            }


            .card {

                padding:
                    28px 22px;

                border-radius:
                    12px;
            }


            .name-row {

                grid-template-columns:
                    1fr;

                gap: 0;
            }


            .card h2 {

                font-size:
                    1.4rem;
            }
        }

    </style>

</head>


<body>

    <div class="register-wrapper">


        <!-- =================================================
             APPVERSE BRAND
             ================================================= -->

        <div class="brand">

            <div class="brand-mark">
                AV
            </div>

            <div class="brand-name">
                AppVerse
            </div>

        </div>


        <!-- =================================================
             USER BADGE
             ================================================= -->

        <div class="user-badge">

            <span class="user-dot"></span>

            User Portal

        </div>


        <!-- =================================================
             REGISTRATION CARD
             ================================================= -->

        <div class="card">

            <h2>
                Create your account
            </h2>

            <p class="subtitle">
                Join AppVerse and discover applications built for you.
            </p>


            <!-- =================================================
                 REGISTRATION FORM
                 ================================================= -->

            <form
                action="${url.registrationAction}"
                method="post"
            >


                <!-- First + Last Name -->

                <div class="name-row">

                    <div class="field">

                        <label for="firstName">
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


                    <div class="field">

                        <label for="lastName">
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

                    <label for="username">
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

                    <label for="email">
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

                    <label for="password">
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

                    <label for="password-confirm">
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
                 KEYCLOAK FIELD ERRORS
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

                <span>
                    Already have an account?
                </span>

                <a href="${url.loginUrl}">
                    Sign In
                </a>

            </div>

        </div>


        <div class="bottom-text">

            Discover. Connect. Explore.

        </div>

    </div>

</body>

</html>