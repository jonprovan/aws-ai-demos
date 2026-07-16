package com.skillstorm.awsdemos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves the landing page linking out to each of the five service demos. */
@Controller
public class HomeController {

    /** Renders the home page template. */
    @GetMapping("/")
    String home() {
        return "index";
    }
}
