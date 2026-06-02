package com.shiksha.scheduler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Async
    public void sendInterviewScheduled(String toEmail, String candidateName,
                                       String jobTitle, String interviewerName,
                                       String date, String time) {
        String subject = "Interview Scheduled — " + jobTitle;
        String body = buildHtml(
            "🎉 Interview Scheduled!",
            "Dear <strong>" + candidateName + "</strong>,",
            "Your interview for <strong>" + jobTitle + "</strong> has been scheduled.",
            "<ul><li><b>Date:</b> " + date + "</li>" +
            "<li><b>Time:</b> " + time + "</li>" +
            "<li><b>Interviewer:</b> " + interviewerName + "</li></ul>",
            "Please be prepared and log in on time. Good luck!",
            "blue"
        );
        sendMail(toEmail, subject, body);
    }

    @Async
    public void sendInterviewRescheduled(String toEmail, String candidateName,
                                          String jobTitle, String newDate, String newTime) {
        String subject = "Interview Rescheduled — " + jobTitle;
        String body = buildHtml(
            "📅 Interview Rescheduled",
            "Dear <strong>" + candidateName + "</strong>,",
            "Your interview for <strong>" + jobTitle + "</strong> has been rescheduled.",
            "<ul><li><b>New Date:</b> " + newDate + "</li>" +
            "<li><b>New Time:</b> " + newTime + "</li></ul>",
            "Please update your calendar accordingly.",
            "orange"
        );
        sendMail(toEmail, subject, body);
    }

    @Async
    public void sendInterviewCancelled(String toEmail, String candidateName, String jobTitle) {
        String subject = "Interview Cancelled — " + jobTitle;
        String body = buildHtml(
            "❌ Interview Cancelled",
            "Dear <strong>" + candidateName + "</strong>,",
            "Unfortunately, your interview for <strong>" + jobTitle + "</strong> has been cancelled.",
            "",
            "Our HR team will reach out to reschedule. We apologize for the inconvenience.",
            "red"
        );
        sendMail(toEmail, subject, body);
    }

    private void sendMail(String to, String subject, String htmlBody) {
        if (mailSender == null) {
            System.out.println("[EMAIL SKIPPED - No mail config] To: " + to + " | Subject: " + subject);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("[EMAIL ERROR] Failed to send to " + to + ": " + e.getMessage());
        }
    }

    private String buildHtml(String title, String greeting, String intro,
                              String details, String footer, String color) {
        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;background:#f4f4f4;padding:20px'>" +
               "<div style='max-width:600px;margin:auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.1)'>" +
               "<div style='background:" + color + ";padding:24px;text-align:center'>" +
               "<h1 style='color:white;margin:0;font-size:22px'>" + title + "</h1></div>" +
               "<div style='padding:24px'>" +
               "<p>" + greeting + "</p>" +
               "<p>" + intro + "</p>" +
               details +
               "<p style='margin-top:16px'>" + footer + "</p>" +
               "</div>" +
               "<div style='background:#f4f4f4;padding:12px;text-align:center;font-size:12px;color:#888'>" +
               "Smart Interview Scheduler &mdash; Automated Notification</div></div></body></html>";
    }
}
