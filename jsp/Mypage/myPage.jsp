<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MY PAGE</title>

	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/Header/header.css">

	<!--cssのfont-awesome(汎用アイコン系)ライブラリ-->
	 <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

	<!--cssのBootstrap Icons(IT機器アイコン系ライブラリ)-->
	<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    
    <!--フォント設定-->
    <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@700&display=swap" rel="stylesheet">
    
    <style>
        .container{
            display: flex;
            justify-content: center;
            align-items: center;    
            min-height: calc(100vh - 100px);
        }

        /*ユーザー情報部*/
        .user-inf {
            width: 500px;
            margin: 30px auto;
            padding: 50px;
            background: #fff;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            border-radius: 10px;
        }

        .title {
            text-align: center;
            margin-bottom: 25px;
            font-size: 30px;
            color: #000000;
        }

        /* 入力項目 */
        .field {
            margin-bottom: 20px;
        }

        .field label {
            display: block;
            margin-bottom: 5px;
            font-size: 14px;
            font-weight: bold;
            color: #555;
        }

        .field input {
            width: 100%;
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 5px;
            box-sizing: border-box;
            font-size: 16px;
        }

        .field input:focus {
            border-color: #085dbe;
            outline: none;
        }

        .note {
            font-size: 11px;
            color: #888;
            margin-top: 4px;
        }

        /* ボタン */
        .update-btn {
            margin-top: 30px;
        }
    </style>
</head>
<body>

    <!--ヘッダー部-->
    <jsp:include page="/jsp/Header/header.jsp" />

    <main>
        <div class="container">
            
            <!--ユーザー情報部-->
            <div class="user-inf">
                <h2 class="title">ユーザー情報</h2>

                <form action="${pageContext.request.contextPath}/UserServlet" method="post">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="returnURL" value="${pageContext.request.requestURI}">

                    <div class="field">
                        <label>ユーザー名</label>
                        <input type="text" name="userName" value="<c:out value='${loginUser.name}' />" required>
                    </div>

                    <div class="field">
                        <label>新パスワード</label>
                        <input type="password" name="password" placeholder="変更する場合のみ入力">
                        <p class="note">※変更しない場合は空欄のまま</p>
                    </div>

                    <div class="field">
                        <label>住所</label>
                        <input type="text" name="address" value="<c:out value='${loginUser.address}' />">
                    </div>

                    <div class="update-btn">
                        <button type="submit" class="btn">情報を更新する</button>
                    </div>
                </form>
            </div>
        </div>
    </main>

</body>
</html>