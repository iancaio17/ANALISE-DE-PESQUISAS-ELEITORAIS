# Bibliotecas incluídas

Os JARs JavaFX são distribuições oficiais **OpenJFX 17.0.10** obtidas do Maven Central, sem modificações:

- `org.openjfx:javafx-base:17.0.10`, `javafx-graphics:17.0.10` e `javafx-controls:17.0.10`;
- variantes `linux` (em `lib/linux`) e `win` (em `lib/windows`).

Licença do OpenJFX: GPL versão 2 com a Classpath Exception. Os textos de licença, informações adicionais e
avisos dos componentes gráficos estão em `lib/licencas`. Código-fonte correspondente:
https://github.com/openjdk/jfx17u/tree/17.0.10%2B2.

O Maven resolve o JUnit Jupiter 5.10.2 (EPL 2.0) para os testes. Essa dependência não é empacotada em
`bin/sape.jar`, que contém somente as classes e os recursos deste projeto.
