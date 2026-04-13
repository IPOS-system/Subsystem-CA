package service;

import api_impl.DatabaseConnection;
import dao.DebtsDAO;
import dao.ItemDAO;
import dao.SalesDAO;
import domain.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class SaleService {

    private final List<SaleItem> basket = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;

    private ItemDAO itemDAO;
    private SalesDAO salesDAO;
    private DebtsDAO debtsDAO;

    private final DebtService debtService;
    private final CustomerService customerService;

    private final PaymentService paymentService;

    private  CustomerAccount currentCustomer;

    public SaleService(CustomerService customerService, PaymentService paymentService){
        this.itemDAO = new ItemDAO();
        this.customerService = customerService;
        this.currentCustomer = null;
        this.salesDAO = new SalesDAO();
        this.paymentService = paymentService;
        this.debtService = new DebtService();
        this.debtsDAO = new DebtsDAO();
    }


    public Result addItemToBasket(Item item, int qty) {
        if (item == null){
            return Result.fail("no item brah");
        }
        else if (qty <= 0) {
            return Result.fail("Invalid quantity");
        }

        else {
            //preventing duplicate sale items in the basekt (will break sql)
            for (SaleItem line : basket) {
                if (line.getItemId().equals(item.getItemId())) {
                    line.updateQuantity(line.getQuantity() + qty);
                    return Result.success("added (updated quantity)");
                }
            }
            basket.add(new SaleItem(item.getItemId(), item.getDescription(), qty, item.getPackageCost()));
           return Result.success("added");
        }
    }

    private BigDecimal getBasketTotal() {
        BigDecimal total = BigDecimal.ZERO;

        for (SaleItem line : basket) {
            total = total.add(line.getOrderItemPrice());
        }

        return total;
    }

    public Result removeItemFromBasket(String itemId) {

        for (SaleItem line : basket) {
            if (line.getItemId().equals(itemId)) {
                basket.remove(line);
                return Result.success("Item removed");
            }
        }

        return Result.fail("Item not found in basket");
    }

    public List<SaleItem> getBasket() {
        return basket;
    }

    public void clearBasket(){
        basket.clear();

    }


    //for orders with SA
    public Result placeOrder(){
        basket.clear();
        return Result.fail("not done yet");
    }

    public List<CustomerAccount> getAllCustomers(){
        return customerService.getAllCustomerAccounts();
    }
    public CustomerAccount findById(String id){
        return customerService.findById(id);
    }

    public void setCustomer(String customerId){
        if(customerId == null){
            currentCustomer = null;
        }
        else{
            currentCustomer = customerService.findById(customerId);
        }
    }

    public String getCurrentCustomerName(){
        if(currentCustomer == null){
            return "no customer selected";
        }
        else return currentCustomer.getAccountHolderName();
    }

    public BigDecimal getTotal() {
        BigDecimal total = getBasketTotal();

        if (currentCustomer == null) {
            return total;
        }

        DiscountPlan plan = customerService.getCustomerDiscountPlan(currentCustomer.getAccountId());
        if (plan == null) {
            return total;
        }

        BigDecimal discountRate = BigDecimal.ZERO;

        if (DiscountPlan.TYPE_FIXED.equalsIgnoreCase(plan.getPlanType())) {
            if (plan.getFixedRate() != null) {
                discountRate = plan.getFixedRate();
            }
        } else if (DiscountPlan.TYPE_TIERED.equalsIgnoreCase(plan.getPlanType())) {
            for (DiscountTier tier : plan.getTiers()) {
                boolean meetsMin = total.compareTo(tier.getMinAmount()) >= 0;
                boolean meetsMax = tier.getMaxAmount() == null || total.compareTo(tier.getMaxAmount()) <= 0;

                if (meetsMin && meetsMax) {
                    discountRate = tier.getDiscountRate();
                    break;
                }
            }
        }

        BigDecimal discountAmount = total.multiply(discountRate).divide(BigDecimal.valueOf(100));
        return total.subtract(discountAmount);
    }

    //for sales to customers in store.
    //attempts to place sale
    public Result placeSale(PaymentInfo paymentMethod) {

        // 1. stock check
        for (SaleItem i : basket){
            Item refItem = itemDAO.findById(i.getItemId());
            if (i.getQuantity() > refItem.getQtyInStock()){
                return Result.fail("quantity in basket of " + i.getItemDescription() + " exceeds local stock");
            }
        }

        BigDecimal total = getTotal();

        // 2. validate payment
        Result paymentResult = paymentService.validatePayment(currentCustomer, total, paymentMethod);
        if (!paymentResult.isSuccess()) {
            return paymentResult;
        }

        Connection con = null;

        try {
            con = DatabaseConnection.getConnection(); //use sam con for transactions
            con.setAutoCommit(false);

            Integer debtId = null; //nullable
            //handle account debt
            if ("account".equals(paymentMethod.method)) {
                debtId = debtService.recordSaleDebt(con, currentCustomer.getAccountId(), total);
            }

            // 3. create sale
            int saleId = salesDAO.createSale(con, currentCustomer.getAccountId(), total, paymentMethod.method, debtId);

            // 4. insert items + update stock
            for (SaleItem i : basket) {
                salesDAO.insertSaleItem(con, saleId, i);
                Result stockResult = itemDAO.reduceStock(con, i.getItemId(), i.getQuantity());
                if (!stockResult.isSuccess()) {
                    throw new RuntimeException("stock update failed");
                }
            }


            con.commit();
            basket.clear();

            return Result.success("order placed");

        } catch (Exception e) {
            try {
                if (con != null) con.rollback();
            } catch (Exception ignored) {}

            return Result.fail("transaction failed");

        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (Exception ignored) {}
        }
    }
}
