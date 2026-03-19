package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import beans.option.CpuOption;
import beans.option.GpuOption;
import beans.option.HddOption;
import beans.option.MemoryOption;
import beans.option.MotherBoardOption;
import beans.option.ProductOption;
import beans.option.SsdOption;

/**
 * 検索オプション関連のユーティリティ。
 */
public class OptionDao {
    
    private static volatile DataSource ds;

    private Connection getConnection() throws Exception {
        synchronized (BaseDao.class) {
            if (ds == null) {
                InitialContext ic = new InitialContext();
                ds = (DataSource) ic.lookup("java:/comp/env/jdbc/resource");
            }
        }
        return ds.getConnection();
    }

    public Map<String, ProductOption<?>> getOptions() throws Exception {
        return getOptions("all");
    }

    public Map<String, ProductOption<?>> getOptions(String category) throws Exception {
        if (category == null || category.isBlank()) {
            category = "all";
        }

        String normalized = category.trim().toLowerCase(Locale.ROOT);
        Map<String, ProductOption<?>> result = new LinkedHashMap<>();

        try (Connection con = getConnection()) {
            List<String> maker = null;
            if(!"all".equals(normalized)) maker = fetchUniqueMaker(con, normalized, "VALUE");

            if ("all".equals(normalized) || "cpu".equals(normalized)) {
                if("all".equals(normalized)) maker = fetchUniqueMaker(con, "cpu", "VALUE");
                IntRange priceRange = fetchCategoryPriceRange(con, "CPU");
                IntRange coreRange = fetchIntRange(con, "CPU_CORE", "VALUE");
                IntRange threadRange = fetchIntRange(con, "CPU_THREAD", "VALUE");
                DoubleRange clockRange = fetchDoubleRange(con, "CPU_CLOCK", "VALUE");

                result.put(
                    "cpu",
                    new CpuOption(
                        maker,
                        fetchValues(con, "CPU_GEN", "VALUE"),
                        priceRange.min,
                        priceRange.max,
                        coreRange.min,
                        coreRange.max,
                        threadRange.min,
                        threadRange.max,
                        clockRange.min,
                        clockRange.max
                    )
                );
            }

            if ("all".equals(normalized) || "gpu".equals(normalized)) {
                if("all".equals(normalized)) maker = fetchUniqueMaker(con, "gpu", "VALUE");
                IntRange priceRange = fetchCategoryPriceRange(con, "GPU");
                IntRange vramRange = fetchIntRange(con, "GPU_VRAM", "VALUE");

                result.put(
                    "gpu",
                    new GpuOption(
                        maker,
                        fetchValues(con, "GPU_SERIES", "VALUE"),
                        fetchValues(con, "GPU_CHIP", "VALUE"),
                        priceRange.min,
                        priceRange.max,
                        vramRange.min,
                        vramRange.max
                    )
                );
            }

            if ("all".equals(normalized) || "memory".equals(normalized)) {
                if("all".equals(normalized)) maker = fetchUniqueMaker(con, "memory", "VALUE");
                IntRange priceRange = fetchCategoryPriceRange(con, "MEMORY");

                result.put(
                    "memory",
                    new MemoryOption(
                        maker,
                        fetchValues(con, "MEMORY_GEN", "VALUE"),
                        fetchValues(con, "MEMORY_CAPACITY", "VALUE"),
                        priceRange.min,
                        priceRange.max
                    )
                );
            }

            if ("all".equals(normalized) || "mother_board".equals(normalized)) {
                if("all".equals(normalized)) maker = fetchUniqueMaker(con, "mother_board", "VALUE");
                IntRange priceRange = fetchCategoryPriceRange(con, "MOTHER_BOARD");

                result.put(
                    "mother_board",
                    new MotherBoardOption(
                        maker,
                        fetchValues(con, "MOTHER_BOARD_CHIPSET", "VALUE"),
                        fetchValues(con, "MOTHER_BOARD_SIZE", "VALUE"),
                        priceRange.min,
                        priceRange.max
                    )
                );
            }

            if ("all".equals(normalized) || "ssd".equals(normalized)) {
                if("all".equals(normalized)) maker = fetchUniqueMaker(con, "ssd", "VALUE");
                IntRange priceRange = fetchCategoryPriceRange(con, "SSD");

                result.put(
                    "ssd",
                    new SsdOption(
                        maker,
                        fetchValues(con, "STORAGE_CAPACITY", "VALUE"),
                        fetchValues(con, "SSD_TYPE", "VALUE"),
                        priceRange.min,
                        priceRange.max
                    )
                );
            }

            if ("all".equals(normalized) || "hdd".equals(normalized)) {
                if("all".equals(normalized)) maker = fetchUniqueMaker(con, "hdd", "VALUE");
                IntRange priceRange = fetchCategoryPriceRange(con, "HDD");
                
                result.put(
                    "hdd",
                    new HddOption(
                        maker,
                        fetchValues(con, "STORAGE_CAPACITY", "VALUE"),
                        fetchValues(con, "HDD_RPM", "VALUE"),
                        priceRange.min,
                        priceRange.max
                    )
                );
            }
        }

        return result;
    }

