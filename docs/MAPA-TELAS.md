# Correspondência com as 22 telas de referência

Formulários de cadastro são janelas JavaFX com Salvar/Cancelar; exclusões pedem confirmação; ações vedadas ao
perfil ficam desabilitadas (e os serviços também as recusam).

| Nº | Tela | Classe(s) | Serviço |
| --- | --- | --- | --- |
| 1 | Login | `TelaLogin` | `AdministracaoService.autenticar` |
| 2 | Início | `TelaInicio` | `PesquisaService`, `DadosDemonstracao` |
| 3 | Administração · Usuários | `admin.AbaUsuarios` | `buscarUsuarios`, `excluirUsuario` |
| 4 | Cadastro de usuário | `admin.FormularioUsuario` | `salvarUsuario` |
| 5 | Permissões | `admin.AbaPermissoes` | perfis do usuário |
| 6 | Parâmetros gerais | `admin.AbaParametros`, `FormularioParametro` | `salvarParametro`, `excluirParametro` |
| 7 | Relatório de usuários | `admin.AbaRelatorioUsuarios` | `gerarRelatorioUsuarios` |
| 8 | Consulta e acompanhamento | `pesquisas.AbaConsultaPesquisas` | `consultar(FiltroPesquisa)` |
| 9 | Cadastro de pesquisa | `pesquisas.FormularioPesquisa` | `salvarPesquisa` |
| 10 | Perguntas | `pesquisas.AbaPerguntas`, `FormularioPergunta` | `salvarPergunta` |
| 11 | Plano amostral e candidatos | `pesquisas.AbaAmostra`, `FormularioMeta` | `salvarMeta`, `associarCandidato` |
| 12 | Configuração da amostra | `coleta.AbaConfiguracaoAmostra`, `FormularioEntrevistador`, `FormularioParticipante` | `ColetaService` |
| 13 | Registro de respostas | `coleta.AbaRegistroRespostas`, `QuestionarioEntrevista`, `CampoResposta` | `registrarEntrevista` |
| 14 | Consulta da coleta | `coleta.AbaConsultaColeta` | `consultarEntrevistas` |
| 15 | Processar pesquisa | `analise.AbaProcessar` | `processarPesquisa` |
| 16 | Resultados estatísticos | `analise.AbaResultados`, `PainelResultado` | `analisar(FiltroEntrevista)` |
| 17 | Comparar pesquisas | `analise.AbaComparar` | `compararPesquisas` |
| 18 | Relatórios e gráficos | `analise.AbaRelatorios`, `TipoGrafico` | `gerarRelatorioConsolidado` |
| 19 | Cadastro de candidatos | `candidatos.AbaCandidatos`, `FormularioCandidato` | `salvarCandidato` |
| 20 | Cadastro de partidos | `candidatos.AbaPartidos`, `FormularioPartido` | `salvarPartido` |
| 21 | Consulta de candidatos | `candidatos.AbaConsultaCandidatos` | `consultarCandidatos` |
| 22 | Indicadores cadastrais | `candidatos.AbaIndicadores` | `gerarIndicadores` |
