/**
 * 作成：小車
 * 最終変更：3月2日
 * 変更内容：---
 * 概要
 * 　Dao全般のベースとなる抽象クラス、必要なメソッドとgetConnectionを実装。
 */


package dao;

import java.sql.Connection;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public abstract class BaseDao<T> {

    private static volatile DataSource ds;

    protected Connection getConnection() throws Exception {
        synchronized (BaseDao.class) {
            if (ds == null) {
                InitialContext ic = new InitialContext();
                ds = (DataSource) ic.lookup("java:/comp/env/jdbc/resource");
            }
        }
        return ds.getConnection();
    }

    public abstract void insert(T bean) throws Exception;

    public abstract T selectById(int id) throws Exception;

    public abstract java.util.List<T> selectAll() throws Exception;

    public abstract void update(T bean) throws Exception;

    public abstract void delete(int id) throws Exception;
}