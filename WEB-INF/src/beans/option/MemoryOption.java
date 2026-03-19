package beans.option;

import java.util.ArrayList;
import java.util.List;
import beans.MemoryBean;

public class MemoryOption extends ProductOption<MemoryBean> {
    protected List<SelectableOption> generation = new ArrayList<>();
    protected List<SelectableOption> capacity = new ArrayList<>();

    public MemoryOption() {
        super();
    }

    public MemoryOption(
        List<String> makerMap,
        List<String> generationMap,
        List<String> capacityMap,
        int priceMinLimit,
        int priceMaxLimit
    ) {
        super("MEMORY", makerMap, priceMinLimit, priceMaxLimit);
        this.generation = mapToselectableOption("MEMORY_GEN", generationMap);
        this.capacity = mapToselectableOption("MEMORY_CAP", capacityMap);
    }

    @Override
    protected Class<MemoryBean> getBeanClass() {
        return MemoryBean.class;
    }

    @Override
    public List<MemoryBean> applyOption(List<MemoryBean> product) {
        product = applyselectableOptions(product, MemoryBean::getMakerName, maker);
        product = applyselectableOptions(product, MemoryBean::getGeneration, generation);
        product = applyselectableOptions(product, MemoryBean::getCapacity, capacity);

        product = applyRangeOption(product, MemoryBean::getPrice, price);

        return product;
    }

    public List<SelectableOption> getGeneration() { return generation; }
    public void setGeneration(List<SelectableOption> generation) { this.generation = generation; }
    public List<SelectableOption> getCapacity() { return capacity; }
    public void setCapacity(List<SelectableOption> capacity) { this.capacity = capacity; }
}