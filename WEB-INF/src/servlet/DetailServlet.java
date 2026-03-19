import java.io.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import dao.*;
import beans.*;

@WebServlet("/Product/details")
public class DetailServlet extends HttpServlet{
	public void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException,IOException{
		request.setCharacterEncoding("UTF-8");
		int id = Integer.parseInt(request.getParameter("id"));//IDを取得して一致する商品IDのページを表示する
		String type = request.getParameter("type");//商品タイプを取得する
		RequestDispatcher d = request.getRequestDispatcher("/jsp/ProductPage/ProductDetails.jsp");
		request.setAttribute("type",type);
		try{
			switch(type){
				case "cpu":{
					CpuDao dao = new CpuDao();
					CpuBean product = dao.selectById(id);
					request.setAttribute("product",product);
					break;
				}
				case "gpu":{
					GpuDao dao = new GpuDao();
					GpuBean product = dao.selectById(id);
					request.setAttribute("product",product);
					break;
				}
				case "memory":{
					MemoryDao dao = new MemoryDao();
					MemoryBean product = dao.selectById(id);
					request.setAttribute("product",product);
					break;
				}
				case "hdd":{
					HddDao dao = new HddDao();
					HddBean product = dao.selectById(id);
					request.setAttribute("product",product);
					break;
				}
				case "ssd":{
					SsdDao dao = new SsdDao();
					SsdBean product = dao.selectById(id);
					request.setAttribute("product",product);
					break;
				}
				case "motherboard":{
					MotherBoardDao dao = new MotherBoardDao();
					MotherBoardBean product = dao.selectById(id);
					request.setAttribute("product",product);
					break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	d.forward(request,response);
	}
}
