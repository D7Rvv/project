<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="bean.*" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>商品編集（デバッグ）</title>
    <style>
        label { display:block; margin-top:8px; }
        input, select { width: 420px; max-width: 100%; }
        .error { color:red; }
    </style>
</head>
<body>

<%
    String type = (String)request.getAttribute("type");
    if (type == null) type = "cpu";

    String mode = (String)request.getAttribute("mode"); // insert/update
    if (mode == null) mode = "insert";

    Object beanObj = request.getAttribute("bean");

    // ★追加：メーカー一覧
    Object makersObj = request.getAttribute("makers");
    List<MakerBean> makers = (makersObj == null) ? new ArrayList<>() : (List<MakerBean>)makersObj;
%>

<h2>商品編集（デバッグ）</h2>

<p>
    type=<b><%= type %></b> / mode=<b><%= mode %></b>
</p>

<% if (request.getAttribute("error") != null) { %>
    <div class="error"><%= request.getAttribute("error") %></div>
<% } %>

<%
    // 共通フィールド
    int productId = 0;
    int makerId = 0;
    String name = "";
    int price = 0;
    String image = "";

    // 固有ID（update用）
    int cpuId = 0, gpuId = 0, motherBoardId = 0, memoryId = 0, ssdId = 0, hddId = 0;

    // 固有フィールド
    String generation = "";
    String capacity = "";
    String typeValue = "";
    String rpm = "";
    String chipset = "";
    String size = "";
    String seriesName = "";
    String chipName = "";
    int vram = 0;
    int core = 0;
    int thread = 0;
    double clock = 0.0;

    if (beanObj instanceof ProductBean) {
        ProductBean p = (ProductBean)beanObj;
        productId = p.getProductId();
        makerId = p.getMakerId();
        name = (p.getName() == null ? "" : p.getName());
        price = p.getPrice();
        image = (p.getImageId() == null ? "" : p.getImageId());
    }

    if (beanObj instanceof CpuBean) {
        CpuBean b=(CpuBean)beanObj;
        cpuId = b.getCpuId();
        generation = (b.getGeneration()==null?"":b.getGeneration());
        core = b.getCore();
        thread = b.getThread();
        clock = b.getClock();
    } else if (beanObj instanceof GpuBean) {
        GpuBean b=(GpuBean)beanObj;
        gpuId = b.getGpuId();
        seriesName = (b.getSeriesName()==null?"":b.getSeriesName());
        chipName = (b.getChipName()==null?"":b.getChipName());
        vram = b.getVram();
    } else if (beanObj instanceof MotherBoardBean) {
        MotherBoardBean b=(MotherBoardBean)beanObj;
        motherBoardId = b.getMotherBoardId();
        chipset = (b.getChipset()==null?"":b.getChipset());
        size = (b.getSize()==null?"":b.getSize());
    } else if (beanObj instanceof MemoryBean) {
        MemoryBean b=(MemoryBean)beanObj;
        memoryId = b.getMemoryId();
        generation = (b.getGeneration()==null?"":b.getGeneration());
        capacity = (b.getCapacity()==null?"":b.getCapacity());
    } else if (beanObj instanceof SsdBean) {
        SsdBean b=(SsdBean)beanObj;
        ssdId = b.getSsdId();
        capacity = (b.getCapacity()==null?"":b.getCapacity());
        typeValue = (b.getType()==null?"":b.getType());
    } else if (beanObj instanceof HddBean) {
        HddBean b=(HddBean)beanObj;
        hddId = b.getHddId();
        capacity = (b.getCapacity()==null?"":b.getCapacity());
        rpm = (b.getRpm()==null?"":b.getRpm());
    }

    String actionValue = "insert".equals(mode) ? "insert" : "update";
%>

