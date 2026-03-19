<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PRODUCT PAGE</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/Header/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/ProductPage/productPage.css">

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

    <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/nouislider@15.8.1/dist/nouislider.min.css">
    <script src="https://cdn.jsdelivr.net/npm/nouislider@15.8.1/dist/nouislider.min.js" defer></script>
</head>

<body>

<jsp:include page="/jsp/Header/header.jsp"/>
<nav class="category-nav">
    <h2>カテゴリ選択</h2>
    <a href="${pageContext.request.contextPath}/servlet/CategoryPageServlet?category=${category == 'CPU' ? 'all' : 'CPU'}"
        class="category-btn ${category == 'CPU' ? 'active' : ''}">
        <i class="bi bi-cpu"></i> CPU
    </a>
    <a href="${pageContext.request.contextPath}/servlet/CategoryPageServlet?category=${category == 'GPU' ? 'all' : 'GPU'}"
        class="category-btn ${category == 'GPU' ? 'active' : ''}">
        <i class="bi bi-gpu-card"></i> GPU
    </a>
    <a href="${pageContext.request.contextPath}/servlet/CategoryPageServlet?category=${category == 'MEMORY' ? 'all' : 'MEMORY'}"
        class="category-btn ${category == 'MEMORY' ? 'active' : ''}">
        <i class="bi bi-memory"></i> MEMORY
    </a>
    <a href="${pageContext.request.contextPath}/servlet/CategoryPageServlet?category=${category == 'MOTHER_BOARD' ? 'all' : 'MOTHER_BOARD'}"
        class="category-btn ${category == 'MOTHER_BOARD' ? 'active' : ''}">
        <i class="bi bi-motherboard"></i> MOTHERBOARD
    </a>
    <a href="${pageContext.request.contextPath}/servlet/CategoryPageServlet?category=${category == 'HDD' ? 'all' : 'HDD'}"
        class="category-btn ${category == 'HDD' ? 'active' : ''}">
        <i class="bi bi-hdd"></i> HDD
    </a>
    <a href="${pageContext.request.contextPath}/servlet/CategoryPageServlet?category=${category == 'SSD' ? 'all' : 'SSD'}"
        class="category-btn ${category == 'SSD' ? 'active' : ''}">
        <i class="bi bi-device-ssd"></i> SSD
    </a>
</nav>
    <div class="search-box" style="margin:20px;">
        <input type="text" id="search-input" placeholder="商品名で検索..." style="padding:8px; width:250px;">
        <button type="submit" id="search-button" style="padding:8px 16px; margin-left:10px;">
            検索
        </button>
    </div>

    <div class="main-container">

        <aside class="sidebar">
            <div class="sidebar-header">
                <h3><i class="fa-solid fa-filter"></i> 絞り込み</h3>
                <a href="" id="clear-button">クリア</a>
            </div>

            <div class="checkbox-group">
                <div id="filter-option-area"></div>
            </div>
        </aside>

        <section class="product-section">

            <div class="product-area" id="product-display-area"></div>

            <div id="page-area">
                <span id="current-page"></span>
            </div>

        </section>

    </div>

    <div id="current-category" style="display:none;">
        <c:out value="${category}" default="CPU"/>
    </div>
<script type="module" src="${pageContext.request.contextPath}/js/dist/index.js"></script>

</body>
</html>