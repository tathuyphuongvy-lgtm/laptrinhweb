<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="vi">
<head>
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Quên mật khẩu | MÂY Fashion</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<main class="account">
    <h1>Khôi phục mật khẩu</h1>
    <%@ include file="feedback.jspf" %>
    <c:choose>
        <c:when test="${step == 'reset'}">
            <form action="${pageContext.request.contextPath}/forgot-password" method="post">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="action" value="resetPassword">
                <label>Mã OTP <input type="text" name="otp" minlength="6" maxlength="6" pattern="[0-9]{6}" inputmode="numeric" autocomplete="one-time-code" required></label>
                <label>Mật khẩu mới <input type="password" name="newPassword" autocomplete="new-password" required></label>
                <button type="submit">Đổi mật khẩu</button>
            </form>
            <div class="account-links"><a href="${pageContext.request.contextPath}/forgot-password">Yêu cầu mã mới</a></div>
        </c:when>
        <c:otherwise>
            <form action="${pageContext.request.contextPath}/forgot-password" method="post">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="action" value="sendOtp">
                <label>Email <input type="email" name="email" maxlength="255" autocomplete="email" required></label>
                <button type="submit">Gửi mã OTP</button>
            </form>
        </c:otherwise>
    </c:choose>
    <div class="account-links"><a href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a></div>
</main>
</body>
</html>
