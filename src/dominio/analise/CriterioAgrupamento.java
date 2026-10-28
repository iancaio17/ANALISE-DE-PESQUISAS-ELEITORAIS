package dominio.analise;

import dominio.coleta.Entrevista;

/** STRATEGY: define por qual característica da amostra as entrevistas serão agrupadas. */
public interface CriterioAgrupamento {
    /** Nome exibido nos relatórios (ex.: "Região"). */
    String getNome();

    /** Grupo ao qual a entrevista pertence (ex.: "Norte"). */
    String grupoDe(Entrevista entrevista);
}
