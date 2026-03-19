<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!--JSTLのライブラリ変数用意-->
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>product</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">

	<!--cssのfont-awesome(汎用アイコン系)ライブラリ-->
	 <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

	<!--cssのBootstrap Icons(IT機器アイコン系ライブラリ)-->
	<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    
    <!--フォント設定-->
    <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@700&display=swap" rel="stylesheet">
</head>

<body>
    <c:import url="/jsp/Header/header.jsp" />
	${product.name}
	${product.price}円
	<img src="${pageContext.request.contextPath}/images/${product.image}" alt="${product.name}" style="width: 300px; height: auto;">

	<!-- 商品のスペックを表示する部分 -->
	 ${product}
	<c:choose>
		<c:when test="${type == 'cpu'}">
			<li>世代：${product.generation}</li>
			<li>クロック：${product.clock}</li>
			<li>コア数：${product.core}</li>
			<li>スレッド数：${product.thread}</li>
		</c:when>
		<c:when test="${type == 'gpu'}">
			<li>シリーズ名：${product.seriesName}</li>
			<li>チップ名：${product.chipName}</li>
			<li>メモリ容量：${product.vram}GB</li>
		</c:when>
		<c:when test="${type == 'memory'}">
			<li>世代：${product.generation}</li>
			<li>容量：${product.capacity}</li>
		</c:when>
		<c:when test="${type == 'hdd'}">
			<li>容量：${product.capacity}</li>
			<li>回転数：${product.rpm}</li>
		</c:when>
		<c:when test="${type == 'ssd'}">
			<li>容量：${product.capacity}</li>
			<li>タイプ：${product.type}</li>
		</c:when>
		<c:when test="${type == 'motherboard'}">
			<li>チップセット：${product.chipset}</li>
			<li>サイズ：${product.size}</li>
		</c:when>
	</c:choose>