    private List<String> fetchValues(Connection con, String table, String column) throws SQLException {
        String sql = "SELECT " + column + " FROM " + table + " ORDER BY " + column;
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<String> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
            return list;
        }
    }

    private List<String> fetchUniqueMaker(Connection con, String category, String column) throws SQLException {

        String table = category.toUpperCase(Locale.ROOT);

        String sql =
        "SELECT DISTINCT M.VALUE " +
        "FROM " + table + " C " +
        "JOIN PRODUCT P ON C.PRODUCT_ID = P.PRODUCT_ID " +
        "JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID " +
        "ORDER BY M.VALUE";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<String> list = new ArrayList<>();

            while (rs.next()) {
                list.add(rs.getString(1));
            }

            return list;
        }
    }

    private IntRange fetchCategoryPriceRange(Connection con, String category) throws SQLException {
        String sql = """
            SELECT MIN(PRODUCT_PRICE) AS MIN_VALUE, MAX(PRODUCT_PRICE) AS MAX_VALUE
            FROM PRODUCT_SEARCH_VIEW
            WHERE CATEGORY = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int min = rs.getInt("MIN_VALUE");
                    int max = rs.getInt("MAX_VALUE");
                    if (rs.wasNull()) {
                        return new IntRange(0, 0);
                    }
                    return normalizeIntRange(min, max);
                }
            }
        }

        return new IntRange(0, 0);
    }

    private IntRange fetchIntRange(Connection con, String table, String column) throws SQLException {
        String sql = "SELECT MIN(TO_NUMBER(" + column + ")) AS MIN_VALUE, MAX(TO_NUMBER(" + column + ")) AS MAX_VALUE FROM " + table;
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int min = rs.getInt("MIN_VALUE");
                int max = rs.getInt("MAX_VALUE");
                if (rs.wasNull()) {
                    return new IntRange(0, 0);
                }
                return normalizeIntRange(min, max);
            }
        }
        return new IntRange(0, 0);
    }

    private DoubleRange fetchDoubleRange(Connection con, String table, String column) throws SQLException {
        String sql = "SELECT MIN(TO_NUMBER(" + column + ")) AS MIN_VALUE, MAX(TO_NUMBER(" + column + ")) AS MAX_VALUE FROM " + table;
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                double min = rs.getDouble("MIN_VALUE");
                double max = rs.getDouble("MAX_VALUE");
                if (rs.wasNull()) {
                    return new DoubleRange(0.0, 0.0);
                }
                return normalizeDoubleRange(min, max);
            }
        }
        return new DoubleRange(0.0, 0.0);
    }

    private IntRange normalizeIntRange(int min, int max) {
        if (min <= max) {
            return new IntRange(min, max);
        }
        return new IntRange(max, min);
    }

    private DoubleRange normalizeDoubleRange(double min, double max) {
        if (min <= max) {
            return new DoubleRange(min, max);
        }
        return new DoubleRange(max, min);
    }

    private static final class IntRange {
        private final int min;
        private final int max;

        private IntRange(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }

    private static final class DoubleRange {
        private final double min;
        private final double max;

        private DoubleRange(double min, double max) {
            this.min = min;
            this.max = max;
        }
    }
}