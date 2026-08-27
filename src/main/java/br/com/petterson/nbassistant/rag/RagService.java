package br.com.petterson.nbassistant.rag;

import br.com.petterson.nbassistant.dto.ChatResponse;
import br.com.petterson.nbassistant.dto.SourceReference;
import br.com.petterson.nbassistant.service.ChatMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;
    private final ChatMemory chatMemory;
    private final ChatMetricsService chatMetricsService;

    private static final double SIMILARITY_THRESHOLD = 0.5;
    private static final int TOP_K = 5;

    private static final String SYSTEM_PROMPT = """
            Você é o NB Assistant, o assistente de conhecimento corporativo da NB.CASH.
            Responda a pergunta do colaborador utilizando as informações do contexto abaixo, extraído dos documentos oficiais da empresa.
            Você também pode considerar o histórico da nossa conversa se for relevante.
            
            Regras importantes:
            - Se o contexto não tiver informação suficiente para responder, diga claramente que não encontrou essa informação nos documentos disponíveis. Não invente respostas.
            - Seja objetivo e direto.
            - Não mencione que está seguindo instruções ou que possui um "contexto".
            
            Contexto:
            {context}
            """;

    public ChatResponse ask(String question, String chatId, String category) {
        chatMetricsService.recordQuestion();

        var searchRequestBuilder = SearchRequest.builder()
                .query(question)
                .topK(TOP_K)
                .similarityThreshold(SIMILARITY_THRESHOLD);

        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("TODOS")) {
            var filterBuilder = new FilterExpressionBuilder();
            searchRequestBuilder.filterExpression(filterBuilder.eq("category", category).build());
            log.info("Buscando chunks filtrados pela categoria: '{}'", category);
        } else {
            log.info("Buscando chunks em todas as categorias");
        }

        SearchRequest searchRequest = searchRequestBuilder.build();
        List<org.springframework.ai.document.Document> results = vectorStore.similaritySearch(searchRequest);

        if (results.isEmpty()) {
            log.info("Nenhum chunk relevante encontrado para a pergunta: '{}'", question);
            return new ChatResponse(
                    "Não encontrei essa informação nos documentos disponíveis. "
                            + "Recomendo entrar em contato com a área responsável.",
                    List.of()
            );
        }

        String context = buildContext(results);

        ChatClient chatClient = chatClientBuilder.build();

        String answer = chatClient.prompt()
                .system(SYSTEM_PROMPT.replace("{context}", context))
                .user(question)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId(chatId).build())
                .call()
                .content();

        List<SourceReference> sources = toSourceReferences(results);

        return new ChatResponse(answer, sources);
    }

    private String buildContext(List<org.springframework.ai.document.Document> results) {
        StringBuilder sb = new StringBuilder();
        for (var doc : results) {
            sb.append("Fonte: ").append(doc.getMetadata().get("fileName"))
                    .append(" (").append(doc.getMetadata().get("location")).append(")\n")
                    .append(doc.getText())
                    .append("\n\n---\n\n");
        }
        return sb.toString();
    }

    private List<SourceReference> toSourceReferences(List<org.springframework.ai.document.Document> results) {
        return results.stream()
                .map(doc -> new SourceReference(
                        (String) doc.getMetadata().get("fileName"),
                        (String) doc.getMetadata().get("location"),
                        doc.getScore() != null ? doc.getScore() : 0.0
                ))
                .collect(Collectors.toMap(
                        s -> s.fileName() + "|" + s.location(),
                        s -> s,
                        (existing, duplicate) -> existing.similarityScore() >= duplicate.similarityScore() ? existing : duplicate,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }
}