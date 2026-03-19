/**
 * 作成：小車
 * 最終変更：3月2日
 * 変更内容：---
 * 概要
 * 　商品用の抽象クラス、商品共通のメソッド等を実装。
 */

package beans;

import java.io.Serializable;

public abstract class ProductBean implements Serializable {

    protected int productId;
    protected String name;
    protected int price;
    protected int stock;
    protected String image;
    protected int makerId;
    protected String makerName;
    protected String productType;

    public ProductBean() {}

    public ProductBean(
        String productType,
        int productId,
        String name,
        int price,
        int stock,
        String image,
        int makerId,
        String makerName
    ) {
        this.productType = productType;
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.image = image;
        this.makerId = makerId;
        this.makerName = makerName;
    }

    public ProductBean(
        int productId,
        String name,
        int price,
        int stock,
        String image,
        int makerId,
        String makerName
    ) {
        this(null, productId, name, price, stock, image, makerId, makerName);
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImageId() {
        return image;
    }

    public void setImageId(String image) {
        this.image = image;
    }

    public int getMakerId() {
        return makerId;
    }

    public void setMakerId(int makerId) {
        this.makerId = makerId;
    }

    public String getMakerName() {
        return makerName;
    }

    public void setMakerName(String makerName) {
        this.makerName = makerName;
    }

    public String getProductType() {
        return productType;
    }
}