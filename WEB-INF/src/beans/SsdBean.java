/**
 * 作成：小車
 * 最終変更：3月2日
 * 変更内容：---
 * 概要
 * 　SSD用のBean、固有メソッド等はない
 */

package beans;


public class SsdBean extends ProductBean {

    private int ssdId;
    private String capacity;
    private String type;

    public SsdBean() {
        super("ssd", 0, "",  0, 0, "", 0, "");
    }

    public SsdBean(
        int ssdId,
        int productId,
        String name,
        int price,
        int stock,
        String image,
        int makerId,
        String makerName,
        String capacity,
        String type
    ) {
        super("ssd", productId, name, price, stock, image, makerId, makerName);
        this.ssdId = ssdId;
        this.capacity = capacity;
        this.type = type;
    }

    public int getSsdId() { return ssdId; }
    public void setSsdId(int ssdId) { this.ssdId = ssdId; }

    public String getCapacity() { return capacity; }
    public void setCapacity(String capacity) { this.capacity = capacity; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}