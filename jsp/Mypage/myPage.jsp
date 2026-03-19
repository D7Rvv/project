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
        .from-item {
            margin-bottom: 20px;
        }

        .from-item label {
            display: block;
            margin-bottom: 5px;
            font-size: 14px;
            font-weight: bold;
            color: #555;
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

        .from-item input, .from-item select{
            width: 100%;
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 5px;
            box-sizing: border-box;
            font-size: 16px;
        }

        .from-item input:focus, .from-item select:focus {
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

                    <div class="from-item">
                        <label for="signup-userName">ユーザー名</label>
                        <input type="text" id="signup-userName" name="userName" value="<c:out value='${loginUser.name}' />" required>
                    </div>

                    <div class="from-item">
                        <label for="signup-password">新パスワード</label>
                        <input type="password" id="signup-password" name="password" required autocomplete="off" placeholder="変更する場合のみ入力">
                        <p class="note">※変更しない場合は空欄のままにしてくださ</p>
                    </div>

                   <div class="form-item">
                        <label for="signup-address">住所</label>
                        
                        <select id="signup-address" name="address" required>
                            <c:forEach var="pref" items="${prefList}">
                                <option value="${pref}" ${loginUser.address == pref ? 'selected' : ''}>
                                    <c:out value="${pref}" />
                                </option>
                            </c:forEach>
                        </select>
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
