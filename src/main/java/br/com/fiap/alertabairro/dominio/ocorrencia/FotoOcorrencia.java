package br.com.fiap.alertabairro.dominio.ocorrencia;

import br.com.fiap.alertabairro.dominio.EntidadeBase;
import br.com.fiap.alertabairro.dominio.Validacao;
import br.com.fiap.alertabairro.util.Formato;

import java.time.LocalDateTime;

/**
 * Foto enviada como evidência de uma ocorrência (tabela FOTO_OCORRENCIA).
 * Guarda o caminho do arquivo, não a imagem.
 * <p>
 * O construtor tem visibilidade de pacote: só {@link Ocorrencia#adicionarFoto}
 * cria fotos, o que garante que toda foto pertence a uma ocorrência válida.
 */
public class FotoOcorrencia extends EntidadeBase {

    private final Ocorrencia ocorrencia;
    private final String caminhoArquivo;
    private final String legenda;
    private final LocalDateTime dataEnvio;

    FotoOcorrencia(Ocorrencia ocorrencia, String caminhoArquivo, String legenda, LocalDateTime dataEnvio) {
        this.ocorrencia = Validacao.obrigatorio(ocorrencia, "Ocorrência");
        this.caminhoArquivo = Validacao.texto(caminhoArquivo, "Caminho do arquivo", 5, 255);
        this.legenda = Validacao.textoOpcional(legenda, "Legenda", 150);
        this.dataEnvio = Validacao.obrigatorio(dataEnvio, "Data de envio");
    }

    /**
     * Retorna ocorrência a que a foto pertence.
     *
     * @return ocorrência a que a foto pertence
     */
    public Ocorrencia getOcorrencia() {
        return ocorrencia;
    }

    /**
     * Retorna caminho do arquivo no armazenamento.
     *
     * @return caminho do arquivo no armazenamento
     */
    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    /**
     * Retorna legenda, ou {@code null}.
     *
     * @return legenda, ou {@code null}
     */
    public String getLegenda() {
        return legenda;
    }

    /**
     * Retorna data e hora do envio.
     *
     * @return data e hora do envio
     */
    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    @Override
    public String resumo() {
        String texto = legenda == null ? caminhoArquivo : legenda + " [" + caminhoArquivo + "]";
        return "Foto " + texto + " enviada em " + Formato.diaHora(dataEnvio);
    }
}
