/**
 * 作成：小車
 * 最終変更：3月18日
 * 変更内容：---
 * 概要
 * 
 * action(login | signup | logout | delete)
 *
 * fromURL
 * 　移動元のURL、下記コードで取得したものの使用を推奨
 * 　String fromURL = request.getRequestURI();
 * 
 * returnURL
 *   ログイン成功時に returnURL にリダイレクトされる
 *   ※未指定の場合は fromURL に遷移
 *
 * userName
 *   ユーザー名（login / signup / delete で使用）
 *
 * password
 *   パスワード（login / signup / delete で使用）
 *
 * address
 *   住所（signup で使用）
 *
 */

package servlet;

import java.io.IOException;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

import dao.UserDao;
import beans.UserBean;
import helpers.common.PasswordHasher;

@WebServlet("/UserServlet")
public class UserServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // =====================
        // 全パラメータを取得
        // =====================

        //　内容変更　岸田　変更日：3月18日 16:00------------------------------------------------------------------

        String action = request.getParameter("action");
        String userName = request.getParameter("userName");
        String password = request.getParameter("password");
        String address = request.getParameter("address");

        //　フルパス　(※フォワードする場合は、アプリ名を除いてからURLを代入)
        String rawFromURL = request.getParameter("rawFromURL");
        
        //　相対パス
        String fromURL = request.getParameter("fromURL");

        // リダイレクト先
        String returnURL = request.getParameter("returnURL");

        // TOP画面の相対パス
        String TopPageURL = "/jsp/Top/index.jsp";

        // rawFromURL が空の場合は、デフォルトでTOP画面に遷移させる
        if (rawFromURL == null || rawFromURL.isEmpty()) {
            rawFromURL = request.getContextPath() + TopPageURL;
        }

        // 前回のページがログイン画面の場合、TOP画面に遷移させる
        if (rawFromURL.contains("login.jsp")) {
            rawFromURL = request.getContextPath() + TopPageURL;
        }

        // fromURL (フォワード用パス) の生成
        // fromURLが空の場合、rawFromURLからアプリ名を抜いて作成する
        if (fromURL == null || fromURL.isEmpty()) {
            fromURL = rawFromURL.replace(request.getContextPath(), "");
        }

        // ----------------------------------------------------------------------------------------------------

        
        try {
            UserDao dao = new UserDao();

            if (action == null) {
                request.setAttribute("error", "不正なアクセスです。");
                request.getRequestDispatcher(fromURL).forward(request, response);
                return;
            }

            switch (action) {

                
                // =====================
                // ログイン
                // =====================
                case "login": {
                    UserBean user = dao.checkLogin(userName, password);

                    if (user != null) {
                        HttpSession session = request.getSession();
                        session.setAttribute("loginUser", user);
                        response.sendRedirect(rawFromURL);

                    }
                    else {
                        rawFromURL = fromURL;
                        request.setAttribute("login_error_msg", "ユーザー名またはパスワードが間違っています");
                        request.setAttribute("rawFromURL", rawFromURL);
                        request.getRequestDispatcher(fromURL).forward(request, response);
                    }
                    break;
                }                

                // =====================
                // 新規登録
                // =====================
                case "signup": {

                    //　3月17日　14：03　追加　岸田 ----------------------------------------------------------------------

                    // エラー判定
                    if (userName == null || userName.isEmpty() || 
                        password == null || password.isEmpty() || 
                        address == null || address.isEmpty()) {
                        
                        request.setAttribute("reg_error_msg", "未入力または未選択の項目があります。");
                        request.setAttribute("rawFromURL", rawFromURL);
                        request.getRequestDispatcher(fromURL).forward(request, response);
                        break;
                    }

                    if (dao.isExistUser(userName)) {
                        request.setAttribute("reg_error_msg", "ユーザー名はすでに使われています。");
                        request.setAttribute("rawFromURL", rawFromURL);
                        request.getRequestDispatcher(fromURL).forward(request, response);
                        break;
                    }

                    // ------------------------------------------------------------------------------------------------

                    String salt = PasswordHasher.getSalt();
                    String hash = PasswordHasher.hash(password, salt);

                    UserBean bean = new UserBean();
                    bean.setName(userName);
                    bean.setAddress(address);
                    bean.setSalt(salt);
                    bean.setHash(hash);

                    dao.insert(bean);

                    request.setAttribute("reg_message", "登録が完了しました。ログインしてください。");
                    request.setAttribute("rawFromURL", rawFromURL);
                    request.setAttribute("action", "login");
                    request.getRequestDispatcher(fromURL).forward(request, response);
                    break;
                }

                // =====================
                // ログアウト
                // =====================
                case "logout": {
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        session.invalidate();
                    }
                    response.sendRedirect(request.getContextPath() + TopPageURL);
                    break;
                }

                // =====================
                // 更新
                // =====================
                case "update": {
                    HttpSession session = request.getSession(false);
                    if (session == null) {
                        request.setAttribute("login_error_msg", "ログインしてください");
                        request.getRequestDispatcher(fromURL).forward(request, response);
                        break;
                    }

                    UserBean loginUser = (UserBean) session.getAttribute("loginUser");
                    if (loginUser == null) {
                        request.setAttribute("login_error_msg", "ログインしてください");
                        request.getRequestDispatcher(fromURL).forward(request, response);
                        break;
                    }

                    //　ユーザー名を更新 (3/17　追加　岸田)
                    if (userName != null && !userName.isEmpty()) {
                        loginUser.setName(userName);
                    }

                    // パスワードを更新する場合はハッシュ化して保存
                    if (password != null && !password.isEmpty()) {
                        String salt = PasswordHasher.getSalt();
                        String hash = PasswordHasher.hash(password, salt);
                        loginUser.setSalt(salt);
                        loginUser.setHash(hash);
                    }

                    // 住所を更新
                    if (address != null) {
                        loginUser.setAddress(address);
                    }

                    dao.update(loginUser);

                    request.setAttribute("message", "ユーザー情報を更新しました。");
                    request.getRequestDispatcher(fromURL)
                           .forward(request, response);
                    break;
                }

                // =====================
                // 削除
                // =====================
                case "delete": {
                    UserBean user = dao.checkLogin(userName, password);

                    if (user != null) {
                        HttpSession session = request.getSession(false);
                        if (session != null) {
                            session.invalidate();
                        }

                        dao.delete(user.getUserId());

                        response.sendRedirect(rawFromURL);
                    } else {
                        request.setAttribute("error", "ユーザー名またはパスワードが間違っています");
                        request.getRequestDispatcher(fromURL)
                               .forward(request, response);
                    }
                    break;
                }

                default: {
                    request.setAttribute("error", "不正なactionです: " + action);
                    request.getRequestDispatcher(fromURL)
                           .forward(request, response);
                    break;
                }
            }

        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher(fromURL)
                   .forward(request, response);
        }
    }
}