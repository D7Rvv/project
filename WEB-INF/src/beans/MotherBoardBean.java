/**
 * 作成：小車
 * 最終変更：3月2日
 * 変更内容：---
 * 概要
 * 　MotherBoard用のBean、固有メソッド等はない
 */

package beans;


public class MotherBoardBean extends ProductBean {

    private int motherBoardId;
    private String chipset;
    private String size;

    public MotherBoardBean() {
        super("motherboard", 0, "",  0, 0, "", 0, "");
    }

    public MotherBoardBean(
        int motherBoardId,
        int productId,
        String name,
        int price,
        int stock,
        String image,
        int makerId,
        String makerName,
        String chipset,
        String size
    ) {
        super("motherboard", productId, name, price, stock, image, makerId, makerName);
        this.motherBoardId = motherBoardId;
        this.chipset = chipset;
        this.size = size;
    }

    public int getMotherBoardId() { return motherBoardId; }
    public void setMotherBoardId(int motherBoardId) { this.motherBoardId = motherBoardId; }

    public String getChipset() { return chipset; }
    public void setChipset(String chipset) { this.chipset = chipset; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
}