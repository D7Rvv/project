package helpers.common;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class CookieMgr {

    private static final String DEFAULT_PATH = "/";

    private static boolean defaultHttpOnly = true;
    private static boolean defaultSecure = false;

    private CookieMgr() {}

    public static void setDefaultHttpOnly(boolean httpOnly) {
        defaultHttpOnly = httpOnly;
    }

    public static void setDefaultSecure(boolean secure) {
        defaultSecure = secure;
    }

    public static void setDefaults(boolean httpOnly, boolean secure) {
        defaultHttpOnly = httpOnly;
        defaultSecure = secure;
    }

    public static boolean isDefaultHttpOnly() {
        return defaultHttpOnly;
    }

    public static boolean isDefaultSecure() {
        return defaultSecure;
    }

    /**
     * Cookieを作成する
     * @param name Cookie名
     * @param value Cookie値
     */
    public static Cookie create(String name, String value) {
        return create(name, value, -1, DEFAULT_PATH, defaultHttpOnly, defaultSecure);
    }

    /**
     * Cookieを作成する
     * @param name Cookie名
     * @param value Cookie値
     * @param maxAge 有効期限（秒）-1でセッションクッキー、0で削除
     */
    public static Cookie create(String name, String value, int maxAge) {
        return create(name, value, maxAge, DEFAULT_PATH, defaultHttpOnly, defaultSecure);
    }

    /**
     * Cookieを作成する
     * @param name Cookie名
     * @param value Cookie値
     * @param maxAge 有効期限（秒）-1でセッションクッキー、0で削除
     * @param path Cookieの有効パス
     */
    public static Cookie create(String name, String value, int maxAge, String path) {
        return create(name, value, maxAge, path, defaultHttpOnly, defaultSecure);
    }

    /**
     * Cookieを作成する
     * @param name Cookie名
     * @param value Cookie値
     * @param maxAge 有効期限（秒）-1でセッションクッキー、0で削除
     * @param path Cookieの有効パス
     * @param httpOnly JavaScriptからアクセス不可にするか
     * @param secure HTTPS通信時のみ送信するか
     */
    public static Cookie create(String name, String value, int maxAge, String path, boolean httpOnly, boolean secure) {
        validateName(name);

        String safeValue = encode(value);
        Cookie cookie = new Cookie(name, safeValue);

        cookie.setMaxAge(maxAge);
        cookie.setPath(path == null || path.isBlank() ? DEFAULT_PATH : path);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);

        return cookie;
    }

    /**
     * Cookieをレスポンスに追加する
     * @param cookie 追加するCookie
     * @param response HttpServletResponseオブジェクト
     */
    public static void add(HttpServletResponse response, Cookie cookie) {
        if (response == null) {
            throw new IllegalArgumentException("response が null です。");
        }
        if (cookie == null) {
            throw new IllegalArgumentException("cookie が null です。");
        }
        response.addCookie(cookie);
    }

    /**
     * Cookieを作成してレスポンスに追加する
     * セッションクッキー（ブラウザを閉じるまで有効）
     * @param response HttpServletResponseオブジェクト
     * @param name Cookie名
     * @param value Cookie値
     */
    public static void add(HttpServletResponse response, String name, String value) {
        response.addCookie(create(name, value));
    }

    /**
     * Cookieを作成してレスポンスに追加する
     * @param response HttpServletResponseオブジェクト
     * @param name Cookie名
     * @param value Cookie値
     * @param maxAge 有効期限（秒）-1でセッションクッキー、0で削除
     */
    public static void add(HttpServletResponse response, String name, String value, int maxAge) {
        response.addCookie(create(name, value, maxAge));
    }

    /**
     * Cookieを作成してレスポンスに追加する
     * @param response HttpServletResponseオブジェクト
     * @param name Cookie名
     * @param value Cookie値
     * @param maxAge 有効期限（秒）-1でセッションクッキー、0で削除
     * @param path Cookieの有効パス
     */
    public static void add(HttpServletResponse response, String name, String value, int maxAge, String path) {
        response.addCookie(create(name, value, maxAge, path));
    }

    /**
     * Cookieを作成してレスポンスに追加する
     * @param response HttpServletResponseオブジェクト
     * @param name Cookie名
     * @param value Cookie値
     * @param maxAge 有効期限（秒）-1でセッションクッキー、0で削除
     * @param path Cookieの有効パス
     * @param httpOnly JavaScriptからアクセス不可にするか
     * @param secure HTTPS通信時のみ送信するか
     */
    public static void add(HttpServletResponse response, String name, String value, int maxAge, String path,
            boolean httpOnly, boolean secure) {
        response.addCookie(create(name, value, maxAge, path, httpOnly, secure));
    }

    /**
     * Cookieを取得する
     * @param request HttpServletRequestオブジェクト
     * @param name Cookie名
     * @return Cookie。見つからなければnull
     */
    public static Cookie getCookie(HttpServletRequest request, String name) {
        if (request == null) {
            throw new IllegalArgumentException("request が null です。");
        }
        validateName(name);

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie != null && name.equals(cookie.getName())) {
                return cookie;
            }
        }

        return null;
    }

    /**
     * 指定名のCookieの値を取得する
     * UTF-8でデコードして返す
     * @param request HttpServletRequestオブジェクト
     * @param name Cookie名
     * @return Cookieの値。見つからなければnull
     */
    public static String getValue(HttpServletRequest request, String name) {
        Cookie cookie = getCookie(request, name);
        if (cookie == null) {
            return null;
        }
        return decode(cookie.getValue());
    }

    /**
     * 指定名のCookieが存在するか
     * @param request HttpServletRequestオブジェクト
     * @param name Cookie名
     */
    public static boolean exists(HttpServletRequest request, String name) {
        return getCookie(request, name) != null;
    }

    /**
     * Cookieを削除する
     * pathは "/" 固定
     * @param response HttpServletResponseオブジェクト
     * @param name Cookie名
     */
    public static void delete(HttpServletResponse response, String name) {
        delete(response, name, DEFAULT_PATH, defaultHttpOnly, defaultSecure);
    }

    /**
     * Cookieを削除する
     * @param response HttpServletResponseオブジェクト
     * @param name Cookie名
     * @param path Cookieの有効パス
     */
    public static void delete(HttpServletResponse response, String name, String path) {
        delete(response, name, path, defaultHttpOnly, defaultSecure);
    }

    /**
     * Cookieを削除する
     * @param response HttpServletResponseオブジェクト
     * @param name Cookie名
     * @param path Cookieの有効パス
     * @param httpOnly JavaScriptからアクセス不可にするか
     * @param secure HTTPS通信時のみ送信するか
     */
    public static void delete(HttpServletResponse response, String name, String path, boolean httpOnly, boolean secure) {
        if (response == null) {
            throw new IllegalArgumentException("response が null です。");
        }
        validateName(name);

        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setPath(path == null || path.isBlank() ? DEFAULT_PATH : path);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);

        response.addCookie(cookie);
    }

    /**
     * 値をUTF-8でURLエンコードする
     * @param value エンコードする値
     * @return エンコードされた値
     */
    public static String encode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * 値をUTF-8でURLデコードする
     * @param value デコードする値
     * @return デコードされた値
     */
    public static String decode(String value) {
        if (value == null) {
            return null;
        }
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    /**
     * Cookie名の簡易チェック
     * @param name Cookie名
     */
    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Cookie名が不正です。");
        }
    }

    /**
     * Coockie名に使用できない文字をエスケープする
     * @param name Coockie名
     * @return エスケープされたCoockie名
     */
    public static String escapeName(String name) {
        if (name == null) {
            return null;
        }
        return name.replaceAll("[=,;\\s]", "_");
    }
}