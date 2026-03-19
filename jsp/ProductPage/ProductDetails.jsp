<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="jakarta.tags.core" prefix="c" %>
        <%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

            <!DOCTYPE html>
            <html lang="ja">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>${product.name} - PRODUCT DETAILS</title>

                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/Header/header.css">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/ProductPage/productDetails.css">

                <!--cssのfont-awesome(汎用アイコン系)ライブラリ-->
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

                <!--cssのBootstrap Icons(IT機器アイコン系ライブラリ)-->
                <link rel="stylesheet"
                    href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

                <!--フォント設定-->
                <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&display=swap"
                    rel="stylesheet">
            </head>

            <body>
                <jsp:include page="/jsp/Header/header.jsp" />

                <div class="main-container">
                    <!-- 商品画像エリア -->
                    <div class="product-image-container">
                        <c:choose>
                            <c:when test="${not empty product.imageId}">
                                <img src="${pageContext.request.contextPath}/images/${product.imageId}"
                                    alt="${product.name}">
                            </c:when>
                            <c:otherwise>
                                <div style="font-size: 5rem; color: #ccc;"><i class="bi bi-box-seam"></i></div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- 商品情報エリア -->
                    <div class="product-info-container">
                        <h1 class="product-name">${product.name}</h1>

                        <div class="product-price">
                            <fmt:formatNumber value="${product.price}" pattern="#,###" /><span>円 (税込)</span>
                        </div>

                        <!-- 商品のスペックを表示する部分 -->
                        <div class="product-specs">
                            <h3><i class="bi bi-list-columns-reverse"></i> 基本スペック</h3>
                            <ul>
                                <c:choose>
                                    <c:when test="${product.productType == 'cpu' || product.productType == 'CPU'}">
                                        <li><span class="spec-label">世代</span><span
                                                class="spec-value">${product.generation}</span></li>
                                        <li><span class="spec-label">クロック</span><span
                                                class="spec-value">${product.clock}</span></li>
                                        <li><span class="spec-label">コア数</span><span
                                                class="spec-value">${product.core}</span></li>
                                        <li><span class="spec-label">スレッド数</span><span
                                                class="spec-value">${product.thread}</span></li>
                                    </c:when>
                                    <c:when test="${product.productType == 'gpu' || product.productType == 'GPU'}">
                                        <li><span class="spec-label">シリーズ名</span><span
                                                class="spec-value">${product.seriesName}</span></li>
                                        <li><span class="spec-label">チップ名</span><span
                                                class="spec-value">${product.chipName}</span></li>
                                        <li><span class="spec-label">メモリ容量</span><span
                                                class="spec-value">${product.vram} GB</span></li>
                                    </c:when>
                                    <c:when test="${product.productType == 'memory' || product.productType == 'MEMORY'}">
                                        <li><span class="spec-label">世代</span><span
                                                class="spec-value">${product.generation}</span></li>
                                        <li><span class="spec-label">容量</span><span
                                                class="spec-value">${product.capacity}</span></li>
                                    </c:when>
                                    <c:when test="${product.productType == 'hdd' || product.productType == 'HDD'}">
                                        <li><span class="spec-label">容量</span><span
                                                class="spec-value">${product.capacity}</span></li>
                                        <li><span class="spec-label">回転数</span><span class="spec-value">${product.rpm}
                                                rpm</span></li>
                                    </c:when>
                                    <c:when test="${product.productType == 'ssd' || product.productType == 'SSD'}">
                                        <li><span class="spec-label">容量</span><span
                                                class="spec-value">${product.capacity}</span></li>
                                        <li><span class="spec-label">タイプ</span><span
                                                class="spec-value">${product.type}</span></li>
                                    </c:when>
                                    <c:when test="${product.productType == 'motherboard' || product.productType == 'mother_board' || product.productType == 'MOTHER_BOARD'}">
                                        <li><span class="spec-label">チップセット</span><span
                                                class="spec-value">${product.chipset}</span></li>
                                        <li><span class="spec-label">フォームファクタ</span><span
                                                class="spec-value">${product.size}</span></li>
                                    </c:when>
                                    <c:otherwise>
                                        <li><span class="spec-label">商品カテゴリ</span><span
                                                class="spec-value">${product.productType}</span></li>
                                    </c:otherwise>
                                </c:choose>
                            </ul>
                        </div>

                        <div class="action-buttons">
                            <form action="${pageContext.request.contextPath}/servlet/CartServlet" method="post"
                                style="flex: 1;">
                                <input type="hidden" name="productId" value="${product.productId}">
                                <input type="hidden" name="action" value="add">
                                <!-- 数量選択追加する場合はここに -->
                                <button type="submit" class="btn-cart">
                                    <i class="bi bi-cart-plus"></i> カートに入れる
                                </button>
                            </form>
                            <a href="javascript:history.back()" class="btn-back">
                                <i class="bi bi-arrow-return-left"></i> 戻る
                            </a>
                        </div>
                    </div>
                </div>
            </body>

            </html>