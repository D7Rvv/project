/**
 * 作成：小車
 * 最終変更：3月2日
 * 変更内容：---
 * 概要
 * 　Dao用の例外クラス
 */

package dao;

public class DaoException extends Exception {

    public DaoException(String msg) {
        super(msg);
    }

    public DaoException(String msg, Throwable cause) {
        super(msg, cause);
    }
}