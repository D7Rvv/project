/**
 * 作成：小車
 * 最終変更：3月2日
 * 変更内容：---
 * 概要
 * 　USER用のBean、checkPassword(String) : boolでパスワードの照合ができる
 */

package beans;

import helpers.common.PasswordHasher;
import helpers.common.PasswordHasherException;
import java.io.Serializable;

public class UserBean implements Serializable {

    private int userId;
    private String salt;
    private String hash;
    private String name;
    private String address;

    public UserBean(){}

    public UserBean(int userId, String salt, String hash, String name, String address){
        this.userId = userId;
        this.salt = salt;
        this.hash = hash;
        this.name = name;
        this.address = address;
    }

    public UserBean(String name, String address, String password) throws PasswordHasherException, Exception{
        this.salt = PasswordHasher.getSalt();
        this.hash = PasswordHasher.hash(password, this.salt);
        this.name = name;
        this.address = address;
    }

    public int getUserId(){ return userId; }
    public void setUserId(int userId){ this.userId = userId; }

    public String getSalt(){ return salt; }
    public void setSalt(String salt){ this.salt = salt; }

    public String getHash(){ return hash; }
    public void setHash(String hash){ this.hash = hash; }

    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }

    public String getAddress(){ return address; }
    public void setAddress(String address){ this.address = address; }

    public boolean checkPassword(String password) throws PasswordHasherException, Exception{
        return PasswordHasher.equalPasswords(this.hash, this.salt, password);
    }
}