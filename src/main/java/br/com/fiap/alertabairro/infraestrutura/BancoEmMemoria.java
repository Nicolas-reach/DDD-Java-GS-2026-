package br.com.fiap.alertabairro.infraestrutura;

import br.com.fiap.alertabairro.dominio.monitoramento.Alerta;
import br.com.fiap.alertabairro.dominio.monitoramento.Sensor;
import br.com.fiap.alertabairro.dominio.ocorrencia.Ocorrencia;
import br.com.fiap.alertabairro.dominio.ocorrencia.TipoOcorrencia;
import br.com.fiap.alertabairro.dominio.territorio.Bairro;
import br.com.fiap.alertabairro.dominio.territorio.Localizacao;
import br.com.fiap.alertabairro.dominio.usuario.Usuario;
import br.com.fiap.alertabairro.repositorio.Repositorio;
import br.com.fiap.alertabairro.repositorio.RepositorioMemoria;

/**
 * Reúne um repositório para cada agregado do domínio. Faz o papel do banco de dados
 * no protótipo; cada repositório corresponde a uma ou mais tabelas do Database Design.
 * <p>
 * Medições, fotos e histórico não têm repositório próprio: em DDD eles fazem parte
 * dos agregados {@link Sensor} e {@link Ocorrencia} e são salvos junto com eles.
 */
public class BancoEmMemoria {

    private final Repositorio<Bairro> bairros = new RepositorioMemoria<>("Bairro");
    private final Repositorio<Localizacao> localizacoes = new RepositorioMemoria<>("Localização");
    private final Repositorio<Usuario> usuarios = new RepositorioMemoria<>("Usuário");
    private final Repositorio<TipoOcorrencia> tipos = new RepositorioMemoria<>("Tipo de ocorrência");
    private final Repositorio<Ocorrencia> ocorrencias = new RepositorioMemoria<>("Ocorrência");
    private final Repositorio<Sensor> sensores = new RepositorioMemoria<>("Sensor");
    private final Repositorio<Alerta> alertas = new RepositorioMemoria<>("Alerta");

    /** Cria o banco com todos os repositórios vazios. */
    public BancoEmMemoria() {
    }

    /**
     * Retorna repositório da tabela BAIRRO.
     *
     * @return repositório da tabela BAIRRO
     */
    public Repositorio<Bairro> bairros() {
        return bairros;
    }

    /**
     * Retorna repositório da tabela LOCALIZACAO.
     *
     * @return repositório da tabela LOCALIZACAO
     */
    public Repositorio<Localizacao> localizacoes() {
        return localizacoes;
    }

    /**
     * Retorna repositório da tabela USUARIO.
     *
     * @return repositório da tabela USUARIO
     */
    public Repositorio<Usuario> usuarios() {
        return usuarios;
    }

    /**
     * Retorna repositório da tabela TIPO_OCORRENCIA.
     *
     * @return repositório da tabela TIPO_OCORRENCIA
     */
    public Repositorio<TipoOcorrencia> tipos() {
        return tipos;
    }

    /**
     * Retorna repositório das tabelas OCORRENCIA, FOTO_OCORRENCIA e HISTORICO_STATUS.
     *
     * @return repositório das tabelas OCORRENCIA, FOTO_OCORRENCIA e HISTORICO_STATUS
     */
    public Repositorio<Ocorrencia> ocorrencias() {
        return ocorrencias;
    }

    /**
     * Retorna repositório das tabelas SENSOR e MEDICAO.
     *
     * @return repositório das tabelas SENSOR e MEDICAO
     */
    public Repositorio<Sensor> sensores() {
        return sensores;
    }

    /**
     * Retorna repositório da tabela ALERTA.
     *
     * @return repositório da tabela ALERTA
     */
    public Repositorio<Alerta> alertas() {
        return alertas;
    }
}
