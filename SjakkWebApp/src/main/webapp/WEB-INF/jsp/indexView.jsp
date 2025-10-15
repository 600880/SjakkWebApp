<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/main.css">
	<title>Chess Site - Login</title>
</head>
<body>

<div class="login-container">
    <h1>Chess Site Login</h1>
    <form id="loginForm" action="/login" method="post">
        <input type="text" name="bruker" placeholder="Username" required>
        <input type="password" name="passord" placeholder="Password" required>
        <button type="submit">Log In</button>
        <div id="errorMessage" class="error-message">${redirectMessage}</div>
    </form>
    <a href="/registrer" class="register-link">Don't have an account? Register</a>
</div>

</body>
</html>
