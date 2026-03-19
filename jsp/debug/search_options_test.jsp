<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="beans.*" %>
<%@ page import="beans.option.*" %>
<%@ page import="dao.SearchDao" %>

<%!
    private String joinOptionValues(List<SelectableOption> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(list.get(i).getValue());
        }
        return sb.toString();
    }

    private String formatRange(RangeOption<?> range) {
        if (range == null) return "";
        Object min = range.getMinValue();
        Object max = range.getMaxValue();
        if (min == null && max == null) return "";
        if (min == null) return "〜" + max;
        if (max == null) return min + "〜";
        return min + "〜" + max;
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>検索テスト＋オプション一覧（デバッグ）</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
        }
        .section {
            background: #f5f5f5;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
        .form-group {
            margin-bottom: 10px;
        }
        label {
            display: inline-block;
            width: 110px;
            font-weight: bold;
        }
        input[type="text"], select {
            padding: 5px 10px;
            font-size: 14px;
        }
        input[type="submit"] {
            padding: 8px 20px;
            background: #007bff;
            color: white;
            border: none;
            border-radius: 3px;
            cursor: pointer;
        }
        input[type="submit"]:hover {
            background: #0056b3;
        }
        table {
            border-collapse: collapse;
            width: 100%;
            margin-top: 20px;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 12px;
            text-align: left;
        }
        th {
            background: #007bff;
            color: white;
        }
        tr:nth-child(even) {
            background: #f9f9f9;
        }
        .error {
            color: red;
            background: #ffe6e6;
            padding: 10px;
            border-radius: 3px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>

<h1>検索テスト＋オプション一覧（デバッグ）</h1>

<%
    String value = (String) request.getAttribute("value");
    String type = (String) request.getAttribute("type");
    String error = (String) request.getAttribute("error");
    Object resultsObj = request.getAttribute("results");
    List<?> results = (resultsObj == null) ? new ArrayList<>() : (List<?>) resultsObj;

    if (value == null) value = "";
    if (type == null) type = "all";

    String optionCategory = request.getParameter("optionCategory");
    if (optionCategory == null || optionCategory.isBlank()) {
        optionCategory = "all";
    }

    Map<String, ProductOption<?>> options = null;
    String optionsError = null;
    try {
        SearchDao dao = new SearchDao();
        options = dao.getOptions(optionCategory);
    } catch (Exception e) {
        optionsError = e.getMessage();
    }
%>

<!-- 検索フォーム -->
<div class="section">
    <h2>商品検索</h2>
    <form method="get" action="<%= request.getContextPath() %>/debug/SearchTest">
        <div class="form-group">
            <label for="value">検索キーワード:</label>
            <input type="text" id="value" name="value" value="<%= value %>" size="40">
        </div>
        <div class="form-group">
            <label for="type">商品タイプ:</label>
            <select id="type" name="type">
                <option value="all" <%= "all".equals(type) ? "selected" : "" %>>すべて</option>
                <option value="cpu" <%= "cpu".equals(type) ? "selected" : "" %>>CPU</option>
                <option value="gpu" <%= "gpu".equals(type) ? "selected" : "" %>>GPU</option>
                <option value="memory" <%= "memory".equals(type) ? "selected" : "" %>>メモリ</option>
                <option value="mother_board" <%= "mother_board".equals(type) ? "selected" : "" %>>マザーボード</option>
                <option value="ssd" <%= "ssd".equals(type) ? "selected" : "" %>>SSD</option>
                <option value="hdd" <%= "hdd".equals(type) ? "selected" : "" %>>HDD</option>
            </select>
        </div>
        <div class="form-group">
            <input type="hidden" name="fromURL" value="<%= request.getContextPath() %>/jsp/debug/search_options_test.jsp">
            <input type="submit" value="検索">
        </div>
    </form>
</div>

<!-- エラーメッセージ表示 -->
<% if (error != null && !error.isEmpty()) { %>
    <div class="error">
        <strong>エラー:</strong> <%= error %>
    </div>
<% } %>

<!-- 検索結果表示 -->
<% if (results.size() > 0) { %>
    <div class="section">
        <h2>検索結果</h2>
        <table>
            <thead>
                <tr>
                    <th>商品ID</th>
                    <th>商品名</th>
                    <th>価格</th>
                    <th>メーカー</th>
                    <th>種別</th>
                </tr>
            </thead>
            <tbody>
                <%
                    for (Object obj : results) {
                        ProductBean bean = (ProductBean) obj;
                        String productType = "";
                        if (bean instanceof CpuBean) productType = "CPU";
                        else if (bean instanceof GpuBean) productType = "GPU";
                        else if (bean instanceof MemoryBean) productType = "MEMORY";
                        else if (bean instanceof MotherBoardBean) productType = "MOTHER_BOARD";
                        else if (bean instanceof SsdBean) productType = "SSD";
                        else if (bean instanceof HddBean) productType = "HDD";
                %>
                <tr>
                    <td><%= bean.getProductId() %></td>
                    <td><%= bean.getName() %></td>
                    <td><%= String.format("%,d", bean.getPrice()) %> 円</td>
                    <td><%= bean.getMakerName() %></td>
                    <td><%= productType %></td>
                </tr>
                <% } %>
            </tbody>
        </table>
    </div>
<% } else if (value != null && !value.isEmpty()) { %>
    <div class="section">
        <div class="error">
            検索キーワード「<%= value %>」に一致する商品はありません
        </div>
    </div>
<% } %>

<!-- getOptionsデバッグ -->
<div class="section">
    <h2>SearchDao#getOptions 出力</h2>
    <form method="get" action="<%= request.getRequestURI() %>">
        <div class="form-group">
            <label for="optionCategory">カテゴリ:</label>
            <select id="optionCategory" name="optionCategory">
                <option value="all" <%= "all".equals(optionCategory) ? "selected" : "" %>>すべて</option>
                <option value="cpu" <%= "cpu".equals(optionCategory) ? "selected" : "" %>>CPU</option>
                <option value="gpu" <%= "gpu".equals(optionCategory) ? "selected" : "" %>>GPU</option>
                <option value="memory" <%= "memory".equals(optionCategory) ? "selected" : "" %>>メモリ</option>
                <option value="mother_board" <%= "mother_board".equals(optionCategory) ? "selected" : "" %>>マザーボード</option>
                <option value="ssd" <%= "ssd".equals(optionCategory) ? "selected" : "" %>>SSD</option>
                <option value="hdd" <%= "hdd".equals(optionCategory) ? "selected" : "" %>>HDD</option>
            </select>
            <input type="submit" value="表示">
        </div>
    </form>

    <% if (optionsError != null) { %>
        <div class="error">
            <strong>getOptions エラー:</strong> <%= optionsError %>
        </div>
    <% } else if (options != null && !options.isEmpty()) { %>
        <% for (Map.Entry<String, ProductOption<?>> categoryEntry : options.entrySet()) {
               String cat = categoryEntry.getKey();
               ProductOption<?> opt = categoryEntry.getValue();
        %>
        <h3><%= cat %></h3>
        <table>
            <thead>
                <tr>
                    <th>フィールド</th>
                    <th>値</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>MAKER</td>
                    <td><%= joinOptionValues(opt.getMaker()) %></td>
                </tr>
                <tr>
                    <td>PRICE</td>
                    <td><%= formatRange(opt.getPrice()) %></td>
                </tr>
                <% if (opt instanceof CpuOption) { %>
                    <tr>
                        <td>CPU_GEN</td>
                        <td><%= joinOptionValues(((CpuOption) opt).getGeneration()) %></td>
                    </tr>
                    <tr>
                        <td>CPU_CORE</td>
                        <td><%= formatRange(((CpuOption) opt).getCore()) %></td>
                    </tr>
                    <tr>
                        <td>CPU_THREAD</td>
                        <td><%= formatRange(((CpuOption) opt).getThread()) %></td>
                    </tr>
                    <tr>
                        <td>CPU_CLOCK</td>
                        <td><%= formatRange(((CpuOption) opt).getClock()) %></td>
                    </tr>
                <% } else if (opt instanceof GpuOption) { %>
                    <tr>
                        <td>GPU_SERIES</td>
                        <td><%= joinOptionValues(((GpuOption) opt).getSeries()) %></td>
                    </tr>
                    <tr>
                        <td>GPU_CHIP</td>
                        <td><%= joinOptionValues(((GpuOption) opt).getChip()) %></td>
                    </tr>
                    <tr>
                        <td>GPU_VRAM</td>
                        <td><%= formatRange(((GpuOption) opt).getVram()) %></td>
                    </tr>
                <% } else if (opt instanceof MemoryOption) { %>
                    <tr>
                        <td>MEMORY_GEN</td>
                        <td><%= joinOptionValues(((MemoryOption) opt).getGeneration()) %></td>
                    </tr>
                    <tr>
                        <td>MEMORY_CAPACITY</td>
                        <td><%= joinOptionValues(((MemoryOption) opt).getCapacity()) %></td>
                    </tr>
                <% } else if (opt instanceof MotherBoardOption) { %>
                    <tr>
                        <td>MOTHERBOARD_CHIPSET</td>
                        <td><%= joinOptionValues(((MotherBoardOption) opt).getChipset()) %></td>
                    </tr>
                    <tr>
                        <td>MOTHERBOARD_SIZE</td>
                        <td><%= joinOptionValues(((MotherBoardOption) opt).getSize()) %></td>
                    </tr>
                <% } else if (opt instanceof SsdOption) { %>
                    <tr>
                        <td>SSD_CAP</td>
                        <td><%= joinOptionValues(((SsdOption) opt).getCapacity()) %></td>
                    </tr>
                    <tr>
                        <td>SSD_TYPE</td>
                        <td><%= joinOptionValues(((SsdOption) opt).getType()) %></td>
                    </tr>
                <% } else if (opt instanceof HddOption) { %>
                    <tr>
                        <td>HDD_CAP</td>
                        <td><%= joinOptionValues(((HddOption) opt).getCapacity()) %></td>
                    </tr>
                    <tr>
                        <td>HDD_RPM</td>
                        <td><%= joinOptionValues(((HddOption) opt).getRpm()) %></td>
                    </tr>
                <% } %>
            </tbody>
        </table>
        <% } %>
    <% } else { %>
        <div>選択したカテゴリにデータがありません</div>
    <% } %>
</div>

<hr>
<p>
    <a href="<%= request.getContextPath() %>/jsp/debug/search_test.jsp">検索テスト（単体）へ</a><br>
    <a href="<%= request.getContextPath() %>/jsp/debug/urls.txt">デバッグページ一覧</a>
</p>

</body>
</html>
