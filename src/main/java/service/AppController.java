package service;


import domain.User;
import gui.*;



import java.awt.*;

public class AppController {

    private final MainFrame mainFrame;
    private final LoginService loginService;
    private final Session session;
    private final CustomerService customerService;
    private final ItemService itemService;
    private final SaleService saleService;
    private final TemplateService templateService;

    private DiscountPlansPage discountPlansPage;
    private CustomersPage customerAccountsPage;


    public AppController(MainFrame mainFrame, LoginService loginService, Session session,
                         CustomerService customerService, ItemService itemService, SaleService saleService) {
        this.mainFrame = mainFrame;
        this.loginService = loginService;
        this.session = session;
        this.customerService = customerService;
        this.itemService = itemService;
        this.saleService = saleService;
        this.templateService = new TemplateService();
    }

    public void start() {
        showLoginPage();
    }

    public boolean authenticateUser(String username, String password){
        User userAuthOutcome = loginService.authenticate(username, password);
        if(userAuthOutcome != null){
            login(userAuthOutcome);
            return true;
        }
        return false;
    }

    public void login(User user) {
        session.setCurrentUser(user);

        mainFrame.clearPages();
        addMainPages();
        mainFrame.showPage("dashboard");
    }

    public void logout() {
        session.logout();
        showLoginPage();
    }


    //this shouldnt be exposed.
    public void showPage(String pageName) {
        mainFrame.showPage(pageName);
    }

    public User getCurrentUser() {
        return session.getCurrentUser();
    }

    private void showLoginPage() {
        mainFrame.clearPages();

        LoginPage loginPage = new LoginPage(this);
        mainFrame.addPage("login", loginPage);
        mainFrame.setEnterButton(loginPage.getLoginBtn());
        mainFrame.showPage("login");
    }


    public Image getLogo(){
        return mainFrame.getLogoImage();
    }

    public Image getBackground(){
        return mainFrame.getBackgroundImage();
    }

    public void showDiscountPlanPage(String currentCustomerID){
        discountPlansPage.setCurrentCustomerId(currentCustomerID);
        showPage("discount");
    }

    public void showCustomersPageAndRefresh(){
        if(customerAccountsPage != null){
            customerAccountsPage.refreshTable();
            showPage("customers");
        }
    }



    private void addMainPages() {
        discountPlansPage = new DiscountPlansPage(this);
        customerAccountsPage = new CustomersPage(this, customerService);

        mainFrame.addPage("dashboard", new Dashboard(this));
        mainFrame.addPage("orders", new OrdersPage(this));
        mainFrame.addPage("users", new UsersPage(this));
        mainFrame.addPage("customers",customerAccountsPage);
        mainFrame.addPage("templates", new TemplatesPage(this));
        mainFrame.addPage("stock", new StockPage(this, itemService));
        mainFrame.addPage("sales", new SalesPage(this, saleService, itemService));
        mainFrame.addPage("reports", new ReportsPage(this));
        mainFrame.addPage("discount", discountPlansPage);

    }

    public TemplateService getTemplateService() {
        return templateService;
    }



}