package beans.option;

import java.util.ArrayList;
import java.util.List;

import beans.CpuBean;

public class CpuOption extends ProductOption<CpuBean> {
    protected List<SelectableOption> generation = new ArrayList<>();
    protected RangeOption<Integer> core = new RangeOption<>();
    protected RangeOption<Integer> thread = new RangeOption<>();
    protected RangeOption<Double> clock = new RangeOption<>();

    public CpuOption() {
        super();
    }

    public CpuOption(
        List<String> makerMap,
        List<String> generationMap,
        int priceMinLimit,
        int priceMaxLimit,
        int coreMinLimit,
        int coreMaxLimit,
        int threadMinLimit,
        int threadMaxLimit,
        double clockMinLimit,
        double clockMaxLimit
    ) {
        super("CPU", makerMap, priceMinLimit, priceMaxLimit);
        this.generation = mapToselectableOption("CPU_GEN", generationMap);
        this.core = new RangeOption<>("CORE", coreMinLimit, coreMaxLimit);
        this.thread = new RangeOption<>("THREAD", threadMinLimit, threadMaxLimit);
        this.clock = new RangeOption<>("CLOCK", clockMinLimit, clockMaxLimit);
    }

    @Override
    protected Class<CpuBean> getBeanClass() {
        return CpuBean.class;
    }

    @Override
    public List<CpuBean> applyOption(List<CpuBean> product) {
        product = applyselectableOptions(product, CpuBean::getMakerName, maker);
        product = applyselectableOptions(product, CpuBean::getGeneration, generation);

        product = applyRangeOption(product, CpuBean::getPrice, price);
        product = applyRangeOption(product, CpuBean::getCore, core);
        product = applyRangeOption(product, CpuBean::getThread, thread);
        product = applyRangeOption(product, CpuBean::getClock, clock);

        return product;
    }

    public List<SelectableOption> getGeneration() { return generation; }
    public void setGeneration(List<SelectableOption> generation) { this.generation = generation; }
    public RangeOption<Integer> getCore() { return core; }
    public void setCore(RangeOption<Integer> core) { this.core = core; }
    public RangeOption<Integer> getThread() { return thread; }
    public void setThread(RangeOption<Integer> thread) { this.thread = thread; }
    public RangeOption<Double> getClock() { return clock; }
    public void setClock(RangeOption<Double> clock) { this.clock = clock; }
}