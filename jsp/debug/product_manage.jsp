<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="bean.*" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>商品管理（デバッグ）</title>
    <style>
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #999; padding: 6px; }
        th { background: #eee; }
        .nav a { margin-right: 10px; }
        .error { color: red; }
    </style>
</head>
<body>

<%
    String type = (String)request.getAttribute("type");
    if (type == null) type = "cpu";

    Object productsObj = request.getAttribute("products");
    List<?> products = (productsObj == null) ? new ArrayList<>() : (List<?>)productsObj;
%>

<h2>商品管理（デバッグ）</h2>

<div class="nav">
    <a href="<%= request.getContextPath() %>/debug/ProductManage?action=list&type=cpu">CPU</a>
    <a href="<%= request.getContextPath() %>/debug/ProductManage?action=list&type=gpu">GPU</a>
    <a href="<%= request.getContextPath() %>/debug/ProductManage?action=list&type=mb">MOTHER_BOARD</a>
    <a href="<%= request.getContextPath() %>/debug/ProductManage?action=list&type=memory">MEMORY</a>
    <a href="<%= request.getContextPath() %>/debug/ProductManage?action=list&type=ssd">SSD</a>
    <a href="<%= request.getContextPath() %>/debug/ProductManage?action=list&type=hdd">HDD</a>
</div>

<hr>

<% if (request.getAttribute("error") != null) { %>
    <div class="error"><%= request.getAttribute("error") %></div>
<% } %>

<p>
    現在の種類：<b><%= type %></b>
</p>

<p>
    <a href="<%= request.getContextPath() %>/debug/ProductManage?action=new&type=<%= type %>">
        ＋ 新規追加
    </a>
</p>

<table>
    <thead>
        <tr>
            <th>ID</th>
            <th>PRODUCT_ID</th>
            <th>メーカーID</th>
            <th>商品名</th>
            <th>価格</th>
            <th>画像ID</th>

            <%-- 固有カラム --%>
            <% if ("cpu".equals(type)) { %>
                <th>世代</th><th>Core</th><th>Thread</th><th>Clock</th>
            <% } else if ("gpu".equals(type)) { %>
                <th>Series</th><th>Chip</th><th>VRAM</th>
            <% } else if ("mb".equals(type)) { %>
                <th>Chipset</th><th>Size</th>
            <% } else if ("memory".equals(type)) { %>
                <th>世代</th><th>容量</th>
            <% } else if ("ssd".equals(type)) { %>
                <th>容量</th><th>Type</th>
            <% } else if ("hdd".equals(type)) { %>
                <th>容量</th><th>RPM</th>
            <% } %>

            <th>操作</th>
        </tr>
    </thead>
    <tbody>
    <%
        for (Object o : products) {
            int id = 0;
            int productId = 0;
            int makerId = 0;
            String name = "";
            int price = 0;
            String image = "";

            if (o instanceof CpuBean) {
                CpuBean b = (CpuBean)o;
                id = b.getCpuId();
                productId = b.getProductId();
                makerId = b.getMakerId();
                name = b.getName();
                price = b.getPrice();
                image = b.getImageId();
            } else if (o instanceof GpuBean) {
                GpuBean b = (GpuBean)o;
                id = b.getGpuId();
                productId = b.getProductId();
                makerId = b.getMakerId();
                name = b.getName();
                price = b.getPrice();
                image = b.getImageId();
            } else if (o instanceof MotherBoardBean) {
                MotherBoardBean b = (MotherBoardBean)o;
                id = b.getMotherBoardId();
                productId = b.getProductId();
                makerId = b.getMakerId();
                name = b.getName();
                price = b.getPrice();
                image = b.getImageId();
            } else if (o instanceof MemoryBean) {
                MemoryBean b = (MemoryBean)o;
                id = b.getMemoryId();
                productId = b.getProductId();
                makerId = b.getMakerId();
                name = b.getName();
                price = b.getPrice();
                image = b.getImageId();
            } else if (o instanceof SsdBean) {
                SsdBean b = (SsdBean)o;
                id = b.getSsdId();
                productId = b.getProductId();
                makerId = b.getMakerId();
                name = b.getName();
                price = b.getPrice();
                image = b.getImageId();
            } else if (o instanceof HddBean) {
                HddBean b = (HddBean)o;
                id = b.getHddId();
                productId = b.getProductId();
                makerId = b.getMakerId();
                name = b.getName();
                price = b.getPrice();
                image = b.getImageId();
            }
    %>
        <tr>
            <td><%= id %></td>
            <td><%= productId %></td>
            <td><%= makerId %></td>
            <td><%= name %></td>
            <td><%= price %></td>
            <td><%= image %></td>

            <%-- 固有カラム表示 --%>
            <% if (o instanceof CpuBean) { CpuBean b=(CpuBean)o; %>
                <td><%= b.getGeneration() %></td>
                <td><%= b.getCore() %></td>
                <td><%= b.getThread() %></td>
                <td><%= b.getClock() %></td>
            <% } else if (o instanceof GpuBean) { GpuBean b=(GpuBean)o; %>
                <td><%= b.getSeriesName() %></td>
                <td><%= b.getChipName() %></td>
                <td><%= b.getVram() %></td>
            <% } else if (o instanceof MotherBoardBean) { MotherBoardBean b=(MotherBoardBean)o; %>
                <td><%= b.getChipset() %></td>
                <td><%= b.getSize() %></td>
            <% } else if (o instanceof MemoryBean) { MemoryBean b=(MemoryBean)o; %>
                <td><%= b.getGeneration() %></td>
                <td><%= b.getCapacity() %></td>
            <% } else if (o instanceof SsdBean) { SsdBean b=(SsdBean)o; %>
                <td><%= b.getCapacity() %></td>
                <td><%= b.getType() %></td>
            <% } else if (o instanceof HddBean) { HddBean b=(HddBean)o; %>
                <td><%= b.getCapacity() %></td>
                <td><%= b.getRpm() %></td>
            <% } %>

            <td>
                <a href="<%= request.getContextPath() %>/debug/ProductManage?action=edit&type=<%= type %>&id=<%= id %>">
                    編集
                </a>

                <form action="<%= request.getContextPath() %>/debug/ProductManage" method="post" style="display:inline;">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="type" value="<%= type %>">
                    <input type="hidden" name="id" value="<%= id %>">
                    <button type="submit" onclick="return confirm('削除しますか？');">削除</button>
                </form>
            </td>
        </tr>
    <% } %>
    </tbody>
</table>

</body>
</html>