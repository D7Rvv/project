package beans.option;

import java.util.ArrayList;
import java.util.List;

import beans.GpuBean;

public class GpuOption extends ProductOption<GpuBean> {
    protected List<SelectableOption> series = new ArrayList<>();
    protected List<SelectableOption> chip = new ArrayList<>();
    protected RangeOption<Integer> vram = new RangeOption<>();

    public GpuOption() {
        super();
    }

    public GpuOption(
        List<String> makerMap,
        List<String> seriesMap,
        List<String> chipMap,
        int priceMinLimit,
        int priceMaxLimit,
        int vramMinLimit,
        int vramMaxLimit
    ) {
        super("GPU", makerMap, priceMinLimit, priceMaxLimit);
        this.series = mapToselectableOption("GPU_SERIES", seriesMap);
        this.chip = mapToselectableOption("GPU_CHIP", chipMap);
        this.vram = new RangeOption<>("VRAM", vramMinLimit, vramMaxLimit);
    }

    @Override
    protected Class<GpuBean> getBeanClass() {
        return GpuBean.class;
    }

    @Override
    public List<GpuBean> applyOption(List<GpuBean> product) {
        product = applyselectableOptions(product, GpuBean::getMakerName, maker);
        product = applyselectableOptions(product, GpuBean::getSeriesName, series);
        product = applyselectableOptions(product, GpuBean::getChipName, chip);

        product = applyRangeOption(product, GpuBean::getPrice, price);
        product = applyRangeOption(product, GpuBean::getVram, vram);

        return product;
    }

    public List<SelectableOption> getSeries() { return series; }
    public void setSeries(List<SelectableOption> series) { this.series = series; }
    public List<SelectableOption> getChip() { return chip; }
    public void setChip(List<SelectableOption> chip) { this.chip = chip; }
    public RangeOption<Integer> getVram() { return vram; }
    public void setVram(RangeOption<Integer> vram) { this.vram = vram; }
}