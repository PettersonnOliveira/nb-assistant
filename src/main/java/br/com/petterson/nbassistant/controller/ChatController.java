package br.com.petterson.nbassistant.controller;

import br.com.petterson.nbassistant.dto.ChatRequest;
import br.com.petterson.nbassistant.dto.ChatResponse;
import br.com.petterson.nbassistant.rag.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final RagService ragService;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String chatId = request.chatId() != null ? request.chatId() : "default-session";
        return ragService.ask(request.question(), chatId, request.category());
    }
}
