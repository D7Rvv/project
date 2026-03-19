<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!--JSTLのライブラリ変数用意-->
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!--住所選択用リスト-->
<c:set var="prefList" value = '${["北海道", "青森県", "岩手県", "宮城県", "秋田県", "山形県", "福島県",
						       "茨城県", "栃木県", "群馬県", "埼玉県", "千葉県", "東京都", "神奈川県",
						       "新潟県", "富山県", "石川県", "福井県", "山梨県", "長野県", "岐阜県",
						       "静岡県", "愛知県", "三重県", "滋賀県", "京都府", "大阪府", "兵庫県",
						       "奈良県", "和歌山県", "鳥取県", "島根県", "岡山県", "広島県", "山口県",
						       "徳島県", "香川県", "愛媛県", "高知県", "福岡県", "佐賀県", "長崎県",
							   "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県"]}'
/>

<!-- 下記EL式の説明　
    ${pageContext.request.requestURI} アプリ名を含めたURLを取得　(※パラメータは含まれない)
    ${pageContext.request.queryString} URLのパラメータ部分を取得
    ${pageContext.request.servletPath} アプリ名を含めないURLを取得　(※パラメータは含まれない)
-->

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>LOGIN PAGE</title>
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/Header/header.css">

	<!--cssのfont-awesome(汎用アイコン系)ライブラリ-->
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

	<!--cssのBootstrap Icons(IT機器アイコン系ライブラリ)-->
	<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    
    <!--フォント設定-->
    <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@700&display=swap" rel="stylesheet">

    <!--スタイルシート-->
    <style>

        /* 加藤------------↓↓↓-------------------*/

        h2{
            font-size:30px;
            padding: 10px;
        }

        summary {
            display: block;
            text-decoration:underline;
            padding: 10px;
            list-style: none;
            user-select: none;
        }
        summary:hover{
            color:#1783ff;
        }
        details[name="a"][open] summary {
            display:none;
        }
        details {
            interpolate-size: allow-keywords;
        }

        details::details-content {
            transition: height 1s ease, opacity 2s ease;
            height: 0;
            opacity: 0;
            overflow: hidden;
        }
        details[open]::details-content {
            height: auto;
            opacity: 1;
        }
        .main {
            text-align: center;
        }

        .main input , .main select{
            width: 100%;
            max-width: 400px;
            padding: 14px 20px;
            margin-bottom: 40px;
            font-size: 16px;
            border: 1px solid #eeeeee;
            border-radius: 10px;
            background-color: #fff;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
            transition: all 0.3s ease;
            outline: none;
            box-sizing: border-box;
        }
        .main input[type="submit"]{
            margin:1px 0px 30px;
            font-weight: 700;
            max-inline-size: 150px;
            height: 35px;
            line-height:20px;
            padding: 0;
            font-size: 14px;
            border: 1px solid #eeeeee;
            border-radius: 50px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
            transition: all 0.3s ease;
            outline: none;
            box-sizing: border-box;
        }
        .main input:hover[type="submit"]{
            background-color: #085dbe;
            color:white;
        }

        .main input:focus, .main select:focus {
            border-color: #085dbe;
        }

        /* 加藤------------↑↑↑-------------------*/

        /*認証部*/
        .authentication{
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            width: 500px;
            margin: auto;
            border: 1px solid #eeeeee;
            border-radius: 15px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
            padding: 20px;

            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
        }

        /*メッセージ部*/
        .msg, .login-msg, .reg-msg{
            color: #085dbe;
        }

        /*　エラーメッセージ処理部　*/
        .error , .login-error-msg, .reg-error-msg, .msg{
            margin: 10px;
            color: red;
        }

        /* タイトル部*/
        .title{
            margin: 10px;
        }

        /* 新規登録フォーム部部 */
        .form-item{
            padding-bottom: 20px;
        }

    </style>    
</head>

