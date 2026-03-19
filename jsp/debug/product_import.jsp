<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>商品CSVインポート（デバッグ）</title>
    <style>
        body { font-family: sans-serif; }
        .box { border: 1px solid #999; padding: 10px; margin-top: 12px; }
        .ok { color: #0a0; }
        .ng { color: #c00; }
        pre { white-space: pre-wrap; }
        code { background:#f5f5f5; padding:2px 4px; }
    </style>
</head>
<body>

<h2>商品CSVインポート（デバッグ）</h2>

<p>
    アクセス：<code><%= request.getContextPath() %>/debug/ProductImport</code>
</p>

<div class="box">
    <h3>CSV仕様（重要）</h3>
    <pre>
# コメント行OK（#で開始）
# 1列目がtype（cpu/gpu/mb/memory/ssd/hdd）
# 2列目が makerName（MAKER.VALUE）
# makerName がDBに無ければ自動でMAKERへ登録
# 全行成功した場合のみ commit。1行でも失敗したら rollback（DB反映0件）

cpu,makerName,name,price,image,generation,core,thread,clock
gpu,makerName,name,price,image,seriesName,chipName,vram
mb,makerName,name,price,image,chipset,size
memory,makerName,name,price,image,generation,capacity
ssd,makerName,name,price,image,capacity,typeValue
hdd,makerName,name,price,image,capacity,rpm

例）
cpu,Intel,"Intel Core i5-14400F",25800,cpu_i5_14400f.jpg,第14世代,10,16,4.7
gpu,NVIDIA,"GeForce RTX 4060 8GB",42800,gpu_rtx4060.jpg,GeForce RTX 40,RTX 4060,8
ssd,Samsung,"Samsung 990 EVO 1TB",11800,ssd_990evo_1tb.jpg,1TB,"M.2 NVMe"
    </pre>
</div>

<div class="box">
    <h3>アップロード</h3>
    <form action="<%= request.getContextPath() %>/debug/ProductImport" method="post" enctype="multipart/form-data">
        <input type="file" name="file" accept=".txt,.csv" required>
        <button type="submit">インポート実行</button>
    </form>
</div>

<%
    Integer processedLines = (Integer)request.getAttribute("processedLines");
    Integer successLines = (Integer)request.getAttribute("successLines");
    Boolean committed = (Boolean)request.getAttribute("committed");
    List<String> logs = (List<String>)request.getAttribute("logs");
    List<String> errors = (List<String>)request.getAttribute("errors");

    if (processedLines != null) {
        boolean ok = (committed != null && committed.booleanValue());
%>

<div class="box">
    <h3>結果</h3>

    <p>処理対象行数（コメント/空行除外）：<b><%= processedLines %></b></p>
    <p>処理成功行数（実行上）：<b><%= successLines %></b></p>

    <% if (ok) { %>
        <p class="ok"><b>COMMIT しました（DBに反映済み）</b></p>
    <% } else { %>
        <p class="ng"><b>ROLLBACK しました（DB反映は0件です）</b></p>
    <% } %>

    <% if (errors != null && !errors.isEmpty()) { %>
        <h4 class="ng">エラー</h4>
        <pre class="ng"><%
            for (String e : errors) out.println(e);
        %></pre>
    <% } %>

    <% if (logs != null && !logs.isEmpty()) { %>
        <h4>ログ</h4>
        <pre><%
            for (String l : logs) out.println(l);
        %></pre>
    <% } %>
</div>

<% } %>

<div class="box">
    <a href="<%= request.getContextPath() %>/debug/ProductManage?action=list&type=cpu">商品管理へ戻る（CPU）</a>
</div>

</body>
</html>