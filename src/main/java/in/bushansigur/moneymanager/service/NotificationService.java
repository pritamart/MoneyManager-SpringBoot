package in.bushansigur.moneymanager.service;

import in.bushansigur.moneymanager.dto.ExpenseDTO;
import in.bushansigur.moneymanager.entity.ProfileEntity;
import in.bushansigur.moneymanager.repository.ExpenseRepository;
import in.bushansigur.moneymanager.repository.IncomeRepository;
import in.bushansigur.moneymanager.repository.ProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    @Value("${money.manager.frontend}")
    private String frontendUrl;

//    @Scheduled(cron = "0 * * * * *", zone = "IST")
    @Scheduled(cron = "0 0 22 * * *")
    private void sendDailyIncomeExpenseReminder() {
        log.info("Job started: sendDailyIncomeExpenseReminder()");

        List<ProfileEntity> profiles = profileRepository.findAll();

        for (ProfileEntity profile : profiles) {

            String body = "<h1 style=\"margin-bottom:16px; font-family:Arial, sans-serif;\">Hi " + profile.getFullName() + ",</h1>\n" +
                    "<p style=\"margin-bottom:20px; font-family:Arial, sans-serif;\">\n" +
                    "  This is a friendly reminder for your daily income and expense from \n" +
                    "  <b style=\"color:blue;\">Samui Group Of Company</b>\n" +
                    "</p>\n" +
                    "<a href=\"" + frontendUrl + "\" " +
                    "style=\"display:inline-block; padding:10px 20px; background-color:green; color:#fff; text-decoration:none; border-radius:4px; font-family:Arial, sans-serif; margin-bottom:20px;\">" +
                    "Go to the website\n" +
                    "</a>\n" +
                    "<p style=\"font-family:Arial, sans-serif; margin-top:30px;\">\n" +
                    "  Thanks and Regards<br>\n" +
                    "  Samui Group Of Company\n" +
                    "</p>\n";


            // Send email
            emailService.sendEmail(
                    profile.getEmail(),
                    "Daily Reminder: Update Your Income & Expenses",
                    body
            );
        }
        log.info("Job End: sendDailyIncomeExpenseReminder()");
    }
//    @Scheduled(cron = "0 0 23 * * *") // runs daily at 11 PM

    @Scheduled(cron = "0 0 23 * * *")
    private void sendDailyExpenseSummery() {
        log.info("Job Start: sendDailyExpenseSummery()");

        List<ProfileEntity> profiles = profileRepository.findAll();

        for (ProfileEntity profile : profiles) {

            List<ExpenseDTO> toDayExpense =
                    expenseService.getExpenseForCurrentUserOnDate(profile.getId(), LocalDate.now());
            if (toDayExpense.isEmpty()) {
                continue;
            }

            StringBuilder table = new StringBuilder();

            table.append("<table style='width:100%; border-collapse:collapse; font-family:Arial, sans-serif;'>");

            table.append("<tr style='background:#f2f2f2;'>")
                    .append("<th style='border:1px solid #ddd; padding:8px;'>ID</th>")
                    .append("<th style='border:1px solid #ddd; padding:8px;'>Name</th>")
                    .append("<th style='border:1px solid #ddd; padding:8px;'>Amount</th>")
                    .append("<th style='border:1px solid #ddd; padding:8px;'>Category</th>")
                    .append("</tr>");

            int i = 1;
            for (ExpenseDTO expense : toDayExpense) {
                table.append("<tr>")
                        .append("<td style='border:1px solid #ddd; padding:8px;'>").append(i++).append("</td>")
                        .append("<td style='border:1px solid #ddd; padding:8px;'>").append(expense.getName()).append("</td>")
                        .append("<td style='border:1px solid #ddd; padding:8px;'>").append(expense.getAmount()).append("</td>")
                        .append("<td style='border:1px solid #ddd; padding:8px;'>")
                        .append(expense.getCategoryId() != null ? expense.getCategoryName() : "N/A")
                        .append("</td>")
                        .append("</tr>");
            }

            table.append("</table>");

            String body = "Hi " + profile.getFullName()
                    + ",<br><br>"
                    + "Here is your today's expense summary:<br><br>"
                    + table
                    + "<br><br>Thanks & Regards<br>"
                    + "<b>MoneyManager</b>";

            emailService.sendEmail(
                    profile.getEmail(),
                    "Daily Reminder: Today’s Expenses",
                    body
            );
        }

        log.info("Job End: sendDailyExpenseSummery()");
    }

}
