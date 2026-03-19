package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class utils{

    private static boolean debugMode = false;

    public static final boolean isDebugMode() {
        return debugMode;
    }

    public static void debugPrint(String label, Object value) {
        debugPrint(label, value, false);
    }

    public static void debugPrint(String label, Object value, boolean force) {

        //オブジェクトは200文字以上は省略して表示
        if (value != null && value.toString().length() > 200 && !force) {
            value = value.toString().substring(0, 200) + "...(length=" + value.toString().length() + ")";
        }
        
        if (isDebugMode()) {
            System.out.println(label + ": " + value);
        }
    }

    public static String nvl(String s, String def) {
        return (s == null) ? def : s;
    }

    public static int parseInt(String s, int def) {
        try {
            if (s == null || s.isBlank()) return def;
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static double parseDouble(String s, double def) {
        try {
            if (s == null || s.isBlank()) return def;
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static int parseIntField(String s, String field) {
        if (isEmpty(s)) {
            throw new IllegalArgumentException(field + " が空です");
        }
        int value = parseInt(s, Integer.MIN_VALUE);
        if (value == Integer.MIN_VALUE) {
            throw new IllegalArgumentException(field + " が数値ではありません: " + s);
        }
        return value;
    }

    public static double parseDoubleField(String s, String field) {
        if (isEmpty(s)) {
            throw new IllegalArgumentException(field + " が空です");
        }
        double value = parseDouble(s, Double.NaN);
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException(field + " が数値ではありません: " + s);
        }
        return value;
    }

    public static int[] listToIntArray(java.util.List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return new int[0];
        }
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty() || s.isBlank();
    }
    
    /**
     * jacksonでJsonに変換する
     */
    public static String toJson(Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON変換に失敗", e);
        }
    }

    /**
    * jacksonでJsonからオブジェクトに変換する
    */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON変換に失敗", e);
        }
    }

    /**
     * jacksonでJsonからジェネリック型を含むオブジェクトに変換する
     */
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON変換に失敗", e);
        }
    }
}