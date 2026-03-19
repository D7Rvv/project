/**
 * 作成：小車
 * 最終変更：3月2日
 * 変更内容：---
 * 概要
 * 　USER用のDao
 * 　checkLogin(int userId | String userName, String password)
 * 　でパスワードが一致している場合のみbeanを返す、不一致であれば独自例外を送出
 */

package dao;

import beans.UserBean;
import helpers.common.PasswordHasherException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao extends BaseDao<UserBean> {

    public UserDao() {
        super();
    }

    @Override
    public void insert(UserBean bean) throws Exception {
        String sql = """
            INSERT INTO USER_DATA (SALT, HASH, NAME, ADDRESS)
            VALUES (?, ?, ?, ?)
        """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, bean.getSalt());
            ps.setString(2, bean.getHash());
            ps.setString(3, bean.getName());
            ps.setString(4, bean.getAddress());

            ps.executeUpdate();
        }
    }

    @Override
    public List<UserBean> selectAll() throws Exception {
        String sql = "SELECT * FROM USER_DATA";
        List<UserBean> list = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UserBean bean = new UserBean(
                        rs.getInt("USER_ID"),
                        rs.getString("SALT"),
                        rs.getString("HASH"),
                        rs.getString("NAME"),
                        rs.getString("ADDRESS")
                );
                list.add(bean);
            }
        }
        return list;
    }

    @Override
    public UserBean selectById(int userId) throws Exception {
        String sql = """
            SELECT
                *
            FROM
                USER_DATA
            WHERE
                USER_ID = ?
        """;
        UserBean bean = null;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                bean = new UserBean(
                        rs.getInt("USER_ID"),
                        rs.getString("SALT"),
                        rs.getString("HASH"),
                        rs.getString("NAME"),
                        rs.getString("ADDRESS")
                );
            }
        }
        return bean;
    }

    @Override
    public void update(UserBean bean) throws Exception {
        String sql = """
        UPDATE
            USER_DATA
        SET
            NAME = ?,
            ADDRESS = ?
            SALT = ?
            HASH = ?
        WHERE
            USER_ID = ?
        """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, bean.getName());
            ps.setString(2, bean.getAddress());
            ps.setString(3, bean.getSalt());
            ps.setString(4, bean.getHash());
            ps.setInt(5, bean.getUserId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int userId) throws Exception {
        String sql = """
            DELETE FROM
                USER_DATA
            WHERE
                USER_ID = ?
        """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new DaoException("ユーザー削除0件でした（USER_ID=" + userId + "）");
            }
        }
    }

    public UserBean checkLogin(int userId, String password) throws Exception, SQLException, DaoException, PasswordHasherException {
        String sql = """
            SELECT
                *
            FROM
                USER_DATA
            WHERE
                USER_ID = ?
        """;

        UserBean user = null;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new UserBean(
                        rs.getInt("USER_ID"),
                        rs.getString("SALT"),
                        rs.getString("HASH"),
                        rs.getString("NAME"),
                        rs.getString("ADDRESS")
                );
            }

            if (user != null && user.checkPassword(password)) {
                return user;
            } else {
                throw new DaoException("ユーザー名またはパスワードが間違っています");
            }
        }
    }

    public UserBean checkLogin(String userName, String password) throws Exception {

        String sql = "SELECT USER_ID FROM USER_DATA WHERE name = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userName);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    int id = rs.getInt("USER_ID");

                    return checkLogin(id, password);
                }
            }
        }

        return null;
    }
        public boolean isExistUser(String userName) throws Exception {
        // COUNT(*) で、該当する名前が何件あるか数える
        String sql = "SELECT COUNT(*) FROM USER_DATA WHERE NAME = ?";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 1件以上見つかったら true (存在する)、0件なら false
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

}