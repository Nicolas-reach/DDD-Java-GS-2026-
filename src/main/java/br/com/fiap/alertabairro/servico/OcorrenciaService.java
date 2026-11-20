package br.com.fiap.alertabairro.servico;

import br.com.fiap.alertabairro.dominio.ocorrencia.HistoricoStatus;
import br.com.fiap.alertabairro.dominio.ocorrencia.Ocorrencia;
import br.com.fiap.alertabairro.dominio.ocorrencia.StatusOcorrencia;
import br.com.fiap.alertabairro.dominio.ocorrencia.TipoOcorrencia;
import br.com.fiap.alertabairro.dominio.territorio.Bairro;
import br.com.fiap.alertabairro.dominio.territorio.Localizacao;
import br.com.fiap.alertabairro.dominio.usuario.Usuario;
import br.com.fiap.alertabairro.repositorio.Repositorio;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Casos de uso das ocorrências: o módulo do morador (registrar e consultar)
 * e o módulo administrativo (atualizar status).
 * <p>
 * O serviço só coordena: busca as entidades nos repositórios, chama os métodos
 * do domínio e salva. As regras ficam nas próprias entidades (separação de
 * responsabilidades).
 */
public class OcorrenciaService {

    private final Repositorio<Ocorrencia> ocorrencias;
    private final Repositorio<Usuario> usuarios;
    private final Repositorio<Localizacao> localizacoes;
    private final Repositorio<TipoOcorrencia> tipos;
    private final AlertaService alertaService;
    private final Clock relogio;

    /**
     * Cria o serviço.
     *
     * @param ocorrencias   repositório de ocorrências
     * @param usuarios      repositório de usuários
     * @param localizacoes  repositório de localizações
     * @param tipos         repositório de tipos de ocorrência
     * @param alertaService serviço de alertas, avisado a cada novo registro
     * @param relogio       relógio do sistema
     */
    public OcorrenciaService(Repositorio<Ocorrencia> ocorrencias, Repositorio<Usuario> usuarios,
                             Repositorio<Localizacao> localizacoes, Repositorio<TipoOcorrencia> tipos,
                             AlertaService alertaService, Clock relogio) {
        this.ocorrencias = ocorrencias;
        this.usuarios = usuarios;
        this.localizacoes = localizacoes;
        this.tipos = tipos;
        this.alertaService = alertaService;
        this.relogio = relogio;
    }

    /**
     * Registra uma ocorrência sem foto e avalia se o bairro precisa de alerta.
     *
     * @param idAutor       quem registra
     * @param idLocalizacao onde aconteceu
     * @param idTipo        categoria do problema
     * @param descricao     relato
     * @param gravidade     gravidade de 1 a 5
     * @return a ocorrência salva, já com número de protocolo
     */
    public Ocorrencia registrar(long idAutor, long idLocalizacao, long idTipo, String descricao, int gravidade) {
        Ocorrencia ocorrencia = criar(idAutor, idLocalizacao, idTipo, descricao, gravidade);
        return concluirRegistro(ocorrencia);
    }

    /**
     * Registra uma ocorrência com foto (sobrecarga do método anterior).
     *
     * @param idAutor       quem registra
     * @param idLocalizacao onde aconteceu
     * @param idTipo        categoria do problema
     * @param descricao     relato
     * @param gravidade     gravidade de 1 a 5
     * @param caminhoFoto   caminho do arquivo da foto
     * @param legendaFoto   legenda da foto
     * @return a ocorrência salva, já com número de protocolo
     */
    public Ocorrencia registrar(long idAutor, long idLocalizacao, long idTipo, String descricao, int gravidade,
                                String caminhoFoto, String legendaFoto) {
        Ocorrencia ocorrencia = criar(idAutor, idLocalizacao, idTipo, descricao, gravidade);
        ocorrencia.adicionarFoto(caminhoFoto, legendaFoto, agora());
        return concluirRegistro(ocorrencia);
    }

    private Ocorrencia criar(long idAutor, long idLocalizacao, long idTipo, String descricao, int gravidade) {
        Usuario autor = usuarios.buscarPorIdOuFalhar(idAutor);
        Localizacao local = localizacoes.buscarPorIdOuFalhar(idLocalizacao);
        TipoOcorrencia tipo = tipos.buscarPorIdOuFalhar(idTipo);
        return new Ocorrencia(autor, local, tipo, descricao, gravidade, agora());
    }

    private Ocorrencia concluirRegistro(Ocorrencia ocorrencia) {
        ocorrencias.salvar(ocorrencia);
        alertaService.avaliarConcentracao(ocorrencia.getBairro());
        return ocorrencia;
    }

    /**
     * Atualiza o status de uma ocorrência (módulo administrativo).
     *
     * @param idOcorrencia  número de protocolo
     * @param idResponsavel quem faz a mudança
     * @param novoStatus    status de destino
     * @param observacao    justificativa
     * @return o registro de histórico criado
     */
    public HistoricoStatus atualizarStatus(long idOcorrencia, long idResponsavel, StatusOcorrencia novoStatus,
                                           String observacao) {
        Ocorrencia ocorrencia = consultarProtocolo(idOcorrencia);
        Usuario responsavel = usuarios.buscarPorIdOuFalhar(idResponsavel);
        HistoricoStatus registro = ocorrencia.alterarStatus(novoStatus, responsavel, observacao, agora());
        ocorrencias.salvar(ocorrencia);
        return registro;
    }

    /**
     * Consulta uma ocorrência pelo número de protocolo.
     *
     * @param protocolo id da ocorrência
     * @return a ocorrência
     */
    public Ocorrencia consultarProtocolo(long protocolo) {
        return ocorrencias.buscarPorIdOuFalhar(protocolo);
    }

    /**
     * Retorna ocorrências ainda em acompanhamento, da mais arriscada para a menos.
     *
     * @return ocorrências ainda em acompanhamento, da mais arriscada para a menos
     */
    public List<Ocorrencia> listarAtivasPorRisco() {
        return ocorrencias.filtrar(Ocorrencia::isAtiva).stream()
                .sorted(Comparator.comparingInt(Ocorrencia::calcularIndiceRisco).reversed()
                        .thenComparing(Ocorrencia::getDataRegistro))
                .toList();
    }

    /**
     * Lista as ocorrências de um bairro (consulta do morador).
     *
     * @param bairro bairro
     * @return ocorrências do bairro, mais recentes primeiro
     */
    public List<Ocorrencia> listarDoBairro(Bairro bairro) {
        return ocorrencias.filtrar(o -> o.getBairro().equals(bairro)).stream()
                .sorted(Comparator.comparing(Ocorrencia::getDataRegistro).reversed())
                .toList();
    }

    /**
     * Retorna todas as ocorrências.
     *
     * @return todas as ocorrências
     */
    public List<Ocorrencia> listarTodas() {
        return ocorrencias.listarTodos();
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(relogio);
    }
}
