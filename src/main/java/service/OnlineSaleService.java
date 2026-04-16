package service;

import dao.OnlineOrderDAO;

import java.util.List;
import java.util.Map;

public class OnlineSaleService {
    private final OnlineOrderDAO dao = new OnlineOrderDAO();

    public List<Map<String, Object>> getAllOrders() {
        return dao.findAllOrders();
    }

    public List<Map<String, Object>> getItemsForOrder(String orderId) {
        return dao.findItemsByOrderId(orderId);
    }
}