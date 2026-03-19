<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="beans.*" %>
<%@ page import="beans.option.*" %>

<%!
    private String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String toJson(List<?> list) {
        if (list == null) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Object obj : list) {
            if (!(obj instanceof ProductBean)) continue;
            ProductBean bean = (ProductBean) obj;
            if (!first) sb.append(",");
            first = false;

            String category = "";
            if (bean instanceof CpuBean) category = "CPU";
            else if (bean instanceof GpuBean) category = "GPU";
            else if (bean instanceof MemoryBean) category = "MEMORY";
            else if (bean instanceof MotherBoardBean) category = "MOTHER_BOARD";
            else if (bean instanceof SsdBean) category = "SSD";
            else if (bean instanceof HddBean) category = "HDD";

            sb.append("{");
            sb.append("\"id\":").append(bean.getProductId()).append(",");
            sb.append("\"name\":\"").append(jsonEscape(bean.getName())).append("\",");
            sb.append("\"price\":").append(bean.getPrice()).append(",");
            sb.append("\"maker\":\"").append(jsonEscape(bean.getMakerName())).append("\",");
            sb.append("\"category\":\"").append(jsonEscape(category)).append("\"");
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>検索テスト（デバッグ）</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
        }
        .search-form {
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
            width: 100px;
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
        .error {
            color: red;
            background: #ffe6e6;
            padding: 10px;
            border-radius: 3px;
            margin-bottom: 20px;
        }
        .success {
            color: green;
            background: #e6ffe6;
            padding: 10px;
            border-radius: 3px;
            margin-bottom: 20px;
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
        .no-results {
            text-align: center;
            color: #999;
            padding: 20px;
        }
        .layout {
            display: flex;
            gap: 20px;
            align-items: flex-start;
        }
        .sidebar {
            width: 320px;
            background: #f5f5f5;
            padding: 15px;
            border-radius: 5px;
        }
        .main {
            flex: 1;
        }
        .filter-group {
            margin-bottom: 16px;
        }
        .filter-group label {
            display: block;
            font-weight: bold;
            margin-bottom: 6px;
        }
        .filter-checkboxes label {
            display: inline-block;
            margin-right: 10px;
        }
        .filter-range {
            display: flex;
            gap: 10px;
            align-items: center;
        }
        .filter-range input {
            width: 100px;
        }
    </style>
</head>
<body>

<h1>検索テスト（デバッグ）</h1>

<%
    String value = (String)request.getAttribute("value");
    String type = (String)request.getAttribute("type");
    String error = (String)request.getAttribute("error");
    Object resultsObj = request.getAttribute("results");
    List<?> results = (resultsObj == null) ? new ArrayList<>() : (List<?>)resultsObj;
    Map<String, ProductOption<?>> options = (Map<String, ProductOption<?>>) request.getAttribute("options");
    ProductOption<?> selectedOption = (options == null || type == null) ? null : options.get(type);

    if (value == null) value = "";
    if (type == null) type = "all";
%>

<div class="layout">
    <aside class="sidebar">
        <!-- 検索フォーム -->
        <div class="search-form">
            <form id="searchForm" method="get" action="#">
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
                    <label for="maxResults">上限件数</label>
                    <input type="number" id="maxResults" name="maxResults" value="10" min="0" style="width: 80px;">
                </div>
                <div class="form-group">
                    <input type="hidden" name="fromURL" value="<%= request.getContextPath() %>/jsp/debug/search_test.jsp">
                    <input type="submit" value="検索">
                </div>
            </form>
        </div>

        <!-- オプション（フィルタ） -->
        <div class="filter-panel">
            <h2>フィルタ</h2>
            <% if (selectedOption == null) { %>
                <p>カテゴリを選択してください。</p>
            <% } else { %>
                <form id="filterForm" action="<%= request.getContextPath() %>/debug/Option" method="get">
                    <input type="hidden" name="action" value="filter">
                    <input type="hidden" name="type" value="<%= type %>">
                    <input type="hidden" name="value" value="<%= value %>">

                    <div class="filter-group">
                        <label>メーカー</label>
                        <div class="filter-checkboxes">
                            <% for (SelectableOption opt : selectedOption.getMaker()) { %>
                                <label>
                                    <input type="checkbox" name="opt_<%= type %>_MAKER" value="<%= opt.getValue() %>">
                                    <%= opt.getValue() %>
                                </label>
                            <% } %>
                        </div>
                    </div>

                    <div class="filter-group">
                        <label>価格</label>
                        <div class="filter-range">
                            <input type="number" name="opt_<%= type %>_PRICE_min" placeholder="最小" min="0">
                            <span>〜</span>
                            <input type="number" name="opt_<%= type %>_PRICE_max" placeholder="最大" min="0">
                        </div>
                    </div>

                    <% if (selectedOption instanceof CpuOption) { %>
                        <div class="filter-group">
                            <label>世代</label>
                            <div class="filter-checkboxes">
                                <% for (SelectableOption opt : ((CpuOption) selectedOption).getGeneration()) { %>
                                    <label>
                                        <input type="checkbox" name="opt_<%= type %>_CPU_GEN" value="<%= opt.getValue() %>">
                                        <%= opt.getValue() %>
                                    </label>
                                <% } %>
                            </div>
                        </div>
                        <div class="filter-group">
                            <label>コア</label>
                            <div class="filter-range">
                                <input type="number" name="opt_<%= type %>_CPU_CORE_min" placeholder="最小" min="0">
                                <span>〜</span>
                                <input type="number" name="opt_<%= type %>_CPU_CORE_max" placeholder="最大" min="0">
                            </div>
                        </div>
                        <div class="filter-group">
                            <label>スレッド</label>
                            <div class="filter-range">
                                <input type="number" name="opt_<%= type %>_CPU_THREAD_min" placeholder="最小" min="0">
                                <span>〜</span>
                                <input type="number" name="opt_<%= type %>_CPU_THREAD_max" placeholder="最大" min="0">
                            </div>
                        </div>
                        <div class="filter-group">
                            <label>クロック</label>
                            <div class="filter-range">
                                <input type="number" name="opt_<%= type %>_CPU_CLOCK_min" placeholder="最小" step="0.1" min="0">
                                <span>〜</span>
                                <input type="number" name="opt_<%= type %>_CPU_CLOCK_max" placeholder="最大" step="0.1" min="0">
                            </div>
                        </div>
                    <% } else if (selectedOption instanceof GpuOption) { %>
                        <div class="filter-group">
                            <label>シリーズ</label>
                            <div class="filter-checkboxes">
                                <% for (SelectableOption opt : ((GpuOption) selectedOption).getSeries()) { %>
                                    <label>
                                        <input type="checkbox" name="opt_<%= type %>_GPU_SERIES" value="<%= opt.getValue() %>">
                                        <%= opt.getValue() %>
                                    </label>
                                <% } %>
                            </div>
                        </div>
                        <div class="filter-group">
                            <label>チップ</label>
                            <div class="filter-checkboxes">
                                <% for (SelectableOption opt : ((GpuOption) selectedOption).getChip()) { %>
                                    <label>
                                        <input type="checkbox" name="opt_<%= type %>_GPU_CHIP" value="<%= opt.getValue() %>">
                                        <%= opt.getValue() %>
                                    </label>
                                <% } %>
                            </div>
                        </div>
                        <div class="filter-group">
                            <label>VRAM (GB)</label>
                            <div class="filter-range">
                                <input type="number" name="opt_<%= type %>_GPU_VRAM_min" placeholder="最小" min="0">
                                <span>〜</span>
                                <input type="number" name="opt_<%= type %>_GPU_VRAM_max" placeholder="最大" min="0">
                            </div>
                        </div>
                    <% } else if (selectedOption instanceof MemoryOption) { %>
                        <div class="filter-group">
                            <label>世代</label>
                            <div class="filter-checkboxes">
                                <% for (SelectableOption opt : ((MemoryOption) selectedOption).getGeneration()) { %>
                                    <label>
                                        <input type="checkbox" name="opt_<%= type %>_MEMORY_GEN" value="<%= opt.getValue() %>">
                                        <%= opt.getValue() %>
                                    </label>
                                <% } %>
                            </div>
                        </div>
                        <div class="filter-group">
                            <label>容量</label>
                            <div class="filter-checkboxes">
                                <% for (SelectableOption opt : ((MemoryOption) selectedOption).getCapacity()) { %>
                                    <label>
                                        <input type="checkbox" name="opt_<%= type %>_MEMORY_CAPACITY" value="<%= opt.getValue() %>">
                                        <%= opt.getValue() %>
                                    </label>
                                <% } %>
                            </div>
                        </div>
                    <% } else if (selectedOption instanceof MotherBoardOption) { %>
                        <div class="filter-group">
                            <label>チップセット</label>
                            <div class="filter-checkboxes">
                                <% for (SelectableOption opt : ((MotherBoardOption) selectedOption).getChipset()) { %>
                                    <label>
                                        <input type="checkbox" name="opt_<%= type %>_MOTHERBOARD_CHIPSET" value="<%= opt.getValue() %>">
                                        <%= opt.getValue() %>
                                    </label>
                                <% } %>
                            </div>
                        </div>
                        <div class="filter-group">
                            <label>サイズ</label>
                            <div class="filter-checkboxes">
                                <% for (SelectableOption opt : ((MotherBoardOption) selectedOption).getSize()) { %>
                                    <label>
                                        <input type="checkbox" name="opt_<%= type %>_MOTHERBOARD_SIZE" value="<%= opt.getValue() %>">
                                        <%= opt.getValue() %>
                                    </label>
                                <% } %>
                            </div>
                        </div>
                    <% } else if (selectedOption instanceof SsdOption) { %>
                        <div class="filter-group">
                            <label>容量</label>
                            <div class="filter-checkboxes">
                                <% for (SelectableOption opt : ((SsdOption) selectedOption).getCapacity()) { %>
                                    <label>
                                        <input type="checkbox" name="opt_<%= type %>_SSD_CAP" value="<%= opt.getValue() %>">
                                        <%= opt.getValue() %>
                                    </label>
                                <% } %>
                            </div>
                        </div>
                        <div class="filter-group">
                            <label>タイプ</label>
                            <div class="filter-checkboxes">
                                <% for (SelectableOption opt : ((SsdOption) selectedOption).getType()) { %>
                                    <label>
                                        <input type="checkbox" name="opt_<%= type %>_SSD_TYPE" value="<%= opt.getValue() %>">
                                        <%= opt.getValue() %>
                                    </label>
                                <% } %>
                            </div>
                        </div>
                    <% } else if (selectedOption instanceof HddOption) { %>
                        <div class="filter-group">
                            <label>容量</label>
                            <div class="filter-checkboxes">
                                <% for (SelectableOption opt : ((HddOption) selectedOption).getCapacity()) { %>
                                    <label>
                                        <input type="checkbox" name="opt_<%= type %>_HDD_CAP" value="<%= opt.getValue() %>">
                                        <%= opt.getValue() %>
                                    </label>
                                <% } %>
                            </div>
                        </div>
                        <div class="filter-group">
                            <label>RPM</label>
                            <div class="filter-checkboxes">
                                <% for (SelectableOption opt : ((HddOption) selectedOption).getRpm()) { %>
                                    <label>
                                        <input type="checkbox" name="opt_<%= type %>_HDD_RPM" value="<%= opt.getValue() %>">
                                        <%= opt.getValue() %>
                                    </label>
                                <% } %>
                            </div>
                        </div>
                    <% } %>

                    <div class="form-group">
                        <button type="button" id="applyFilter">フィルタ適用</button>
                        <button type="button" id="resetFilter">リセット</button>
                    </div>
                </form>
            <% } %>
        </div>
    </aside>

    <div class="main">
        <!-- エラーメッセージ表示 -->
<% if (error != null && !error.isEmpty()) { %>
    <div class="error">
        <strong>エラー:</strong> <%= error %>
    </div>
<% } %>

<!-- 検索結果表示 -->
    <div class="success" id="resultSummary" style="display:none;">
        検索キーワード「<span id="resultKeyword"></span>」で <span id="resultCount" style="font-weight: bold;"></span> 件見つかりました
    </div>

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
        <tbody id="resultTableBody">
        </tbody>
    </table>

    <div class="no-results" id="noResults" style="display:none;">
        検索キーワード「<span id="noResultsKeyword"></span>」に一致する商品はありません
    </div>
    </div> <!-- .main -->
</div> <!-- .layout -->

<hr>

<p>
    <a href="<%= request.getContextPath() %>/jsp/debug/urls.txt">デバッグページ一覧</a>
</p>

<script src="<%= request.getContextPath() %>/js/search_test.js"></script>

</body>
</html>
