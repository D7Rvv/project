/**
 * 作成：小車
 * 最終変更：3月2日
 * 変更内容：---
 * 概要
 * 　MEMORY用のBean、固有メソッド等はない
 */


package beans;


public class MemoryBean extends ProductBean {

    private int memoryId;
    private String generation;
    private String capacity;

    public MemoryBean() {
        super("memory", 0, "", 0, 0, "", 0, "");
    }

    public MemoryBean(
        int memoryId,
        int productId,
        String name,
        int price,
        int stock,
        String image,
        int makerId,
        String makerName,
        String generation,
        String capacity
    ) {
        super("memory", productId, name, price, stock, image, makerId, makerName);
        this.memoryId = memoryId;
        this.generation = generation;
        this.capacity = capacity;
    }

    public int getMemoryId() { return memoryId; }
    public void setMemoryId(int memoryId) { this.memoryId = memoryId; }

    public String getGeneration() { return generation; }
    public void setGeneration(String generation) { this.generation = generation; }

    public String getCapacity() { return capacity; }
    public void setCapacity(String capacity) { this.capacity = capacity; }
}