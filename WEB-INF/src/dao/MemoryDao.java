/**
 * 作成：小車
 * 最終変更：3月3日
 * 変更内容：selectByProductIdsを追加
 * 概要
 * 　MEMORY用のDao、固有メソッド等はない
 */

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import beans.MemoryBean;

public class MemoryDao extends ProductDao<MemoryBean> {

    @Override
    protected MemoryBean createEmptyBean() {
        return new MemoryBean();
    }

    @Override
    public void insert(MemoryBean bean) throws DaoException {
        String sql = """
            INSERT INTO MEMORY (PRODUCT_ID, GEN_ID, CAPACITY_ID)
            VALUES (?, ?, ?)
            """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                int productId = insertProduct(con, bean);

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, getIdByValue(con, "MEMORY_GEN", "GEN_ID", bean.getGeneration()));
                    ps.setInt(3, getIdByValue(con, "MEMORY_CAPACITY", "CAPACITY_ID", bean.getCapacity()));
                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("MEMORY INSERT FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("MEMORY CONNECTION ERROR", e);
        }
    }

    @Override
    public MemoryBean selectById(int id) throws DaoException {
        String sql = """
            SELECT
                M.MEMORY_ID,
                M.PRODUCT_ID,
                P.NAME,
                P.PRICE,
                P.STOCK,
                P.IMAGE,
                P.MAKER_ID,
                MK.VALUE AS MAKER_NAME,
                G.VALUE AS GEN,
                C.VALUE AS CAPACITY
            FROM MEMORY M
            JOIN PRODUCT P ON M.PRODUCT_ID = P.PRODUCT_ID
            JOIN MAKER MK ON P.MAKER_ID = MK.MAKER_ID
            JOIN MEMORY_GEN G ON M.GEN_ID = G.GEN_ID
            JOIN MEMORY_CAPACITY C ON M.CAPACITY_ID = C.CAPACITY_ID
            WHERE M.MEMORY_ID = ?
            """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createNewBean(rs);
                }
            }

        } catch (Exception e) {
            throw new DaoException("MEMORY SELECT FAILED", e);
        }

        return null;
    }

    public List<MemoryBean> selectByProductIds(int[] productIds) throws DaoException {

        List<MemoryBean> list = new ArrayList<>();
        if (productIds == null || productIds.length == 0) return list;

        final int CHUNK = 1000;

        for (int offset = 0; offset < productIds.length; offset += CHUNK) {
            int len = Math.min(CHUNK, productIds.length - offset);

            String placeholders = String.join(",", java.util.Collections.nCopies(len, "?"));

            String sql = """
                SELECT
                    M.MEMORY_ID,
                    M.PRODUCT_ID,
                    P.NAME,
                    P.PRICE,
                    P.STOCK,
                    P.IMAGE,
                    P.MAKER_ID,
                    MK.VALUE AS MAKER_NAME,
                    G.VALUE AS GEN,
                    C.VALUE AS CAPACITY
                FROM MEMORY M
                JOIN PRODUCT P ON M.PRODUCT_ID = P.PRODUCT_ID
                JOIN MAKER MK ON P.MAKER_ID = MK.MAKER_ID
                JOIN MEMORY_GEN G ON M.GEN_ID = G.GEN_ID
                JOIN MEMORY_CAPACITY C ON M.CAPACITY_ID = C.CAPACITY_ID
                WHERE M.PRODUCT_ID IN (""" + placeholders + ")";

            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                for (int i = 0; i < len; i++) {
                    ps.setInt(i + 1, productIds[offset + i]);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(createNewBean(rs));
                    }
                }

            } catch (Exception e) {
                throw new DaoException("MEMORY SELECT BY IDS FAILED", e);
            }
        }

        return list;
    }

    @Override
    public List<MemoryBean> selectAll() throws DaoException {
        String sql = """
            SELECT
                M.MEMORY_ID,
                M.PRODUCT_ID,
                P.NAME,
                P.PRICE,
                P.STOCK,
                P.IMAGE,
                P.MAKER_ID,
                MK.VALUE AS MAKER_NAME,
                G.VALUE AS GEN,
                C.VALUE AS CAPACITY
            FROM MEMORY M
            JOIN PRODUCT P ON M.PRODUCT_ID = P.PRODUCT_ID
            JOIN MAKER MK ON P.MAKER_ID = MK.MAKER_ID
            JOIN MEMORY_GEN G ON M.GEN_ID = G.GEN_ID
            JOIN MEMORY_CAPACITY C ON M.CAPACITY_ID = C.CAPACITY_ID
            """;

        List<MemoryBean> list = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(createNewBean(rs));
            }

        } catch (Exception e) {
            throw new DaoException("MEMORY SELECT ALL FAILED", e);
        }

        return list;
    }

    @Override
    public void update(MemoryBean bean) throws DaoException {
        String sql = """
            UPDATE MEMORY
            SET GEN_ID = ?, CAPACITY_ID = ?
            WHERE MEMORY_ID = ?
            """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                updateProduct(con, bean);

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, getIdByValue(con, "MEMORY_GEN", "GEN_ID", bean.getGeneration()));
                    ps.setInt(2, getIdByValue(con, "MEMORY_CAPACITY", "CAPACITY_ID", bean.getCapacity()));
                    ps.setInt(3, bean.getMemoryId());
                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("MEMORY UPDATE FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("MEMORY CONNECTION ERROR", e);
        }
    }

    @Override
    public void delete(int id) throws DaoException {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                deleteEntityAndProduct(con, "MEMORY", "MEMORY_ID", id);
                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("MEMORY DELETE FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("MEMORY CONNECTION ERROR", e);
        }
    }

    @Override
    protected MemoryBean createNewBean(ResultSet rs) throws Exception {
        return new MemoryBean(
            rs.getInt("MEMORY_ID"),
            rs.getInt("PRODUCT_ID"),
            rs.getString("NAME"),
            rs.getInt("PRICE"),
            rs.getInt("STOCK"),
            rs.getString("IMAGE"),
            rs.getInt("MAKER_ID"),
            rs.getString("MAKER_NAME"),
            rs.getString("GEN"),
            rs.getString("CAPACITY")
        );
    }
}