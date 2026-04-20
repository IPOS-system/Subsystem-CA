import org.junit.runner.RunWith;
import org.junit.runners.Suite;



// Runs all test classes
@RunWith(Suite.class)
@Suite.SuiteClasses({
        SaleItemTest.class,
        ItemTest.class,



        ISoldStockAPITest.class,
        IAccInfoAPISystemTest.class,


})
public class RootSuite {
}
