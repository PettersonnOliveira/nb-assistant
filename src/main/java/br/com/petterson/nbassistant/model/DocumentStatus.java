package br.com.petterson.nbassistant.model;

public enum DocumentStatus {
    PENDING,      // upload feito, aguardando processamento
    PROCESSING,   // extração/chunking/embedding em andamento
    PROCESSED,    // indexado com sucesso, disponível para busca
    FAILED        // erro no processamento
}