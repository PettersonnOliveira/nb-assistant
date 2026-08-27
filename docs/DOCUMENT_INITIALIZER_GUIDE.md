# 📚 Guia do DocumentInitializer

Guia detalhado para uso e operação do sistema de carga automática de documentos.

## 🎯 O que é o DocumentInitializer

O **DocumentInitializer** é um componente Spring que roda automaticamente na inicialização da aplicação e processa documentos previamente cadastrados no sistema, sem necessidade de upload manual pela interface.

### Funcionalidades

- ✅ Lê arquivos do diretório `storage-local/` organizados por categorias
- ✅ Cria registros na tabela `documents` automaticamente
- ✅ Processa os arquivos para gerar embeddings no PGVector
- ✅ Pula documentos que já estão processados (evita duplicação)
- ✅ É robusto a erros individuais (não falha a aplicação inteira se um arquivo falhar)
- ✅ Marca documentos com erro como `FAILED` com motivo da falha

## 📁 Estrutura de Diretórios

### Estrutura esperada

```
storage-local/
├── ATENDIMENTO/
│   └── FAQ_de_Atendimento_ao_Cliente.md
├── COMPLIANCE/
│   └── Politica_de_KYC_e_Compliance.md
├── CONTAS_KIDS_TEEN/
│   └── MANUAL DE CONTAS KIDS.docx
├── MARKETING/
│   └── plano_marketing.html
├── OPERACOES/
│   └── Manual Interno NB.CASH.pdf
├── PIX_PAGAMENTOS/
│   └── Guia_de_Pix_e_Pagamentos.md
├── PREVENCAO_FRAUDE/
│   └── Politica_de_Prevencao_a_Fraude.md
├── PRODUTOS_FINANCEIROS/
│   └── tabela_tarifas_nbcash.html
├── RECURSOS_HUMANOS/
│   └── Política de RH e Benefícios.md
├── SEGURANCA/
│   └── Manual de Segurança da Informação.md
└── TECNOLOGIA_APIS/
    └── especificacao_apis_nbcash.json
```

### Regras importantes

- **Nome da pasta**: Deve ser EXATAMENTE igual ao nome do enum `DocumentCategory` (ex: `PIX_PAGAMENTOS`, não `pix_pagamentos`)
- **Formatos suportados**: PDF, DOCX, CSV, XLSX, PPTX, MARKDOWN, HTML, JSON
- **Nome dos arquivos**: Pode ser qualquer nome, mas preferencialmente sem caracteres especiais

## 🚀 Como Adicionar Novos Documentos

### Passo 1: Coloque o arquivo na pasta correta

```bash
# Exemplo: Adicionar documento na categoria PIX_PAGAMENTOS
cp novo_documento.pdf storage-local/PIX_PAGAMENTOS/
```

### Passo 2: Reinicie o container

```bash
docker-compose restart app
```

### Passo 3: Verifique o processamento

```bash
# Ver logs do container
docker logs nb-assistant-app --tail 50
```

Você deverá ver logs como:

```
INFO 1 --- [main] b.c.p.n.initializer.DocumentInitializer  : Processando categoria: PIX_PAGAMENTOS
INFO 1 --- [main] b.c.p.n.initializer.DocumentInitializer  : Documento criado: novo_documento.pdf (ID: xxx)
INFO 1 --- [main] b.c.p.n.ingestion.EmbeddingService       : Documento 'novo_documento.pdf' indexado com X chunks no vector store
INFO 1 --- [main] b.c.p.n.ingestion.IngestionService       : Documento 'novo_documento.pdf' processado com sucesso: X chunks indexados
```

## 🔍 Como Verificar o Estado do Sistema

### Via PostgreSQL

```sql
-- Ver todos os documentos
SELECT id, original_file_name, category, status, uploaded_at, processed_at, chunk_count 
FROM documents 
ORDER BY uploaded_at DESC;

-- Ver documentos por categoria e status
SELECT category, status, COUNT(*) 
FROM documents 
GROUP BY category, status 
ORDER BY category, status;

-- Ver embeddings por categoria
SELECT metadata->>'category' as category, COUNT(*) 
FROM vector_store 
GROUP BY metadata->>'category' 
ORDER BY category;

-- Ver documentos com erro
SELECT id, original_file_name, category, failure_reason 
FROM documents 
WHERE status = 'FAILED';

-- Total de chunks indexados
SELECT COUNT(*) FROM vector_store;
```

### Via API

```bash
# Dashboard completo
curl http://localhost:8080/api/dashboard

# Categorias disponíveis
curl http://localhost:8080/api/documents/categories

# Todos os documentos
curl http://localhost:8080/api/documents
```

### Via Docker

```bash
# Verificar arquivos no container
docker exec nb-assistant-app ls -la /app/storage-local/

# Verificar logs de processamento
docker logs nb-assistant-app | grep -i "initializer"
```

## 🧹 Como Limpar e Recomeçar

### Limpar documentos do banco

```sql
-- Apagar todos os documentos
DELETE FROM documents;

-- Apagar embeddings
DELETE FROM vector_store;

-- Apagar documentos de uma categoria específica
DELETE FROM documents WHERE category = 'ATENDIMENTO';
DELETE FROM vector_store WHERE metadata->>'category' = 'ATENDIMENTO';

-- Apagar apenas documentos com erro
DELETE FROM documents WHERE status = 'FAILED';
```

### Limpar arquivos físicos

