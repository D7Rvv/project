/**
 * 作成：小車
 * 最終変更：3月3日
 * 変更内容：selectByProductIdsを追加
 * 概要
 * 　HDD用のDao、固有メソッド等はない
 */

package dao;

import beans.HddBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class HddDao extends ProductDao<HddBean> {

    @Override
    protected HddBean createEmptyBean() {
        return new HddBean();
    }

    @Override
    public void insert(HddBean bean) throws DaoException {
        String sql = """
            INSERT INTO HDD (PRODUCT_ID, CAPACITY_ID, RPM_ID)
            VALUES (?, ?, ?)
            """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                int productId = insertProduct(con, bean);

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, getIdByValue(con, "STORAGE_CAPACITY", "CAPACITY_ID", bean.getCapacity()));
                    ps.setInt(3, getIdByValue(con, "HDD_RPM", "RPM_ID", bean.getRpm()));
                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("HDD INSERT FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("HDD CONNECTION ERROR", e);
        }
    }

    @Override
    public HddBean selectById(int id) throws DaoException {
        String sql = """
            SELECT
                H.HDD_ID,
                H.PRODUCT_ID,
                P.NAME,
                P.PRICE,
                P.STOCK,
                P.IMAGE,
                P.MAKER_ID,
                M.VALUE AS MAKER_NAME,
                C.VALUE AS CAPACITY,
                R.VALUE AS RPM
            FROM HDD H
            JOIN PRODUCT P ON H.PRODUCT_ID = P.PRODUCT_ID
            JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
            JOIN STORAGE_CAPACITY C ON H.CAPACITY_ID = C.CAPACITY_ID
            JOIN HDD_RPM R ON H.RPM_ID = R.RPM_ID
            WHERE H.HDD_ID = ?
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
            throw new DaoException("HDD SELECT FAILED", e);
        }

        return null;
    }

    public List<HddBean> selectByProductIds(int[] productIds) throws DaoException {

        List<HddBean> list = new ArrayList<>();
        if (productIds == null || productIds.length == 0) return list;

        final int CHUNK = 1000;

        for (int offset = 0; offset < productIds.length; offset += CHUNK) {
            int len = Math.min(CHUNK, productIds.length - offset);

            String placeholders = String.join(",", java.util.Collections.nCopies(len, "?"));

            String sql = """
                SELECT
                    H.HDD_ID,
                    H.PRODUCT_ID,
                    P.NAME,
                    P.PRICE,
                    P.STOCK,
                    P.IMAGE,
                    P.MAKER_ID,
                    M.VALUE AS MAKER_NAME,
                    C.VALUE AS CAPACITY,
                    R.VALUE AS RPM
                FROM HDD H
                JOIN PRODUCT P ON H.PRODUCT_ID = P.PRODUCT_ID
                JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
                JOIN STORAGE_CAPACITY C ON H.CAPACITY_ID = C.CAPACITY_ID
                JOIN HDD_RPM R ON H.RPM_ID = R.RPM_ID
                WHERE H.PRODUCT_ID IN (""" + placeholders + ")";

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
                throw new DaoException("HDD SELECT BY IDS FAILED", e);
            }
        }

        return list;
    }

    @Override
    public List<HddBean> selectAll() throws DaoException {
        String sql = """
            SELECT
                H.HDD_ID,
                H.PRODUCT_ID,
                P.NAME,
                P.PRICE,
                P.STOCK,
                P.IMAGE,
                P.MAKER_ID,
                M.VALUE AS MAKER_NAME,
                C.VALUE AS CAPACITY,
                R.VALUE AS RPM
            FROM HDD H
            JOIN PRODUCT P ON H.PRODUCT_ID = P.PRODUCT_ID
            JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
            JOIN STORAGE_CAPACITY C ON H.CAPACITY_ID = C.CAPACITY_ID
            JOIN HDD_RPM R ON H.RPM_ID = R.RPM_ID
            """;

        List<HddBean> list = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(createNewBean(rs));
            }

        } catch (Exception e) {
            throw new DaoException("HDD SELECT ALL FAILED", e);
        }

        return list;
    }

    @Override
    public void update(HddBean bean) throws DaoException {
        String sql = """
            UPDATE HDD
            SET CAPACITY_ID = ?, RPM_ID = ?
            WHERE HDD_ID = ?
            """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                updateProduct(con, bean);

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, getIdByValue(con, "STORAGE_CAPACITY", "CAPACITY_ID", bean.getCapacity()));
                    ps.setInt(2, getIdByValue(con, "HDD_RPM", "RPM_ID", bean.getRpm()));
                    ps.setInt(3, bean.getHddId());
                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("HDD UPDATE FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("HDD CONNECTION ERROR", e);
        }
    }

    @Override
    public void delete(int id) throws DaoException {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                deleteEntityAndProduct(con, "HDD", "HDD_ID", id);
                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("HDD DELETE FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("HDD CONNECTION ERROR", e);
        }
    }

    @Override
    protected HddBean createNewBean(ResultSet rs) throws Exception {
        return new HddBean(
            rs.getInt("HDD_ID"),
            rs.getInt("PRODUCT_ID"),
            rs.getString("NAME"),
            rs.getInt("PRICE"),
            rs.getInt("STOCK"),
            rs.getString("IMAGE"),
            rs.getInt("MAKER_ID"),
            rs.getString("MAKER_NAME"),
            rs.getString("CAPACITY"),
            rs.getString("RPM")
         );
    }
}