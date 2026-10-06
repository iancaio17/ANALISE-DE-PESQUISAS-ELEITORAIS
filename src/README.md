# Sistema de Análise de Pesquisas Eleitorais

**IMPORTANTE:** Este é um projeto exclusivamente acadêmico e de caráter simulado. O sistema não deve ser utilizado para realizar campanha eleitoral, propaganda política, persuasão de eleitores, recomendação de voto ou avaliação real de candidatos, partidos ou propostas políticas. Todos os dados utilizados devem ser fictícios[cite: 1].

## 📌 Sobre o Projeto
O objetivo geral deste projeto é desenvolver uma solução desktop computacional que apoie a coleta, organização, processamento e análise de dados de pesquisas eleitorais simuladas[cite: 1].

## 🚀 Tecnologias Utilizadas
* **Linguagem:** Java
* **Interface Gráfica:** JavaFX[cite: 3]
* **Persistência de Dados:** Arquivos (texto, binário ou serialização)[cite: 4]
* **Arquitetura:** Camadas bem definidas (Apresentação, Aplicação, Domínio, Persistência e Infraestrutura)[cite: 4]
* **Paradigmas:** Orientação a Objetos, Design Patterns (Singleton, Factory, Builder, Strategy)[cite: 4]

## 👥 Divisão de Módulos e Responsabilidades
Para o desenvolvimento em grupo de 5 integrantes, o sistema foi dividido da seguinte forma[cite: 6, 7, 8]:

* **Aluno 1 - Administração:** Gestão de contas de usuário (Administrador, Pesquisador, Analista), autenticação, controle de perfis e parâmetros gerais (regiões, escolaridade, etc)[cite: 6, 7].
* **Aluno 2 - Pesquisas:** Gestão das pesquisas, configuração de perguntas e opções de resposta, acompanhamento da coleta[cite: 6, 7].
* **Aluno 3 - Coleta e Amostra:** Gestão das entrevistas simuladas, características da amostra e registro de respostas dos participantes[cite: 6, 7].
* **Aluno 4 - Análise Estatística:** Processamento dos resultados, cálculos estatísticos (respostas por região, escolaridade), comparação de pesquisas, gráficos e exportação de relatórios[cite: 6, 7].
* **Aluno 5 - Candidatos e Partidos:** Gestão de candidatos, partidos políticos fictícios, gerenciamento de situações de candidatura e indicadores cadastrais[cite: 3, 7].

## ⚙️ Regras Gerais
1. A interface deve ser amigável e exibir mensagens claras de erro utilizando exceções personalizadas[cite: 4].
2. Nenhuma classe/entidade deve ser replicada entre pacotes. O uso deve ser feito chamando os serviços do módulo correspondente[cite: 8].
3. O sistema requer autenticação obrigatória (Login e Senha)[cite: 4].
4. Os dados devem ser manipulados respeitando as permissões de cada perfil[cite: 4].