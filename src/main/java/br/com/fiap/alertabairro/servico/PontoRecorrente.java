package br.com.fiap.alertabairro.servico;

import br.com.fiap.alertabairro.dominio.territorio.Localizacao;

import java.time.LocalDateTime;

/**
 * Local com problemas repetidos (resultado de consulta do módulo de inteligência).
 *
 * @param local              ponto com ocorrências repetidas
 * @param quantidade         ocorrências válidas no ponto
 * @param maiorIndiceRisco   maior índice de risco entre elas
 * @param ultimaOcorrencia   data da ocorrência mais recente
 */
public record PontoRecorrente(Localizacao local, long quantidade, int maiorIndiceRisco,
                              LocalDateTime ultimaOcorrencia) {
}
