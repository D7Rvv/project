<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.beans.BeanInfo" %>
<%@ page import="java.beans.Introspector" %>
<%@ page import="java.beans.PropertyDescriptor" %>
<%@ page import="java.lang.reflect.Method" %>

<%!
    private String esc(Object value) {
        if (value == null) return "";
        String s = String.valueOf(value);
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String dumpBean(Object bean) {
        if (bean == null) {
            return "<div class='null'>null</div>";
        }

        StringBuilder sb = new StringBuilder();

        try {
            BeanInfo info = Introspector.getBeanInfo(bean.getClass(), Object.class);
            PropertyDescriptor[] props = info.getPropertyDescriptors();

            sb.append("<table class='debug-table'>");
            sb.append("<tr><th>property</th><th>value</th></tr>");

            for (PropertyDescriptor pd : props) {
                Method getter = pd.getReadMethod();
                if (getter == null) {
                    continue;
                }

                Object value;
                try {
                    value = getter.invoke(bean);
                } catch (Exception e) {
                    value = "[getter error] " + e.getClass().getName() + ": " + e.getMessage();
                }

                sb.append("<tr>");
                sb.append("<td>").append(esc(pd.getName())).append("</td>");
                sb.append("<td>").append(esc(value)).append("</td>");
                sb.append("</tr>");
            }

            sb.append("</table>");
        } catch (Exception e) {
            sb.append("<div class='error'>");
            sb.append("Beanの展開に失敗しました: ");
            sb.append(esc(e.getClass().getName()));
            sb.append(" / ");
            sb.append(esc(e.getMessage()));
            sb.append("</div>");
        }

        return sb.toString();
    }
%>

<%
    Object product = request.getAttribute("product");
    Object productId = request.getAttribute("productId");
    String errorMsg = (String) request.getAttribute("errorMsg");

    String paramProductId = request.getParameter("productId");
    String paramCategory = request.getParameter("category");
    String paramDebug = request.getParameter("debug");
%>

<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>ProductServlet Debug Output</title>
    <style>
        body {
            margin: 24px;
            font-family: sans-serif;
            background: #f7f7f8;
            color: #222;
        }

        h1, h2 {
            margin-top: 0;
        }

        .card {
            background: #fff;
            border: 1px solid #d9dce1;
            border-radius: 10px;
            padding: 16px;
            margin-bottom: 20px;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
        }

        .debug-table {
            width: 100%;
            border-collapse: collapse;
            background: #fff;
        }

        .debug-table th,
        .debug-table td {
            border: 1px solid #ccc;
            padding: 8px;
            text-align: left;
            vertical-align: top;
            word-break: break-word;
        }

        .debug-table th {
            background: #f3f4f6;
            width: 240px;
        }

        .mono {
            font-family: Consolas, Monaco, monospace;
            white-space: pre-wrap;
        }

        .error {
            color: #b91c1c;
            font-weight: bold;
        }

        .ok {
            color: #166534;
            font-weight: bold;
        }

        .null {
            color: #777;
        }
    </style>
</head>
<body>

    <h1>ProductServlet 出力確認</h1>

    <div class="card">
        <h2>概要</h2>

        <% if (errorMsg != null) { %>
            <div class="error"><%= errorMsg %></div>
        <% } else if (product != null) { %>
            <div class="ok">product の取得に成功しています。</div>
        <% } else { %>
            <div class="null">product は設定されていません。</div>
        <% } %>
    </div>

    <div class="card">
        <h2>受信値</h2>
        <table class="debug-table">
            <tr>
                <th>request.getParameter("productId")</th>
                <td class="mono"><%= esc(paramProductId) %></td>
            </tr>
            <tr>
                <th>request.getParameter("category")</th>
                <td class="mono"><%= esc(paramCategory) %></td>
            </tr>
            <tr>
                <th>request.getParameter("debug")</th>
                <td class="mono"><%= esc(paramDebug) %></td>
            </tr>
        </table>
    </div>

    <div class="card">
        <h2>Servletが設定した属性</h2>
        <table class="debug-table">
            <tr>
                <th>request attribute "productId"</th>
                <td class="mono"><%= esc(productId) %></td>
            </tr>
            <tr>
                <th>request attribute "product"</th>
                <td class="mono"><%= product == null ? "null" : esc(product.getClass().getName()) %></td>
            </tr>
            <tr>
                <th>request attribute "errorMsg"</th>
                <td><%= errorMsg == null ? "null" : errorMsg %></td>
            </tr>
        </table>
    </div>

    <div class="card">
        <h2>ProductBean 詳細</h2>
        <%= dumpBean(product) %>
    </div>

</body>
</html>