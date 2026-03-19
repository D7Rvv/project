/**
 * 作成：小車
 * 最終変更：3月2日
 * 変更内容：---
 * 概要
 * 　商品用の抽象クラス、商品関連を実装
 */

package dao;

import beans.ProductBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @param <T> ProductBean を継承した各種 Bean
 */
public abstract class ProductDao<T extends ProductBean> extends BaseDao<T> {

    protected int insertProduct(Connection con, ProductBean bean) throws Exception {
        String sql = """
            INSERT INTO PRODUCT (MAKER_ID, NAME, PRICE, STOCK, IMAGE)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"PRODUCT_ID"})) {
            ps.setInt(1, bean.getMakerId());
            ps.setString(2, bean.getName());
            ps.setInt(3, bean.getPrice());
            ps.setInt(4, bean.getStock());
            ps.setString(5, bean.getImageId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new Exception("PRODUCT INSERT FAILED");
    }

    protected void updateProduct(Connection con, ProductBean bean) throws Exception {
        String sql = """
            UPDATE PRODUCT
            SET MAKER_ID = ?, NAME = ?, PRICE = ?, STOCK = ?, IMAGE = ?
            WHERE PRODUCT_ID = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bean.getMakerId());
            ps.setString(2, bean.getName());
            ps.setInt(3, bean.getPrice());
            ps.setInt(4, bean.getStock());
            ps.setString(5, bean.getImageId());
            ps.setInt(6, bean.getProductId());
            ps.executeUpdate();
        }
    }

    protected void deleteProduct(Connection con, int productId) throws Exception {
        String sql = "DELETE FROM PRODUCT WHERE PRODUCT_ID = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        }
    }

    protected void mapProduct(ResultSet rs, ProductBean bean) throws Exception {
        bean.setProductId(rs.getInt("PRODUCT_ID"));
        bean.setName(rs.getString("NAME"));
        bean.setPrice(rs.getInt("PRICE"));
        bean.setStock(rs.getInt("STOCK"));
        bean.setImageId(rs.getString("IMAGE"));
        bean.setMakerId(rs.getInt("MAKER_ID"));
        bean.setMakerName(rs.getString("MAKER_NAME"));
    }

    protected T selectProductById(Connection con, int productId, T bean) throws Exception {
        String sql = """
            SELECT
                P.PRODUCT_ID,
                P.NAME,
                P.PRICE,
                P.STOCK,
                P.IMAGE,
                P.MAKER_ID,
                M.VALUE AS MAKER_NAME
            FROM PRODUCT P
            JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
            WHERE P.PRODUCT_ID = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mapProduct(rs, bean);
                    return bean;
                }
            }
        }

        return null;
    }

    protected List<T> selectAllProduct(Connection con) throws Exception {
        String sql = """
            SELECT
                P.PRODUCT_ID,
                P.NAME,
                P.PRICE,
                P.STOCK,
                P.IMAGE,
                P.MAKER_ID,
                M.VALUE AS MAKER_NAME
            FROM PRODUCT P
            JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
            """;

        List<T> list = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                T bean = createEmptyBean();
                mapProduct(rs, bean);
                list.add(bean);
            }
        }

        return list;
    }

    protected Integer selectIdByValue(Connection con, String table, String idCol, String value) throws Exception {
        String sql = "SELECT " + idCol + " FROM " + table + " WHERE VALUE = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(idCol);
            }
        }
        return null;
    }

    protected int getIdByValue(Connection con, String table, String idCol, String value) throws Exception {
        Integer id = selectIdByValue(con, table, idCol, value);
        if (id != null) return id;
        throw new Exception(table + " NOT FOUND: " + value);
    }

    protected void deleteEntityAndProduct(Connection con, String entityTable, String idCol, int id) throws Exception {
        int productId = 0;

        try (PreparedStatement ps = con.prepareStatement("SELECT PRODUCT_ID FROM " + entityTable + " WHERE " + idCol + " = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    productId = rs.getInt("PRODUCT_ID");
                }
            }
        }

        if (productId == 0) {
            throw new Exception(entityTable + " not found: " + id);
        }

        try (PreparedStatement ps = con.prepareStatement("DELETE FROM " + entityTable + " WHERE " + idCol + " = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }

        deleteProduct(con, productId);
    }

    protected abstract T createEmptyBean();

    protected abstract T createNewBean(ResultSet rs) throws Exception;
}