package beans.option;

import beans.ProductBean;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "optionType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CpuOption.class, name = "CPU"),
    @JsonSubTypes.Type(value = GpuOption.class, name = "GPU"),
    @JsonSubTypes.Type(value = HddOption.class, name = "HDD"),
    @JsonSubTypes.Type(value = MemoryOption.class, name = "MEMORY"),
    @JsonSubTypes.Type(value = MotherBoardOption.class, name = "MOTHER_BOARD"),
    @JsonSubTypes.Type(value = SsdOption.class, name = "SSD")
})
public abstract class ProductOption<T extends ProductBean> {
    protected String optionType;

    protected List<SelectableOption> maker = new ArrayList<>();
    protected RangeOption<Integer> price = new RangeOption<>();

    public abstract List<T> applyOption(List<T> product);
    protected abstract Class<T> getBeanClass();

    protected ProductOption() {
    }

    public ProductOption(String optionType, List<String> makerMap, int priceMinLimit, int priceMaxLimit) {
        this.optionType = optionType;
        this.maker = mapToselectableOption("MAKER", makerMap);
        this.price = new RangeOption<>("PRICE", priceMinLimit, priceMaxLimit);
    }

    public List<ProductBean> filter(List<ProductBean> products) {
        if (products == null) return Collections.emptyList();

        List<T> typedList = new ArrayList<>();
        for (ProductBean p : products) {
            if (getBeanClass().isInstance(p)) {
                typedList.add(getBeanClass().cast(p));
            }
        }

        List<T> filtered = applyOption(typedList);
        return new ArrayList<>(filtered);
    }

    protected final List<SelectableOption> mapToselectableOption(String name, List<String> map) {
        List<SelectableOption> options = new ArrayList<>();
        for (var value : map) {
            SelectableOption option = new SelectableOption(name, value, false);
            options.add(option);
        }
        return options;
    }

    protected final List<T> applyselectableOptions(List<T> product, Function<T, String> getter, List<SelectableOption> options) {
        for (SelectableOption option : options) {
            product = applySelectableOption(product, getter, option);
        }
        return product;
    }

    protected final List<T> applySelectableOption(List<T> product, Function<T, String> getter, SelectableOption option) {
        if (option.isSelected()) {
            product = product.stream()
                .filter(p -> getter.apply(p).equals(option.getValue()))
                .toList();
        }
        return product;
    }

    protected final <N extends Number> List<T> applyRangeOption(List<T> product, Function<T, N> getter, RangeOption<? extends Number> option) {
        if (option.isSet()) {
            double min = option.getMinValue().doubleValue();
            double max = option.getMaxValue().doubleValue();
            product = product.stream()
                .filter(p -> {
                    N value = getter.apply(p);
                    if (value == null) return false;
                    double d = value.doubleValue();
                    return d >= min && d <= max;
                })
                .toList();
        }
        return product;
    }

    protected final <N extends Number> List<T> applyNumberOption(List<T> product, Function<T, N> getter, NumberOption<N> option) {
        if (option.isSet()) {
            N target = option.getValue();
            product = product.stream()
                .filter(p -> {
                    N value = getter.apply(p);
                    return value != null && value.equals(target);
                })
                .toList();
        }
        return product;
    }

    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }

    public List<SelectableOption> getMaker() { return maker; }
    public void setMaker(List<SelectableOption> maker) { this.maker = maker; }

    public RangeOption<Integer> getPrice() { return price; }
    public void setPrice(RangeOption<Integer> price) { this.price = price; }
}