/**
 * 作成：小車
 * 最終変更：3月2日
 * 変更内容：---
 * 概要
 * 　CPU用のDao、固有メソッド等はない
 */

package dao;

import beans.CpuBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import utils.utils;

public class CpuDao extends ProductDao<CpuBean> {

    @Override
    protected CpuBean createEmptyBean() {
        return new CpuBean();
    }

    @Override
    public void insert(CpuBean bean) throws DaoException {
        String sql = """
            INSERT INTO CPU (PRODUCT_ID, GEN_ID, CORE_ID, THREAD_ID, CLOCK_ID)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                int productId = insertProduct(con, bean);

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, getIdByValue(con, "CPU_GEN", "GEN_ID", bean.getGeneration()));
                    ps.setInt(3, getIdByValue(con, "CPU_CORE", "CORE_ID", Integer.toString(bean.getCore())));
                    ps.setInt(4, getIdByValue(con, "CPU_THREAD", "THREAD_ID", Integer.toString(bean.getThread())));
                    ps.setInt(5, getIdByValue(con, "CPU_CLOCK", "CLOCK_ID", Double.toString(bean.getClock())));
                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("CPU INSERT FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("CPU CONNECTION ERROR", e);
        }
    }

    @Override
    public CpuBean selectById(int id) throws DaoException {
        String sql = """
            SELECT
                C.CPU_ID,
                C.PRODUCT_ID,
                P.NAME,
                P.PRICE,
                P.STOCK,
                P.IMAGE,
                P.MAKER_ID,
                M.VALUE AS MAKER_NAME,
                G.VALUE AS GENERATION,
                CC.VALUE AS CORE,
                CT.VALUE AS THREAD,
                CCL.VALUE AS CLOCK
            FROM CPU C
            JOIN PRODUCT P ON C.PRODUCT_ID = P.PRODUCT_ID
            JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
            JOIN CPU_GEN G ON C.GEN_ID = G.GEN_ID
            JOIN CPU_CORE CC ON C.CORE_ID = CC.CORE_ID
            JOIN CPU_THREAD CT ON C.THREAD_ID = CT.THREAD_ID
            JOIN CPU_CLOCK CCL ON C.CLOCK_ID = CCL.CLOCK_ID
            WHERE C.CPU_ID = ?
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
            throw new DaoException("CPU SELECT FAILED", e);
        }

        return null;
    }

    public List<CpuBean> selectByProductIds(int[] productIds) throws DaoException {

        List<CpuBean> list = new ArrayList<>();
        if (productIds == null || productIds.length == 0) return list;

        final int CHUNK = 1000;

        for (int offset = 0; offset < productIds.length; offset += CHUNK) {
            int len = Math.min(CHUNK, productIds.length - offset);

            String placeholders = String.join(",", java.util.Collections.nCopies(len, "?"));

            String sql = """
                SELECT
                    C.CPU_ID,
                    C.PRODUCT_ID,
                    P.NAME,
                    P.PRICE,
                    P.STOCK,
                    P.IMAGE,
                    P.MAKER_ID,
                    M.VALUE AS MAKER_NAME,
                    G.VALUE AS GENERATION,
                    CC.VALUE AS CORE,
                    CT.VALUE AS THREAD,
                    CCL.VALUE AS CLOCK
                FROM CPU C
                JOIN PRODUCT P ON C.PRODUCT_ID = P.PRODUCT_ID
                JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
                JOIN CPU_GEN G ON C.GEN_ID = G.GEN_ID
                JOIN CPU_CORE CC ON C.CORE_ID = CC.CORE_ID
                JOIN CPU_THREAD CT ON C.THREAD_ID = CT.THREAD_ID
                JOIN CPU_CLOCK CCL ON C.CLOCK_ID = CCL.CLOCK_ID
                WHERE C.PRODUCT_ID IN (""" + placeholders + ")";

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
                throw new DaoException("CPU SELECT BY IDS FAILED", e);
            }
        }

        return list;
    }
    
    @Override
    public List<CpuBean> selectAll() throws DaoException {
        String sql = """
            SELECT
                C.CPU_ID,
                C.PRODUCT_ID,
                P.NAME,
                P.PRICE,
                P.STOCK,
                P.IMAGE,
                P.MAKER_ID,
                M.VALUE AS MAKER_NAME,
                G.VALUE AS GENERATION,
                CC.VALUE AS CORE,
                CT.VALUE AS THREAD,
                CCL.VALUE AS CLOCK
            FROM CPU C
            JOIN PRODUCT P ON C.PRODUCT_ID = P.PRODUCT_ID
            JOIN MAKER M ON P.MAKER_ID = M.MAKER_ID
            JOIN CPU_GEN G ON C.GEN_ID = G.GEN_ID
            JOIN CPU_CORE CC ON C.CORE_ID = CC.CORE_ID
            JOIN CPU_THREAD CT ON C.THREAD_ID = CT.THREAD_ID
            JOIN CPU_CLOCK CCL ON C.CLOCK_ID = CCL.CLOCK_ID
            """;

        List<CpuBean> list = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(createNewBean(rs));
            }
        } catch (Exception e) {
            throw new DaoException("CPU SELECT ALL FAILED", e);
        }

        return list;
    }

    @Override
    public void update(CpuBean bean) throws DaoException {
        String sql = """
            UPDATE CPU
            SET GEN_ID = ?, CORE_ID = ?, THREAD_ID = ?, CLOCK_ID = ?
            WHERE CPU_ID = ?
            """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                updateProduct(con, bean);

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, getIdByValue(con, "CPU_GEN", "GEN_ID", bean.getGeneration()));
                    ps.setInt(2, getIdByValue(con, "CPU_CORE", "CORE_ID", Integer.toString(bean.getCore())));
                    ps.setInt(3, getIdByValue(con, "CPU_THREAD", "THREAD_ID", Integer.toString(bean.getThread())));
                    ps.setInt(4, getIdByValue(con, "CPU_CLOCK", "CLOCK_ID", Double.toString(bean.getClock())));
                    ps.setInt(5, bean.getCpuId());
                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("CPU UPDATE FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("CPU CONNECTION ERROR", e);
        }
    }

    @Override
    public void delete(int id) throws DaoException {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                deleteEntityAndProduct(con, "CPU", "CPU_ID", id);
                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new DaoException("CPU DELETE FAILED", e);
            }

        } catch (Exception e) {
            throw new DaoException("CPU CONNECTION ERROR", e);
        }
    }

    @Override
    protected CpuBean createNewBean(ResultSet rs) throws Exception {
        return new CpuBean(
            rs.getInt("CPU_ID"),
            rs.getInt("PRODUCT_ID"),
            rs.getString("NAME"),
            rs.getInt("PRICE"),
            rs.getInt("STOCK"),
            rs.getString("IMAGE"),
            rs.getInt("MAKER_ID"),
            rs.getString("MAKER_NAME"),
            rs.getString("GENERATION"),
            utils.parseInt(rs.getString("CORE"), 0),
            utils.parseInt(rs.getString("THREAD"), 0),
            utils.parseDouble(rs.getString("CLOCK"), 0.0)
         );
    }
}
