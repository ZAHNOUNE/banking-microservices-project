package ma.ebank.chatbot.controllers;

import ma.ebank.chatbot.dto.ChatRequest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatControllerTest {
    private final ChatClient chatClient = mock(ChatClient.class);
    private final ChatController controller = new ChatController(chatClient);

    @Test void missingMessageIsRejectedBeforeModelCall() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.ask(new ChatRequest(null, null)));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(chatClient);
    }

    @Test void blankMessageIsRejectedBeforeModelCall() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.ask(new ChatRequest("   ", null)));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(chatClient);
    }

    @Test void nullRequestIsRejectedBeforeModelCall() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.ask(null));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(chatClient);
    }
}
