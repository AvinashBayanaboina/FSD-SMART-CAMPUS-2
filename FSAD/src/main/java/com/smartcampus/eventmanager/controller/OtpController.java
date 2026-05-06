package com.smartcampus.eventmanager.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OtpController {

    @Autowired
    private UserDetailsService userDetailsService;

    @GetMapping("/verify-otp")
    public String showVerifyOtpPage(HttpSession session, Model model) {
        String preAuthUser = (String) session.getAttribute("PRE_AUTH_USER");
        if (preAuthUser == null) {
            return "redirect:/login"; // Redirect to login if they haven't gone through step 1
        }
        model.addAttribute("email", preAuthUser);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") String otp, HttpSession session, RedirectAttributes redirectAttributes) {
        String sessionOtp = (String) session.getAttribute("OTP");
        String preAuthUser = (String) session.getAttribute("PRE_AUTH_USER");

        if (sessionOtp != null && sessionOtp.equals(otp) && preAuthUser != null) {
            // OTP is correct, manually log the user in
            UserDetails userDetails = userDetailsService.loadUserByUsername(preAuthUser);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Clear session attributes
            session.removeAttribute("OTP");
            session.removeAttribute("PRE_AUTH_USER");

            // Redirect based on role
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/admin/dashboard"; 
            } else {
                return "redirect:/events"; 
            }
        }

        redirectAttributes.addFlashAttribute("error", "Invalid OTP. Please try again.");
        return "redirect:/verify-otp";
    }
}
