package aplicacao.servicos;

import aplicacao.SistemaContexto;
import dominio.admin.Parametro;
import dominio.admin.Perfil;
import dominio.admin.TipoParametro;
import dominio.admin.Usuario;
import dominio.analise.Relatorio;
import dominio.analise.RelatorioUsuarios;
import infraestrutura.excecoes.RegraNegocioException;
import infraestrutura.utils.Validacao;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import persistencia.EstadoSistema;

/** Módulo Administração: usuários, perfis de acesso e parâmetros gerais. Somente o administrador acessa. */
public class AdministracaoService extends ServicoBase {

    public AdministracaoService(SistemaContexto contexto) {
        super(contexto);
    }

    public Usuario autenticar(String email, String senha) {
        return contexto.entrar(email, senha);
    }

    // ------------------------------------------------------------------ usuários

    public List<Usuario> listarUsuarios() {
        return List.copyOf(ler(Perfil.ADMINISTRADOR).getUsuarios());
    }

    /** Busca combinando nome (parte do texto), perfil e status. Critério nulo ou vazio não filtra. */
    public List<Usuario> buscarUsuarios(String nome, Perfil perfil, Boolean ativo) {
        return listarUsuarios().stream()
                .filter(u -> Validacao.chave(u.getNome()).contains(Validacao.chave(nome)))
                .filter(u -> perfil == null || u.possuiPerfil(perfil))
                .filter(u -> ativo == null || u.isAtivo() == ativo)
                .toList();
    }

    /**
     * Cria o usuário (se o id não existir) ou atualiza o existente.
     * Ao editar, senha vazia mantém a senha atual.
     */
    public void salvarUsuario(int id, String nome, String email, String senha, Set<Perfil> perfis, boolean ativo) {
        alterar(Perfil.ADMINISTRADOR, estado -> {
            String emailInformado = Validacao.texto(email, "E-mail");
            boolean emailEmUso = estado.getUsuarios().stream()
                    .anyMatch(u -> u.getId() != id && u.getEmail().equalsIgnoreCase(emailInformado));
            Validacao.exigir(!emailEmUso, "E-mail já utilizado por outro usuário.");

            Usuario existente = estado.buscarUsuario(id).orElse(null);
            if (existente == null) {
                Usuario novo = new Usuario(id, nome, emailInformado, senha, perfis);
                novo.setAtivo(ativo);
                estado.getUsuarios().add(novo);
            } else {
                atualizarUsuario(existente, nome, emailInformado, senha, perfis, ativo);
            }
            exigirAdministradorAtivo(estado);
        });
    }

    private void atualizarUsuario(Usuario usuario, String nome, String email, String senha, Set<Perfil> perfis,
            boolean ativo) {
        boolean propriaConta = contexto.getUsuarioLogado().getId() == usuario.getId();
        Validacao.exigir(!(propriaConta && !ativo), "Você não pode desativar sua própria conta.");
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setPerfis(perfis);
        usuario.setAtivo(ativo);
        if (senha != null && !senha.isEmpty()) {
            usuario.setSenha(senha);
        }
    }

    public void excluirUsuario(int id) {
        alterar(Perfil.ADMINISTRADOR, estado -> {
            Validacao.exigir(contexto.getUsuarioLogado().getId() != id, "Você não pode excluir sua própria conta.");
            Validacao.exigir(estado.getUsuarios().removeIf(u -> u.getId() == id), "Usuário não encontrado.");
            exigirAdministradorAtivo(estado);
        });
    }

    private static void exigirAdministradorAtivo(EstadoSistema estado) {
        boolean existe = estado.getUsuarios().stream().anyMatch(u -> u.isAtivo() && u.isAdministrador());
        Validacao.exigir(existe, "Mantenha pelo menos um administrador ativo.");
    }

    public int proximoIdUsuario() {
        return proximoId(listarUsuarios().stream().mapToInt(Usuario::getId));
    }

    public RelatorioUsuarios gerarRelatorioUsuarios() {
        return new RelatorioUsuarios(listarUsuarios());
    }

    // ------------------------------------------------------------------ parâmetros gerais

    /** Qualquer usuário logado pode consultar os parâmetros (usados nos formulários de todos os módulos). */
    public List<Parametro> listarParametros() {
        return List.copyOf(ler().getParametros());
    }

    /** Valores cadastrados para um tipo de parâmetro (ex.: todas as regiões). */
    public List<String> valores(TipoParametro tipo) {
        return listarParametros().stream().filter(p -> p.getTipo() == tipo).map(Parametro::getValor).toList();
    }

    public void salvarParametro(int id, TipoParametro tipo, String valor) {
        alterar(Perfil.ADMINISTRADOR, estado -> {
            Parametro existente = estado.buscarParametro(id).orElse(null);
            if (existente != null) {
                Validacao.exigir(!emUso(estado, existente), "Este parâmetro já está em uso. Cadastre uma nova categoria.");
            }
            Parametro novo = new Parametro(id, tipo, valor);
            boolean repetido = estado.getParametros().stream().anyMatch(p -> p.getId() != id
                    && p.getTipo() == tipo && p.getValor().equalsIgnoreCase(novo.getValor()));
            Validacao.exigir(!repetido, "Categoria já cadastrada.");
            estado.getParametros().removeIf(p -> p.getId() == id);
            estado.getParametros().add(novo);
        });
    }

    public void excluirParametro(int id) {
        alterar(Perfil.ADMINISTRADOR, estado -> {
            Parametro parametro = estado.buscarParametro(id)
                    .orElseThrow(() -> new RegraNegocioException("Parâmetro não encontrado."));
            Validacao.exigir(!emUso(estado, parametro), "Não é possível excluir uma categoria em uso.");
            estado.getParametros().remove(parametro);
        });
    }

    public int proximoIdParametro() {
        return proximoId(listarParametros().stream().mapToInt(Parametro::getId));
    }

    /** Categoria usada por participantes, regiões de pesquisas ou metas do plano amostral. */
    private static boolean emUso(EstadoSistema estado, Parametro parametro) {
        TipoParametro tipo = parametro.getTipo();
        String valor = parametro.getValor();
        boolean usadaPorParticipante = estado.getParticipantes().stream()
                .anyMatch(p -> ValidadorPerfil.valorDe(p.getPerfilDemografico(), tipo).equalsIgnoreCase(valor));
        boolean usadaPorPesquisa = estado.getPesquisas().stream().anyMatch(pesquisa ->
                (tipo == TipoParametro.REGIAO && pesquisa.getRegioes().stream().anyMatch(valor::equalsIgnoreCase))
                || pesquisa.getPlanoAmostral().getMetasPorPerfil().keySet().stream()
                        .anyMatch(perfil -> ValidadorPerfil.valorDe(perfil, tipo).equalsIgnoreCase(valor)));
        return usadaPorParticipante || usadaPorPesquisa;
    }

    public void exportarRelatorio(Relatorio relatorio, String formato, Path destino) {
        exportar(Perfil.ADMINISTRADOR, relatorio, formato, destino);
    }
}
