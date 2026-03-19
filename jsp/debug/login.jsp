<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="bean.UserBean" %>
<!DOCTYPE html>
<html>
<head>
    <title>Login Test</title>
</head>
<body>

<%
    // セッションからログインユーザー取得
    UserBean loginUser = (UserBean)session.getAttribute("loginUser");

    // 現在のURLを取得（fromURL用）
    String fromURL = request.getRequestURI();
%>

<!-- ログイン済み表示 -->
<% if (loginUser != null) { %>
    <h3>ログイン中：<%= loginUser.getName() %></h3>
    <form action="<%= request.getContextPath() %>/debug/UserTest" method="post">
        <input type="hidden" name="action" value="logout">
        <input type="hidden" name="fromURL" value="<%= fromURL %>">
        <input type="submit" value="ログアウト">
    </form>
    <hr>
<% } %>

<!-- エラーメッセージ表示 -->
<% if (request.getAttribute("error") != null) { %>
    <p style="color:red;">
        <%= request.getAttribute("error") %>
    </p>
<% } %>

<!-- メッセージ表示 -->
<% if (request.getAttribute("message") != null) { %>
    <p style="color:blue;">
        <%= request.getAttribute("message") %>
    </p>
<% } %>

<h2>ログイン</h2>
<form action="<%= request.getContextPath() %>/debug/UserTest" method="post">
    <input type="hidden" name="action" value="login">
    <input type="hidden" name="returnURL" value="../jsp/debug/login.jsp">
    <input type="hidden" name="fromURL" value="<%= fromURL %>">

    ユーザー名: <input type="text" name="userName" required><br>
    パスワード: <input type="password" name="password" required><br>

    <input type="submit" value="ログイン">
</form>

<hr>

<h2>新規登録</h2>
<form action="<%= request.getContextPath() %>/debug/UserTest" method="post">
    <input type="hidden" name="action" value="signup">
    <input type="hidden" name="fromURL" value="<%= fromURL %>">

    名前: <input type="text" name="userName" required><br>
    住所: <input type="text" name="address" required><br>
    パスワード: <input type="password" name="password" required><br>

    <input type="submit" value="新規登録">
</form>

<h3 style="color:red;">アカウント削除</h3>

<form action="<%= request.getContextPath() %>/debug/UserTest" method="post">
    
    <input type="hidden" name="action" value="delete">

    <input type="hidden" name="fromURL" value="<%= fromURL %>">

    <div>
        ユーザー名：
        <input type="text" name="userName" required>
    </div>

    <div>
        パスワード：
        <input type="password" name="password" required>
    </div>

    <div>
        <button type="submit" onclick="return confirm('本当に削除しますか？');">
            アカウント削除（デバッグ）
        </button>
    </div>

</form>
</body>
</html>