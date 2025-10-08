<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chess Site - Login</title>
    <style>
        /* General page styling */
        body {
            font-family: Arial, sans-serif;
            background-color: #f2f2f2;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }

        /* Login container */
        .login-container {
            background-color: #fff;
            padding: 40px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.2);
            width: 300px;
            text-align: center;
        }

        h1 {
            margin-bottom: 30px;
            color: #333;
        }

        /* Input fields */
        input[type="text"], input[type="password"] {
            width: 100%;
            padding: 12px 10px;
            margin: 8px 0;
            border: 1px solid #ccc;
            border-radius: 5px;
            box-sizing: border-box;
        }

        /* Button styling */
        button {
            width: 100%;
            padding: 12px;
            margin-top: 15px;
            background-color: #4CAF50;
            border: none;
            border-radius: 5px;
            color: white;
            font-size: 16px;
            cursor: pointer;
        }

        button:hover {
            background-color: #45a049;
        }

        /* Optional: link for registration */
        .register-link {
            margin-top: 15px;
            display: block;
            font-size: 14px;
            color: #555;
            text-decoration: none;
        }

        .register-link:hover {
            text-decoration: underline;
        }
        
                /* Error message styling */
        .error-message {
            color: red;
            font-size: 14px;
            margin-top: 10px;
        }
    </style>
</head>
<body>

<div class="login-container">
    <h1>Chess Site Login</h1>
    <form id="loginForm" action="/login" method="post">
        <input type="text"" name="bruker" placeholder="Username" required>
        <input type="password" name="passord" placeholder="Password" required>
        <button type="submit">Log In</button>
        <div id="errorMessage" class="error-message">${redirectMessage}</div>
    </form>
    <a href="/registrer" class="register-link">Don't have an account? Register</a>
</div>

</body>
</html>