package beans.option;

/**
 * 選択式のオプションを表すクラス。
 * 例えば、CPUのメーカーや世代など、複数の選択肢から選ぶタイプのオプションに使用される。
 * id: データベースなどで一意に識別するためのID
 * value: ユーザーに表示される選択肢の値
 * isSelected: ユーザーがこの選択肢を選択しているかどうかを示すフラグ
 */
import com.fasterxml.jackson.annotation.JsonProperty;

public class SelectableOption extends BaseOption{
    protected int id;
    protected String value;
    protected boolean isSelected;
    
    public SelectableOption() {}

    public SelectableOption(String name, String value, boolean isSelected) {
        this.name = name;
        this.value = value;
        this.isSelected = isSelected;
    }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    @JsonProperty("isSelected")
    public boolean isSelected() { return isSelected; }

    @JsonProperty("isSelected")
    public void setSelected(boolean isSelected) { this.isSelected = isSelected; }
}
