package com.lost2found.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {


    @GetMapping("/report")
    public String report() {

        return "report"; // report.html in templates folder
    }


    @GetMapping("/search")
    public String search() {
        return "search"; // search.html in templates folder
    }

    @GetMapping("/TermsAndCondition")
    public String termsAndCondition() {
        return "TermsAndCondition";
    }
}
