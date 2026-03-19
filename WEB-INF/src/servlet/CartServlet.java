package servlet;

import beans.*;
import dao.*;
import helpers.common.CookieMgr;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * カート操作を担当するサーブレットです。
 *
 * 役割:
 * - カートへの商品追加
 * - カート内商品の削除
 * - カート内商品の数量更新
 * - カートのクリア
 * - 購入前の在庫確認
 *
 * カート情報は "cart" Cookie に JSON 文字列として保存します。
 *
 * 入力パラメータ:
 * - action    : 実行する処理
 *               add / remove / update / clear / confirm
 * - itemId    : 対象商品のID
 * - quantity  : 追加数または更新数
 * - fromURL   : エラー時などに戻す転送先
 * - returnURL : 正常終了時の転送先
 *
 * 出力:
 * - request attribute に message / error / cartProducts / outOfStockProducts を設定
 * - 指定先 JSP / Servlet へ forward
 */
@WebServlet("/servlet/Cart")
public class CartServlet extends HttpServlet {

    /**
     * GET リクエストを POST 処理へ委譲します。
     *
     * @param req  リクエスト
     * @param resp レスポンス
     * @throws ServletException サーブレット処理に失敗した場合
     * @throws IOException      入出力に失敗した場合
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    /**
     * カート操作のメイン処理です。
     *
     * action ごとの動作:
     * - add     : 商品をカートへ追加
     * - remove  : 商品をカートから削除
     * - update  : 商品数量を更新
     * - clear   : カートを空にする
     * - confirm : 在庫不足商品がないか確認する
     *
     * 処理の流れ:
     * 1. action を取得
     * 2. fromURL / returnURL を決定
     * 3. cart Cookie を取得。存在しなければ作成
     * 4. action に応じて処理
     * 5. 結果を request attribute に設定
     * 6. 最後に1回だけ forward
     *
     * @param req  リクエスト
     * @param resp レスポンス
     * @throws ServletException サーブレット処理に失敗した場合
     * @throws IOException      入出力に失敗した場合
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = normalizeAction(req.getParameter("action"));
        String fromURL = resolveForwardPath(req, req.getParameter("fromURL"));
        String returnURL = resolveForwardPath(req, req.getParameter("returnURL"));
        if (returnURL.isEmpty()) {
            returnURL = fromURL;
        }

        String destination = returnURL;

        try {
            Cookie cookie = getOrCreateCartCookie(req, resp);

            if (action == null) {
                req.setAttribute("error", "[CartServlet]action が指定されていません");
                destination = fromURL;
            } else {
                switch (action) {
                    case "add" -> {
                        int itemId = parsePositiveInt(req.getParameter("itemId"), "itemId");
                        int quantity = parsePositiveInt(req.getParameter("quantity"), "quantity");

                        SearchDao sDao = new SearchDao();
                        ProductBean product = sDao.getProductById(itemId);

                        if (product == null) {
                            req.setAttribute("error", "[CartServlet]商品の取得に失敗しました");
                            destination = fromURL;
                            break;
                        }

                        if (!sDao.hasProductStock(itemId, quantity)) {
                            req.setAttribute("error", "[CartServlet]在庫が不足しているため、カートに追加できません");
                            destination = fromURL;
                            break;
                        }

                        // これ以降、stock は在庫数ではなく購入数として扱う
                        product.setStock(quantity);

                        addProductToCart(req, resp, product);
                        req.setAttribute("message", "商品がカートに追加されました");
                    }

                    case "remove" -> {
                        int itemId = parsePositiveInt(req.getParameter("itemId"), "itemId");
                        removeProductFromCart(req, resp, itemId);
                        req.setAttribute("message", "商品がカートから削除されました");
                    }

                    case "update" -> {
                        int itemId = parsePositiveInt(req.getParameter("itemId"), "itemId");
                        int quantity = parsePositiveInt(req.getParameter("quantity"), "quantity");

                        SearchDao sDao = new SearchDao();
                        if (!sDao.hasProductStock(itemId, quantity)) {
                            req.setAttribute("error", "[CartServlet]在庫が不足しているため、数量を更新できません");
                            destination = fromURL;
                            break;
                        }

                        updateProductInCart(req, resp, itemId, quantity);
                        req.setAttribute("message", "商品の数量が更新されました");
                    }

                    case "clear" -> {
                        clearCart(req, resp);
                        req.setAttribute("message", "カートがクリアされました");
                    }

                    case "confirm" -> {
                        ProductBean[] cartProducts = getProductFromCookie(req, cookie);

                        ProductBean[] outOfStockProducts = hasProductStockInCart(req);
                        if (outOfStockProducts.length > 0) {
                            req.setAttribute("error", "[CartServlet]在庫が不足している商品があります");
                            req.setAttribute("outOfStockProducts", outOfStockProducts);
                            destination = fromURL;
                            break;
                        }

                        req.setAttribute("cartProducts", cartProducts);
                    }

                    default -> {
                        req.setAttribute("error", "[CartServlet]不正なアクションが指定されました");
                        destination = fromURL;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", "[CartServlet]パラメータが不正です: " + e.getMessage());
            destination = fromURL;
        } catch (Exception e) {
            req.setAttribute("error", "[CartServlet]エラーが発生しました" + (e.getMessage() != null ? ": " + e.getMessage() : ""));
            destination = fromURL;
        }

        req.getRequestDispatcher(destination).forward(req, resp);
    }

    /**
     * action を正規化します。
     *
     * @param action 生の action パラメータ
     * @return trim + 小文字化した action。未指定または空文字の場合は null
     */
    private String normalizeAction(String action) {
        if (action == null) {
            return null;
        }

        String normalized = action.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * forward 先のパスを正規化します。
     *
     * ルール:
     * - null または空文字なら現在のURIを返す
     * - contextPath 付きなら除去する
     * - "/" で始まらない場合は "/" を補う
     *
     * @param req リクエスト
     * @param raw 生のパス
     * @return RequestDispatcher に渡せるアプリ内パス
     */
    private String resolveForwardPath(HttpServletRequest req, String raw) {
        String path = raw;

        if (path == null || path.isBlank()) {
            path = req.getRequestURI();
        }

        String contextPath = req.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        if (path.isEmpty()) {
            return "/";
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return path;
    }

    /**
     * 正の整数パラメータを取得します。
     *
     * @param raw       生の文字列
     * @param paramName パラメータ名
     * @return 1以上の整数
     */
    private int parsePositiveInt(String raw, String paramName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(paramName + " が指定されていません");
        }

        final int value;
        try {
            value = Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(paramName + " は整数で指定してください");
        }

        if (value <= 0) {
            throw new IllegalArgumentException(paramName + " は1以上で指定してください");
        }

        return value;
    }

    /**
     * cart Cookie を取得します。
     * 存在しない場合は新規作成してレスポンスへ追加します。
     *
     * @param req  リクエスト
     * @param resp レスポンス
     * @return 利用可能な cart Cookie
     */
    private Cookie getOrCreateCartCookie(HttpServletRequest req, HttpServletResponse resp) {
        Cookie cookie = CookieMgr.getCookie(req, "cart");
        if (cookie == null) {
            cookie = CookieMgr.create("cart", "[]", 60 * 60 * 24 * 30);
            CookieMgr.add(resp, cookie);
        }
        return cookie;
    }

    /**
     * cart Cookie へ配列内容を保存し、レスポンスへ再送します。
     *
     * @param cookie       cart Cookie
     * @param cartProducts 保存する商品配列
     * @param resp         レスポンス
     * @throws Exception JSON 変換に失敗した場合
     */
    private void saveCartCookie(Cookie cookie, ProductBean[] cartProducts, HttpServletResponse resp) throws Exception {
        String cartJson = utils.toJson(cartProducts);
        cookie.setValue(cartJson);
        CookieMgr.add(resp, cookie);
    }

    /**
     * cart Cookie からカート内商品一覧を取得します。
     *
     * @param req リクエスト
     * @return cart Cookie を復元した商品配列
     * @throws Exception JSON 復元に失敗した場合
     */
    private ProductBean[] getProductFromCookie(HttpServletRequest req) throws Exception {
        Cookie cookie = CookieMgr.getCookie(req, "cart");
        return getProductFromCookie(req, cookie);
    }

    /**
     * 指定された Cookie からカート内商品一覧を取得します。
     *
     * @param req    リクエスト
     * @param cookie cart Cookie
     * @return Cookie を復元した商品配列
     * @throws Exception JSON 復元に失敗した場合
     */
    private ProductBean[] getProductFromCookie(HttpServletRequest req, Cookie cookie) throws Exception {
        if (cookie == null) {
            return new ProductBean[0];
        }

        String value = cookie.getValue();
        String cartJson = (value == null || value.isBlank()) ? "[]" : value;
        return utils.fromJson(cartJson, ProductBean[].class);
    }

    /**
     * 商品をカートへ追加し、cart Cookie を更新します。
     * product の stock フィールドには、在庫数ではなく購入数が入っている前提です。
     * 同一商品がすでにカート内にある場合は、新規追加ではなく数量を加算します。
     *
     * @param req     リクエスト
     * @param resp    レスポンス
     * @param product 追加する商品
     * @throws Exception Cookie 取得または JSON 更新に失敗した場合
     */
    private void addProductToCart(HttpServletRequest req, HttpServletResponse resp, ProductBean product) throws Exception {
        Cookie cookie = getOrCreateCartCookie(req, resp);
        ProductBean[] cartProducts = getProductFromCookie(req, cookie);
        cartProducts = addProductToCartArray(cartProducts, product);
        saveCartCookie(cookie, cartProducts, resp);
    }

    /**
     * カート配列に商品を追加した新しい配列を返します。
     * 同じ productId が存在する場合は、その商品の stock を加算します。
     *
     * @param cartProducts 現在のカート配列
     * @param product      追加する商品
     * @return 商品追加後の新しい配列
     */
    private ProductBean[] addProductToCartArray(ProductBean[] cartProducts, ProductBean product) {
        List<ProductBean> list = new ArrayList<>(Arrays.asList(cartProducts));

        for (ProductBean current : list) {
            if (current.getProductId() == product.getProductId()) {
                current.setStock(current.getStock() + product.getStock());
                return list.toArray(new ProductBean[0]);
            }
        }

        list.add(product);
        return list.toArray(new ProductBean[0]);
    }

    /**
     * 指定した商品IDをカート配列から除外した新しい配列を返します。
     *
     * @param cartProducts 現在のカート配列
     * @param itemId       削除対象の商品ID
     * @return 指定商品を除外した新しい配列
     */
    private ProductBean[] removeProductFromCartArray(ProductBean[] cartProducts, int itemId) {
        List<ProductBean> list = new ArrayList<>(Arrays.asList(cartProducts));
        list.removeIf(p -> p.getProductId() == itemId);
        return list.toArray(new ProductBean[0]);
    }

    /**
     * 商品をカートから削除し、cart Cookie を更新します。
     *
     * @param req    リクエスト
     * @param resp   レスポンス
     * @param itemId 削除対象の商品ID
     * @throws Exception Cookie 取得または JSON 更新に失敗した場合
     */
    private void removeProductFromCart(HttpServletRequest req, HttpServletResponse resp, int itemId) throws Exception {
        Cookie cookie = getOrCreateCartCookie(req, resp);
        ProductBean[] cartProducts = getProductFromCookie(req, cookie);
        cartProducts = removeProductFromCartArray(cartProducts, itemId);
        saveCartCookie(cookie, cartProducts, resp);
    }

    /**
     * カート内商品の数量を更新し、cart Cookie を更新します。
     * この処理では ProductBean の stock を購入数として扱います。
     *
     * @param req      リクエスト
     * @param resp     レスポンス
     * @param itemId   更新対象の商品ID
     * @param quantity 更新後の数量
     * @throws Exception Cookie 取得または JSON 更新に失敗した場合
     */
    private void updateProductInCart(HttpServletRequest req, HttpServletResponse resp, int itemId, int quantity) throws Exception {
        Cookie cookie = getOrCreateCartCookie(req, resp);
        ProductBean[] cartProducts = getProductFromCookie(req, cookie);

        boolean updated = false;
        for (ProductBean product : cartProducts) {
            if (product.getProductId() == itemId) {
                product.setStock(quantity);
                updated = true;
                break;
            }
        }

        if (!updated) {
            throw new IllegalArgumentException("指定された商品はカート内に存在しません");
        }

        saveCartCookie(cookie, cartProducts, resp);
    }

    /**
     * カートを空にします。
     *
     * @param req  リクエスト
     * @param resp レスポンス
     * @throws Exception Cookie 更新に失敗した場合
     */
    private void clearCart(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Cookie cookie = getOrCreateCartCookie(req, resp);
        saveCartCookie(cookie, new ProductBean[0], resp);
    }

    /**
     * カート内に在庫不足の商品があるか確認し、
     * 在庫不足の商品だけを配列で返します。
     *
     * @param req リクエスト
     * @return 在庫不足の商品配列。該当がなければ空配列
     * @throws Exception Cookie 取得、JSON 復元、在庫確認のいずれかに失敗した場合
     */
    private ProductBean[] hasProductStockInCart(HttpServletRequest req) throws Exception {
        ProductBean[] cartProducts = getProductFromCookie(req);

        SearchDao sDao = new SearchDao();
        List<ProductBean> outOfStockProducts = new ArrayList<>();

        for (ProductBean product : cartProducts) {
            if (!sDao.hasProductStock(product.getProductId(), product.getStock())) {
                outOfStockProducts.add(product);
            }
        }

        return outOfStockProducts.toArray(new ProductBean[0]);
    }
}