package br.com.fiap.alertabairro.dominio.ocorrencia;

import br.com.fiap.alertabairro.dominio.EntidadeBase;
import br.com.fiap.alertabairro.dominio.Validacao;
import br.com.fiap.alertabairro.dominio.usuario.Usuario;
import br.com.fiap.alertabairro.util.Formato;

import java.time.LocalDateTime;

/**
 * Registro de uma mudança de status de uma ocorrência, com quem fez e quando
 * (tabela HISTORICO_STATUS). Dá transparência ao acompanhamento.
 * <p>
 * É imutável e só é criado por {@link Ocorrencia#alterarStatus}, por isso o
 * construtor tem visibilidade de pacote.
 */
public class HistoricoStatus extends EntidadeBase {

    private final Ocorrencia ocorrencia;
    private final StatusOcorrencia statusAnterior;
    private final StatusOcorrencia statusNovo;
    private final String observacao;
    private final Usuario responsavel;
    private final LocalDateTime dataAlteracao;

    HistoricoStatus(Ocorrencia ocorrencia, StatusOcorrencia statusAnterior, StatusOcorrencia statusNovo,
                    String observacao, Usuario responsavel, LocalDateTime dataAlteracao) {
        this.ocorrencia = Validacao.obrigatorio(ocorrencia, "Ocorrência");
        this.statusAnterior = Validacao.obrigatorio(statusAnterior, "Status anterior");
        this.statusNovo = Validacao.obrigatorio(statusNovo, "Status novo");
        this.observacao = Validacao.textoOpcional(observacao, "Observação", 300);
        this.responsavel = Validacao.obrigatorio(responsavel, "Responsável");
        this.dataAlteracao = Validacao.obrigatorio(dataAlteracao, "Data da alteração");
    }

    /**
     * Retorna ocorrência alterada.
     *
     * @return ocorrência alterada
     */
    public Ocorrencia getOcorrencia() {
        return ocorrencia;
    }

    /**
     * Retorna status antes da mudança.
     *
     * @return status antes da mudança
     */
    public StatusOcorrencia getStatusAnterior() {
        return statusAnterior;
    }

    /**
     * Retorna status depois da mudança.
     *
     * @return status depois da mudança
     */
    public StatusOcorrencia getStatusNovo() {
        return statusNovo;
    }

    /**
     * Retorna observação do responsável, ou {@code null}.
     *
     * @return observação do responsável, ou {@code null}
     */
    public String getObservacao() {
        return observacao;
    }

    /**
     * Retorna quem fez a mudança.
     *
     * @return quem fez a mudança
     */
    public Usuario getResponsavel() {
        return responsavel;
    }

    /**
     * Retorna data e hora da mudança.
     *
     * @return data e hora da mudança
     */
    public LocalDateTime getDataAlteracao() {
        return dataAlteracao;
    }

    @Override
    public String resumo() {
        String obs = observacao == null ? "" : " - \"" + observacao + "\"";
        return String.format("%s  %s -> %s  por %s (%s)%s",
                Formato.diaHora(dataAlteracao), statusAnterior.getDescricao(), statusNovo.getDescricao(),
                responsavel.getNome(), responsavel.getPerfil().getDescricao(), obs);
    }
}
