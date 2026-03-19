package beans.option;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 数値の範囲を指定するオプションを表すクラス。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RangeOption<T extends Number> extends BaseOption {

    protected T minValue;
    protected T maxValue;

    protected T minLimit;
    protected T maxLimit;

    public RangeOption() {
        // Jackson deserialization only
    }

    public RangeOption(String name, T minLimit, T maxLimit) {
        this(name, minLimit, maxLimit, minLimit, maxLimit);
    }

    public RangeOption(String name, T minLimit, T maxLimit, T defaultValue) {
        this(name, minLimit, maxLimit, defaultValue, defaultValue);
    }

    public RangeOption(String name, T minLimit, T maxLimit, T minValue, T maxValue) {
        this.name = name;
        this.minLimit = minLimit;
        this.maxLimit = maxLimit;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @JsonIgnore
    public boolean isSet() {
        return minValue != null && maxValue != null;
    }

    public T getMinValue() {
        return minValue;
    }

    public void setMinValue(T minValue) {
        this.minValue = minValue;
    }

    public T getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(T maxValue) {
        this.maxValue = maxValue;
    }

    public T getMinLimit() {
        return minLimit;
    }

    public void setMinLimit(T minLimit) {
        this.minLimit = minLimit;
    }

    public T getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(T maxLimit) {
        this.maxLimit = maxLimit;
    }
}