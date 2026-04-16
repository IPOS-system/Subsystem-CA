package service;

import domain.SaleItem;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReceiptService {

    private static final String TEMPLATE_NAME = "receipt";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TemplateService templateService;
    private final TimeService timeService;

    public ReceiptService(TemplateService templateService, TimeService timeService) {
        this.templateService = templateService;
        this.timeService = timeService;
    }

    public String createReceipt(String saleId, String customerName, List<SaleItem> items, BigDecimal total) throws IOException {
        String template = templateService.loadTemplate(TEMPLATE_NAME);
        String filledTemplate = templateService.applyValues(template, templateService.loadValues(TEMPLATE_NAME));

        String itemsText = buildItemsTable(items);

        BigDecimal originalTotal = calculateOriginalTotal(items);
        BigDecimal discount = originalTotal.subtract(total);

        filledTemplate = filledTemplate.replace("{SALE_ID}", safe(saleId));
        filledTemplate = filledTemplate.replace("{CUSTOMER_NAME}", safe(customerName));
        filledTemplate = filledTemplate.replace("{DATE}", timeService.today().format(DATE_FORMAT));
        filledTemplate = filledTemplate.replace("{ITEMS}", itemsText);
        filledTemplate = filledTemplate.replace("{SUBTOTAL}", formatMoney(originalTotal));
        filledTemplate = filledTemplate.replace("{DISCOUNT}", formatMoney(discount));
        filledTemplate = filledTemplate.replace("{TOTAL}", formatMoney(total));


        return filledTemplate;
    }

    private BigDecimal calculateOriginalTotal(List<SaleItem> items) {
        BigDecimal total = BigDecimal.ZERO;

        for (SaleItem item : items) {
            total = total.add(item.getOrderItemPrice());
        }

        return total;
    }



    private String buildItemsTable(List<SaleItem> items) {
        StringBuilder sb = new StringBuilder();

        for (SaleItem item : items) {
            sb.append(formatItemLine(item)).append("\n");
        }

        return sb.toString().trim();
    }

    private String formatItemLine(SaleItem item) {
        String description = item.getItemDescription();
        int quantity = item.getQuantity();
        BigDecimal lineTotal = item.getOrderItemPrice();

        return String.format(
                "%-20s x%d %8s",
                shorten(description, 20),
                quantity,
                formatMoney(lineTotal)
        );
    }

    private String formatMoney(BigDecimal amount) {
        return "£" + amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String shorten(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }
}