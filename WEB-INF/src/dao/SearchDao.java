package dao;

import beans.ProductBean;
import beans.option.ProductOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import utils.utils;

public class SearchDao {

    private static volatile DataSource ds;

    private static final String QUERY_WHITESPACE_REGEX = "\\s+";
    private static final String OR_KEYWORD = "or";

    protected Connection getConnection() throws Exception {
        synchronized (BaseDao.class) {
            if (ds == null) {
                InitialContext ic = new InitialContext();
                ds = (DataSource) ic.lookup("java:/comp/env/jdbc/resource");
            }
        }
        return ds.getConnection();
    }

    public List<ProductBean> search(String value, Map<String, ProductOption<?>> options, int maxResults) throws Exception {
        return search(value, options, maxResults, 0);
    }

    public List<ProductBean> search(String value, Map<String, ProductOption<?>> options, int maxResults, int offset) throws Exception {
        Map<String, List<Integer>> idsByCategory = queryProductIds(value);
    
        utils.debugPrint("[search] optionKeys", options == null ? null : options.keySet());
        utils.debugPrint("[search] idsByCategory(before)", idsByCategory);
    
        if (options != null && !options.isEmpty()) {
            List<String> selectedMakers = getSelectedMakers(options);
            utils.debugPrint("[search] selectedMakers", selectedMakers);
    
            if (!selectedMakers.isEmpty()) {
                idsByCategory = applyMakerFilter(idsByCategory, selectedMakers);
                utils.debugPrint("[search] idsByCategory(after maker filter)", idsByCategory);
            }
    
            idsByCategory = applyOptionFilter(idsByCategory, options);
        }
    
        utils.debugPrint("[search] idsByCategory(after)", idsByCategory);
    
        return loadBeans(idsByCategory, maxResults, offset);
    }

    /**
     * 指定したタイプ（カテゴリ）の商品のみを検索します。
     *
     * @param type gpu / cpu / memory / mother_board / ssd / hdd
     */
    public List<ProductBean> searchByType(String type, String value, Map<String, ProductOption<?>> options, int maxResults) throws Exception {
        return searchByType(type, value, options, maxResults, 0);
    }

    /**
     * 指定したタイプ（カテゴリ）の商品のみを検索します。
     *
     * @param type gpu / cpu / memory / mother_board / ssd / hdd
     */
    public List<ProductBean> searchByType(String type, String value, Map<String, ProductOption<?>> options, int maxResults, int offset) throws Exception {
        utils.debugPrint("[searchByType] options", options);
        utils.debugPrint("[searchByType] optionsIsEmpty", options == null || options.isEmpty());
        if (type == null || type.isBlank() || "all".equalsIgnoreCase(type)) {
            return search(value, options, maxResults, offset);
        }

        String key = type.trim().toLowerCase();
        List<ProductBean> results = new ArrayList<>();

        List<Integer> ids = queryProductIdsByType(key, value);

        utils.debugPrint("[searchByType] key", key);
        utils.debugPrint("[searchByType] ids(before)", ids);

        if (ids == null || ids.isEmpty()) {
            return results;
        }

        if (options != null && !options.isEmpty()) {
            List<String> selectedMakers = getSelectedMakers(options);
            utils.debugPrint("[searchByType] selectedMakers", selectedMakers);

            if (!selectedMakers.isEmpty()) {
                ids = filterByMaker(key, ids, selectedMakers);
                utils.debugPrint("[searchByType] ids(after maker filter)", ids);
            }

            ProductOption<?> option = options.get(key);
            utils.debugPrint("[searchByType] optionExists", option != null);

            if (option != null) {
                ids = applyOptionFilter(key, ids, option);
            }
        }

        utils.debugPrint("[searchByType] ids(after)", ids);

        Map<String, List<Integer>> single = new LinkedHashMap<>();
        single.put(key, ids);

        return loadBeans(single, maxResults, offset);
    }

    public int countSearch(String value, Map<String, ProductOption<?>> options) throws Exception {
        Map<String, List<Integer>> idsByCategory = queryProductIds(value);
        if (options != null && !options.isEmpty()) {
            List<String> selectedMakers = getSelectedMakers(options);
            if (!selectedMakers.isEmpty()) {
                idsByCategory = applyMakerFilter(idsByCategory, selectedMakers);
            }
            idsByCategory = applyOptionFilter(idsByCategory, options);
        }
        return countIds(idsByCategory);
    }

