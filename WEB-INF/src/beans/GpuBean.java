/**
 * 作成：小車
 * 最終変更：3月2日
 * 変更内容：---
 * 概要
 * 　GPU用のBean、固有メソッド等はない
 */

package beans;

public class GpuBean extends ProductBean {

    private int gpuId;
    private String seriesName;
    private String chipName;
    private int vram;

    public GpuBean() {
        super("gpu", 0, "", 0, 0, "", 0, "");
    }

    public GpuBean(
        int gpuId,
        int productId,
        String name,
        int price,
        int stock,
        String image,
        int makerId,
        String makerName,
        String seriesName,
        String chipName,
        int vram
    ) {
        super("gpu", productId, name, price, stock, image, makerId, makerName);
        this.gpuId = gpuId;
        this.seriesName = seriesName;
        this.chipName = chipName;
        this.vram = vram;
    }

    public int getGpuId() { return gpuId; }
    public void setGpuId(int gpuId) { this.gpuId = gpuId; }

    public String getSeriesName() { return seriesName; }
    public void setSeriesName(String seriesName) { this.seriesName = seriesName; }

    public String getChipName() { return chipName; }
    public void setChipName(String chipName) { this.chipName = chipName; }

    public int getVram() { return vram; }
    public void setVram(int vram) { this.vram = vram; }
}
