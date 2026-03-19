<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!--JSTLのライブラリ変数用意-->
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TOP</title>

	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/Header/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/Top/index.css">

	<!--cssのfont-awesome(汎用アイコン系)ライブラリ-->
	 <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

	<!--cssのBootstrap Icons(IT機器アイコン系ライブラリ)-->
	<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    
    <!--フォント設定-->
    <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@700&display=swap" rel="stylesheet">
</head>

<body>
	<!--ヘッダー部-->
    <jsp:include page="/jsp/Header/header.jsp"/>

	<!--メイン部-->
	<div class="main-container">
		<div class="main-title">
			<h1>購入履歴</h1>
		</div>
		<div class="errorMsg">
			${errorMsg}
		</div>
		<!--購入履歴-->
		<div class="">
            <div><h1>購入履歴</h1></div>
            <div>
                <c:forEach var="history" items="${purchaseHistory}">
                    <div class="purchase-item">
                        <p>商品名: ${history.productName}</p>
                        <p>購入日: ${history.purchaseDate}</p>
                        <p>価格: ${history.price}円</p>
                    </div>
                </c:forEach>
            </div>
        </div>

	</div>

</body>
</html>