    public int countSearchByType(String type, String value, Map<String, ProductOption<?>> options) throws Exception {
        if (type == null || type.isBlank() || "all".equalsIgnoreCase(type)) {
            return countSearch(value, options);
        }

        String key = type.trim().toLowerCase();

        List<Integer> ids = queryProductIdsByType(key, value);
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        if (options != null && !options.isEmpty()) {
            ProductOption<?> option = options.get(key);
            if (option != null) {
                ids = applyOptionFilter(key, ids, option);
            }
        }

        return ids.size();
    }

    private int countIds(Map<String, List<Integer>> idsByCategory) {
        if (idsByCategory == null || idsByCategory.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (List<Integer> ids : idsByCategory.values()) {
            if (ids != null) {
                total += ids.size();
            }
        }
        return total;
    }

    private List<Integer> queryProductIdsByType(String typeKey, String value) throws Exception {
        if (value == null || value.isBlank()) {
            String sql =
                "SELECT DISTINCT PRODUCT_ID " +
                "FROM PRODUCT_SEARCH_VIEW " +
                "WHERE CATEGORY = ? " +
                "ORDER BY PRODUCT_ID";

            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, typeKey.toUpperCase());
                try (ResultSet rs = ps.executeQuery()) {
                    List<Integer> ids = new ArrayList<>();
                    while (rs.next()) {
                        ids.add(rs.getInt("PRODUCT_ID"));
                    }
                    return ids;
                }
            }
        }

        List<List<String>> orGroups = parseQuery(value);
        if (orGroups.isEmpty()) {
            return List.of();
        }

        StringBuilder where = new StringBuilder("CATEGORY = ?");
        List<String> bindValues = new ArrayList<>();
        bindValues.add(typeKey.toUpperCase());

        for (List<String> andTerms : orGroups) {
            if (andTerms.isEmpty()) continue;

            StringJoiner andJoiner = new StringJoiner(" AND ");
            for (String term : andTerms) {
                if (term == null || term.isBlank()) continue;
                andJoiner.add("UPPER(NVL(SEARCH_TEXT, '')) LIKE ?");
                bindValues.add("%" + term.toUpperCase() + "%");
            }

            if (andJoiner.length() == 0) continue;
            where.append(" AND (").append(andJoiner).append(")");
        }

