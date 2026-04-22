package com.churchmanagement.backend.task;

import com.churchmanagement.backend.config.AppProperties;
import com.churchmanagement.backend.model.Member;
import com.churchmanagement.backend.repository.MemberRepository;
import com.churchmanagement.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BirthdayNotificationTask {

    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final AppProperties appProperties;

    // Runs every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void checkUpcomingBirthdays() {
        log.info("Checking for upcoming birthdays...");
        
        // Target date is 3 days from now
        LocalDate targetDate = LocalDate.now().plusDays(3);
        int month = targetDate.getMonthValue();
        int day = targetDate.getDayOfMonth();

        List<Member> upcomingBirthdays = memberRepository.findByBirthdayMonthAndDay(month, day);

        List<String> recipients = new java.util.ArrayList<>(appProperties.getNotifications().getBirthday().getEmails());
        recipients.add(appProperties.getDefaultAdmin().getEmail());

        for (Member member : upcomingBirthdays) {
            String subject = "🎂 Birthday Reminder: " + member.getFirstName() + " " + member.getLastName();
            
            java.util.Map<String, Object> variables = new java.util.HashMap<>();
            variables.put("memberName", member.getFirstName() + " " + member.getLastName());
            variables.put("birthdayDate", targetDate.getMonth().name() + " " + targetDate.getDayOfMonth());

            for (String recipient : recipients) {
                if (recipient == null || recipient.isEmpty() || recipient.contains("${")) continue;
                try {
                    emailService.sendHtmlEmail(recipient, subject, "emails/birthday-notification", variables);
                    log.info("Birthday notification sent to {} for member: {} {}", recipient, member.getFirstName(), member.getLastName());
                } catch (Exception e) {
                    log.error("Failed to send birthday notification to {} for member: {} {}", recipient, member.getFirstName(), member.getLastName(), e);
                }
            }
        }
    }
}
