package in.bushansigur.moneymanager.service;

import in.bushansigur.moneymanager.entity.ProfileEntity;
import in.bushansigur.moneymanager.repository.ExpenseRepository;
import in.bushansigur.moneymanager.repository.IncomeRepository;
import in.bushansigur.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileRepository profileRepository;
    private final EmailService emailService;

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
    }
}
