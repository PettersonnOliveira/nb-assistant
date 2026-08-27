package br.com.petterson.nbassistant.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentCategory {
    RECURSOS_HUMANOS("Recursos Humanos", "👥"),
    PRODUTOS_FINANCEIROS("Produtos Financeiros", "💳"),
    CONTAS_KIDS_TEEN("Contas Kids e Teen", "👨‍👩‍👧"),
    COMPLIANCE("Compliance", "⚖️"),
    SEGURANCA("Segurança", "🔐"),
    PREVENCAO_FRAUDE("Prevenção à Fraude", "🛡️"),
    PIX_PAGAMENTOS("Pix e Pagamentos", "📱"),
    ATENDIMENTO("Atendimento", "🎧"),
    TECNOLOGIA_APIS("Tecnologia e APIs", "💻"),
    MARKETING("Marketing", "📢"),
    OPERACOES("Operações", "⚙️");

    private final String displayName;
    private final String emoji;
}