        String sql =
            "SELECT DISTINCT PRODUCT_ID " +
            "FROM PRODUCT_SEARCH_VIEW " +
            "WHERE " + where +
            " ORDER BY PRODUCT_ID";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < bindValues.size(); i++) {
                ps.setString(i + 1, bindValues.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> ids = new ArrayList<>();
                while (rs.next()) {
                    ids.add(rs.getInt("PRODUCT_ID"));
                }
                return ids;
            }
        }
    }

    public List<ProductBean> search(String value) throws Exception {
        return search(value, null, 0, 0);
    }

    public List<ProductBean> search(String value, int maxResults) throws Exception {
        return search(value, null, maxResults, 0);
    }

    public List<ProductBean> search(String value, int maxResults, int offset) throws Exception {
        return search(value, null, maxResults, offset);
    }

    private Map<String, List<Integer>> queryProductIds(String value) throws Exception {
        Map<String, List<Integer>> result = new LinkedHashMap<>();
        result.put("cpu", new ArrayList<>());
        result.put("gpu", new ArrayList<>());
        result.put("memory", new ArrayList<>());
        result.put("mother_board", new ArrayList<>());
        result.put("ssd", new ArrayList<>());
        result.put("hdd", new ArrayList<>());

        if (value == null || value.isBlank()) {
            String sql =
                "SELECT DISTINCT PRODUCT_ID, CATEGORY " +
                "FROM PRODUCT_SEARCH_VIEW " +
                "ORDER BY PRODUCT_ID";

            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    addId(result, rs.getInt("PRODUCT_ID"), rs.getString("CATEGORY"));
                }
            }

            return result;
        }

        List<List<String>> orGroups = parseQuery(value);
        if (orGroups.isEmpty()) {
            return result;
        }

        StringBuilder where = new StringBuilder();
        List<String> bindValues = new ArrayList<>();

        for (List<String> andTerms : orGroups) {
            if (andTerms.isEmpty()) continue;

            StringJoiner andJoiner = new StringJoiner(" AND ");
            for (String term : andTerms) {
                if (term == null || term.isBlank()) continue;
                andJoiner.add("UPPER(NVL(SEARCH_TEXT, '')) LIKE ?");
                bindValues.add("%" + term.toUpperCase() + "%");
            }

            if (andJoiner.length() == 0) continue;
            if (where.length() > 0) where.append(" OR ");
            where.append("(").append(andJoiner).append(")");
        }

        if (where.length() == 0) {
            return result;
        }

        String sql =
            "SELECT DISTINCT PRODUCT_ID, CATEGORY " +
            "FROM PRODUCT_SEARCH_VIEW " +
            "WHERE " + where +
            " ORDER BY PRODUCT_ID";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < bindValues.size(); i++) {
                ps.setString(i + 1, bindValues.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    addId(result, rs.getInt("PRODUCT_ID"), rs.getString("CATEGORY"));
                }
            }
        }

        return result;
    }

    private void addId(Map<String, List<Integer>> target, int productId, String category) {
        switch (category) {
            case "CPU" -> target.get("cpu").add(productId);
            case "GPU" -> target.get("gpu").add(productId);
            case "MEMORY" -> target.get("memory").add(productId);
            case "MOTHER_BOARD" -> target.get("mother_board").add(productId);
            case "SSD" -> target.get("ssd").add(productId);
            case "HDD" -> target.get("hdd").add(productId);
            default -> {
            }
        }
        // ignore unknown category
    }

    private Map<String, List<Integer>> applyOptionFilter(
            Map<String, List<Integer>> idsByCategory,
            Map<String, ProductOption<?>> options
    ) throws Exception {
        Map<String, List<Integer>> filtered = new LinkedHashMap<>(idsByCategory);

        for (Map.Entry<String, ProductOption<?>> e : options.entrySet()) {
            String key = e.getKey();
            ProductOption<?> option = e.getValue();
            List<Integer> ids = filtered.get(key);

            utils.debugPrint("[applyOptionFilter] key", key);
            utils.debugPrint("[applyOptionFilter] ids(before)", ids);
            utils.debugPrint("[applyOptionFilter] optionExists", option != null);

            if (ids == null || ids.isEmpty() || option == null) {
                continue;
            }

            List<Integer> newIds = applyOptionFilter(key, ids, option);
            filtered.put(key, newIds);

            utils.debugPrint("[applyOptionFilter] ids(after)", newIds);
        }

        return filtered;
    }


    private List<Integer> applyOptionFilter(String categoryKey, List<Integer> productIds, ProductOption<?> option) throws Exception {
        if (productIds == null || productIds.isEmpty() || option == null) {
            return productIds;
        }
    
        int[] ids = utils.listToIntArray(productIds);
        List<? extends ProductBean> beans;
    
        switch (categoryKey) {
            case "cpu" -> beans = new CpuDao().selectByProductIds(ids);
            case "gpu" -> beans = new GpuDao().selectByProductIds(ids);
            case "memory" -> beans = new MemoryDao().selectByProductIds(ids);
            case "mother_board" -> beans = new MotherBoardDao().selectByProductIds(ids);
            case "ssd" -> beans = new SsdDao().selectByProductIds(ids);
            case "hdd" -> beans = new HddDao().selectByProductIds(ids);
            default -> {
                utils.debugPrint("[applyOptionFilterSingle] unknownKey", categoryKey);
                return productIds;
            }
        }
    
        utils.debugPrint("[applyOptionFilterSingle] categoryKey", categoryKey);
        utils.debugPrint("[applyOptionFilterSingle] beanCount(before)", beans.size());
    
        @SuppressWarnings({"unchecked", "rawtypes"})
        List<ProductBean> casted = (List) beans;
    
        @SuppressWarnings({"unchecked", "rawtypes"})
        List<ProductBean> filtered = ((ProductOption) option).applyOption(casted);
    
        utils.debugPrint("[applyOptionFilterSingle] beanCount(after)", filtered == null ? 0 : filtered.size());
    
        return filtered == null
            ? List.of()
            : filtered.stream().map(ProductBean::getProductId).toList();
    }

    private List<ProductBean> loadBeans(Map<String, List<Integer>> idsByCategory, int maxResults, int offset) throws Exception {
        List<ProductBean> result = new ArrayList<>();
        if (idsByCategory == null || idsByCategory.isEmpty()) return result;

        int safeOffset = Math.max(0, offset);
        int remaining = maxResults > 0 ? maxResults : Integer.MAX_VALUE;
        int skipped = 0;

        for (String category : List.of("cpu", "gpu", "memory", "mother_board", "ssd", "hdd")) {
            List<Integer> ids = idsByCategory.get(category);
            if (ids == null || ids.isEmpty()) continue;

            int categorySize = ids.size();

            if (skipped + categorySize <= safeOffset) {
                skipped += categorySize;
                continue;
            }

            int startIndex = Math.max(0, safeOffset - skipped);
            int endExclusive = Math.min(categorySize, startIndex + remaining);

            if (startIndex < endExclusive) {
                List<Integer> targetIds = new ArrayList<>(ids.subList(startIndex, endExclusive));
                for (ProductBean bean : loadByCategory(category, targetIds)) {
                    result.add(bean);
                }
                remaining -= (endExclusive - startIndex);
                if (remaining <= 0) {
                    return result;
                }
            }

            skipped += categorySize;
        }

        return result;
    }

    private List<? extends ProductBean> loadByCategory(String category, List<Integer> ids) throws Exception {
        int[] idArray = utils.listToIntArray(ids);
        return switch (category) {
            case "cpu" -> new CpuDao().selectByProductIds(idArray);
            case "gpu" -> new GpuDao().selectByProductIds(idArray);
            case "memory" -> new MemoryDao().selectByProductIds(idArray);
            case "mother_board" -> new MotherBoardDao().selectByProductIds(idArray);
            case "ssd" -> new SsdDao().selectByProductIds(idArray);
            case "hdd" -> new HddDao().selectByProductIds(idArray);
            default -> List.of();
        };
    }

    private List<String> getSelectedMakers(Map<String, ProductOption<?>> options) {
        Set<String> selectedMakers = new HashSet<>();
        if (options != null) {
            for (ProductOption<?> option : options.values()) {
                List<beans.option.SelectableOption> makers = option.getMaker();
                if (makers != null) {
                    for (beans.option.SelectableOption maker : makers) {
                        if (maker.isSelected()) {
                            selectedMakers.add(maker.getValue());
                        }
                    }
                }
            }
        }
        return new ArrayList<>(selectedMakers);
    }

    private Map<String, List<Integer>> applyMakerFilter(Map<String, List<Integer>> idsByCategory, List<String> selectedMakers) throws Exception {
        if (selectedMakers == null || selectedMakers.isEmpty()) {
            return idsByCategory;
        }
        Map<String, List<Integer>> filtered = new LinkedHashMap<>();
        for (Map.Entry<String, List<Integer>> entry : idsByCategory.entrySet()) {
            String category = entry.getKey();
            List<Integer> ids = entry.getValue();
            List<Integer> filteredIds = filterByMaker(category, ids, selectedMakers);
            filtered.put(category, filteredIds);
        }
        return filtered;
    }

    private List<Integer> filterByMaker(String category, List<Integer> productIds, List<String> selectedMakers) throws Exception {
        if (productIds == null || productIds.isEmpty() || selectedMakers == null || selectedMakers.isEmpty()) {
            return productIds;
        }
        List<? extends ProductBean> beans = loadByCategory(category, productIds);
        List<Integer> filteredIds = new ArrayList<>();
        for (ProductBean bean : beans) {
            if (selectedMakers.contains(bean.getMakerName())) {
                filteredIds.add(bean.getProductId());
            }
        }
        return filteredIds;
    }

    /**
     * スペース＝OR、+＝AND
     */
    private List<List<String>> parseQuery(String input) {
        // OR はキーワード "or" で区切ります。
        // その他のトークンは AND として扱います。
        // ダブルクォートで囲まれた部分は 1 つのトークンになります。
        // 例: str1 or str2 str3 or "str 4" -> [[str1], [str2, str3], [str 4]]

        // トークン分解 (ダブルクォートを考慮)
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                if (!inQuotes) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }

            if (!inQuotes && Character.toString(c).matches(QUERY_WHITESPACE_REGEX)) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }

            current.append(c);
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        List<List<String>> groups = new ArrayList<>();
        List<String> currentGroup = new ArrayList<>();

        for (String token : tokens) {
            if (token.equalsIgnoreCase(OR_KEYWORD)) {
                if (!currentGroup.isEmpty()) {
                    groups.add(new ArrayList<>(currentGroup));
                    currentGroup.clear();
                }
                continue;
            }
            currentGroup.add(token);
        }

        if (!currentGroup.isEmpty()) {
            groups.add(currentGroup);
        }

        return groups;
    }

    public ProductBean getProductById(int productId) throws Exception {
        //カテゴリを取得
        String Data = getCategoryAndIdByProductId(productId);
        String category = Data.split(",")[0];
        int categoryId = Integer.parseInt(Data.split(",")[1]);

        if (category == null) {
            return null;
        }
        //カテゴリとIDからSQLを発行してBeanを取得
        return switch (category) {
            case "CPU" -> new CpuDao().selectById(categoryId);
            case "GPU" -> new GpuDao().selectById(categoryId);
            case "MEMORY" -> new MemoryDao().selectById(categoryId);
            case "MOTHER_BOARD" -> new MotherBoardDao().selectById(categoryId);
            case "SSD" -> new SsdDao().selectById(categoryId);
            case "HDD" -> new HddDao().selectById(categoryId);
            default -> null;
        };
    }

    public ProductBean[] getProductByIds(int[] productIds) throws Exception {
        if (productIds == null || productIds.length == 0) {
            return new ProductBean[0];
        }
        List<ProductBean> result = new ArrayList<>();
        String sql = "SELECT PRODUCT_ID, CATEGORY FROM PRODUCT_SEARCH_VIEW WHERE PRODUCT_ID IN (" +
                String.join(",", java.util.Collections.nCopies(productIds.length, "?")) + ")";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < productIds.length; i++) {
                ps.setInt(i + 1, productIds[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("PRODUCT_ID");
                    String category = rs.getString("CATEGORY");
                    ProductBean bean = switch (category) {
                        case "CPU" -> new CpuDao().selectById(productId);
                        case "GPU" -> new GpuDao().selectById(productId);
                        case "MEMORY" -> new MemoryDao().selectById(productId);
                        case "MOTHER_BOARD" -> new MotherBoardDao().selectById(productId);
                        case "SSD" -> new SsdDao().selectById(productId);
                        case "HDD" -> new HddDao().selectById(productId);
                        default -> null;
                    };
                    if (bean != null) {
                        result.add(bean);
                    }
                }
            }
        }
        return result.toArray(new ProductBean[0]);
    }

    private String getCategoryAndIdByProductId(int productId) throws Exception {
        String sql = "SELECT CATEGORY, CATEGORY_ID FROM PRODUCT_SEARCH_VIEW WHERE PRODUCT_ID = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("CATEGORY") + "," + rs.getString("CATEGORY_ID");
                }
            }
        }

        return null;
    }

    public String getProductCategory(int productId) throws Exception {
        String sql = "SELECT CATEGORY FROM PRODUCT_SEARCH_VIEW WHERE PRODUCT_ID = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("CATEGORY");
                }
            }
        }
        return null;
    }
    /**
     * 指定した商品IDの商品の在庫があるかどうかを返します。
     */
    public boolean hasProductStock(int productId) throws Exception {
        String sql = "SELECT PRODUCT_STOCK FROM PRODUCT_SEARCH_VIEW WHERE PRODUCT_ID = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("PRODUCT_STOCK") > 0;
                }
            }
        }
        return false;
    }

    /**
     * 指定した商品IDの商品の在庫が一定以上あるかどうかを返します。
     */
    public boolean hasProductStock(int productId, int requiredStock) throws Exception {
        String sql = "SELECT PRODUCT_STOCK FROM PRODUCT_SEARCH_VIEW WHERE PRODUCT_ID = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("PRODUCT_STOCK") >= requiredStock;
                }
            }
        }
        return false;
    }
}