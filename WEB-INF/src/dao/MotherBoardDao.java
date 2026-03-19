/**
 * 作成：小車
 * 最終変更：3月3日
 * 変更内容：selectByProductIdsを追加
 * 概要
 * 　MotherBoard用のDao、固有メソッド等はない
 */

package dao;

import beans.MotherBoardBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MotherBoardDao extends ProductDao<MotherBoardBean> {

    @Override
    protected MotherBoardBean createEmptyBean() {
        return new MotherBoardBean();
    }

    @Override
    public void insert(MotherBoardBean bean) throws DaoException {
        String sql = """
            INSERT INTO MOTHER_BOARD (PRODUCT_ID, CHIPSET_ID, SIZE_ID)
            VALUES (?, ?, ?)
            """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                int productId = insertProduct(con, bean);

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, getIdByValue(con, "MOTHER_BOARD_CHIPSET", "CHIPSET_ID", bean.getChipset()));
                    ps.setInt(3, getIdByValue(con, "MOTHER_BOARD_SIZE", "SIZE_ID", bean.getSize()));
                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("MOTHERBOARD INSERT FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("MOTHERBOARD CONNECTION ERROR", e);
        }
    }

    @Override
    public MotherBoardBean selectById(int id) throws DaoException {
        String sql = """
            SELECT
                MB.MOTHER_BOARD_ID,
                MB.PRODUCT_ID,
                P.NAME,
                P.PRICE,
                P.STOCK,
                P.IMAGE,
                P.MAKER_ID,
                M.VALUE AS MAKER_NAME,
                C.VALUE AS CHIPSET,
                S.VALUE AS "SIZE"
            FROM MOTHER_BOARD MB
            JOIN PRODUCT P ON MB.PRODUCT_ID = P.PRODUCT_ID
            JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
            JOIN MOTHER_BOARD_CHIPSET C ON MB.CHIPSET_ID = C.CHIPSET_ID
            JOIN MOTHER_BOARD_SIZE S ON MB.SIZE_ID = S.SIZE_ID
            WHERE MB.MOTHER_BOARD_ID = ?
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
            throw new DaoException("MOTHERBOARD SELECT FAILED", e);
        }

        return null;
    }

    public List<MotherBoardBean> selectByProductIds(int[] productIds) throws DaoException {

        List<MotherBoardBean> list = new ArrayList<>();
        if (productIds == null || productIds.length == 0) return list;

        final int CHUNK = 1000;

        for (int offset = 0; offset < productIds.length; offset += CHUNK) {
            int len = Math.min(CHUNK, productIds.length - offset);

            String placeholders = String.join(",", java.util.Collections.nCopies(len, "?"));

            String sql = """
                SELECT
                    MB.MOTHER_BOARD_ID,
                    MB.PRODUCT_ID,
                    P.NAME,
                    P.PRICE,
                    P.STOCK,
                    P.IMAGE,
                    P.MAKER_ID,
                    M.VALUE AS MAKER_NAME,
                    C.VALUE AS CHIPSET,
                    S.VALUE AS "SIZE"
                FROM MOTHER_BOARD MB
                JOIN PRODUCT P ON MB.PRODUCT_ID = P.PRODUCT_ID
                JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
                JOIN MOTHER_BOARD_CHIPSET C ON MB.CHIPSET_ID = C.CHIPSET_ID
                JOIN MOTHER_BOARD_SIZE S ON MB.SIZE_ID = S.SIZE_ID
                WHERE MB.PRODUCT_ID IN (""" + placeholders + ")";

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
                throw new DaoException("MOTHERBOARD SELECT BY IDS FAILED", e);
            }
        }

        return list;
    }

    @Override
    public List<MotherBoardBean> selectAll() throws DaoException {
        String sql = """
            SELECT
                MB.MOTHER_BOARD_ID,
                MB.PRODUCT_ID,
                P.NAME,
                P.PRICE,
                P.STOCK,
                P.IMAGE,
                P.MAKER_ID,
                M.VALUE AS MAKER_NAME,
                C.VALUE AS CHIPSET,
                S.VALUE AS "SIZE"
            FROM MOTHER_BOARD MB
            JOIN PRODUCT P ON MB.PRODUCT_ID = P.PRODUCT_ID
            JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
            JOIN MOTHER_BOARD_CHIPSET C ON MB.CHIPSET_ID = C.CHIPSET_ID
            JOIN MOTHER_BOARD_SIZE S ON MB.SIZE_ID = S.SIZE_ID
            """;

        List<MotherBoardBean> list = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(createNewBean(rs));
            }

        } catch (Exception e) {
            throw new DaoException("MOTHERBOARD SELECT ALL FAILED：" + e.getMessage(), e);
        }

        return list;
    }

    @Override
    public void update(MotherBoardBean bean) throws DaoException {
        String sql = """
            UPDATE MOTHER_BOARD
            SET CHIPSET_ID = ?, SIZE_ID = ?
            WHERE MOTHER_BOARD_ID = ?
            """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                updateProduct(con, bean);

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, getIdByValue(con, "MOTHER_BOARD_CHIPSET", "CHIPSET_ID", bean.getChipset()));
                    ps.setInt(2, getIdByValue(con, "MOTHER_BOARD_SIZE", "SIZE_ID", bean.getSize()));
                    ps.setInt(3, bean.getMotherBoardId());
                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("MOTHERBOARD UPDATE FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("MOTHERBOARD CONNECTION ERROR", e);
        }
    }

    @Override
    public void delete(int id) throws DaoException {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                deleteEntityAndProduct(con, "MOTHER_BOARD", "MOTHER_BOARD_ID", id);
                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("MOTHERBOARD DELETE FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("MOTHERBOARD CONNECTION ERROR", e);
        }
    }
    
    @Override
    protected MotherBoardBean createNewBean(ResultSet rs) throws Exception {
        return new MotherBoardBean(
            rs.getInt("MOTHER_BOARD_ID"),
            rs.getInt("PRODUCT_ID"),
            rs.getString("NAME"),
            rs.getInt("PRICE"),
            rs.getInt("STOCK"),
            rs.getString("IMAGE"),
            rs.getInt("MAKER_ID"),
            rs.getString("MAKER_NAME"),
            rs.getString("CHIPSET"),
            rs.getString("SIZE")
        );
    }
}