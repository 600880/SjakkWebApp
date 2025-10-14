<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
	<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/main.css">
	<title>Chess Site - Register</title>
	</head>
<body>

<div class="register-container">
    <h1>Create Your Account</h1>
    <form id="registerForm" action="/registrer" method="post">
        <input type="email" name="epost" placeholder="Email" required>
        <input type="password" name="passord" placeholder="Password" required>
        <input type="password" name="passordRepetert" placeholder="Confirm Password" required>
        <button type="submit">Register</button>
        <div id="errorMessage" class="error-message">${redirectMessage}</div>
    </form>
    <a href="/index" class="login-link">Already have an account? Log in</a>
</div>

</body>
</html>
