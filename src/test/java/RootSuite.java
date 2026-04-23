import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all test classes
@RunWith(Suite.class)
@Suite.SuiteClasses({

    // Riya test cases
    SaleItemTest.class,
    ItemTest.class,

    // Rishi test cases
    ISoldStockAPITest.class,
    IAccInfoAPISystemTest.class,

    // Finn test cases
    ICatalogueAPITest.class,
    IPaymentAPITest.class

})

public class RootSuite {}
