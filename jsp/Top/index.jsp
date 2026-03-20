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
			<h1>どの商品をお探しですか？</h1>
		</div>

		<div class="errorMsg">
			${errorMsg}
		</div>
		
		<!--　カテゴリグループ部　-->
		<div class="category-group">
			<a href="${pageContext.request.contextPath}/servlet/Category?category=CPU" class="category-card">
				<div class="card-icon"><i class="bi bi-cpu"></i></div>
				<h3>CPU</h3>
				<p>Intel Core / AMD Ryzen</p>
			</a>

			<a href="${pageContext.request.contextPath}/servlet/Category?category=GPU" class="category-card">
				<div class="card-icon"><i class="bi bi-gpu-card"></i></div>
				<h3>GPU</h3>
				<p>NVIDIA GeForce / Radeon</p>
			</a>

			<a href="${pageContext.request.contextPath}/servlet/Category?category=MEMORY" class="category-card">
				<div class="card-icon"><i class="bi bi-memory"></i></div>
				<h3>MEMORY</h3>
				<p>DDR4 / DDR5</p>
			</a>

			<a href="${pageContext.request.contextPath}/servlet/Category?category=MOTHER-BOARD" class="category-card">
				<div class="card-icon"><i class="bi bi-motherboard"></i></div>
				<h3>MOTHERBOARD</h3>
				<p>ATX / Micro-ATX</p>
			</a>

			<a href="${pageContext.request.contextPath}/servlet/Category?category=HDD" class="category-card">
				<div class="card-icon"><i class="bi bi-hdd"></i></div>
				<h3>HDD</h3>
				<p>3.5inch / NAS用</p>
			</a>

			<a href="${pageContext.request.contextPath}/servlet/Category?category=SSD" class="category-card">
				<div class="card-icon"><i class="bi bi-device-ssd"></i></div>
				<h3>SSD</h3>
				<p>M.2 NVMe / SATA</p>
			</a>
		</div>

	</div>

</body>
</html>