package ma.ebank.chatbot.controllers;

import lombok.AllArgsConstructor;
import ma.ebank.chatbot.dto.ChatRequest;
import ma.ebank.chatbot.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/chat")
@AllArgsConstructor
@CrossOrigin("*")
public class ChatController {

    private final ChatClient chatClient;

    // Endpoint de conversation consommé par la page HTML intégrée au dashboard Angular.
    @PostMapping("/ask")
    public ChatResponse ask(@RequestBody ChatRequest request) {
        if (request == null || request.message() == null || request.message().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message requis");
        }
        String reply = chatClient.prompt()
                .user(request.message())
                .call()
                .content();
        return new ChatResponse(reply);
    }
}
