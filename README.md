# SAPE — Sistema de Análise de Pesquisas Eleitorais

Projeto da disciplina **Técnicas de Programação 1 (CIC0197) · UnB**. Aplicação desktop JavaFX para cadastrar
pesquisas eleitorais **fictícias**, coletar respostas, processar estatísticas e gerar relatórios. Todo dado deve ser
inventado: o sistema é acadêmico e simulado.

Módulos: **Administração**, **Pesquisas**, **Coleta e Amostra**, **Análise Estatística e Relatórios** e
**Candidatos e Partidos**.

## Como executar

Requisito: **Java 17 ou superior (x64)**. O JavaFX para Windows e Linux já está em `lib`.

| Sistema | Executar | Recompilar (exige JDK) |
| --- | --- | --- |
| Windows | `iniciar.bat` | `compilar.bat` |
| Linux | `bash iniciar.sh` | `bash compilar.sh` |
| macOS / outros | `mvn javafx:run` ou IntelliJ | — |

Extraia o ZIP inteiro antes de abrir. `bin/sape.jar` já vem compilado; depois de alterar o código, recompile.

Login inicial: `admin@simulado.local` / `Admin12345`. Troque a senha em **Administração → Usuários**
(senha vazia na edição mantém a atual).

**Demonstração opcional:** na página Início, como administrador, use **Carregar demonstração fictícia** (uma vez,
antes de cadastrar pesquisas, candidatos e partidos). Cria 4 pesquisas, 3 candidatos, 1 partido, 16 entrevistas e as
contas `pesquisador@simulado.local` / `Pesquisa123` e `analista@simulado.local` / `Analise123`.

### IntelliJ / Maven

Abra o `pom.xml` como projeto Maven (JDK 17+). Execute `mvn javafx:run` ou a classe `apresentacao.Launcher`
(diretório de trabalho na raiz do projeto). Testes: `mvn clean verify`.

## Organização do código

```
src/
  apresentacao/    telas JavaFX (um pacote por módulo; uma classe por aba e por formulário)
  aplicacao/       SistemaContexto (sessão) e serviços de cada módulo
  dominio/         entidades, enums com regras, cálculo estatístico e relatórios
  persistencia/    EstadoSistema (dados) e SistemaRepository (gravação em arquivo)
  infraestrutura/  exceções, validações, senhas e exportação CSV/PDF
resources/         folha de estilos
tests/integracao/  testes JUnit dos serviços, das regras e da persistência
```

Detalhes em [`docs/ARQUITETURA.md`](docs/ARQUITETURA.md); correspondência com as 22 telas em
[`docs/MAPA-TELAS.md`](docs/MAPA-TELAS.md).

### Técnicas de orientação a objetos usadas

- **Herança:** `Pessoa` → `Usuario`, `Entrevistador`, `Participante`, `Candidato`; `RegraNegocioException` →
  exceções específicas; `Relatorio` → relatórios de usuários, resultado, consolidado e comparativo;
  `ServicoBase` → serviços; `ModuloView`/`Aba` → telas.
- **Interfaces:** `CriterioAgrupamento`, `ExportadorRelatorio`, `CampoResposta`, `Navegador`.
- **Polimorfismo:** `descrever()` das pessoas, `gerarLinhas()` dos relatórios, critérios de agrupamento,
  campos de resposta (única, múltipla e discursiva) e `TipoGrafico`.
- **Padrões:** Builder (`Pesquisa.Builder`), Strategy (critérios), Factory (`ExportadorFactory`), Template Method
  (`Relatorio`, `ExportadorArquivo`), Repository / Unit of Work (`SistemaRepository`), enums com comportamento.

## Regras de negócio principais

- **Perfis:** administrador acessa tudo; pesquisador gerencia pesquisas e coleta; analista processa e analisa.
  Candidatos e partidos são consultados por todos e alterados só por administradores. Um usuário pode ter vários
  perfis; usuário inativo não entra; o último administrador ativo é preservado.
- **Senha:** 8+ caracteres, com letra e número (guardada com PBKDF2). E-mail único.
- **Pesquisa:** perguntas, candidatos e metas só mudam enquanto está *planejada*. Intenção de voto e segundo turno
  exigem candidatos. Só candidaturas *Candidato* ou *Validada* (de partido ativo) do mesmo cargo entram, sem duplicar.
  A previsão de entrevistas pode aumentar durante a coleta, nunca ficar abaixo do já realizado.
- **Coleta:** só pesquisa *em coleta*, dentro do período, em região da pesquisa, com número de entrevista único,
  perguntas obrigatórias respondidas e dentro da previsão. Cada participante fictício tem uma entrevista;
  entrevista concluída e seus dados são imutáveis.
- **Análise:** só depois de encerrada. *Não sabe*, *Não respondeu* e *Nenhuma das opções* são contados à parte e
  ficam fora da base percentual. Percentuais com uma casa decimal pelo método do maior resto (somam 100,0%).
  Comparação só entre pesquisas processadas e compatíveis (diferença = B − A em pontos percentuais).
- **Candidatos e partidos:** sigla e número de partido únicos; número de candidato único por cargo; partido com
  candidatos não é excluído; excluir candidato mantém o partido.

## Exportação (CSV e PDF)

Todo relatório exportado leva também os **gráficos**, calculados só com as respostas válidas (as especiais ficam
fora, como na tela): resultados filtrados (geral e agrupamento), consolidado (um gráfico por pergunta) e
comparativo (duas séries, pesquisa A e B). No **PDF** as barras são desenhadas após as tabelas; o **CSV** só guarda
texto, então cada gráfico vira uma tabela de dados com uma coluna de barras em texto (`█`).

## Dados

As alterações são gravadas na hora em `dados/sistema.dat` (cópia anterior em `.bak`). Uma segunda instância é
bloqueada e um arquivo corrompido nunca é sobrescrito. `-Dsape.dados=/pasta` escolhe outro local.

## Verificações realizadas

- **41 testes de integração** (JUnit 5) passando: acesso e perfis, pesquisas, candidatos e partidos, coleta,
  estatística/filtros/comparação e persistência/exportação.
- As **18 abas** dos cinco módulos e o menu principal foram montados com dados de demonstração em JavaFX
  (modo sem tela), sem erros; o aplicativo (`bin/sape.jar`) abre e cria a base.
- Testado em Linux com JDK 21 (compilando para Java 17). Windows e macOS não foram executados nativamente;
  as bibliotecas JavaFX oficiais para Windows estão incluídas.

Bibliotecas de terceiros: [`lib/TERCEIROS.md`](lib/TERCEIROS.md).
