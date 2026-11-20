package br.com.fiap.alertabairro.servico;

import br.com.fiap.alertabairro.dominio.ocorrencia.Ocorrencia;
import br.com.fiap.alertabairro.dominio.ocorrencia.StatusOcorrencia;
import br.com.fiap.alertabairro.dominio.ocorrencia.TipoOcorrencia;
import br.com.fiap.alertabairro.dominio.territorio.Bairro;
import br.com.fiap.alertabairro.dominio.territorio.Localizacao;
import br.com.fiap.alertabairro.repositorio.Repositorio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Módulo de inteligência: análises por bairro, tipo e período, pontos recorrentes
 * e ranking de risco.
 * <p>
 * Ocorrências descartadas (improcedentes ou de teste) ficam fora das análises.
 */
public class AnaliseService {

    private final Repositorio<Ocorrencia> ocorrencias;
    private final Repositorio<Bairro> bairros;

    /**
     * Cria o serviço.
     *
     * @param ocorrencias repositório de ocorrências
     * @param bairros     repositório de bairros
     */
    public AnaliseService(Repositorio<Ocorrencia> ocorrencias, Repositorio<Bairro> bairros) {
        this.ocorrencias = ocorrencias;
        this.bairros = bairros;
    }

    /**
     * Conta as ocorrências válidas de cada bairro em um período, incluindo bairros sem registro.
     *
     * @param inicio primeiro dia (inclusive)
     * @param fim    último dia (inclusive)
     * @return bairro e quantidade, do maior para o menor
     */
    public Map<Bairro, Long> contarPorBairro(LocalDate inicio, LocalDate fim) {
        List<Ocorrencia> noPeriodo = validasNoPeriodo(inicio, fim);
        Map<Bairro, Long> resultado = new LinkedHashMap<>();
        for (Bairro bairro : bairros.listarTodos()) {
            resultado.put(bairro, noPeriodo.stream().filter(o -> o.getBairro().equals(bairro)).count());
        }
        return ordenarDecrescente(resultado);
    }

    /**
     * Conta as ocorrências válidas por tipo em um período.
     *
     * @param inicio primeiro dia (inclusive)
     * @param fim    último dia (inclusive)
     * @return tipo e quantidade, do maior para o menor
     */
    public Map<TipoOcorrencia, Long> contarPorTipo(LocalDate inicio, LocalDate fim) {
        Map<TipoOcorrencia, Long> contagem = validasNoPeriodo(inicio, fim).stream()
                .collect(Collectors.groupingBy(Ocorrencia::getTipo, Collectors.counting()));
        return ordenarDecrescente(contagem);
    }

    /**
     * Encontra locais com problemas repetidos.
     *
     * @param minimoOcorrencias quantidade mínima de ocorrências no mesmo ponto
     * @return pontos recorrentes, do que tem mais ocorrências para o que tem menos
     */
    public List<PontoRecorrente> pontosRecorrentes(int minimoOcorrencias) {
        Map<Localizacao, List<Ocorrencia>> porLocal = validas().stream()
                .collect(Collectors.groupingBy(Ocorrencia::getLocalizacao));
        return porLocal.entrySet().stream()
                .filter(e -> e.getValue().size() >= minimoOcorrencias)
                .map(e -> new PontoRecorrente(e.getKey(), e.getValue().size(),
                        e.getValue().stream().mapToInt(Ocorrencia::calcularIndiceRisco).max().orElse(0),
                        e.getValue().stream().map(Ocorrencia::getDataRegistro).max(Comparator.naturalOrder())
                                .orElse(null)))
                .sorted(Comparator.comparingLong(PontoRecorrente::quantidade).reversed()
                        .thenComparing(Comparator.comparingInt(PontoRecorrente::maiorIndiceRisco).reversed()))
                .toList();
    }

    /**
     * Soma o índice de risco das ocorrências ainda ativas de cada bairro: mostra
     * onde a situação pede mais atenção agora.
     *
     * @return bairro e índice somado, do maior para o menor
     */
    public Map<Bairro, Long> indiceRiscoAtivoPorBairro() {
        Map<Bairro, Long> resultado = new LinkedHashMap<>();
        for (Bairro bairro : bairros.listarTodos()) {
            long soma = ocorrencias.filtrar(o -> o.isAtiva() && o.getBairro().equals(bairro)).stream()
                    .mapToLong(Ocorrencia::calcularIndiceRisco).sum();
            resultado.put(bairro, soma);
        }
        return ordenarDecrescente(resultado);
    }

    /**
     * Taxa de ocorrências por 10 mil habitantes no período, para comparar bairros
     * de tamanhos diferentes.
     *
     * @param bairro bairro com população estimada
     * @param inicio primeiro dia (inclusive)
     * @param fim    último dia (inclusive)
     * @return ocorrências por 10 mil habitantes, ou -1 se a população não foi informada
     */
    public double taxaPorDezMilHabitantes(Bairro bairro, LocalDate inicio, LocalDate fim) {
        if (bairro.getPopulacaoEstimada() == null) {
            return -1;
        }
        long quantidade = validasNoPeriodo(inicio, fim).stream().filter(o -> o.getBairro().equals(bairro)).count();
        return quantidade * 10_000.0 / bairro.getPopulacaoEstimada();
    }

    private List<Ocorrencia> validas() {
        return ocorrencias.filtrar(o -> o.getStatus() != StatusOcorrencia.DESCARTADA);
    }

    private List<Ocorrencia> validasNoPeriodo(LocalDate inicio, LocalDate fim) {
        LocalDateTime de = inicio.atStartOfDay();
        LocalDateTime ate = fim.plusDays(1).atStartOfDay();
        return validas().stream().filter(o -> o.registradaEntre(de, ate)).toList();
    }

    private static <K> Map<K, Long> ordenarDecrescente(Map<K, Long> mapa) {
        return mapa.entrySet().stream()
                .sorted(Map.Entry.<K, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    /**
     * Utilitário para exibir mapas de contagem em uma linha.
     *
     * @param mapa   contagem
     * @param nome   como mostrar a chave
     * @param <K>    tipo da chave
     * @return texto como "Jardim Beira-Rio: 7 | Vila Nova Esperança: 4"
     */
    public static <K> String formatar(Map<K, Long> mapa, Function<K, String> nome) {
        return mapa.entrySet().stream()
                .map(e -> nome.apply(e.getKey()) + ": " + e.getValue())
                .collect(Collectors.joining(" | "));
    }
}
