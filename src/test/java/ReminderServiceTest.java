import domain.DebtRecord;
import org.junit.Before;
import org.junit.Test;
import service.ReminderService;
import service.TemplateService;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import static org.junit.Assert.*;

// tests for ReminderService
// checking that the reminder letter includes the right details
// using a temp template file for the tests

public class ReminderServiceTest {

    private ReminderService reminderService;
    private DebtRecord sampleDebt;

    @Before
    public void setUp() throws Exception {
        // make a temp folder for the test template
        Path tempDir = java.nio.file.Files.createTempDirectory("reminder_test");

        // simple template with placeholders
        String template =
                "{SECOND}REMINDER - INVOICE NO.: {INVOICENO}\n" +
                        "Account: {CUSTACC}\n" +
                        "Client: {CLIENT}\n" +
                        "Unpaid: {UNPAID}\n" +
                        "Month: {BEGINMTH}\n" +
                        "{FIRSTORSECONDMESSAGE}";

        // save template into temp folder
        Path templateFile = tempDir.resolve("reminder1.txt");
        java.nio.file.Files.writeString(templateFile, template);

        // use temp folder instead of real templates
        TemplateService testTemplateService = new TemplateService(tempDir);
        reminderService = new ReminderService(testTemplateService);

        // sample debt for most tests
        sampleDebt = new DebtRecord(
                42,
                new BigDecimal("294.00"),
                "no_need",
                "no_need",
                Date.valueOf(LocalDate.of(2025, 1, 1))
        );
    }

    // account number should be in the letter
    @Test
    public void testFirstReminderHasAccountNumber() throws Exception {
        String letter = reminderService.buildOverdueReminder("F", "ACC-00235", "John Smith", sampleDebt);
        assertTrue("Letter should contain the account number", letter.contains("ACC-00235"));
    }

    // customer name should be in the letter
    @Test
    public void testFirstReminderHasClientName() throws Exception {
        String letter = reminderService.buildOverdueReminder("F", "ACC-00235", "John Smith", sampleDebt);
        assertTrue("Letter should contain the customer name", letter.contains("John Smith"));
    }

    // debt id should be in the letter
    @Test
    public void testFirstReminderHasDebtId() throws Exception {
        String letter = reminderService.buildOverdueReminder("F", "ACC-00235", "John Smith", sampleDebt);
        assertTrue("Letter should contain the debt ID (42)", letter.contains("42"));
    }

    // amount should be in the letter
    @Test
    public void testFirstReminderHasAmountOwed() throws Exception {
        String letter = reminderService.buildOverdueReminder("F", "ACC-00235", "John Smith", sampleDebt);
        assertTrue("Letter should contain the amount owed (294.00)", letter.contains("294.00"));
    }

    // month should be in the letter
    @Test
    public void testFirstReminderHasMonthName() throws Exception {
        String letter = reminderService.buildOverdueReminder("F", "ACC-00235", "John Smith", sampleDebt);
        assertTrue("Letter should contain the month name (January)", letter.contains("January"));
    }

    // first reminder should not say SECOND
    @Test
    public void testFirstReminderDoesNotSaySecond() throws Exception {
        String letter = reminderService.buildOverdueReminder("F", "ACC-00235", "John Smith", sampleDebt);
        assertFalse("First reminder should not start with SECOND", letter.startsWith("SECOND"));
    }

    // second reminder tests

    // second reminder should say SECOND
    @Test
    public void testSecondReminderSaysSecond() throws Exception {
        String letter = reminderService.buildOverdueReminder("S", "ACC-00235", "John Smith", sampleDebt);
        assertTrue("Second reminder should contain the word SECOND", letter.contains("SECOND"));
    }

    // second reminder should still have the customer name
    @Test
    public void testSecondReminderHasClientName() throws Exception {
        String letter = reminderService.buildOverdueReminder("S", "ACC-00235", "Jane Doe", sampleDebt);
        assertTrue("Second reminder should still contain customer name", letter.contains("Jane Doe"));
    }

    // second reminder should still have the debt id
    @Test
    public void testSecondReminderHasDebtId() throws Exception {
        String letter = reminderService.buildOverdueReminder("S", "ACC-00235", "John Smith", sampleDebt);
        assertTrue("Second reminder should still contain the debt ID", letter.contains("42"));
    }

    // try with different debt details as well
    @Test
    public void testReminderWorksWithDifferentDebtDetails() throws Exception {
        // another sample debt
        DebtRecord bigDebt = new DebtRecord(
                99,
                new BigDecimal("1500.75"),
                "no_need",
                "no_need",
                Date.valueOf(LocalDate.of(2025, 3, 1))
        );
        String letter = reminderService.buildOverdueReminder("F", "ACC-00100", "Alice Brown", bigDebt);
        assertTrue("Letter should contain the correct amount", letter.contains("1500.75"));
        assertTrue("Letter should contain March as the month", letter.contains("March"));
    }
}
