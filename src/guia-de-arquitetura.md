# 🏛️ Guia de Arquitetura e Estrutura de Pacotes

Bem-vindos ao guia de arquitetura do **Sistema de Análise de Pesquisas Eleitorais (SAPE)**. 

Este documento foi criado para alinhar toda a equipe sobre onde cada parte do código deve ficar. Estamos utilizando uma **Arquitetura em Camadas**. Isso significa que o nosso código será dividido por responsabilidades, garantindo que o sistema seja fácil de manter, testar e dividir entre os membros do grupo.

Abaixo, apresentamos a árvore de diretórios do projeto e a explicação detalhada de cada pacote.

---

## 🌳 Visão Geral da Árvore de Diretórios

```text
eleicoes/
├── apresentacao/          # Telas e controladores (JavaFX)
├── aplicacao/             # Regras de orquestração e DTOs
├── dominio/               # Classes de negócio puras (Coração do sistema)
├── persistencia/          # Salvar/Carregar dados em arquivos
└── infraestrutura/        # Ferramentas e utilitários globais
```

---

## 📚 Detalhamento das Camadas

### 1. 🖥️ `apresentacao` (Camada Visual / UI)
**Objetivo:** Responsável exclusivamente por interagir com o usuário. Aqui ficam as telas e os Controladores do JavaFX. **Regra de Ouro:** Esta camada *nunca* deve acessar arquivos de texto diretamente ou conter regras de negócio complexas; ela deve apenas chamar os *Serviços* da camada de Aplicação.

* **`principal/`**: Telas globais do sistema, como a tela de Login e o Menu Principal.
* **`admin/`**: Telas de gestão de usuários (CRUD) e configuração de parâmetros gerais.
* **`pesquisa/`**: Telas onde o pesquisador cria e configura as pesquisas eleitorais simuladas.
* **`coleta/`**: Telas focadas no registro das entrevistas e digitação das respostas.
* **`analise/`**: Telas visuais para exibir gráficos, tabelas e relatórios estatísticos.
* **`candidato/`**: Telas para cadastrar e gerenciar candidatos e partidos fictícios.

### 2. ⚙️ `aplicacao` (Casos de Uso e Orquestração)
**Objetivo:** É a ponte entre as Telas (`apresentacao`) e as regras de negócio (`dominio`). É aqui que os fluxos acontecem (ex: *receber os dados da tela de login, validar, e iniciar a sessão*).

* **`servicos/`**: Contém as classes que executam as ações do sistema (ex: `UsuarioService`, `PesquisaService`). As telas chamam os métodos destas classes.
* **`dtos/`**: *Data Transfer Objects* (Objetos de Transferência de Dados). São classes simples usadas apenas para transportar dados entre as telas e os serviços, ajudando a proteger as classes principais do domínio.

### 3. 🧠 `dominio` (Entidades e Regras de Negócio)
**Objetivo:** O coração do software. Aqui não entra JavaFX nem código de leitura de arquivo. Apenas classes Java puras representando os conceitos do mundo real e suas regras estritas.

* **`admin/`**: Classes `Usuario`, `Perfil`, `Parametro`.
* **`pesquisa/`**: Classes `Pesquisa`, `Pergunta`, `PlanoAmostral`.
* **`coleta/`**: Classes `Entrevistador`, `Participante`, `Resposta`.
* **`analise/`**: Classes `Analise`, `Resultado`, `Relatorio`.
* **`candidato/`**: Classes `Candidato`, `Partido`, `Candidatura`.

> **💡 Dica para a equipe:** Se um aluno for responsável pelo módulo "Candidato", ele vai criar as classes dentro de `dominio/candidato/`, os serviços em `aplicacao/servicos/` e as telas em `apresentacao/candidato/`.

### 4. 💾 `persistencia` (Acesso a Dados)
**Objetivo:** Cuidar de salvar e recuperar os dados de forma permanente. Como não usaremos banco de dados, esta camada manipulará arquivos (texto, binário ou JSON/XML).

* **`repositorios/`**: Contém as **Interfaces** que definem o que pode ser feito no "banco" (ex: `salvar()`, `buscarTodos()`, `excluir()`). A camada de `aplicacao` só conversa com essas interfaces.
* **`arquivos/`**: Contém a **implementação real** que abre o arquivo `.dat` ou `.txt` e escreve/lê os bytes de fato.

### 5. 🛠️ `infraestrutura` (Suporte Técnico)
**Objetivo:** Agrupar códigos que prestam suporte a todas as outras camadas do projeto. Coisas genéricas e utilitárias.

* **`sessao/`**: Classes para controlar quem é o usuário logado no momento (ex: `SessaoAtual.getUsuarioLogado()`).
* **`excecoes/`**: Onde criaremos nossos erros personalizados (ex: `AcessoNegadoException`, `CandidatoInvalidoException`). Isso deixa o código elegante e fácil de tratar.
* **`utils/`**: Classes com métodos estáticos que ajudam no dia a dia, como geradores de PDF, formatadores de data, exportadores para CSV, etc.

---

## 🚦 Regras de Convivência do Código (Para a Equipe)

1. **Apresentação não salva arquivos:** A tela (Controller) passa os dados para o `Servico`, e o `Servico` manda o `Repositorio` salvar.
2. **Cada um no seu quadrado:** Se você precisa de uma informação de *Usuário* no módulo de *Pesquisa*, não acesse o repositório de Usuário diretamente. Chame o `UsuarioService`.
3. **Nomes em Minúsculo:** Todos os nomes de pacotes (pastas) devem ser escritos com letras **minúsculas** (ex: `coleta`, e não `Coleta`).
4. **Dados Fictícios:** Lembrem-se sempre que os dados inseridos e processados são apenas para fins didáticos.