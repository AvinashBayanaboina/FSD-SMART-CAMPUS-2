package com.smartcampus.eventmanager.controller;

import com.smartcampus.eventmanager.model.Event;
import com.smartcampus.eventmanager.model.Registration;
import com.smartcampus.eventmanager.service.EventService;
import com.smartcampus.eventmanager.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final RegistrationService registrationService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredEvents", eventService.getUpcomingEvents());
        model.addAttribute("totalEvents", eventService.getTotalEvents());
        model.addAttribute("totalRegistrations", eventService.getTotalRegistrations());
        return "home";
    }

    @GetMapping("/events")
    public String listEvents(@RequestParam(required = false) String dept, 
                             @RequestParam(required = false) String type, 
                             Model model) {
        if (dept != null && !dept.isEmpty()) {
            model.addAttribute("events", eventService.filterByDepartment(dept));
        } else {
            model.addAttribute("events", eventService.getAllEvents());
        }
        return "events";
    }

    @GetMapping("/register/{id}")
    public String showRegistrationForm(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        Registration registration = new Registration();
        registration.setEvent(event);
        
        model.addAttribute("registration", registration);
        model.addAttribute("event", event);
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute Registration registration, 
                                      BindingResult result, 
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("event", eventService.getEventById(registration.getEvent().getId()).get());
            return "register";
        }

        try {
            Event event = eventService.getEventById(registration.getEvent().getId())
                    .orElseThrow(() -> new RuntimeException("Event not found"));
            registration.setEvent(event);
            registrationService.registerStudent(registration);
            redirectAttributes.addFlashAttribute("success", "Successfully registered for " + event.getTitle());
            return "redirect:/events";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("event", eventService.getEventById(registration.getEvent().getId()).get());
            return "register";
        }
    }

    private final com.smartcampus.eventmanager.repository.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin-login")
    public String adminLogin() {
        return "admin-login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@RequestParam String email, @RequestParam String password, RedirectAttributes redirectAttributes) {
        if (!email.toLowerCase().endsWith("@fsd.in")) {
            redirectAttributes.addFlashAttribute("error", "Only @fsd.in email addresses are allowed for signup.");
            return "redirect:/signup";
        }
        if (userRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Email is already registered.");
            return "redirect:/signup";
        }
        
        com.smartcampus.eventmanager.model.AppUser newUser = new com.smartcampus.eventmanager.model.AppUser();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole("USER");
        userRepository.save(newUser);
        
        redirectAttributes.addFlashAttribute("success", "Registration successful. Please log in.");
        return "redirect:/login";
    }

    @GetMapping("/my-registrations")
    public String showMyRegistrations(@RequestParam(required = false) String email, Model model) {
        if (email != null && !email.isEmpty()) {
            if (!email.toLowerCase().endsWith("@fsd.in")) {
                model.addAttribute("error", "Only @fsd.in email addresses are allowed.");
            } else {
                model.addAttribute("registrations", registrationService.getRegistrationsByStudent(email));
            }
            model.addAttribute("searchedEmail", email);
        }
        return "my-registrations";
    }
}
