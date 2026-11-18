# Arquitetura

## Camadas

| Camada | Pacote | Responsabilidade |
| --- | --- | --- |
| Apresentação | `apresentacao` | Telas JavaFX. Não contém regra de negócio: lê dados dos serviços e mostra erros |
| Aplicação | `aplicacao` | `SistemaContexto` (sessão) e os serviços dos cinco módulos |
| Domínio | `dominio` | Entidades, enums com regras, estatística e relatórios |
| Persistência | `persistencia` | `EstadoSistema` (todos os dados) e `SistemaRepository` (arquivo) |
| Infraestrutura | `infraestrutura` | Exceções, validações, senhas e exportadores |

A dependência segue um só sentido: apresentação → aplicação → domínio, com persistência e infraestrutura abaixo.

## Fluxo de uma operação

1. A tela chama um método do serviço (ex.: `ColetaService.registrarEntrevista`).
2. O serviço (`ServicoBase.alterar`) abre uma **cópia** do estado, confere o perfil do usuário logado e aplica a regra.
3. Se nada falhou, o repositório grava a cópia em arquivo temporário e só então a publica. Qualquer
   `RegraNegocioException` descarta a cópia: nada é salvo pela metade.
4. A tela mostra a mensagem do erro, se houver, e recarrega a aba.

## Telas

- `SapeApp` → `TelaLogin` → `TelaPrincipal` (menu lateral) → um `ModuloView` por módulo.
- `ModuloView` é um `TabPane`; cada aba é uma subclasse de `Aba`, criada de novo a cada carga para refletir
  sempre os dados atuais.
- Formulários (`Formulario*`) são janelas com Salvar/Cancelar. Componentes comuns ficam em `componentes`
  (`Componentes`, `Dialogos`, `Leitura`, `BarraExportacao`).
- Filtros de consulta são objetos de aplicação (`FiltroPesquisa`, `FiltroEntrevista`) que implementam
  `Predicate`, para que a tela só os preencha.

## Domínio

- `Pessoa` (abstrata) é a base de `Usuario`, `Entrevistador`, `Participante` e `Candidato`.
- Enums carregam regras: `SituacaoPesquisa.permiteProcessamento()`, `TipoPesquisa.exigeCandidatos()`,
  `SituacaoCandidatura.podeParticiparDePesquisa()`, `RespostaEspecial.ehEspecial()`.
- `ProcessadorEstatistico` calcula `Distribuicao`s (geral e por `CriterioAgrupamento`); `CalculadoraPercentual`
  aplica o maior resto.
- `Relatorio` (Template Method) define `paraTexto()` a partir de `gerarLinhas()` de cada subclasse e expõe
  `gerarGraficos()` (objetos `Grafico`, sem depender de formato). Os exportadores CSV/PDF recebem qualquer
  `Relatorio`: o PDF desenha as barras (`DesenhistaDeGraficos`) e o CSV grava a tabela do gráfico.

## Persistência

Um único arquivo serializado (`dados/sistema.dat`) guarda todos os módulos. A gravação é atômica (arquivo
temporário + cópia `.bak`), há bloqueio contra duas instâncias e a leitura de um arquivo inválido gera erro
explícito, sem recriar a base.

## Escopo

Aplicação local, sem servidor. Processamento síncrono. A estrutura de uma pesquisa fica imutável depois de
iniciar a coleta. Não há migração de bases de versões anteriores.
