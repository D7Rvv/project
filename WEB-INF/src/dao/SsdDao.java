/**
 * 作成：小車
 * 最終変更：3月3日
 * 変更内容：SelectByProductIdsを追加
 * 概要
 * 　SSD用のDao、固有メソッド等はない
 */

package dao;

import beans.SsdBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SsdDao extends ProductDao<SsdBean> {

    @Override
    protected SsdBean createEmptyBean() {
        return new SsdBean();
    }

    @Override
    public void insert(SsdBean bean) throws DaoException {
        String sql = """
            INSERT INTO SSD (PRODUCT_ID, CAPACITY_ID, TYPE_ID)
            VALUES (?, ?, ?)
            """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                int productId = insertProduct(con, bean);

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, getIdByValue(con, "STORAGE_CAPACITY", "CAPACITY_ID", bean.getCapacity()));
                    ps.setInt(3, getIdByValue(con, "SSD_TYPE", "TYPE_ID", bean.getType()));
                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("SSD INSERT FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("SSD CONNECTION ERROR", e);
        }
    }

    @Override
    public SsdBean selectById(int id) throws DaoException {
        String sql = """
            SELECT
                S.SSD_ID,
                S.PRODUCT_ID,
                P.NAME,
                P.PRICE,
                P.STOCK,
                P.IMAGE,
                P.MAKER_ID,
                M.VALUE AS MAKER_NAME,
                C.VALUE AS CAPACITY,
                T.VALUE AS TYPE
            FROM SSD S
            JOIN PRODUCT P ON S.PRODUCT_ID = P.PRODUCT_ID
            JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
            JOIN STORAGE_CAPACITY C ON S.CAPACITY_ID = C.CAPACITY_ID
            JOIN SSD_TYPE T ON S.TYPE_ID = T.TYPE_ID
            WHERE S.SSD_ID = ?
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
            throw new DaoException("SSD SELECT FAILED", e);
        }

        return null;
    }

    public List<SsdBean> selectByProductIds(int[] productIds) throws DaoException {

        List<SsdBean> list = new ArrayList<>();
        if (productIds == null || productIds.length == 0) return list;

        final int CHUNK = 1000;

        for (int offset = 0; offset < productIds.length; offset += CHUNK) {
            int len = Math.min(CHUNK, productIds.length - offset);

            String placeholders = String.join(",", java.util.Collections.nCopies(len, "?"));

            String sql = """
                SELECT
                    S.SSD_ID,
                    S.PRODUCT_ID,
                    P.NAME,
                    P.PRICE,
                    P.STOCK,
                    P.IMAGE,
                    P.MAKER_ID,
                    M.VALUE AS MAKER_NAME,
                    C.VALUE AS CAPACITY,
                    T.VALUE AS TYPE
                FROM SSD S
                JOIN PRODUCT P ON S.PRODUCT_ID = P.PRODUCT_ID
                JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
                JOIN STORAGE_CAPACITY C ON S.CAPACITY_ID = C.CAPACITY_ID
                JOIN SSD_TYPE T ON S.TYPE_ID = T.TYPE_ID
                WHERE S.PRODUCT_ID IN (""" + placeholders + ")";

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
                throw new DaoException("SSD SELECT BY IDS FAILED", e);
            }
        }

        return list;
    }

    @Override
    public List<SsdBean> selectAll() throws DaoException {
        String sql = """
            SELECT
                S.SSD_ID,
                S.PRODUCT_ID,
                P.NAME,
                P.PRICE,
                P.STOCK,
                P.IMAGE,
                P.MAKER_ID,
                M.VALUE AS MAKER_NAME,
                C.VALUE AS CAPACITY,
                T.VALUE AS TYPE
            FROM SSD S
            JOIN PRODUCT P ON S.PRODUCT_ID = P.PRODUCT_ID
            JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
            JOIN STORAGE_CAPACITY C ON S.CAPACITY_ID = C.CAPACITY_ID
            JOIN SSD_TYPE T ON S.TYPE_ID = T.TYPE_ID
            """;

        List<SsdBean> list = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(createNewBean(rs));
            }

        } catch (Exception e) {
            throw new DaoException("SSD SELECT ALL FAILED", e);
        }

        return list;
    }

    @Override
    public void update(SsdBean bean) throws DaoException {
        String sql = """
            UPDATE SSD
            SET CAPACITY_ID = ?, TYPE_ID = ?
            WHERE SSD_ID = ?
            """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                updateProduct(con, bean);

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, getIdByValue(con, "STORAGE_CAPACITY", "CAPACITY_ID", bean.getCapacity()));
                    ps.setInt(2, getIdByValue(con, "SSD_TYPE", "TYPE_ID", bean.getType()));
                    ps.setInt(3, bean.getSsdId());
                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("SSD UPDATE FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("SSD CONNECTION ERROR", e);
        }
    }

    @Override
    public void delete(int id) throws DaoException {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                deleteEntityAndProduct(con, "SSD", "SSD_ID", id);
                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("SSD DELETE FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("SSD CONNECTION ERROR", e);
        }
    }

    @Override
    protected SsdBean createNewBean(ResultSet rs) throws Exception {
        return new SsdBean(
            rs.getInt("SSD_ID"),
            rs.getInt("PRODUCT_ID"),
            rs.getString("NAME"),
            rs.getInt("PRICE"),
            rs.getInt("STOCK"),
            rs.getString("IMAGE"),
            rs.getInt("MAKER_ID"),
            rs.getString("MAKER_NAME"),
            rs.getString("CAPACITY"),
            rs.getString("TYPE")
        );
    }
}