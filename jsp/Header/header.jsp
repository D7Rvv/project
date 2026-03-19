<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!--JSTLのライブラリ変数用意-->
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!--ページ元のURL-->
<c:set var="fromURL" value="${pageContext.request.requestURI}"/>

<!--ページ元のパラメータ-->
<c:set var="query" value="${pageContext.request.queryString}"/>

<!--ページ元のフルパス-->
<c:set var="rawFromURL" value="${fromURL}${not empty query ? '?' : ''}${query}"/>

<!-- 
    ※共通ファイル
    このファイルで、<html>や<body>は書かないで。
-->

<!--ヘッダー部-->
<header class="site-header">
    <a href="${pageContext.request.contextPath}/jsp/Top/index.jsp" class="brand-logo">
        <span style="color: #085dbe">KNOCK</span><span> PARTS</span>
    </a>
    
    <!--検索バー-->
    <form class="search-form">
        <input type="text" placeholder="キーワード検索">
        <button type="submit"><i class="fa-solid fa-magnifying-glass"></i></button>
    </form>
    
    <!--ナビゲーション部-->
    <nav>
        <ul>
            <li>
                <!--ユーザーアイコンのログインステータス判定-->
                <c:choose>

                    <%--ログイン前 (「loginUser.name」が空の場合) --%>
                    <c:when test="${empty loginUser.name}">
                        <c:set var="displayName" value="ログイン" />
                    </c:when>

                    <%--ログイン後 (ユーザー名を表示する) --%>
                    <c:otherwise>
                        <c:set var="displayName" value="${loginUser.name}さん" />
                    </c:otherwise>

                </c:choose>
                
                <!--ログインステータスに応じて、サブメニューを追加する処理-->
                <c:choose>

                    <%--ログイン前の場合--%>
                    <c:when test="${empty loginUser.name}">
                        <a href="${pageContext.request.contextPath}/jsp/Login/login.jsp?rawFromURL=${rawFromURL}" class="nav-btn">
                            <i style="font-size: 25px;" class="fa-solid fa-user"></i>
                            <span class="user-name nav-font"><c:out value="${displayName}" /></span>
                        </a>
                    </c:when>

                    <%--ログイン済の場合--%>
                    <c:otherwise>
                        <div class="user-menu-container">
                            <input type="checkbox" id="user-menu-check" style="display: none;">

                            <%--オーバーレイ用--%>
                            <label for="user-menu-check" class="menu-overlay"></label>

                            <label for="user-menu-check" class="nav-btn" style="cursor: pointer;">
                                <i style="font-size: 25px;" class="fa-solid fa-user"></i>
                                <span class="user-name nav-font"><c:out value="${displayName}" /></span>
                            </label>
                            
                            <%--サブメニュー部--%>
                            <div class="user-sub-menu">
                                <form action="${pageContext.request.contextPath}/UserServlet" method="post">
                                    <ul class="sub-menu-list">
                                        <li>
                                            <button type="submit" name="action" value="logout">
                                                <i class="fa-solid fa-history"></i> 購入履歴
                                            </button>
                                        </li>
                                        <li>
                                            <a href="${pageContext.request.contextPath}/jsp/Mypage/myPage.jsp">
                                                <i class="fa-solid fa-circle-user"></i> マイページ
                                            </a>
                                        </li>
                                        <li>
                                            <button type="submit" name="action" value="logout">
                                                <i class="fa-solid fa-right-from-bracket"></i> ログアウト
                                            </button>
                                        </li>
                                    </ul>
                                </form>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </li>

            <li>
                <a href="#" class="nav-btn">
                    <i style="font-size: 25px;" class="fa-solid fa-cart-shopping"></i>
                    <span class="nav-font">カート</span>
                </a>
            </li>
        </ul>
    </nav>
</header>