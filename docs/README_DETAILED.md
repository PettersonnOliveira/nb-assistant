# 🤖 NB Assistant - Documentação Completa

**Conhecimento corporativo, respostas confiáveis.**

Esta é a documentação detalhada do projeto NB Assistant, incluindo instruções completas de operação, manutenção e desenvolvimento.

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Arquitetura](#arquitetura)
3. [Stack Tecnológica](#stack-tecnológica)
4. [Funcionalidades](#funcionalidades)
5. [API Endpoints](#api-endpoints)
6. [Como Rodar Localmente](#como-rodar-localmente)
7. [Operação e Manutenção](#operação-e-manutenção)
8. [Deploy na OCI](#deploy-na-oci)
9. [Solução de Problemas](#solução-de-problemas)

## 🎯 Visão Geral

Assistente de IA corporativo baseado em **RAG (Retrieval-Augmented Generation)**, desenvolvido para a **NB.CASH** — fintech fictícia de contas digitais para crianças, adolescentes e jovens adultos.

### Funcionamento

O **NB Assistant** permite que colaboradores façam perguntas em linguagem natural e recebam respostas objetivas, fundamentadas nos documentos corporativos indexados e acompanhadas das fontes utilizadas.

### Características Principais

- ✅ Upload de documentos em múltiplos formatos
- ✅ Processamento automático com embeddings
- ✅ Busca semântica com RAG
- ✅ Chat com memória de contexto
- ✅ Filtro por categoria
- ✅ Citação de fontes
- ✅ Dashboard de monitoramento
- ✅ Carga automática de documentos (DocumentInitializer)

## 🏗️ Arquitetura

```text
Usuário
   ↓
Frontend (Thymeleaf)
   ↓
Spring Boot REST API
   ├───────────────────────┬─────────────────────────┐
   ↓                       ↓                         ↓
UploadController      ChatController         DashboardController
   ↓                       ↓                         ↓
UploadService          RagService             DashboardService
   ↓                       ↓
StorageService         Busca semântica
   ↓                       ↓
OCI Object Storage    PostgreSQL + pgvector
   │                       ↑
   └──────────────┐        │
                  ↓        │
             IngestionService
                  ↓
             ParserFactory
                  ↓
        ┌─────────┼──────────┬──────────┐
        ↓         ↓          ↓          ↓
      Parser    Parser     Parser     Parser
        ↓
   ParsedDocument
        ↓
   ChunkService
        ↓
   EmbeddingService
        ↓
     PGVector
        ↓
     Gemini
        ↓
Resposta + fontes
```

## 🔄 Pipeline RAG

```text
Upload
  ↓
Storage
  ↓
Parser Factory
  ↓
Extração
  ↓
Chunking
  ↓
Embeddings
  ↓
PGVector
  ↓
Busca semântica
  ↓
Contexto recuperado
  ↓
Google Gemini
  ↓
Resposta
  ↓
Fontes
```

## 🛠️ Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| Backend | Java 21, Spring Boot 3.5.5, Maven |
| IA | Spring AI 1.1.0, Google Gemini |
| Modelo de chat | `gemini-2.5-flash` |
| Embeddings | `gemini-embedding-2` |
| Banco | PostgreSQL + pgvector |
| Frontend | Thymeleaf, HTML, CSS, JavaScript |
| Containers | Docker / Podman |
| Cloud | Oracle Cloud Infrastructure (OCI Compute) |
| Storage | OCI Object Storage + fallback/local |

## ✨ Funcionalidades

### 📄 Upload e Ingestão

- Upload via `MultipartFile`
- Classificação por categoria
- Persistência dos metadados do documento
- Status de processamento: `PENDING`, `PROCESSING`, `PROCESSED`, `FAILED`
- Registro do motivo da falha
- Storage com OCI Object Storage e fallback/local

### 🔄 Carga Automática (DocumentInitializer)

Veja o guia detalhado em [DOCUMENT_INITIALIZER_GUIDE.md](DOCUMENT_INITIALIZER_GUIDE.md)

### 🧩 Parsers

**Formatos validados:**
- ✅ PDF
- ✅ DOCX
- ✅ CSV
- ✅ Markdown

**Parsers implementados (pendentes de validação na OCI):**
- ⚠️ XLSX
- ⚠️ PPTX
- ⚠️ HTML
- ⚠️ JSON

### 💬 Chat Corporativo

- Interface de chat baseada em Thymeleaf
- Perguntas em linguagem natural
- RAG com busca semântica
- Respostas fundamentadas no contexto recuperado
- Citação de fontes
- Histórico temporário por sessão usando `chatId`
- Memória de contexto com Spring AI
- Renderização de Markdown
- Fallback quando não existe informação suficiente

### 📊 Dashboard

- Total de documentos
- Documentos processados
- Documentos falhos
- Documentos pendentes/em processamento
- Total de chunks
- Distribuição de documentos por categoria

## 🗂️ Categorias de Documentos

- 👥 Recursos Humanos
- 💳 Produtos Financeiros
- 👨‍👩‍👧 Contas Kids e Teen
- ⚖️ Compliance
- 🔐 Segurança
- 🛡️ Prevenção à Fraude
- 📱 Pix e Pagamentos
- 📞 Atendimento
- 🧑‍💻 Tecnologia e APIs
- 📢 Marketing
- ⚙️ Operações

## 🌐 API Endpoints

### Upload

```http
POST /api/documents/upload
Content-Type: multipart/form-data
```

**Campos:**
- `file`: arquivo a ser processado
- `category`: categoria do documento (enum DocumentCategory)

### Chat

```http
POST /api/chat
Content-Type: application/json
```

**Exemplo:**
```json
{
  "question": "Qual é o objetivo da Conta Kids?",
  "chatId": "uuid-da-sessao"
}
```

### Dashboard

```http
GET /api/dashboard
```

### Categorias

```http
GET /api/documents/categories
```

### Listar Documentos

```http
GET /api/documents
```

## 🏃 Como Rodar Localmente

### Pré-requisitos

- Java 21
- Docker/Podman
- PostgreSQL + pgvector
- Chave da API Gemini

### Passo a Passo

```bash
# 1. Clone o repositório
git clone https://github.com/PettersonnOliveira/nb-assistant.git
cd nb-assistant

# 2. Configure a chave do Gemini
export GEMINI_API_KEY=sua-chave-aqui

# 3. (Opcional) Configure documentos pré-cadastrados
mkdir -p storage-local/{ATENDIMENTO,COMPLIANCE,CONTAS_KIDS_TEEN,MARKETING,OPERACOES,PIX_PAGAMENTOS,PREVENCAO_FRAUDE,PRODUTOS_FINANCEIROS,RECURSOS_HUMANOS,SEGURANCA,TECNOLOGIA_APIS}
# Coloque seus arquivos nas respectivas pastas

# 4. Suba a infraestrutura
docker compose up -d

# 5. Execute a aplicação
./mvnw spring-boot:run
```

Acesse: `http://localhost:8080`

## 🐳 Como Rodar com Docker

```bash
# 1. Configure a chave do Gemini
export GEMINI_API_KEY=sua-chave-aqui

# 2. (Opcional) Configure documentos pré-cadastrados
mkdir -p storage-local/{ATENDIMENTO,COMPLIANCE,CONTAS_KIDS_TEEN,MARKETING,OPERACOES,PIX_PAGAMENTOS,PREVENCAO_FRAUDE,PRODUTOS_FINANCEIROS,RECURSOS_HUMANOS,SEGURANCA,TECNOLOGIA_APIS}

# 3. Suba os containers
docker compose up -d --build

# 4. Acesse a aplicação
# http://localhost:8080
```

## 📊 Operação e Manutenção

### Verificação via PostgreSQL

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

### Verificação via API

```bash
# Dashboard
curl http://localhost:8080/api/dashboard

# Categorias
curl http://localhost:8080/api/documents/categories

# Documentos
curl http://localhost:8080/api/documents
```

### Limpeza e Recomeço

```sql
-- Apagar todos os documentos
DELETE FROM documents;

-- Apagar embeddings
DELETE FROM vector_store;

-- Apagar categoria específica
DELETE FROM documents WHERE category = 'ATENDIMENTO';
DELETE FROM vector_store WHERE metadata->>'category' = 'ATENDIMENTO';
```

```bash
# Limpar arquivos físicos
rm -rf storage-local/

# Recriar estrutura
mkdir -p storage-local/{ATENDIMENTO,COMPLIANCE,CONTAS_KIDS_TEEN,MARKETING,OPERACOES,PIX_PAGAMENTOS,PREVENCAO_FRAUDE,PRODUTOS_FINANCEIROS,RECURSOS_HUMANOS,SEGURANCA,TECNOLOGIA_APIS}

# Reiniciar containers
docker-compose down
docker-compose up -d --build
```

## ☁️ Deploy na OCI

A aplicação está publicada em uma **OCI Compute Instance com Oracle Linux 9**.

Ambiente de produção:

```text
Oracle Cloud Infrastructure
        ↓
Compute Instance
        ↓
Podman rootless
        ├── NB Assistant
        └── PostgreSQL + pgvector
```

### Problemas tratados no ambiente OCI:

- Versão incorreta do SDK OCI Object Storage
- Regra de ingress da Security List
- Porta 8080 no `firewalld`
- Permissões de `storage-local` com SELinux em modo Enforcing
- Uso do sufixo `:Z` nos volumes
- Execução concorrente de `podman-compose`

## 🔒 Configuração e Segurança

A API key do Gemini não deve ser versionada no repositório.

A aplicação utiliza variável de ambiente:

```properties
spring.ai.google.genai.api-key=${GEMINI_API_KEY}
```

## 🧪 Estado do MVP

### ✅ Núcleo concluído

- Spring Boot + Java 21
- PostgreSQL + pgvector
- Gemini
- Upload
- Storage
- Parsing
- Chunking
- Embeddings
- Busca semântica
- RAG
- Fontes
- Chat
- Memória temporária de sessão
- Dashboard
- DocumentInitializer
- OCI
- Deploy público

### ⚠️ Pendências conhecidas

- Validação completa de todos os parsers em produção (XLSX, PPTX, HTML, JSON)
- Melhoria da tolerância a erros no parser XLSX
- Persistência permanente do histórico de conversas
- Autenticação e autorização por usuário/departamento
- Observabilidade avançada

## 🚀 Próximos Passos Sugeridos

1. **Validação de parsers**: Testar e validar todos os parsers em produção
2. **Melhoria do XLSX**: Investigar e corrigir problemas de compatibilidade do Apache POI
3. **Autenticação**: Implementar autenticação JWT e autorização por departamento
4. **Histórico persistente**: Salvar histórico de conversas no banco de dados
5. **Observabilidade**: Adicionar Prometheus/Grafana para monitoramento
6. **Tests**: Implementar testes unitários e de integração
7. **CI/CD**: Configurar pipeline automatizado de deploy

## 👤 Autor

Desenvolvido por [Petterson Oliveira](https://github.com/PettersonnOliveira) como parte do desafio **Alura Agentes**.

## 📄 Licença

Este projeto foi desenvolvido para fins educacionais como parte do desafio Alura Agentes.