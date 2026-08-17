package br.com.petterson.nbassistant.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.petterson.nbassistant.model.DocumentCategory;

@Controller
public class WebController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("categories", DocumentCategory.values());
        return "index";
    }
}