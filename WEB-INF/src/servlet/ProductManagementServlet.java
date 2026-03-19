/**
 * 商品管理サーブレット
 * 商品の一覧、編集、削除、CSV一括読み込み機能を提供
 */

package servlet;

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.naming.*;
import javax.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.nio.charset.*;
import beans.*;
import dao.*;
import utils.*;

@MultipartConfig
@WebServlet("/admin/ProductManagement")
public class ProductManagementServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = utils.nvl(request.getParameter("action"), "list");
        String type = utils.nvl(request.getParameter("type"), "all");

        try {
            switch (action) {
                case "list":
                    listProducts(request, response, type);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                default:
                    listProducts(request, response, type);
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/jsp/admin/productManagement.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = utils.nvl(request.getParameter("action"), "");

        try {
            switch (action) {
                case "update":
                    updateProduct(request, response);
                    break;
                case "delete":
                    deleteProduct(request, response);
                    break;
                case "upload":
                    uploadCsv(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/admin/ProductManagement");
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/jsp/admin/productManagement.jsp").forward(request, response);
        }
    }

    private Connection getConnection() throws Exception {
        if (ds == null) {
            synchronized (ProductManagementServlet.class) {
                if (ds == null) {
                    InitialContext ic = new InitialContext();
                    ds = (DataSource) ic.lookup("java:/comp/env/jdbc/resource");
                }
            }
        }
        return ds.getConnection();
    }

    private static volatile DataSource ds;

    @SuppressWarnings("unchecked")
    private void listProducts(HttpServletRequest request, HttpServletResponse response, String type)
            throws Exception {
        List<ProductBean> allProducts = new ArrayList<>();

        if ("all".equals(type) || "cpu".equals(type)) {
            CpuDao cpuDao = new CpuDao();
            allProducts.addAll((List)cpuDao.selectAll());
        }
        if ("all".equals(type) || "gpu".equals(type)) {
            GpuDao gpuDao = new GpuDao();
            allProducts.addAll((List)gpuDao.selectAll());
        }
        if ("all".equals(type) || "memory".equals(type)) {
            MemoryDao memoryDao = new MemoryDao();
            allProducts.addAll((List)memoryDao.selectAll());
        }
        if ("all".equals(type) || "motherboard".equals(type)) {
            MotherBoardDao mbDao = new MotherBoardDao();
            allProducts.addAll((List)mbDao.selectAll());
        }
        if ("all".equals(type) || "ssd".equals(type)) {
            SsdDao ssdDao = new SsdDao();
            allProducts.addAll((List)ssdDao.selectAll());
        }
        if ("all".equals(type) || "hdd".equals(type)) {
            HddDao hddDao = new HddDao();
            allProducts.addAll((List)hddDao.selectAll());
        }

        request.setAttribute("products", allProducts);
        request.setAttribute("currentType", type);
        request.getRequestDispatcher("/jsp/admin/productManagement.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String type = request.getParameter("type");
        int id = Integer.parseInt(request.getParameter("id"));

        ProductBean product = null;

        switch (type) {
            case "cpu":
                CpuDao cpuDao = new CpuDao();
                product = (ProductBean) cpuDao.selectById(id);
                break;
            case "gpu":
                GpuDao gpuDao = new GpuDao();
                product = (ProductBean) gpuDao.selectById(id);
                break;
            case "memory":
                MemoryDao memoryDao = new MemoryDao();
                product = (ProductBean) memoryDao.selectById(id);
                break;
            case "motherboard":
                MotherBoardDao mbDao = new MotherBoardDao();
                product = (ProductBean) mbDao.selectById(id);
                break;
            case "ssd":
                SsdDao ssdDao = new SsdDao();
                product = (ProductBean) ssdDao.selectById(id);
                break;
            case "hdd":
                HddDao hddDao = new HddDao();
                product = (ProductBean) hddDao.selectById(id);
                break;
        }

        if (product != null) {
            request.setAttribute("product", product);
            request.setAttribute("productType", type);
        }

        // メーカー一覧を取得
        MakerDao makerDao = new MakerDao();
        List<MakerBean> makers = makerDao.selectAll();
        request.setAttribute("makers", makers);

        request.getRequestDispatcher("/jsp/admin/productManagement.jsp").forward(request, response);
    }

    private void updateProduct(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String type = request.getParameter("type");
        int id = Integer.parseInt(request.getParameter("id"));

        // 共通フィールド
        String name = request.getParameter("name");
        int price = Integer.parseInt(request.getParameter("price"));
        int stock = utils.parseInt(request.getParameter("stock"), 0);
        String image = request.getParameter("image");
        int makerId = Integer.parseInt(request.getParameter("makerId"));

        switch (type) {
            case "cpu":
                CpuDao cpuDao = new CpuDao();
                CpuBean cpu = (CpuBean) cpuDao.selectById(id);
                if (cpu != null) {
                    cpu.setName(name);
                    cpu.setPrice(price);
                    cpu.setImageId(image);
                    cpu.setStock(stock);
                    cpu.setMakerId(makerId);
                    cpu.setGeneration(request.getParameter("generation"));
                    cpu.setCore(Integer.parseInt(request.getParameter("core")));
                    cpu.setThread(Integer.parseInt(request.getParameter("thread")));
                    cpu.setClock(Double.parseDouble(request.getParameter("clock")));
                    cpuDao.update(cpu);
                }
                break;
            // 他のタイプも同様に実装
            case "gpu":
                GpuDao gpuDao = new GpuDao();
                GpuBean gpu = (GpuBean) gpuDao.selectById(id);
                if (gpu != null) {
                    gpu.setName(name);
                    gpu.setPrice(price);
                    gpu.setImageId(image);
                    gpu.setStock(stock);
                    gpu.setMakerId(makerId);
                    gpu.setSeriesName(request.getParameter("series"));
                    gpu.setChipName(request.getParameter("model"));
                    gpu.setVram(Integer.parseInt(request.getParameter("vram")));
                    gpuDao.update(gpu);
                }
                break;
            case "memory":
                MemoryDao memoryDao = new MemoryDao();
                MemoryBean memory = (MemoryBean) memoryDao.selectById(id);
                if (memory != null) {
                    memory.setName(name);
                    memory.setPrice(price);
                    memory.setImageId(image);
                    memory.setStock(stock);
                    memory.setMakerId(makerId);
                    memory.setGeneration(request.getParameter("generation"));
                    memory.setCapacity(request.getParameter("capacity"));
                    memoryDao.update(memory);
                }
                break;
            case "motherboard":
                MotherBoardDao mbDao = new MotherBoardDao();
                MotherBoardBean mb = (MotherBoardBean) mbDao.selectById(id);
                if (mb != null) {
                    mb.setName(name);
                    mb.setPrice(price);
                    mb.setImageId(image);
                    mb.setStock(stock);
                    mb.setMakerId(makerId);
                    mb.setChipset(request.getParameter("chipset"));
                    mb.setSize(request.getParameter("formFactor"));
                    mbDao.update(mb);
                }
                break;
            case "ssd":
                SsdDao ssdDao = new SsdDao();
                SsdBean ssd = (SsdBean) ssdDao.selectById(id);
                if (ssd != null) {
                    ssd.setName(name);
                    ssd.setPrice(price);
                    ssd.setImageId(image);
                    ssd.setStock(stock);
                    ssd.setMakerId(makerId);
                    ssd.setCapacity(request.getParameter("capacity"));
                    ssd.setType(request.getParameter("interface"));
                    ssdDao.update(ssd);
                }
                break;
            case "hdd":
                HddDao hddDao = new HddDao();
                HddBean hdd = (HddBean) hddDao.selectById(id);
                if (hdd != null) {
                    hdd.setName(name);
                    hdd.setPrice(price);
                    hdd.setImageId(image);
                    hdd.setStock(stock);
                    hdd.setMakerId(makerId);
                    hdd.setCapacity(request.getParameter("capacity"));
                    hdd.setRpm(request.getParameter("rpm"));
                    hddDao.update(hdd);
                }
                break;
        }

        response.sendRedirect(request.getContextPath() + "/admin/ProductManagement");
    }

    private void deleteProduct(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String type = request.getParameter("type");
        int id = Integer.parseInt(request.getParameter("id"));

        switch (type) {
            case "cpu":
                CpuDao cpuDao = new CpuDao();
                cpuDao.delete(id);
                break;
            case "gpu":
                GpuDao gpuDao = new GpuDao();
                gpuDao.delete(id);
                break;
            case "memory":
                MemoryDao memoryDao = new MemoryDao();
                memoryDao.delete(id);
                break;
            case "motherboard":
                MotherBoardDao mbDao = new MotherBoardDao();
                mbDao.delete(id);
                break;
            case "ssd":
                SsdDao ssdDao = new SsdDao();
                ssdDao.delete(id);
                break;
            case "hdd":
                HddDao hddDao = new HddDao();
                hddDao.delete(id);
                break;
        }

        response.sendRedirect(request.getContextPath() + "/admin/ProductManagement");
    }

    private void uploadCsv(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        request.setCharacterEncoding("UTF-8");

        List<String> logs = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int processedLines = 0;
        int successLines = 0;

        Part filePart;
        try {
            filePart = request.getPart("csvFile");
            if (filePart == null || filePart.getSize() == 0) {
                errors.add("ファイルが選択されていません。");
                setResultAndForward(request, response, logs, errors, processedLines, successLines, false);
                return;
            }
        } catch (Exception e) {
            errors.add("ファイル取得に失敗しました: " + e.getMessage());
            setResultAndForward(request, response, logs, errors, processedLines, successLines, false);
            return;
        }

        boolean committed = false;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(filePart.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                int lineNo = 0;

                while ((line = br.readLine()) != null) {
                    lineNo++;

                    String raw = line;
                    line = line.trim();

                    if (line.isEmpty()) continue;
                    if (line.startsWith("#")) continue;

                    processedLines++;

                    List<String> cols = parseCsvLine(line);
                    if (cols.isEmpty()) continue;

                    try {
                        String type = cols.get(0).trim().toLowerCase();
                        importOneLine(con, type, cols, lineNo, logs);
                        successLines++;
                    } catch (Exception e) {
                        errors.add("[" + lineNo + "行目] " + e.getMessage() + " / line=" + raw);
                        throw e;
                    }
                }

                con.commit();
                committed = true;

            } catch (Exception e) {
                try { con.rollback(); } catch (Exception ignore) {}
            }

        } catch (Exception e) {
            errors.add("DB接続/トランザクションで例外が発生しました: " + e.getMessage());
        }

        setResultAndForward(request, response, logs, errors, processedLines, successLines, committed);
    }

    private void setResultAndForward(HttpServletRequest request, HttpServletResponse response,
                                     List<String> logs, List<String> errors,
                                     int processedLines, int successLines,
                                     boolean committed)
            throws ServletException, IOException {
        request.setAttribute("logs", logs);
        request.setAttribute("errors", errors);
        request.setAttribute("processedLines", processedLines);
        request.setAttribute("successLines", successLines);
        request.setAttribute("committed", committed);
        request.getRequestDispatcher("/jsp/admin/productManagement.jsp").forward(request, response);
    }

    private void importOneLine(Connection con, String type, List<String> c, int lineNo, List<String> logs) throws Exception {

        if (c.size() < 2) throw new Exception("列数不足: makerNameがありません");

        String makerName = c.get(1).trim();
        if (makerName.isEmpty()) throw new Exception("makerName（2列目）が空です");

        int makerId = getOrCreateIdByValue(con, "MAKER", "MAKER_ID", makerName);

        // CSV仕様拡張: type,maker,name,price,stock?,image,...
        int stock = 0;
        int imageIndex = 4;
        if (c.size() > 5) {
            String maybeStock = c.get(4).trim();
            try {
                stock = Integer.parseInt(maybeStock);
                imageIndex = 5;
            } catch (NumberFormatException ignore) {
                // stock not present; keep default
            }
        }

        switch (type) {
            case "cpu": {
                requireCols(type, c, 9);

                int productId = insertProduct(con, makerId, c.get(2), toInt(c.get(3), "price"), stock, c.get(imageIndex));
                int genId = getOrCreateIdByValue(con, "CPU_GEN", "GEN_ID", c.get(imageIndex + 1));

                int coreId = getOrCreateIdByValue(con, "CPU_CORE", "CORE_ID", c.get(imageIndex + 2));
                int threadId = getOrCreateIdByValue(con, "CPU_THREAD", "THREAD_ID", c.get(imageIndex + 3));
                int clockId = getOrCreateIdByValue(con, "CPU_CLOCK", "CLOCK_ID", c.get(imageIndex + 4));

                String sql = """
                    INSERT INTO CPU (PRODUCT_ID, GEN_ID, CORE_ID, THREAD_ID, CLOCK_ID)
                    VALUES (?, ?, ?, ?, ?)
                    """;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, genId);
                    ps.setInt(3, coreId);
                    ps.setInt(4, threadId);
                    ps.setInt(5, clockId);
                    ps.executeUpdate();
                }

                logs.add("[" + lineNo + "] cpu OK: " + c.get(2));
                break;
            }

            case "gpu": {
                requireCols(type, c, 8);

                int productId = insertProduct(con, makerId, c.get(2), toInt(c.get(3), "price"), stock, c.get(imageIndex));
                int seriesId = getOrCreateIdByValue(con, "GPU_SERIES", "SERIES_ID", c.get(imageIndex + 1));
                int chipId = getOrCreateIdByValue(con, "GPU_CHIP", "CHIP_ID", c.get(imageIndex + 2));
                int vramId = getOrCreateIdByValue(con, "GPU_VRAM", "VRAM_ID", c.get(imageIndex + 3));

                String sql = """
                    INSERT INTO GPU (PRODUCT_ID, SERIES_ID, CHIP_ID, VRAM_ID)
                    VALUES (?, ?, ?, ?)
                    """;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, seriesId);
                    ps.setInt(3, chipId);
                    ps.setInt(4, vramId);
                    ps.executeUpdate();
                }

                logs.add("[" + lineNo + "] gpu OK: " + c.get(2));
                break;
            }

            case "mb": {
                requireCols(type, c, 7);

                int productId = insertProduct(con, makerId, c.get(2), toInt(c.get(3), "price"), stock, c.get(imageIndex));
                int chipsetId = getOrCreateIdByValue(con, "MOTHER_BOARD_CHIPSET", "CHIPSET_ID", c.get(imageIndex + 1));
                int sizeId = getOrCreateIdByValue(con, "MOTHER_BOARD_SIZE", "SIZE_ID", c.get(imageIndex + 2));

                String sql = """
                    INSERT INTO MOTHER_BOARD (PRODUCT_ID, CHIPSET_ID, SIZE_ID)
                    VALUES (?, ?, ?)
                    """;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, chipsetId);
                    ps.setInt(3, sizeId);
                    ps.executeUpdate();
                }

                logs.add("[" + lineNo + "] mb OK: " + c.get(2));
                break;
            }

            case "memory": {
                requireCols(type, c, 7);

                int productId = insertProduct(con, makerId, c.get(2), toInt(c.get(3), "price"), stock, c.get(imageIndex));
                int genId = getOrCreateIdByValue(con, "MEMORY_GEN", "GEN_ID", c.get(imageIndex + 1));
                int capacityId = getOrCreateIdByValue(con, "MEMORY_CAPACITY", "CAPACITY_ID", c.get(imageIndex + 2));

                String sql = """
                    INSERT INTO MEMORY (PRODUCT_ID, GEN_ID, CAPACITY_ID)
                    VALUES (?, ?, ?)
                    """;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, genId);
                    ps.setInt(3, capacityId);
                    ps.executeUpdate();
                }

                logs.add("[" + lineNo + "] memory OK: " + c.get(2));
                break;
            }

            case "ssd": {
                requireCols(type, c, 7);

                int productId = insertProduct(con, makerId, c.get(2), toInt(c.get(3), "price"), stock, c.get(imageIndex));
                int capacityId = getOrCreateIdByValue(con, "STORAGE_CAPACITY", "CAPACITY_ID", c.get(imageIndex + 1));
                int typeId = getOrCreateIdByValue(con, "SSD_TYPE", "TYPE_ID", c.get(imageIndex + 2));

                String sql = """
                    INSERT INTO SSD (PRODUCT_ID, CAPACITY_ID, TYPE_ID)
                    VALUES (?, ?, ?)
                    """;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, capacityId);
                    ps.setInt(3, typeId);
                    ps.executeUpdate();
                }

                logs.add("[" + lineNo + "] ssd OK: " + c.get(2));
                break;
            }

            case "hdd": {
                requireCols(type, c, 7);

                int productId = insertProduct(con, makerId, c.get(2), toInt(c.get(3), "price"), stock, c.get(imageIndex));
                int capacityId = getOrCreateIdByValue(con, "STORAGE_CAPACITY", "CAPACITY_ID", c.get(imageIndex + 1));
                int rpmId = getOrCreateIdByValue(con, "HDD_RPM", "RPM_ID", c.get(imageIndex + 2));

                String sql = """
                    INSERT INTO HDD (PRODUCT_ID, CAPACITY_ID, RPM_ID)
                    VALUES (?, ?, ?)
                    """;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, capacityId);
                    ps.setInt(3, rpmId);
                    ps.executeUpdate();
                }

                logs.add("[" + lineNo + "] hdd OK: " + c.get(2));
                break;
            }

            default:
                throw new Exception("type が不正です: " + type + "（cpu/gpu/mb/memory/ssd/hdd のいずれか）");
        }
    }

    private int insertProduct(Connection con, int makerId, String name, int price, int stock, String image) throws Exception {
        if (name == null || name.trim().isEmpty()) throw new Exception("name が空です");
        if (image == null) image = "";

        String sql = """
            INSERT INTO PRODUCT (MAKER_ID, NAME, PRICE, STOCK, IMAGE)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"PRODUCT_ID"})) {
            ps.setInt(1, makerId);
            ps.setString(2, name.trim());
            ps.setInt(3, price);
            ps.setInt(4, stock);
            ps.setString(5, image.trim());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        throw new Exception("PRODUCT INSERT FAILED");
    }

    private int getOrCreateIdByValue(Connection con, String table, String idCol, String value) throws Exception {
        value = (value == null) ? "" : value.trim();
        if (value.isEmpty()) throw new Exception(table + ".VALUE が空です");

        Integer id = selectIdByValue(con, table, idCol, value);
        if (id != null) return id;

        String insertSql = "INSERT INTO " + table + " (VALUE) VALUES (?)";
        try (PreparedStatement ps = con.prepareStatement(insertSql, new String[]{idCol})) {
            ps.setString(1, value);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs != null && rs.next()) return rs.getInt(1);
            }

        } catch (SQLException e) {
            if (!isUniqueViolation(e)) throw e;
        }

        id = selectIdByValue(con, table, idCol, value);
        if (id != null) return id;

        throw new Exception("IDの取得に失敗: table=" + table + " value=" + value);
    }

    private Integer selectIdByValue(Connection con, String table, String idCol, String value) throws Exception {
        String selectSql = "SELECT " + idCol + " FROM " + table + " WHERE VALUE = ?";
        try (PreparedStatement ps = con.prepareStatement(selectSql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    private boolean isUniqueViolation(SQLException e) {
        SQLException cur = e;
        while (cur != null) {
            String state = cur.getSQLState();
            int code = cur.getErrorCode();
            String msg = (cur.getMessage() == null) ? "" : cur.getMessage().toLowerCase();

            if ("23505".equals(state)) return true;
            if ("23000".equals(state)) return true;
            if (msg.contains("unique constraint") || msg.contains("duplicate") || msg.contains("ora-00001")) return true;
            if (code == 1) return true;

            cur = cur.getNextException();
        }
        return false;
    }

    private void requireCols(String type, List<String> cols, int expected) throws Exception {
        if (cols.size() < expected) {
            throw new Exception("列数不足: type=" + type + " expected=" + expected + " actual=" + cols.size());
        }
    }

    private int toInt(String s, String field) throws Exception {
        try {
            return utils.parseIntField(s, field);
        } catch (IllegalArgumentException e) {
            throw new Exception(e.getMessage(), e);
        }
    }

    private List<String> parseCsvLine(String line) throws Exception {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(ch);
                }
            } else {
                if (ch == ',') {
                    out.add(sb.toString().trim());
                    sb.setLength(0);
                } else if (ch == '"') {
                    inQuotes = true;
                } else {
                    sb.append(ch);
                }
            }
        }

        if (inQuotes) throw new Exception("CSVのダブルクォートが閉じていません: " + line);

        out.add(sb.toString().trim());
        return out;
    }
}