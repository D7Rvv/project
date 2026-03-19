/**
 * JSON/APIでオプションの情報をやりとりするサーブレット
 *
 *  - GET/POST いずれでも動作
 *  - JSON読み書きの共通処理をまとめる
 *  - actionパラメータで振り分ける
 */

package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import beans.ProductBean;
import beans.option.CpuOption;
import beans.option.GpuOption;
import beans.option.HddOption;
import beans.option.MemoryOption;
import beans.option.MotherBoardOption;
import beans.option.ProductOption;
import beans.option.SsdOption;
import dao.OptionDao;
import dao.SearchDao;

import com.fasterxml.jackson.core.type.TypeReference;

import utils.utils;

@WebServlet("/Servlet/API/Option")
public class ProductServletAPI extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String requestId = createRequestId();

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        String rawBody = "";
        Map<String, Object> requestData = null;

        try {
            rawBody = readBody(request);

            requestData = readJson(rawBody, new TypeReference<Map<String, Object>>() {});
            if (requestData == null) {
                requestData = new HashMap<>();
            }
            String action = utils.nvl(asString(requestData.get("action")), "ping");
            String type = utils.nvl(asString(requestData.get("type")), "all");
            
            System.out.println("API受信： action = " + action + ", requestId = " + requestId);

            switch (action) {
                case "ping": {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("ok", true);
                    m.put("requestId", requestId);
                    writeJson(response, m);
                    break;
                }

                case "getOptions": {

                    Map<String, ProductOption<?>> result = new OptionDao().getOptions(type);

                    writeJson(response, result);
                    break;
                }

                case "search": {

                    String searchText = utils.nvl(asString(requestData.get("searchText")), "");
                    int maxResults = toInt(requestData.get("maxResults"), 0);
                    int offset = toInt(requestData.get("offset"), 0);
                    Object rawOptions = requestData.get("options");


                    Map<String, ProductOption<?>> options = parseOptionsSafely(rawOptions, requestId);

                    SearchDao sDao = new SearchDao();
                    List<ProductBean> products;
                    int totalCount;

                    if (type == null || type.isBlank() || "all".equalsIgnoreCase(type)) {

                        products = sDao.search(searchText, options, maxResults, offset);
                        totalCount = sDao.countSearch(searchText, options);
                    } else {

                        products = sDao.searchByType(type, searchText, options, maxResults, offset);
                        totalCount = sDao.countSearchByType(type, searchText, options);
                    }

                    Map<String, Object> responseBody = buildSearchResponse(products, offset, maxResults, totalCount);

                    writeJson(response, responseBody);
                    break;
                }

                case "getProduct": {

                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("error", "getProduct is not implemented");
                    m.put("requestId", requestId);

                    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    writeJson(response, m);
                    break;
                }

                default: {

                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("error", "unknown action: " + action);
                    m.put("requestId", requestId);

                    writeJson(response, m);
                    break;
                }
            }

        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("error", e.getMessage());
            m.put("errorType", e.getClass().getName());
            m.put("requestId", requestId);

            writeJson(response, m);
        }
    }

    private Map<String, Object> buildSearchResponse(List<ProductBean> products, int offset, int maxResults, int totalCount) {
        int safeOffset = Math.max(0, offset);
        int safeTotalCount = Math.max(0, totalCount);
        int resultCount = products == null ? 0 : products.size();

        int totalPage;
        int retultPage;

        if (maxResults > 0) {
            totalPage = safeTotalCount == 0 ? 0 : (int) Math.ceil((double) safeTotalCount / maxResults);
            retultPage = safeTotalCount == 0 ? 0 : (safeOffset / maxResults) + 1;
            if (totalPage > 0 && retultPage > totalPage) {
                retultPage = totalPage;
            }
        } else {
            totalPage = safeTotalCount == 0 ? 0 : 1;
            retultPage = safeTotalCount == 0 ? 0 : 1;
        }

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("offset", safeOffset);
        info.put("resultCount", resultCount);
        info.put("totalCount", safeTotalCount);
        info.put("retultPage", retultPage);
        info.put("totalPage", totalPage);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("products", products);
        response.put("info", info);

        return response;
    }

    private Map<String, ProductOption<?>> parseOptionsSafely(Object rawOptions, String requestId) {
        try {
            return parseOptions(rawOptions, requestId);
        } catch (Exception e) {
            utils.debugPrint("[parseOptionsSafely] requestId", requestId);
            utils.debugPrint("[parseOptionsSafely] rawOptions", rawOptions);
            utils.debugPrint("[parseOptionsSafely] error", e.getClass().getName() + ": " + e.getMessage());
            return null;
        }
    }

    private Map<String, ProductOption<?>> parseOptions(Object rawOptions, String requestId) throws IOException {
        if (rawOptions == null) {
            return null;
        }
    
        String json = utils.toJson(rawOptions);
        if (json == null || json.isBlank() || "null".equals(json)) {
            return null;
        }
    
        Map<String, Object> rawMap = utils.fromJson(
            json,
            new TypeReference<Map<String, Object>>() {}
        );
    
        if (rawMap == null || rawMap.isEmpty()) {
            return null;
        }
    
        Map<String, ProductOption<?>> parsed = new LinkedHashMap<>();
    
        for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
        
            if (value == null) {
                continue;
            }
        
            String optionJson = utils.toJson(value);
            ProductOption<?> option = parseSingleOption(key, optionJson);
        
            if (option != null) {
                parsed.put(key, option);
            }
        }
    
        utils.debugPrint("[parseOptions] requestId", requestId);
        utils.debugPrint("[parseOptions] parsedKeys", parsed.keySet());
    
        return parsed.isEmpty() ? null : parsed;
    }
    
    private ProductOption<?> parseSingleOption(String key, String optionJson) throws IOException {
        if (key == null || key.isBlank() || optionJson == null || optionJson.isBlank()) {
            return null;
        }
    
        return switch (key) {
            case "cpu" -> utils.fromJson(optionJson, CpuOption.class);
            case "gpu" -> utils.fromJson(optionJson, GpuOption.class);
            case "memory" -> utils.fromJson(optionJson, MemoryOption.class);
            case "mother_board" -> utils.fromJson(optionJson, MotherBoardOption.class);
            case "ssd" -> utils.fromJson(optionJson, SsdOption.class);
            case "hdd" -> utils.fromJson(optionJson, HddOption.class);
            default -> {
                utils.debugPrint("[parseSingleOption] unknownKey", key);
                yield null;
            }
        };
    }

    private int toInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void writeJson(HttpServletResponse response, Object value) throws IOException {
        String json = utils.toJson(value);
        response.getWriter().write(json);
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }

        return sb.toString();
    }

    private <T> T readJson(String body, TypeReference<T> typeRef) throws IOException {
        if (body == null || body.isBlank()) {
            return null;
        }
        return utils.fromJson(body, typeRef);
    }

    private String createRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }
}