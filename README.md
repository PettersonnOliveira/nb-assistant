# 🤖 NB Assistant

**Conhecimento corporativo, respostas confiáveis.**

Assistente de IA corporativo baseado em **RAG (Retrieval-Augmented Generation)**, desenvolvido para a **NB.CASH** — fintech fictícia de contas digitais para crianças, adolescentes e jovens adultos — como parte do desafio **Alura Agentes**.

## 📋 Sobre o projeto

A NB.CASH possui diversos documentos internos (manuais, políticas e guias) distribuídos entre diferentes áreas, dificultando o acesso rápido às informações.

O **NB Assistant** permite que colaboradores façam perguntas em linguagem natural e recebam respostas objetivas, fundamentadas nos documentos corporativos indexados e acompanhadas das fontes utilizadas.

Quando a informação necessária não está disponível no contexto recuperado, o assistente informa que não encontrou evidências suficientes em vez de simplesmente inventar uma resposta.

## 🚀 Demonstração

🌐 **Aplicação online:** [NB Assistant — OCI](http://163.176.185.227:8080)

### � Dashboard

<p align="center">
  <img src="docs/demo-dashboard.png" width="850">
</p>

<p align="center">
  <em>Visão geral dos documentos, processamento, chunks e perguntas realizadas.</em>
</p>

### 👨‍👩‍👧 Seleção de assunto e consulta

<p align="center">
  <img src="docs/demo-kids-teen.png" width="850">
</p>

<p align="center">
  <em>Seleção de conhecimento e resposta baseada nos documentos da base.</em>
</p>

### � RAG com fontes

<p align="center">
  <img src="docs/demo-pix-pagamentos.png" width="850">
</p>

<p align="center">
  <em>Consulta sobre Pix com resposta contextualizada e fontes recuperadas pelo RAG.</em>
</p>

## � Seleção por categoria

O usuário pode selecionar o assunto antes de realizar uma consulta.

A categoria selecionada é enviada junto com a pergunta e utilizada como filtro na recuperação semântica, restringindo o contexto aos documentos daquela área.

Também existe a opção de consultar **Todos os assuntos**, permitindo uma busca global na base documental.

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
Upload / Documentos pré-indexados
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
Filtro por categoria
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

## 🛠️ Stack tecnológica

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

### � Ingestão e gerenciamento de documentos

- Classificação por categoria
- Persistência dos metadados do documento
- Carga inicial de documentos previamente organizados por categoria

### 🧩 Parsers

O pipeline utiliza `DocumentParser` + `ParserFactory`, permitindo adicionar novos formatos sem alterar o restante da ingestão.

**Formatos validados no ambiente de produção:**

- ✅ PDF
- ✅ DOCX
- ✅ CSV
- ✅ Markdown
- ✅ JSON
- ✅ HTML
- ✅ PPTX
- ✅ XLSX

### 💬 Chat corporativo

- Interface de chat baseada em Thymeleaf
- Perguntas em linguagem natural
- RAG com busca semântica
- Filtro por categoria selecionada
- Opção de busca global em todos os assuntos
- Respostas fundamentadas no contexto recuperado
- Citação de fontes
- Histórico temporário por sessão usando `chatId`
- Memória de contexto com Spring AI
- Renderização de Markdown
- Fallback quando não existe informação suficiente

### 📚 Fontes

Cada resposta pode apresentar:

- documento utilizado;
- página, slide, parágrafo ou seção;
- `similarityScore` como indicador de relevância.

### 📊 Dashboard

O dashboard disponibiliza informações sobre:

- total de documentos;
- documentos processados;
- documentos falhos;
- documentos pendentes/em processamento;
- total de chunks;
- distribuição de documentos por categoria.

> As métricas de embeddings armazenados e perguntas realizadas ainda precisam ser confirmadas na versão final do dashboard.

## 🗂️ Categorias de documentos

- 👥 Recursos Humanos
- 💳 Produtos Financeiros
- 👨‍👩‍👧 Contas Kids e Teen
- ⚖️ Compliance
- 🔐 Segurança
- 🛡️ Prevenção à Fraude
- 📱 Pix e Pagamentos
- 🎧 Atendimento
- 💻 Tecnologia e APIs
- 📢 Marketing
- ⚙️ Operações

## 🧠 Memória de conversa

Cada sessão da interface recebe um `chatId` exclusivo, utilizado pelo Spring AI para manter o contexto temporário da conversa.

Com isso, perguntas de acompanhamento podem utilizar o histórico da mesma sessão.

Ao iniciar uma nova sessão, um novo `chatId` é criado.

## 🧱 Decisões de arquitetura

### Strategy Pattern para parsers

A abstração:

```text
DocumentParser
       ↓
ParserFactory
       ↓
Parser específico
```

permite adicionar novos formatos sem alterar o pipeline principal de ingestão.

### Pipeline de ingestão isolado

O `IngestionService` orquestra:

```text
extração
  ↓
chunking
  ↓
embedding
  ↓
indexação
```

e mantém o status do documento durante o processamento.

### Sem entidade `DocumentChunk`

Os chunks e seus vetores são mantidos no `VectorStore` do Spring AI (`vector_store`) em vez de duplicar o conteúdo em uma tabela adicional.

### Abstração de Storage

O `StorageService` desacopla o armazenamento do restante da aplicação, permitindo utilizar OCI Object Storage ou storage local.

### Seleção de conhecimento por categoria

A categoria selecionada pelo usuário é utilizada no processo de recuperação para restringir a busca aos documentos relacionados ao assunto escolhido.

## 🌐 API

### Upload

```http
POST /api/documents/upload
Content-Type: multipart/form-data
```

Campos:

```text
file
category
```

### Categorias disponíveis

```http
GET /api/documents/categories
```

### Chat

```http
POST /api/chat
Content-Type: application/json
```

Exemplo:

```json
{
  "question": "Qual é o objetivo da Conta Kids?",
  "category": "CONTAS_KIDS_TEEN",
  "chatId": "uuid-da-sessao"
}
```

### Dashboard

```http
GET /api/dashboard
```

### Endpoint de teste

```http
GET /api/test
```

## 🏃 Como rodar localmente

### Pré-requisitos

- Java 21
- Docker/Podman
- PostgreSQL + pgvector
- Chave da API Gemini

### 1. Clone o repositório

```bash
git clone https://github.com/PettersonnOliveira/nb-assistant.git
cd nb-assistant
```

### 2. Configure a chave do Gemini

Linux/macOS:

```bash
export GEMINI_API_KEY=sua-chave-aqui
```

PowerShell:

```powershell
$env:GEMINI_API_KEY="sua-chave-aqui"
```

### 3. Suba a infraestrutura

```bash
docker compose up -d
```

### 4. Execute a aplicação

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Acesse:

```text
http://localhost:8080
```

## ☁️ Deploy na OCI

A aplicação foi implantada em uma **OCI Compute Instance com Oracle Linux 9** para demonstração.

Ambiente de produção/demonstração:

```text
Oracle Cloud Infrastructure
        ↓
Compute Instance
        ↓
Podman rootless
        ├── NB Assistant
        └── PostgreSQL + pgvector
```

Também foram tratados problemas específicos do ambiente OCI/Oracle Linux, incluindo:

- versão do SDK OCI Object Storage;
- regra de ingress da Security List;
- porta 8080 no `firewalld`;
- permissões de `storage-local` com SELinux em modo Enforcing;
- uso do sufixo `:Z` nos volumes;
- execução concorrente de `podman-compose`;
- configuração de credenciais do Google GenAI;
- inicialização do PGVector e do modelo de embeddings.

## 🔒 Configuração e segurança

A API key do Gemini não deve ser versionada no repositório.

A aplicação utiliza variável de ambiente:

```properties
spring.ai.google.genai.api-key=${GEMINI_API_KEY}
```

Mantenha segredos e credenciais fora do Git.

## 🧪 Estado do MVP

### ✅ Núcleo concluído

- Spring Boot + Java 21
- PostgreSQL + pgvector
- Gemini
- Upload e ingestão
- Storage
- Parsing
- Chunking
- Embeddings
- Busca semântica
- Filtro por categoria
- RAG
- Fontes
- Chat
- Memória temporária de sessão
- Dashboard
- DocumentInitializer para carga inicial
- OCI
- Deploy público

### ⚠️ Pendências conhecidas

**Parsers implementados no código, mas ainda pendentes de validação em produção na OCI:**

- XLSX
- PPTX
- HTML
- JSON

Além disso:

- confirmação/ajuste das métricas de embeddings e perguntas no dashboard;
- evolução futura da administração dos documentos previamente indexados.

## 📌 Limitações atuais

O projeto foi construído como um MVP do desafio. Algumas funcionalidades que não fazem parte do escopo obrigatório podem ser evoluídas posteriormente, como autenticação completa, autorização por usuário/departamento, persistência permanente do histórico e observabilidade avançada.

## 👤 Autor

Desenvolvido por [Petterson Oliveira](https://github.com/PettersonnOliveira) como parte do desafio **Alura Agentes**.