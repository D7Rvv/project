package beans.option;

import java.util.ArrayList;
import java.util.List;

import beans.MotherBoardBean;

public class MotherBoardOption extends ProductOption<MotherBoardBean> {
    protected List<SelectableOption> chipset = new ArrayList<>();
    protected List<SelectableOption> size = new ArrayList<>();

    public MotherBoardOption() {
        super();
    }

    public MotherBoardOption(
        List<String> makerMap,
        List<String> chipsetMap,
        List<String> sizeMap,
        int priceMinLimit,
        int priceMaxLimit
    ) {
        super("MOTHER_BOARD", makerMap, priceMinLimit, priceMaxLimit);
        this.chipset = mapToselectableOption("MOTHERBOARD_CHIPSET", chipsetMap);
        this.size = mapToselectableOption("MOTHERBOARD_SIZE", sizeMap);
    }

    @Override
    protected Class<MotherBoardBean> getBeanClass() {
        return MotherBoardBean.class;
    }

    @Override
    public List<MotherBoardBean> applyOption(List<MotherBoardBean> product) {
        product = applyselectableOptions(product, MotherBoardBean::getMakerName, maker);
        product = applyselectableOptions(product, MotherBoardBean::getChipset, chipset);
        product = applyselectableOptions(product, MotherBoardBean::getSize, size);

        product = applyRangeOption(product, MotherBoardBean::getPrice, price);

        return product;
    }

    public List<SelectableOption> getChipset() { return chipset; }
    public void setChipset(List<SelectableOption> chipset) { this.chipset = chipset; }
    public List<SelectableOption> getSize() { return size; }
    public void setSize(List<SelectableOption> size) { this.size = size; }
}   