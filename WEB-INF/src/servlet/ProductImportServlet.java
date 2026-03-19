/**
 * 作成：小車（支援：ChatGPT）
 * 最終変更：2026-03-03
 *
 * 概要:
 * デバッグ用、本番では使用しない
 * URL:
 *  /debug/ProductImport
 *
 * CSV仕様:
 *  各行の先頭3列は type,makerName,name,price の順。
 *  4列目はオプションで在庫数(stock)を設定でき、指定しない場合は 0 になる。
 *  4列目に在庫数を指定した場合は、その次の列が image になる。
 *
 *  cpu   : type,makerName,name,price,(stock),image,generation,core,thread,clock
 *  gpu   : type,makerName,name,price,(stock),image,seriesName,chipName,vram
 *  mb    : type,makerName,name,price,(stock),image,chipset,size
 *  memory: type,makerName,name,price,(stock),image,generation,capacity
 *  ssd   : type,makerName,name,price,(stock),image,capacity,typeValue
 *  hdd   : type,makerName,name,price,(stock),image,capacity,rpm

 */

package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import utils.utils;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/debug/ProductImport")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 10L * 1024 * 1024,
        maxRequestSize = 20L * 1024 * 1024
)
public class ProductImportServlet extends HttpServlet {

    private static volatile DataSource ds;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/jsp/debug/product_import.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        List<String> logs = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int processedLines = 0;
        int successLines = 0;

        Part filePart;
        try {
            filePart = request.getPart("file");
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
        request.getRequestDispatcher("/jsp/debug/product_import.jsp").forward(request, response);
    }

    private void importOneLine(Connection con, String type, List<String> c, int lineNo, List<String> logs) throws Exception {

        if (c.size() < 2) throw new Exception("列数不足: makerNameがありません");

        String makerName = c.get(1).trim();
        if (makerName.isEmpty()) throw new Exception("makerName（2列目）が空です");

        int makerId = getOrCreateIdByValue(con, "MAKER", "MAKER_ID", makerName);

        // CSV仕様拡張: type,maker,name,price,stock?,image,...
        int stock = 0;
        int imageIndex = 4;
        if (c.size() > 5 && isInteger(c.get(4).trim())) {
            stock = utils.parseIntField(c.get(4), "stock");
            imageIndex = 5;
        }

        switch (type) {
            case "cpu": {
                requireCols(type, c, 9);

                int productId = insertProduct(con, makerId, c.get(2), utils.parseIntField(c.get(3), "price"), stock, c.get(imageIndex));
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

                int productId = insertProduct(con, makerId, c.get(2), utils.parseIntField(c.get(3), "price"), stock, c.get(imageIndex));
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

                int productId = insertProduct(con, makerId, c.get(2), utils.parseIntField(c.get(3), "price"), stock, c.get(imageIndex));
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
                // ★修正：MEMORYは CAPACITY ではなく CAPACITY_ID
                // MemoryDaoの設計：MEMORY_CAPACITY(CAPACITY_ID, VALUE) を参照する
                requireCols(type, c, 7);

                int productId = insertProduct(con, makerId, c.get(2), utils.parseIntField(c.get(3), "price"), stock, c.get(imageIndex));
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

                int productId = insertProduct(con, makerId, c.get(2), utils.parseIntField(c.get(3), "price"), stock, c.get(imageIndex));
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

                int productId = insertProduct(con, makerId, c.get(2), utils.parseIntField(c.get(3), "price"), stock, c.get(imageIndex));
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

    // UNIQUE前提の get-or-create
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

    private boolean isInteger(String value) {
        if (value == null || value.isEmpty()) return false;
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private Connection getConnection() throws Exception {
        if (ds == null) {
            synchronized (ProductImportServlet.class) {
                if (ds == null) {
                    InitialContext ic = new InitialContext();
                    ds = (DataSource) ic.lookup("java:/comp/env/jdbc/resource");
                }
            }
        }
        return ds.getConnection();
    }
}