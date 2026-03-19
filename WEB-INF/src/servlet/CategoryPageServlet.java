package servlet;

import java.io.IOException;
import java.io.*;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.RequestDispatcher;

public class CategoryPageServlet extends HttpServlet{
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {

        RequestDispatcher rd;

        response.setContentType("text/html; UTF-8");
		request.setCharacterEncoding("UTF-8");
        
        String category = request.getParameter("category");

        if(category != null && !category.isEmpty()){
            request.setAttribute("category", category);
            rd = request.getRequestDispatcher("/jsp/ProductPage/productPage.jsp");
			rd.forward(request, response);
            return;
        }
        else{
            request.setAttribute("errorMsg", "※CategoryPageServletでエラー発生<br>" + 
                                            "※category変数の中身がありません");
            rd = request.getRequestDispatcher("/jsp/Top/index.jsp");
			rd.forward(request, response);
            return;
        }
    }
}