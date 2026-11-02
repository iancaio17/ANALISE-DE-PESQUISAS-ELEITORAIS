package persistencia.repositorios;

import dominio.coleta.Entrevista;
import dominio.coleta.Entrevistador;
import dominio.coleta.Participante;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ColetaArquivoRepository {


    private static ColetaArquivoRepository instance;


    private static final String DIR_BASE = "src/persistencia/arquivos/";
    private static final String ARQ_ENTREVISTAS = DIR_BASE + "entrevistas.dat";
    private static final String ARQ_ENTREVISTADORES = DIR_BASE + "entrevistadores.dat";
    private static final String ARQ_PARTICIPANTES = DIR_BASE + "participantes.dat";


    private List<Entrevista> entrevistas;
    private List<Entrevistador> entrevistadores;
    private List<Participante> participantes;


    private ColetaArquivoRepository() {
        garantirDiretorio();
        this.entrevistas = carregarArquivo(ARQ_ENTREVISTAS);
        this.entrevistadores = carregarArquivo(ARQ_ENTREVISTADORES);
        this.participantes = carregarArquivo(ARQ_PARTICIPANTES);
    }


    public static synchronized ColetaArquivoRepository getInstance() {
        if (instance == null) {
            instance = new ColetaArquivoRepository();
        }
        return instance;
    }

    private void garantirDiretorio() {
        File diretorio = new File(DIR_BASE);
        if (!diretorio.exists()) {
            diretorio.mkdirs();
        }
    }



    @SuppressWarnings("unchecked")
    private <T> List<T> carregarArquivo(String caminho) {
        File arquivo = new File(caminho);
        if (!arquivo.exists() || arquivo.length() == 0) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
            return (List<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar arquivo (" + caminho + "): " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private <T> void salvarArquivo(String caminho, List<T> lista) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(caminho))) {
            oos.writeObject(lista);
        } catch (IOException e) {
            throw new RuntimeException("Falha crítica ao salvar dados em " + caminho, e);
        }
    }



    public void salvarEntrevista(Entrevista entrevista) {
        this.entrevistas.add(entrevista);
        salvarArquivo(ARQ_ENTREVISTAS, this.entrevistas);
    }

    public List<Entrevista> listarEntrevistas() {
        return Collections.unmodifiableList(this.entrevistas);
    }



    public void salvarEntrevistador(Entrevistador entrevistador) {
        this.entrevistadores.add(entrevistador);
        salvarArquivo(ARQ_ENTREVISTADORES, this.entrevistadores);
    }

    public List<Entrevistador> listarEntrevistadores() {
        return Collections.unmodifiableList(this.entrevistadores);
    }



    public void salvarParticipante(Participante participante) {
        this.participantes.add(participante);
        salvarArquivo(ARQ_PARTICIPANTES, this.participantes);
    }

    public List<Participante> listarParticipantes() {
        return Collections.unmodifiableList(this.participantes);
    }


    public void limparTudo() {
        this.entrevistas.clear();
        this.entrevistadores.clear();
        this.participantes.clear();
        salvarArquivo(ARQ_ENTREVISTAS, this.entrevistas);
        salvarArquivo(ARQ_ENTREVISTADORES, this.entrevistadores);
        salvarArquivo(ARQ_PARTICIPANTES, this.participantes);
    }
}