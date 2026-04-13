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
    private final SaleService orderService;
    private final CatalogueService catalogueService;
    private final TemplateService templateService;

    private LoginPage loginPage;
    private Dashboard dashboardPage;
    private OrdersPage ordersPage;
    private UsersPage usersPage;
    private CustomersPage customersPage;
    private TemplatesPage templatesPage;
    private StockPage stockPage;
    private SalesPage salesPage;
    private ReportsPage reportsPage;
    private DiscountPlansPage discountPlansPage;

    public AppController(MainFrame mainFrame, LoginService loginService, Session session,
                         CustomerService customerService, ItemService itemService,
                         SaleService saleService, SaleService orderService,
                         TemplateService templateService, CatalogueService catalogueService) {
        this.mainFrame = mainFrame;
        this.loginService = loginService;
        this.session = session;
        this.customerService = customerService;
        this.itemService = itemService;
        this.saleService = saleService;
        this.orderService = orderService;
        this.catalogueService = catalogueService;
        this.templateService = templateService;
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
        showPage("dashboard");
    }

    public void logout() {
        session.logout();
        showLoginPage();
    }

    public void showPage(String pageName) {

        //refreshing? who want to refresh

        if (pageName.equals("sales") && salesPage != null) {
            salesPage.refresh();
        } else if (pageName.equals("orders") && ordersPage != null) {
            //ordersPage.refresh();
        } else if (pageName.equals("stock") && stockPage != null) {
            //stockPage.refresh();
        } else if (pageName.equals("customers") && customersPage != null) {
            customersPage.refresh();
        }

        mainFrame.showPage(pageName);
    }

    public User getCurrentUser() {
        return session.getCurrentUser();
    }

    private void showLoginPage() {
        mainFrame.clearPages();

        loginPage = new LoginPage(this);
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
        if (discountPlansPage != null) {
            discountPlansPage.setCurrentCustomerId(currentCustomerID);
        }
        showPage("discount");
    }

    private void addMainPages() {

        dashboardPage = new Dashboard(this);
        ordersPage = new OrdersPage(this, orderService, catalogueService);
        usersPage = new UsersPage(this);
        customersPage = new CustomersPage(this, customerService);
        templatesPage = new TemplatesPage(this);
        stockPage = new StockPage(this, itemService);
        salesPage = new SalesPage(this, saleService, itemService);
        reportsPage = new ReportsPage(this);
        discountPlansPage = new DiscountPlansPage(this);

        mainFrame.addPage("dashboard", dashboardPage);
        mainFrame.addPage("orders", ordersPage);
        mainFrame.addPage("users", usersPage);
        mainFrame.addPage("customers", customersPage);
        mainFrame.addPage("templates", templatesPage);
        mainFrame.addPage("stock", stockPage);
        mainFrame.addPage("sales", salesPage);
        mainFrame.addPage("reports", reportsPage);
        mainFrame.addPage("discount", discountPlansPage);
    }

    public TemplateService getTemplateService() {
        return templateService;
    }
}