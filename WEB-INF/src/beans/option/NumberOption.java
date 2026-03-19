package beans.option;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NumberOption<T extends Number> extends RangeOption<T> {

    public NumberOption() {
        super();
    }

    public NumberOption(String name, T minLimit, T maxLimit) {
        super(name, minLimit, maxLimit, minLimit);
    }

    public NumberOption(String name, T minLimit, T maxLimit, T defaultValue) {
        super(name, minLimit, maxLimit, defaultValue);
    }

    @JsonProperty("numberValue")
    public T getNumberValue() {
        return getMinValue();
    }

    @JsonProperty("numberValue")
    @JsonAlias("value")
    public void setNumberValue(T numberValue) {
        if (numberValue == null) {
            setMinValue(null);
            setMaxValue(null);
            return;
        }

        setMinValue(numberValue);
        setMaxValue(numberValue);
    }

    @JsonIgnore
    public T getValue() {
        return getMinValue();
    }

    @JsonIgnore
    public void setValue(T value) {
        setNumberValue(value);
    }
}