package servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/servlet/OrderServlet")
public class OrderServlet extends HttpServlet{

    String DEBUG_PAGE_JSP = "/jsp/debug/Order.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        switch(action) {
            case "confirm":
                //注文確認処理
                break;
            case "complete":
                //注文完了処理
                break;
            default:
                req.setAttribute("errorMsg", "※OrderServletでエラー発生<br>" + 
                                            "※不正なアクションが指定されました");
                req.getRequestDispatcher(DEBUG_PAGE_JSP).forward(req, resp);
                return;
        }
    }
}