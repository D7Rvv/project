/**
 * 作成：小車（支援：ChatGPT）
 * 最終変更：2026-03-03
 *
 * 概要：
 *  商品管理（CPU/GPU/MOTHER_BOARD/MEMORY/SSD/HDD）を1本のServletでCRUDする。
 *  makerId をプルダウン化するため、new/edit表示時に MAKER 一覧を取得して JSP に渡す。
 *  デバッグ用、本番では使用しない
 *
 * URL:
 *  /debug/ProductManage
 */

package servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

import dao.*;
import beans.*;
import utils.*;

@WebServlet("/debug/ProductManage")
public class ProductManageServlet extends HttpServlet {

    // -------------------------
    // GET: 一覧/新規/編集画面
    // -------------------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = utils.nvl(request.getParameter("action"), "list");
        String type = utils.nvl(request.getParameter("type"), "cpu"); // デフォルトはCPU

        try {
            switch (action) {
                case "list": {
                    setListAttributes(request, type);
                    request.setAttribute("type", type);
                    request.getRequestDispatcher("/jsp/debug/product_manage.jsp")
                           .forward(request, response);
                    break;
                }
                case "new": {
                    // ★追加：メーカー一覧
                    setMakerAttributes(request);

                    request.setAttribute("type", type);
                    request.setAttribute("mode", "insert");
                    request.setAttribute("bean", createEmptyBean(type));
                    request.getRequestDispatcher("/jsp/debug/product_edit.jsp")
                           .forward(request, response);
                    break;
                }
                case "edit": {
                    int id = utils.parseInt(request.getParameter("id"), 0);

                    // ★追加：メーカー一覧
                    setMakerAttributes(request);

                    Object bean = selectById(type, id);
                    if (bean == null) {
                        request.setAttribute("error", "対象が見つかりませんでした (type=" + type + ", id=" + id + ")");
                        setListAttributes(request, type);
                        request.setAttribute("type", type);
                        request.getRequestDispatcher("/jsp/debug/product_manage.jsp")
                               .forward(request, response);
                        return;
                    }

                    request.setAttribute("type", type);
                    request.setAttribute("mode", "update");
                    request.setAttribute("bean", bean);
                    request.getRequestDispatcher("/jsp/debug/product_edit.jsp")
                           .forward(request, response);
                    break;
                }
                default: {
                    request.setAttribute("error", "不正なactionです: " + action);
                    setListAttributes(request, type);
                    request.setAttribute("type", type);
                    request.getRequestDispatcher("/jsp/debug/product_manage.jsp")
                           .forward(request, response);
                    break;
                }
            }

        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            try {
                setListAttributes(request, type);
            } catch (Exception ignore) {}
            request.setAttribute("type", type);
            request.getRequestDispatcher("/jsp/debug/product_manage.jsp")
                   .forward(request, response);
        }
    }

    // -------------------------
    // POST: insert/update/delete
    // -------------------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = utils.nvl(request.getParameter("action"), "");
        String type = utils.nvl(request.getParameter("type"), "cpu");

        try {
            switch (action) {
                case "insert": {
                    Object bean = buildBeanFromRequest(type, request, false);
                    insert(type, bean);
                    response.sendRedirect(request.getContextPath() + "/debug/ProductManage?action=list&type=" + type);
                    break;
                }
                case "update": {
                    Object bean = buildBeanFromRequest(type, request, true);
                    update(type, bean);
                    response.sendRedirect(request.getContextPath() + "/debug/ProductManage?action=list&type=" + type);
                    break;
                }
                case "delete": {
                    int id = utils.parseInt(request.getParameter("id"), 0);
                    delete(type, id);
                    response.sendRedirect(request.getContextPath() + "/debug/ProductManage?action=list&type=" + type);
                    break;
                }
                default: {
                    request.setAttribute("error", "不正なactionです: " + action);
                    setListAttributes(request, type);
                    request.setAttribute("type", type);
                    request.getRequestDispatcher("/jsp/debug/product_manage.jsp")
                           .forward(request, response);
                    break;
                }
            }

        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            try {
                setListAttributes(request, type);
            } catch (Exception ignore) {}
            request.setAttribute("type", type);
            request.getRequestDispatcher("/jsp/debug/product_manage.jsp")
                   .forward(request, response);
        }
    }

    // =========================
    // ★追加：メーカー一覧設定
    // =========================
    private void setMakerAttributes(HttpServletRequest request) throws Exception {
        MakerDao makerDao = new MakerDao();
        List<MakerBean> makers = makerDao.selectAll();
        request.setAttribute("makers", makers);
    }

    // =========================
    // 一覧設定
    // =========================
    private void setListAttributes(HttpServletRequest request, String type) throws Exception {
        switch (type) {
            case "cpu": {
                CpuDao dao = new CpuDao();
                List<CpuBean> list = dao.selectAll();
                request.setAttribute("products", list);
                break;
            }
            case "gpu": {
                GpuDao dao = new GpuDao();
                List<GpuBean> list = dao.selectAll();
                request.setAttribute("products", list);
                break;
            }
            case "mb": {
                MotherBoardDao dao = new MotherBoardDao();
                List<MotherBoardBean> list = dao.selectAll();
                request.setAttribute("products", list);
                break;
            }
            case "memory": {
                MemoryDao dao = new MemoryDao();
                List<MemoryBean> list = dao.selectAll();
                request.setAttribute("products", list);
                break;
            }
            case "ssd": {
                SsdDao dao = new SsdDao();
                List<SsdBean> list = dao.selectAll();
                request.setAttribute("products", list);
                break;
            }
            case "hdd": {
                HddDao dao = new HddDao();
                List<HddBean> list = dao.selectAll();
                request.setAttribute("products", list);
                break;
            }
            default:
                throw new Exception("不正なtypeです: " + type);
        }
    }

    // =========================
    // 1件取得
    // =========================
    private Object selectById(String type, int id) throws Exception {
        switch (type) {
            case "cpu": return new CpuDao().selectById(id);
            case "gpu": return new GpuDao().selectById(id);
            case "mb": return new MotherBoardDao().selectById(id);
            case "memory": return new MemoryDao().selectById(id);
            case "ssd": return new SsdDao().selectById(id);
            case "hdd": return new HddDao().selectById(id);
            default: throw new Exception("不正なtypeです: " + type);
        }
    }

    // =========================
    // insert/update/delete 振り分け
    // =========================
    private void insert(String type, Object bean) throws Exception {
        switch (type) {
            case "cpu": new CpuDao().insert((CpuBean) bean); break;
            case "gpu": new GpuDao().insert((GpuBean) bean); break;
            case "mb": new MotherBoardDao().insert((MotherBoardBean) bean); break;
            case "memory": new MemoryDao().insert((MemoryBean) bean); break;
            case "ssd": new SsdDao().insert((SsdBean) bean); break;
            case "hdd": new HddDao().insert((HddBean) bean); break;
            default: throw new Exception("不正なtypeです: " + type);
        }
    }

    private void update(String type, Object bean) throws Exception {
        switch (type) {
            case "cpu": new CpuDao().update((CpuBean) bean); break;
            case "gpu": new GpuDao().update((GpuBean) bean); break;
            case "mb": new MotherBoardDao().update((MotherBoardBean) bean); break;
            case "memory": new MemoryDao().update((MemoryBean) bean); break;
            case "ssd": new SsdDao().update((SsdBean) bean); break;
            case "hdd": new HddDao().update((HddBean) bean); break;
            default: throw new Exception("不正なtypeです: " + type);
        }
    }

    private void delete(String type, int id) throws Exception {
        switch (type) {
            case "cpu": new CpuDao().delete(id); break;
            case "gpu": new GpuDao().delete(id); break;
            case "mb": new MotherBoardDao().delete(id); break;
            case "memory": new MemoryDao().delete(id); break;
            case "ssd": new SsdDao().delete(id); break;
            case "hdd": new HddDao().delete(id); break;
            default: throw new Exception("不正なtypeです: " + type);
        }
    }

    // =========================
    // new画面用：空Bean
    // =========================
    private Object createEmptyBean(String type) throws Exception {
        switch (type) {
            case "cpu": return new CpuBean();
            case "gpu": return new GpuBean();
            case "mb": return new MotherBoardBean();
            case "memory": return new MemoryBean();
            case "ssd": return new SsdBean();
            case "hdd": return new HddBean();
            default: throw new Exception("不正なtypeです: " + type);
        }
    }

    // =========================
    // リクエスト -> Bean組み立て
    // =========================
    private Object buildBeanFromRequest(String type, HttpServletRequest request, boolean isUpdate) throws Exception {

        // 共通
        String name = utils.nvl(request.getParameter("name"), "");
        int price = utils.parseInt(request.getParameter("price"), 0);
        String image = utils.nvl(request.getParameter("image"), "");
        int makerId = utils.parseInt(request.getParameter("makerId"), 0);

        int productId = utils.parseInt(request.getParameter("productId"), 0);

        switch (type) {

            case "cpu": {
                CpuBean b = new CpuBean();
                if (isUpdate) {
                    b.setCpuId(utils.parseInt(request.getParameter("cpuId"), 0));
                    b.setProductId(productId);
                }
                b.setName(name);
                b.setPrice(price);
                b.setImageId(image);
                b.setMakerId(makerId);

                b.setGeneration(utils.nvl(request.getParameter("generation"), ""));
                b.setCore(utils.parseInt(request.getParameter("core"), 0));
                b.setThread(utils.parseInt(request.getParameter("thread"), 0));
                b.setClock(utils.parseDouble(request.getParameter("clock"), 0.0));

                return b;
            }

            case "gpu": {
                GpuBean b = new GpuBean();
                if (isUpdate) {
                    b.setGpuId(utils.parseInt(request.getParameter("gpuId"), 0));
                    b.setProductId(productId);
                }
                b.setName(name);
                b.setPrice(price);
                b.setImageId(image);
                b.setMakerId(makerId);

                b.setSeriesName(utils.nvl(request.getParameter("seriesName"), ""));
                b.setChipName(utils.nvl(request.getParameter("chipName"), ""));
                b.setVram(utils.parseInt(request.getParameter("vram"), 0));

                return b;
            }

            case "mb": {
                MotherBoardBean b = new MotherBoardBean();
                if (isUpdate) {
                    b.setMotherBoardId(utils.parseInt(request.getParameter("motherBoardId"), 0));
                    b.setProductId(productId);
                }
                b.setName(name);
                b.setPrice(price);
                b.setImageId(image);
                b.setMakerId(makerId);

                b.setChipset(utils.nvl(request.getParameter("chipset"), ""));
                b.setSize(utils.nvl(request.getParameter("size"), ""));

                return b;
            }

            case "memory": {
                MemoryBean b = new MemoryBean();
                if (isUpdate) {
                    b.setMemoryId(utils.parseInt(request.getParameter("memoryId"), 0));
                    b.setProductId(productId);
                }
                b.setName(name);
                b.setPrice(price);
                b.setImageId(image);
                b.setMakerId(makerId);

                b.setGeneration(utils.nvl(request.getParameter("generation"), ""));
                b.setCapacity(utils.nvl(request.getParameter("capacity"), ""));

                return b;
            }

            case "ssd": {
                SsdBean b = new SsdBean();
                if (isUpdate) {
                    b.setSsdId(utils.parseInt(request.getParameter("ssdId"), 0));
                    b.setProductId(productId);
                }
                b.setName(name);
                b.setPrice(price);
                b.setImageId(image);
                b.setMakerId(makerId);

                b.setCapacity(utils.nvl(request.getParameter("capacity"), ""));
                b.setType(utils.nvl(request.getParameter("typeValue"), ""));

                return b;
            }

            case "hdd": {
                HddBean b = new HddBean();
                if (isUpdate) {
                    b.setHddId(utils.parseInt(request.getParameter("hddId"), 0));
                    b.setProductId(productId);
                }
                b.setName(name);
                b.setPrice(price);
                b.setImageId(image);
                b.setMakerId(makerId);

                b.setCapacity(utils.nvl(request.getParameter("capacity"), ""));
                b.setRpm(utils.nvl(request.getParameter("rpm"), ""));

                return b;
            }

            default:
                throw new Exception("不正なtypeです: " + type);
        }
    }
}