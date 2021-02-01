package org.csu.mypetstore.web.servlets;

import org.csu.mypetstore.domain.Account;
import org.csu.mypetstore.domain.Cart;
import org.csu.mypetstore.domain.Item;
import org.csu.mypetstore.service.CatalogService;
import org.csu.mypetstore.service.LogService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AddItemToCartServlet extends HttpServlet {
    //Servlet的功能即负责中转
    //1.重点是处理完请求跳转的页面
    private static final String VIEW_CART = "/WEB-INF/jsp/cart/Cart.jsp";

    //2.定义处理该请求所需要的数据,别人请求你需要数据
    private String workingItemId;   //要加购物车的商品的ID需要
    private Cart cart;             //购物车,定义一个购物车对象, 购物车要放到session中去

    //3.是否需要调用业务逻辑层
    private CatalogService catalogService;  //如果需要的话定义一个Service

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        workingItemId = request.getParameter("workingItemId");  //给我item的ID

        Account account;
        //从对话中，获取购物车, 购物车肯定是放到session里面的
        HttpSession session = request.getSession();
        cart = (Cart)session.getAttribute("cart");
        account = (Account)session.getAttribute("account");

        if(cart == null){
            //第一次使用购物车(如果购物车是空的)
            cart = new Cart();
        }

        if(cart.containsItemId(workingItemId)){
            //判断是否已经有该商品，数量加一
            cart.incrementQuantityByItemId(workingItemId);

            if(account != null){
                HttpServletRequest httpRequest= (HttpServletRequest) request;
                String strBackUrl = "http://" + request.getServerName() + ":" + request.getServerPort()
                        + httpRequest.getContextPath() + httpRequest.getServletPath() + "?" + (httpRequest.getQueryString());

                LogService logService = new LogService();
                Item item = catalogService.getItem(workingItemId);
                String logInfo = logService.logInfo(" ") + strBackUrl + " " + item + "数量+1 ";
                logService.insertLogInfo(account.getUsername(), logInfo);
            }
        }else{
            catalogService = new CatalogService();  //业务逻辑层new出来,判断该商品是否还有库存
            boolean isInStock = catalogService.isItemInStock(workingItemId);
            Item item = catalogService.getItem(workingItemId);  //获取item的信息
            cart.addItem(item, isInStock);  //调用已写的方法
            session.setAttribute("cart", cart);  //将cart放到session中去

            if(account != null){
                HttpServletRequest httpRequest= request;
                String strBackUrl = "http://" + request.getServerName() + ":" + request.getServerPort()
                        + httpRequest.getContextPath() + httpRequest.getServletPath() + "?" + (httpRequest.getQueryString());

                LogService logService = new LogService();
                String logInfo = logService.logInfo(" ") + strBackUrl + " 添加物品 " + item + " 到购物车";
                logService.insertLogInfo(account.getUsername(), logInfo);
            }


            request.getRequestDispatcher(VIEW_CART).forward(request, response);   //页面跳转到cart.jsp
        }
    }
}
