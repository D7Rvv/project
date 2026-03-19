/**
 * 作成：小車（支援：ChatGPT）
 * 最終変更：2026-03-03
 * 概要
 * 　MAKER用のDao
 *
 */

package dao;

import beans.MakerBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MakerDao extends BaseDao<MakerBean> {

    @Override
    public void insert(MakerBean bean) throws DaoException {
        String sql = """
            INSERT INTO MAKER (VALUE)
            VALUES (?)
            """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"MAKER_ID"})) {

            ps.setString(1, bean.getValue());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    bean.setMakerId(rs.getInt(1));
                }
            }

        } catch (Exception e) {
            throw new DaoException("MAKER INSERT FAILED", e);
        }
    }

    @Override
    public MakerBean selectById(int id) throws DaoException {
        String sql = """
            SELECT MAKER_ID, VALUE
            FROM MAKER
            WHERE MAKER_ID = ?
            """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new MakerBean(
                        rs.getInt("MAKER_ID"),
                        rs.getString("VALUE")
                    );
                }
            }

        } catch (Exception e) {
            throw new DaoException("MAKER SELECT FAILED", e);
        }

        return null;
    }

    @Override
    public List<MakerBean> selectAll() throws DaoException {
        String sql = """
            SELECT MAKER_ID, VALUE
            FROM MAKER
            ORDER BY MAKER_ID
            """;

        List<MakerBean> list = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new MakerBean(
                    rs.getInt("MAKER_ID"),
                    rs.getString("VALUE")
                ));
            }

        } catch (Exception e) {
            throw new DaoException("MAKER SELECT ALL FAILED", e);
        }

        return list;
    }

    @Override
    public void update(MakerBean bean) throws DaoException {
        String sql = """
            UPDATE MAKER
            SET VALUE = ?
            WHERE MAKER_ID = ?
            """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, bean.getValue());
            ps.setInt(2, bean.getMakerId());
            ps.executeUpdate();

        } catch (Exception e) {
            throw new DaoException("MAKER UPDATE FAILED", e);
        }
    }

    @Override
    public void delete(int id) throws DaoException {
        String sql = "DELETE FROM MAKER WHERE MAKER_ID = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new DaoException("MAKER DELETE FAILED", e);
        }
    }

    public Integer selectIdByValue(String value) throws DaoException {
        String sql = "SELECT MAKER_ID FROM MAKER WHERE VALUE = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, value);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("MAKER_ID");
                }
            }

        } catch (Exception e) {
            throw new DaoException("MAKER SELECT ID BY VALUE FAILED", e);
        }

        return null;
    }
}