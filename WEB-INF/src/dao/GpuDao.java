/**
 * 作成：小車
 * 最終変更：3月3日
 * 変更内容：selectByProductIdsを追加
 * 概要
 * 　GPU用のDao、固有メソッド等はない
 */

package dao;

import beans.GpuBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import utils.utils;

public class GpuDao extends ProductDao<GpuBean> {

    @Override
    protected GpuBean createEmptyBean() {
        return new GpuBean();
    }

    @Override
    public void insert(GpuBean bean) throws DaoException {
        String sql = """
            INSERT INTO GPU (PRODUCT_ID, SERIES_ID, CHIP_ID, VRAM_ID)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                int productId = insertProduct(con, bean);

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, getIdByValue(con, "GPU_SERIES", "SERIES_ID", bean.getSeriesName()));
                    ps.setInt(3, getIdByValue(con, "GPU_CHIP", "CHIP_ID", bean.getChipName()));
                    ps.setInt(4, getIdByValue(con, "GPU_VRAM", "VRAM_ID", Integer.toString(bean.getVram())));
                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("GPU INSERT FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("GPU CONNECTION ERROR", e);
        }
    }

    @Override
    public GpuBean selectById(int id) throws DaoException {
        String sql = """
            SELECT
                G.GPU_ID,
                G.PRODUCT_ID,
                P.NAME,
                P.PRICE,
                P.STOCK,
                P.IMAGE,
                P.MAKER_ID,
                M.VALUE AS MAKER_NAME,
                S.VALUE AS SERIES_NAME,
                C.VALUE AS CHIP_NAME,
                GV.VALUE AS VRAM
            FROM GPU G
            JOIN PRODUCT P ON G.PRODUCT_ID = P.PRODUCT_ID
            JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
            JOIN GPU_SERIES S ON G.SERIES_ID = S.SERIES_ID
            JOIN GPU_CHIP C ON G.CHIP_ID = C.CHIP_ID
            JOIN GPU_VRAM GV ON G.VRAM_ID = GV.VRAM_ID
            WHERE G.GPU_ID = ?
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
            throw new DaoException("GPU SELECT FAILED", e);
        }

        return null;
    }

    public List<GpuBean> selectByProductIds(int[] productIds) throws DaoException {

        List<GpuBean> list = new ArrayList<>();
        if (productIds == null || productIds.length == 0) return list;

        final int CHUNK = 1000;

        for (int offset = 0; offset < productIds.length; offset += CHUNK) {
            int len = Math.min(CHUNK, productIds.length - offset);

            String placeholders = String.join(",", java.util.Collections.nCopies(len, "?"));

            String sql = """
                SELECT
                    G.GPU_ID,
                    G.PRODUCT_ID,
                    P.NAME,
                    P.PRICE,
                    P.STOCK,
                    P.IMAGE,
                    P.MAKER_ID,
                    M.VALUE AS MAKER_NAME,
                    S.VALUE AS SERIES_NAME,
                    C.VALUE AS CHIP_NAME,
                    GV.VALUE AS VRAM
                FROM GPU G
                JOIN PRODUCT P ON G.PRODUCT_ID = P.PRODUCT_ID
                JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
                JOIN GPU_SERIES S ON G.SERIES_ID = S.SERIES_ID
                JOIN GPU_CHIP C ON G.CHIP_ID = C.CHIP_ID
                JOIN GPU_VRAM GV ON G.VRAM_ID = GV.VRAM_ID
                WHERE G.PRODUCT_ID IN (""" + placeholders + ")";

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
                throw new DaoException("GPU SELECT BY IDS FAILED", e);
            }
        }

        return list;
    }

    @Override
    public List<GpuBean> selectAll() throws DaoException {
        String sql = """
            SELECT
                G.GPU_ID,
                G.PRODUCT_ID,
                P.NAME,
                P.PRICE,
                P.STOCK,
                P.IMAGE,
                P.MAKER_ID,
                M.VALUE AS MAKER_NAME,
                S.VALUE AS SERIES_NAME,
                C.VALUE AS CHIP_NAME,
                GV.VALUE AS VRAM
            FROM GPU G
            JOIN PRODUCT P ON G.PRODUCT_ID = P.PRODUCT_ID
            JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
            JOIN GPU_SERIES S ON G.SERIES_ID = S.SERIES_ID
            JOIN GPU_CHIP C ON G.CHIP_ID = C.CHIP_ID
            JOIN GPU_VRAM GV ON G.VRAM_ID = GV.VRAM_ID
            """;

        List<GpuBean> list = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(createNewBean(rs));
            }

        } catch (Exception e) {
            throw new DaoException("GPU SELECT ALL FAILED", e);
        }

        return list;
    }

    @Override
    public void update(GpuBean bean) throws DaoException {
        String sql = """
            UPDATE GPU
            SET SERIES_ID = ?, CHIP_ID = ?, VRAM_ID = ?
            WHERE GPU_ID = ?
            """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                updateProduct(con, bean);

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, getIdByValue(con, "GPU_SERIES", "SERIES_ID", bean.getSeriesName()));
                    ps.setInt(2, getIdByValue(con, "GPU_CHIP", "CHIP_ID", bean.getChipName()));
                    ps.setInt(3, getIdByValue(con, "GPU_VRAM", "VRAM_ID", Integer.toString(bean.getVram())));
                    ps.setInt(4, bean.getGpuId());
                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("GPU UPDATE FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("GPU CONNECTION ERROR", e);
        }
    }

    @Override
    public void delete(int id) throws DaoException {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                deleteEntityAndProduct(con, "GPU", "GPU_ID", id);
                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("GPU DELETE FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("GPU CONNECTION ERROR", e);
        }
    }

    @Override
    protected GpuBean createNewBean(ResultSet rs) throws Exception {
        return new GpuBean(
            rs.getInt("GPU_ID"),
            rs.getInt("PRODUCT_ID"),
            rs.getString("NAME"),
            rs.getInt("PRICE"),
            rs.getInt("STOCK"),
            rs.getString("IMAGE"),
            rs.getInt("MAKER_ID"),
            rs.getString("MAKER_NAME"),
            rs.getString("SERIES_NAME"),
            rs.getString("CHIP_NAME"),
            utils.parseInt(rs.getString("VRAM"), 0)
         );
    }
}
