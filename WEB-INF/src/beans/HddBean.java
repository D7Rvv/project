/**
 * 作成：小車
 * 最終変更：3月2日
 * 変更内容：---
 * 概要
 * 　HDD用のBean、固有メソッド等はない
 */

package beans;


public class HddBean extends ProductBean {

    private int hddId;
    private String capacity;
    private String rpm;

    public HddBean() {
        super("hdd", 0, "",  0, 0, "", 0, "");
    }

    public HddBean(
        int hddId,
        int productId,
        String name,
        int price,
        int stock,
        String image,
        int makerId,
        String makerName,
        String capacity,
        String rpm
    ) {
        super("hdd", productId, name, price, stock, image, makerId, makerName);
        this.hddId = hddId;
        this.capacity = capacity;
        this.rpm = rpm;
    }

    public int getHddId() { return hddId; }
    public void setHddId(int hddId) { this.hddId = hddId; }

    public String getCapacity() { return capacity; }
    public void setCapacity(String capacity) { this.capacity = capacity; }

    public String getRpm() { return rpm; }
    public void setRpm(String rpm) { this.rpm = rpm; }
}