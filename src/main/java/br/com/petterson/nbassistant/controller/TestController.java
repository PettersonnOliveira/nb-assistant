package br.com.petterson.nbassistant.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    private final ChatModel chatModel;

    public TestController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/test")
    public String test() {
        return chatModel.call("Diga apenas: OK!");
    }
}