/**
 * 作成：小車
 * 最終変更：3月3日
 * 変更内容：---
 * 概要
 * 　Maker用のBean、固有メソッド等はない
 */

package beans;

import java.io.Serializable;

public class MakerBean implements Serializable {

    private int makerId;
    private String value;

    public MakerBean() {}

    public MakerBean(int makerId, String value) {
        this.makerId = makerId;
        this.value = value;
    }

    public int getMakerId() {
        return makerId;
    }

    public void setMakerId(int makerId) {
        this.makerId = makerId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}