```bash
# Remover todos os arquivos de storage
rm -rf storage-local/

# Recriar estrutura
mkdir -p storage-local/{ATENDIMENTO,COMPLIANCE,CONTAS_KIDS_TEEN,MARKETING,OPERACOES,PIX_PAGAMENTOS,PREVENCAO_FRAUDE,PRODUTOS_FINANCEIROS,RECURSOS_HUMANOS,SEGURANCA,TECNOLOGIA_APIS}
```

### Reiniciar containers

```bash
docker-compose down
docker-compose up -d --build
```

## ⚠️ Solução de Problemas

### Documento não aparece no frontend

**Causa provável**: Documento está com status diferente de `PROCESSED`

**Solução**:
```sql
-- Verificar status do documento
SELECT id, original_file_name, category, status, failure_reason 
FROM documents 
WHERE original_file_name = 'nome_do_arquivo';

-- Se estiver FAILED, verifique o motivo e corrija o arquivo
-- Se estiver PENDING/PROCESSING, aguarde ou reinicie o container
```

### DocumentInitializer não roda

**Causas possíveis**:
1. Classe não está sendo carregada pelo Spring
2. Diretório `storage-local/` não existe
3. Permissões de arquivo incorretas

**Solução**:
```bash
# Verificar se a classe existe
find . -name "DocumentInitializer.java"

# Verificar se o diretório existe
ls -la storage-local/

# Verificar permissões (no container)
docker exec nb-assistant-app ls -la /app/storage-local/

# Verificar logs de inicialização
docker logs nb-assistant-app | grep -i "initializer"
```

### Erro ao processar arquivo específico

**Causa provável**: Formato não suportado ou arquivo corrompido

**Solução**:
```bash
# Ver logs específicos do erro
docker logs nb-assistant-app | grep -i "error"

# Ver documento com falha
SELECT id, original_file_name, category, failure_reason 
FROM documents 
WHERE status = 'FAILED';

# Corrija o arquivo ou remova-o e tente novamente
```

### Parser XLSX falha

**Causa conhecida**: Problema de compatibilidade do Apache POI

**Solução temporária**:
- Converta o arquivo XLSX para CSV ou HTML
- Use o formato alternativo que funciona corretamente

## 📊 Exemplo de Fluxo Completo

### Cenário: Adicionar nova categoria de documentos

```bash
# 1. Criar nova categoria no enum DocumentCategory
# (Edite o arquivo DocumentCategory.java)

# 2. Criar pasta da nova categoria
mkdir -p storage-local/NOVA_CATEGORIA

# 3. Adicionar documentos
cp documento1.pdf storage-local/NOVA_CATEGORIA/
cp documento2.docx storage-local/NOVA_CATEGORIA/

# 4. Reconstruir e reiniciar
docker-compose down
docker-compose up -d --build

# 5. Verificar processamento
docker logs nb-assistant-app --tail 50

# 6. Verificar no frontend
# Acesse http://localhost:8080 e verifique se a nova categoria aparece
```

## 🔧 Configuração Avançada

### Alterar diretório de storage

**Arquivo**: `LocalStorageService.java`

```java
private static final String BASE_DIR = "storage-local"; // Altere aqui
```

**Arquivo**: `docker-compose.yml`

```yaml
volumes:
  - ./novo-diretorio:/app/novo-diretorio:Z
```

**Arquivo**: `DocumentInitializer.java`

```java
private static final String STORAGE_DIR = "novo-diretorio"; // Altere aqui
```

### Desabilitar DocumentInitializer

Se quiser desabilitar o processamento automático:

**Arquivo**: `DocumentInitializer.java`

```java
// Comente a anotação @Component
// @Component
@RequiredArgsConstructor
public class DocumentInitializer {
    // ...
}
```

Ou remova a anotação `@EventListener` do método.

## 📝 Melhores Práticas

1. **Organização**: Mantenha os arquivos organizados por categoria
2. **Nomes descritivos**: Use nomes de arquivos que descrevam o conteúdo
3. **Backup**: Faça backup dos documentos originais antes de processar
4. **Teste local**: Teste o processamento localmente antes de colocar em produção
5. **Monitoramento**: Verifique os logs após cada reinicialização
6. **Limpeza**: Remova documentos antigos com falha para manter o banco limpo

## 🎓 Conceitos Técnicos

### Como funciona o processamento

1. **Inicialização**: O Spring dispara o evento `ApplicationReadyEvent`
2. **Leitura**: O `DocumentInitializer` lê o diretório `storage-local/`
3. **Iteração**: Para cada categoria, verifica os arquivos existentes
4. **Deducação**: Verifica se o documento já está processado (evita duplicação)
5. **Criação**: Cria registro na tabela `documents` com status `PENDING`
6. **Processamento**: Chama `IngestionService.ingest()` para processar
7. **Parser**: O parser apropriado extrai o texto do arquivo
8. **Chunking**: O texto é dividido em chunks de 700 caracteres
9. **Embedding**: Gera embeddings usando Gemini
10. **Indexação**: Salva os embeddings no PGVector
11. **Atualização**: Atualiza status para `PROCESSED` ou `FAILED`

### Tolerância a falhas

O sistema é projetado para ser tolerante a falhas:

- **Falha individual**: Se um arquivo falhar, apenas esse arquivo é marcado como `FAILED`
- **Continuação**: O processamento continua para os próximos arquivos
- **Logging**: Erros são logados detalhadamente para diagnóstico
- **Recuperação**: Documentos com falha podem ser corrigidos e reprocessados

## 📞 Suporte

Para problemas ou dúvidas:

1. Verifique os logs do container
2. Consulte a seção de solução de problemas
3. Verifique o estado do PostgreSQL
4. Teste com um arquivo simples (como um Markdown)

---

Este guia é complementar ao README.md principal e foca especificamente na operação do DocumentInitializer.