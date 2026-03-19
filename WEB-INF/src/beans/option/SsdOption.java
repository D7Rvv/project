package beans.option;

import java.util.ArrayList;
import java.util.List;

import beans.SsdBean;

public class SsdOption extends ProductOption<SsdBean> {
    protected List<SelectableOption> capacity = new ArrayList<>();
    protected List<SelectableOption> type = new ArrayList<>();

    public SsdOption() {
        super();
    }

    public SsdOption(
        List<String> makerMap,
        List<String> capacityMap,
        List<String> typeMap,
        int priceMinLimit,
        int priceMaxLimit
    ) {
        super("SSD", makerMap, priceMinLimit, priceMaxLimit);
        this.capacity = mapToselectableOption("SSD_CAP", capacityMap);
        this.type = mapToselectableOption("SSD_TYPE", typeMap);
    }

    @Override
    protected Class<SsdBean> getBeanClass() {
        return SsdBean.class;
    }

    @Override
    public List<SsdBean> applyOption(List<SsdBean> product) {
        product = applyselectableOptions(product, SsdBean::getMakerName, maker);
        product = applyselectableOptions(product, SsdBean::getCapacity, capacity);
        product = applyselectableOptions(product, SsdBean::getType, type);

        product = applyRangeOption(product, SsdBean::getPrice, price);

        return product;
    }

    public List<SelectableOption> getCapacity() { return capacity; }
    public void setCapacity(List<SelectableOption> capacity) { this.capacity = capacity; }
    public List<SelectableOption> getType() { return type; }
    public void setType(List<SelectableOption> type) { this.type = type; }
}