<form action="<%= request.getContextPath() %>/debug/ProductManage" method="post">
    <input type="hidden" name="action" value="<%= actionValue %>">
    <input type="hidden" name="type" value="<%= type %>">

    <%-- update時のみ必要なID類 --%>
    <input type="hidden" name="productId" value="<%= productId %>">
    <input type="hidden" name="cpuId" value="<%= cpuId %>">
    <input type="hidden" name="gpuId" value="<%= gpuId %>">
    <input type="hidden" name="motherBoardId" value="<%= motherBoardId %>">
    <input type="hidden" name="memoryId" value="<%= memoryId %>">
    <input type="hidden" name="ssdId" value="<%= ssdId %>">
    <input type="hidden" name="hddId" value="<%= hddId %>">

    <h3>共通</h3>

    <%-- ★改修：メーカーIDをプルダウン --%>
    <label>メーカー
        <select name="makerId" required>
            <option value="">-- 選択してください --</option>
            <%
                for (MakerBean m : makers) {
                    int mid = m.getMakerId();
                    String val = (m.getValue() == null ? "" : m.getValue());
                    String selected = (mid == makerId) ? "selected" : "";
            %>
                <option value="<%= mid %>" <%= selected %>><%= mid %> : <%= val %></option>
            <%
                }
            %>
        </select>
    </label>

    <label>商品名
        <input type="text" name="name" value="<%= name %>" required>
    </label>

    <label>価格
        <input type="number" name="price" value="<%= price %>" required>
    </label>

    <label>画像ID（例: cpu_i5_14400f.jpg）
        <input type="text" name="image" value="<%= image %>" required>
    </label>

    <h3>固有</h3>

    <% if ("cpu".equals(type)) { %>
        <label>世代（CPU_GEN.VALUE）
            <input type="text" name="generation" value="<%= generation %>" required>
        </label>
        <label>Core
            <input type="number" name="core" value="<%= core %>" required>
        </label>
        <label>Thread
            <input type="number" name="thread" value="<%= thread %>" required>
        </label>
        <label>Clock（例: 4.7）
            <input type="text" name="clock" value="<%= clock %>" required>
        </label>

    <% } else if ("gpu".equals(type)) { %>
        <label>Series Name
            <input type="text" name="seriesName" value="<%= seriesName %>" required>
        </label>
        <label>Chip Name
            <input type="text" name="chipName" value="<%= chipName %>" required>
        </label>
        <label>VRAM（GB）
            <input type="number" name="vram" value="<%= vram %>" required>
        </label>

    <% } else if ("mb".equals(type)) { %>
        <label>Chipset
            <input type="text" name="chipset" value="<%= chipset %>" required>
        </label>
        <label>Size（例: ATX / mATX / ITX）
            <input type="text" name="size" value="<%= size %>" required>
        </label>

    <% } else if ("memory".equals(type)) { %>
        <label>世代（MEMORY_GEN.VALUE）
            <input type="text" name="generation" value="<%= generation %>" required>
        </label>
        <label>容量（例: 32GB / 16GBx2）
            <input type="text" name="capacity" value="<%= capacity %>" required>
        </label>

    <% } else if ("ssd".equals(type)) { %>
        <label>容量（例: 1TB）
            <input type="text" name="capacity" value="<%= capacity %>" required>
        </label>
        <label>Type（例: M.2 NVMe / SATA）
            <input type="text" name="typeValue" value="<%= typeValue %>" required>
        </label>

    <% } else if ("hdd".equals(type)) { %>
        <label>容量（例: 2TB）
            <input type="text" name="capacity" value="<%= capacity %>" required>
        </label>
        <label>RPM（例: 7200rpm）
            <input type="text" name="rpm" value="<%= rpm %>" required>
        </label>
    <% } %>

    <div style="margin-top:16px;">
        <button type="submit"><%= "insert".equals(mode) ? "追加" : "更新" %></button>
        <a href="<%= request.getContextPath() %>/debug/ProductManage?action=list&type=<%= type %>">戻る</a>
    </div>
</form>

</body>
</html>