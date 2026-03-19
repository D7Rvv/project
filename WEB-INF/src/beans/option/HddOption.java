package beans.option;

import java.util.ArrayList;
import java.util.List;

import beans.HddBean;

public class HddOption extends ProductOption<HddBean> {
    protected List<SelectableOption> capacity = new ArrayList<>();
    protected List<SelectableOption> rpm = new ArrayList<>();

    public HddOption() {
        super();
    }

    public HddOption(
        List<String> makerMap,
        List<String> capacityMap,
        List<String> rpmMap,
        int priceMinLimit,
        int priceMaxLimit
    ) {
        super("HDD", makerMap, priceMinLimit, priceMaxLimit);
        this.capacity = mapToselectableOption("HDD_CAP", capacityMap);
        this.rpm = mapToselectableOption("HDD_RPM", rpmMap);
    }

    @Override
    protected Class<HddBean> getBeanClass() {
        return HddBean.class;
    }

    @Override
    public List<HddBean> applyOption(List<HddBean> product) {
        product = applyselectableOptions(product, HddBean::getMakerName, maker);
        product = applyselectableOptions(product, HddBean::getCapacity, capacity);
        product = applyselectableOptions(product, HddBean::getRpm, rpm);

        product = applyRangeOption(product, HddBean::getPrice, price);

        return product;
    }

    public List<SelectableOption> getCapacity() { return capacity; }
    public void setCapacity(List<SelectableOption> capacity) { this.capacity = capacity; }
    public List<SelectableOption> getRpm() { return rpm; }
    public void setRpm(List<SelectableOption> rpm) { this.rpm = rpm; }
}