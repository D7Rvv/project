/**
 * productIDを受け取り、それ単体の商品情報を返すサーブレット
 * 例）http://192.168.1.3:8080/np03_23th/servlet/ProductServlet?productId=1
 * 例）http://192.168.1.3:8080/np03_23th/servlet/ProductServlet?productId=2&category=cpu&debug=true
 */
package servlet;

import java.io.IOException;

import beans.ProductBean;
import dao.SearchDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/servlet/ProductServlet")
public class ProductServlet extends HttpServlet{

    //商品ページのURL
    String PRODUCT_PAGE_JSP = "/jsp/ProductPage/ProductDetails.jsp";
    //デバッグ用のURL
    String DEBUG_PAGE_JSP = "/jsp/debug/product_detail.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String productId = req.getParameter("productId");

        try {
            int id = Integer.parseInt(productId);
            SearchDao sDao = new SearchDao();

            ProductBean product = sDao.getProductById(id);

            req.setAttribute("product", product);
            req.setAttribute("productId", id);
        } catch (NumberFormatException e) {
            req.setAttribute("errorMsg", "※ProductServletでエラー発生<br>" + 
                                            "※productId変数の中身が不正です");
        } catch (Exception e) {
            req.setAttribute("errorMsg", "※ProductServletでエラー発生<br>" + 
                                            "※データベース接続に失敗しました");
        } finally {
            // JSなどを書き換えず常に商品詳細画面(PRODUCT_PAGE_JSP)を表示させるよう修正
            req.getRequestDispatcher(PRODUCT_PAGE_JSP).forward(req, resp);
        }
    }
}
