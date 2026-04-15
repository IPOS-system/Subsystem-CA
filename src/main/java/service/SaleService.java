package service;

import api.IPaymentAPI;
import api_impl.DatabaseConnection;
import api_impl.IAccInfoAPIService;
import api_impl.IPaymentAPIService;
import dao.DebtsDAO;
import dao.ItemDAO;
import dao.SalesDAO;
import domain.*;
import jdk.management.jfr.RecordingInfo;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
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
    private ReceiptService receiptService;

    private IAccInfoAPIService iAccInfoAPIService; //for sending orders to ipos sa
    private IPaymentAPIService iPaymentAPIService;

    public TimeService timeService ;

    public SaleService(CustomerService customerService, PaymentService paymentService, TimeService timeService, DebtService debtService){
        this.itemDAO = new ItemDAO();
        this.customerService = customerService;
        this.currentCustomer = null;
        this.salesDAO = new SalesDAO();
        this.paymentService = paymentService;
        this.debtService = debtService;
        this.debtsDAO = new DebtsDAO();
        this.iAccInfoAPIService = new IAccInfoAPIService();
        this.timeService = timeService;
        this.receiptService = new ReceiptService(new TemplateService(), timeService);

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
        // map basket -> order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (SaleItem saleItem : basket) {
            orderItems.add(new OrderItem(
                    saleItem.getItemId(),
                    saleItem.getItemDescription(),
                    saleItem.getQuantity(),
                    saleItem.getUnitPrice(),
                    saleItem.getOrderItemPrice()
            ));
        }

        Result orderSendResult = iAccInfoAPIService.sendOrder(orderItems);
        //now we send this to ipos sa and see what they say.
        System.out.println("now we will try and call SA to send the order. ");
        basket.clear();
        return orderSendResult;
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
    public Result placeSale(PaymentInfo paymentInfo) {

        // 1. stock check
        for (SaleItem i : basket){
            Item refItem = itemDAO.findById(i.getItemId());
            if (i.getQuantity() > refItem.getQtyInStock()){
                return Result.fail("quantity in basket of " + i.getItemDescription() + " exceeds local stock");
            }
        }


        BigDecimal total = getTotal();

        // 2. validate payment
        Result paymentResult = paymentService.validatePayment(currentCustomer, total, paymentInfo);
        if (!paymentResult.isSuccess()) {
            return paymentResult;//if failure
        }

        Connection con = null;

        try {

            String accountId = (currentCustomer != null) ? currentCustomer.getAccountId() : null;
            String customerName = (currentCustomer != null) ? currentCustomer.getContactName() : "Walk-in Customer";

            con = DatabaseConnection.getConnection(); //use sam con for transactions
            con.setAutoCommit(false);

            Integer debtId = null; //nullable
            //handle account debt
            if ("account".equals(paymentInfo.method)) {
                if (accountId == null) {
                    return Result.fail("account payment requires a customer account");
                }
                debtId = debtService.recordSaleDebt(con, accountId, total);
            }

            // 3. create sale
            int saleId = salesDAO.createSale(con, accountId, total, paymentInfo.method,  paymentInfo.cardNumber,paymentInfo.expiry, debtId, Date.valueOf(timeService.today()));

            //4. insert items + update stock
            for (SaleItem i : basket) {
                salesDAO.insertSaleItem(con, saleId, i);
                Result stockResult = itemDAO.reduceStock(con, i.getItemId(), i.getQuantity());
                if (!stockResult.isSuccess()) {
                    throw new RuntimeException("stock update failed");
                }
            }


            con.commit();//from here its a success

            String reciept = receiptService.createReceipt(String.valueOf(saleId),customerName , basket);
            //send the card details to PU;
            //iPaymentAPIService.Pay();
            System.out.println("now we will try and call PU to make the payment...");

            basket.clear();

            return Result.success(reciept);

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
