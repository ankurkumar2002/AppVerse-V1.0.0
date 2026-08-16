<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta
        name="viewport"
        content="width=device-width, initial-scale=1.0"
    >

    <title>Sign in | AppVerse</title>

    <style>

        /* =====================================================
           VARIABLES
           ===================================================== */

        :root {

            --bg: #08090d;
            --bg-secondary: #0d0f14;

            --card: rgba(17, 19, 25, 0.92);

            --border: rgba(255, 255, 255, 0.09);
            --border-hover: rgba(255, 255, 255, 0.16);

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
                    circle at 50% 30%,
                    rgba(117, 87, 232, 0.08),
                    transparent 32%
                ),
                var(--bg);

            color: var(--text-primary);

            display: flex;

            align-items: center;
            justify-content: center;

            min-height: 100vh;

            padding: 30px;

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
           MAIN WRAPPER
           ===================================================== */

        .login-wrapper {

            width: 100%;

            max-width: 420px;

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

            margin-bottom: 28px;
        }


        .brand-mark {

            width: 42px;
            height: 42px;

            display: flex;

            align-items: center;
            justify-content: center;

            margin-bottom: 12px;

            border-radius: 11px;

            background: rgba(117, 87, 232, 0.1);

            border: 1px solid rgba(117, 87, 232, 0.2);

            color: var(--accent-light);

            font-size: 18px;

            font-weight: 700;

            letter-spacing: -1px;

            box-shadow:
                0 0 30px rgba(117, 87, 232, 0.08);
        }


        .brand-name {

            font-size: 1.2rem;

            font-weight: 700;

            letter-spacing: -0.5px;

            color: var(--text-primary);
        }


        /* =====================================================
           LOGIN CARD
           ===================================================== */

        .card {

            width: 100%;

            padding: 34px;

            background:
                linear-gradient(
                    180deg,
                    rgba(255, 255, 255, 0.035),
                    rgba(255, 255, 255, 0.018)
                ),
                var(--card);

            border: 1px solid var(--border);

            border-radius: 14px;

            box-shadow:
                0 25px 70px rgba(0, 0, 0, 0.35);

            backdrop-filter: blur(18px);
            -webkit-backdrop-filter: blur(18px);
        }


        /* =====================================================
           HEADING
           ===================================================== */

        .card h2 {

            margin: 0;

            text-align: center;

            color: var(--text-primary);

            font-size: 1.65rem;

            font-weight: 700;

            letter-spacing: -0.7px;
        }


        .subtitle {

            margin: 9px 0 28px;

            text-align: center;

            color: var(--text-secondary);

            font-size: 0.88rem;

            line-height: 1.5;
        }


        /* =====================================================
           ERROR
           ===================================================== */

        .error-box {

            margin-bottom: 20px;

            padding: 12px 14px;

            border-radius: 8px;

            background: var(--error-bg);

            border: 1px solid var(--error-border);

            color: var(--error-text);

            font-size: 0.82rem;

            line-height: 1.45;
        }


        /* =====================================================
           FORM
           ===================================================== */

        form {

            width: 100%;
        }


        .field {

            margin-bottom: 17px;
        }


        .field label {

            display: block;

            margin-bottom: 7px;

            color: var(--text-secondary);

            font-size: 0.76rem;

            font-weight: 600;
        }


        /* =====================================================
           INPUT
           ===================================================== */

        input[type="text"],
        input[type="email"],
        input[type="password"] {

            width: 100%;

            height: 48px;

            padding: 0 14px;

            border: 1px solid var(--border);

            border-radius: 8px;

            outline: none;

            background: var(--input-bg);

            color: var(--text-primary);

            font-family: inherit;

            font-size: 0.88rem;

            transition:
                border-color 0.2s ease,
                box-shadow 0.2s ease,
                background 0.2s ease;
        }


        input[type="text"]::placeholder,
        input[type="email"]::placeholder,
        input[type="password"]::placeholder {

            color: var(--text-muted);
        }


        input[type="text"]:focus,
        input[type="email"]:focus,
        input[type="password"]:focus {

            border-color: var(--accent);

            background: #0f1117;

            box-shadow:
                0 0 0 3px rgba(117, 87, 232, 0.1);
        }


        /* =====================================================
           SUBMIT BUTTON
           ===================================================== */

        input[type="submit"] {

            width: 100%;

            height: 48px;

            margin-top: 5px;

            border: none;

            border-radius: 8px;

            background: var(--accent);

            color: white;

            font-family: inherit;

            font-size: 0.88rem;

            font-weight: 650;

            cursor: pointer;

            transition:
                background 0.2s ease,
                transform 0.2s ease,
                box-shadow 0.2s ease;
        }


        input[type="submit"]:hover {

            background: var(--accent-light);

            transform: translateY(-1px);

            box-shadow:
                0 10px 25px rgba(117, 87, 232, 0.22);
        }


        input[type="submit"]:active {

            transform: translateY(0);
        }


        /* =====================================================
           FOOTER
           ===================================================== */

        .footer {

            margin-top: 23px;

            padding-top: 21px;

            border-top: 1px solid var(--border);

            text-align: center;

            color: var(--text-muted);

            font-size: 0.78rem;
        }


        .footer a {

            color: var(--accent-light);

            text-decoration: none;

            font-weight: 600;

            transition: color 0.2s ease;
        }


        .footer a:hover {

            color: #a895f7;

            text-decoration: underline;
        }


        /* =====================================================
           SMALL BRAND MESSAGE
           ===================================================== */

        .bottom-text {

            margin-top: 18px;

            text-align: center;

            color: var(--text-muted);

            font-size: 0.68rem;

            letter-spacing: 0.1px;
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

        @media (max-width: 500px) {

            body {

                padding: 20px;
            }


            .card {

                padding: 28px 22px;

                border-radius: 12px;
            }


            .card h2 {

                font-size: 1.5rem;
            }
        }

    </style>

</head>


<body style="visibility:hidden;">

    <div class="login-wrapper">


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
             LOGIN CARD
             ================================================= -->

        <div class="card">

            <h2>
                Welcome back
            </h2>

            <p class="subtitle">
                Sign in to continue to your AppVerse account.
            </p>


            <!-- =================================================
                 KEYCLOAK ERROR
                 ================================================= -->

            <#if message?has_content>

                <div class="error-box">

                    ${kcSanitize(message.summary)?no_esc}

                </div>

            </#if>


            <!-- =================================================
                 LOGIN FORM
                 ================================================= -->

            <form
                action="${url.loginAction}"
                method="post"
            >


                <!-- Username -->

                <div class="field">

                    <label for="username">
                        Username or Email
                    </label>

                    <input
                        id="username"
                        name="username"
                        type="text"
                        placeholder="Enter your username or email"
                        value="${(login.username!'')}"
                        autocomplete="username"
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
                        placeholder="Enter your password"
                        autocomplete="current-password"
                        required
                    />

                </div>


                <!-- Submit -->

                <input
                    type="submit"
                    value="Sign In"
                />

            </form>


            <!-- =================================================
                 REGISTER
                 ================================================= -->

            <div class="footer">

                <span>
                    Don't have an account?
                </span>

                <a
                    id="register-link"
                    href="${url.registrationUrl}"
                >
                    Create one
                </a>

            </div>

        </div>


        <div class="bottom-text">
            Secure authentication powered by AppVerse
        </div>

    </div>


    <!-- =====================================================
         REGISTER REDIRECT LOGIC
         ===================================================== -->

    <script>

        const params =
            new URLSearchParams(
                window.location.search
            );


        if (params.get('register') === 'true') {

            window.onload = () => {

                document
                    .getElementById('register-link')
                    ?.click();

            };

        } else {

            document.body.style.visibility = 'visible';

        }

    </script>

</body>

</html>