<body>
    <!--ヘッダー部-->
    <jsp:include page="/jsp/Header/header.jsp"/>

    <main class="main">

        <!--認証部-->
        <div class="authentication">

            <!--ログイン部-->
            <div class="login">
                <details name="a" ${param.action != 'signup' ? 'open' : ''}>
                    <summary>ログイン</summary>
                    
                    <!--タイトル部-->
                    <div class="title">
                        <h2>ログイン</h2>
                    </div>

                    <!--メッセージ部-->
                    <div class="msg">
                        <c:out value="${param.action != 'signup' ? message : ''}" />
                    </div>

                    <!--エラーメッセージ-->
                    <div class="error">
                        <c:out value="${param.action != 'signup' ? error : ''}" />
                    </div>

                    <!--ログイン処理メッセージ部-->
                    <div class="login-msg">
                        <c:out value="${login_message}" />
                    </div>

                    <!--ログインエラーメッセージ部-->
                    <div class="login-error-msg">
                        <c:out value="${login_error_msg}" />
                    </div>

                    <form action="${pageContext.request.contextPath}/UserServlet" method="post">
                        <input type="hidden" name="action" value="login">
                        
                        <c:choose>
                            <%-- 他画面の「ログイン」ボタンから渡された戻り先URLがある場合 --%>
                            <c:when test="${not empty param.rawFromURL}">
                                <input type="hidden" name="rawFromURL" value="${param.rawFromURL}">
                                <input type="hidden" name="fromURL" value="${pageContext.request.servletPath}">
                            </c:when>

                            <%-- ログイン画面を直接開いた場合など --%>
                            <c:otherwise>
                                <input type="hidden" name="rawFromURL" value="${pageContext.request.requestURI}">
                                <input type="hidden" name="fromURL" value="${pageContext.request.servletPath}">
                            </c:otherwise>
                        </c:choose>

                        <div class="form-item">
                            <label for="login-userName">ユーザー名</label>
                            <input type="text" id="login-userName" name="userName" required>
                        </div>

                        <div class="form-item">
                            <label for="login-password">パスワード</label>
                            <input type="password" id="login-password" name="password" required autocomplete="off">
                        </div>

                        <input type="submit" value="ログイン">
                    </form>
                </details>
            </div>

            <!--新規登録部-->
            <div class="registration">
                <details name="a" ${param.action == 'signup' ? 'open' : ''}>
                    <summary>新規登録はこちら</summary>

                    <div class="title">
                        <h2>新規登録</h2>
                    </div>

                    <!--メッセージ部-->
                    <div class="msg">
                        <c:out value="${param.action == 'signup' ? message : ''}" />
                    </div>

                    <!--エラーメッセージ-->
                    <div class="error">
                        <c:out value="${param.action == 'signup' ? error : ''}" />
                    </div>

                    <!--新規登録メッセージ部-->
                    <div class="reg-msg">
                        <c:out value="${reg_message}" />
                    </div>

                    <!--新規登録エラーメッセージ部-->
                    <div class="reg-error-msg">
                        <c:out value="${reg_error_msg}" />
                    </div>

                    <!--新規登録フォーム部-->
                    <form action="${pageContext.request.contextPath}/UserServlet" method="post">
                        <input type="hidden" name="action" value="signup">

                        <c:choose>
                            <%-- 他画面の「ログイン」ボタンから渡された戻り先URLがある場合 --%>
                            <c:when test="${not empty param.rawFromURL}">
                                <input type="hidden" name="rawFromURL" value="${param.rawFromURL}">
                                <input type="hidden" name="fromURL" value="${pageContext.request.servletPath}">
                            </c:when>

                            <%-- ログイン画面を直接開いた場合など --%>
                            <c:otherwise>
                                <input type="hidden" name="rawFromURL" value="${pageContext.request.requestURI}">
                                <input type="hidden" name="fromURL" value="${pageContext.request.servletPath}">
                            </c:otherwise>
                        </c:choose>

                        <div class="form-item">
                            <label for="signup-userName">ユーザー名</label>
                            <input type="text" id="signup-userName" name="userName" required>
                        </div>
                        
                        <div class="form-item">
                            <label for="signup-password">パスワード</label>
                            <input type="password" id="signup-password" name="password" required autocomplete="off">
                        </div>

                        <div class="form-item">
                            <label for="signup-address">住所</label>
                            <select id="signup-address" name="address" required>
                                <option disabled ${empty param.address ? 'selected' : ''}>都道府県を選択してください</option>
					    	
                                <!--preflist = 都道府県リスト-->
                                <c:forEach var="pref" items="${prefList}">
                                    <option value="${pref}" ${param.address == pref ? 'selected' : ''}>${pref}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <input type="submit" value="新規登録">
                    </form>
                </details>
            </div>

        </div>
    </main>
</body>
</html>