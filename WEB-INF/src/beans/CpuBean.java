/**
 * 作成：小車
 * 最終変更：3月2日
 * 変更内容：---
 * 概要
 * 　CPU用のBean、固有メソッド等はない
 */

package beans;

public class CpuBean extends ProductBean {

    private int cpuId;
    private String generation;
    private int core;
    private int thread;
    private double clock;

    public CpuBean() {
        super("cpu", 0, "", 0, 0, "", 0, "");
    }

    public CpuBean(
        int cpuId,
        int productId,
        String name,
        int price,
        int stock,
        String image,
        int makerId,
        String makerName,
        String generation,
        int core,
        int thread,
        double clock
    ) {
        super("cpu", productId, name, price, stock, image, makerId, makerName);
        this.cpuId = cpuId;
        this.generation = generation;
        this.core = core;
        this.thread = thread;
        this.clock = clock;
    }

    public int getCpuId() { return cpuId; }
    public void setCpuId(int cpuId) { this.cpuId = cpuId; }

    public String getGeneration() { return generation; }
    public void setGeneration(String generation) { this.generation = generation; }

    public int getCore() { return core; }
    public void setCore(int core) { this.core = core; }

    public int getThread() { return thread; }
    public void setThread(int thread) { this.thread = thread; }

    public double getClock() { return clock; }
    public void setClock(double clock) { this.clock = clock; }
}
