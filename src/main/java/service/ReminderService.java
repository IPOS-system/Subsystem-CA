package service;

import domain.DebtRecord;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReminderService {

    private static final String REMINDER_TEMPLATE_NAME = "reminder1";

    private final TemplateService templateService;

    public ReminderService() {
        this.templateService = new TemplateService();
    }

    public ReminderService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public String buildOverdueReminder(String firstOrSecond, String custAcc, String holderName, DebtRecord currentDebt) throws IOException {
        String template = templateService.loadTemplate(REMINDER_TEMPLATE_NAME);
        String second= "";

        String month = currentDebt.getCurrentMonth()
                .toLocalDate()
                .getMonth()
                .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.UK);

        Map<String, String> savedValues = templateService.loadValues(REMINDER_TEMPLATE_NAME);
        template = templateService.applyValues(template, savedValues);
        if("F".equals(firstOrSecond)){
            firstOrSecond = "This is the first reminder we are sending you\n " +
                    "for your outstanding debt beginning month "+month;
        }
        else{
            firstOrSecond = "This is the second reminder we are sending you\n " +
                    "for your outstanding debt beginning month "+month;
            second = "SECOND ";
        }

        Map<String, String> runtimeValues = new LinkedHashMap<>();
        runtimeValues.put("SECOND",second);
        runtimeValues.put("INVOICENO", String.valueOf(currentDebt.getDebtId()));
        runtimeValues.put("FIRSTORSECONDMESSAGE", firstOrSecond);

        runtimeValues.put("CUSTACC", custAcc);
        runtimeValues.put("CLIENT", holderName);
        runtimeValues.put("UNPAID", String.valueOf(currentDebt.getRemaining()));
        runtimeValues.put("BEGINMTH",month);

        return templateService.applyValues(template, runtimeValues);
    }
}