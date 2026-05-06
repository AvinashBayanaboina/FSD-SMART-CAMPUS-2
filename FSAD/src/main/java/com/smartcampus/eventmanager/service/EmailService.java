package com.smartcampus.eventmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject("Your Login OTP - SmartCampus");
            
            // HTML content for the email
            String htmlContent = "<html>" +
                    "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>" +
                    "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);'>" +
                    "<h2 style='color: #333333; text-align: center;'>SmartCampus Verification</h2>" +
                    "<p style='color: #555555; font-size: 16px;'>Hello,</p>" +
                    "<p style='color: #555555; font-size: 16px;'>Your One-Time Password (OTP) for login is:</p>" +
                    "<div style='text-align: center; margin: 20px 0;'>" +
                    "<span style='display: inline-block; font-size: 24px; font-weight: bold; background-color: #e2e8f0; color: #1a202c; padding: 10px 20px; border-radius: 4px; letter-spacing: 2px;'>" + otp + "</span>" +
                    "</div>" +
                    "<p style='color: #555555; font-size: 14px; text-align: center;'>This OTP is valid for your current session. Please do not share this code with anyone.</p>" +
                    "<hr style='border: none; border-top: 1px solid #eeeeee; margin: 20px 0;' />" +
                    "<p style='color: #888888; font-size: 12px; text-align: center;'>If you did not request this, please ignore this email.</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
                    
            helper.setText(htmlContent, true); // Set the second parameter to true to send HTML content

            mailSender.send(message);
            System.out.println("OTP HTML Email sent successfully to " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send HTML OTP email. Please check your email configuration.");
            e.printStackTrace();
        }
    }
}
