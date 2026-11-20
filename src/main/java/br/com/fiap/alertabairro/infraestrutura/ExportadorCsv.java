package br.com.fiap.alertabairro.infraestrutura;

import br.com.fiap.alertabairro.dominio.monitoramento.Medicao;
import br.com.fiap.alertabairro.dominio.monitoramento.Sensor;
import br.com.fiap.alertabairro.dominio.ocorrencia.Ocorrencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Exporta ocorrências e medições em CSV para a análise de dados (Data Science).
 * <p>
 * Os nomes das colunas seguem as colunas das tabelas do Database Design. O separador
 * é vírgula e o decimal é ponto, o padrão do {@code pandas.read_csv}.
 */
public class ExportadorCsv {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** Cria o exportador. */
    public ExportadorCsv() {
    }

    /**
     * Exporta ocorrências.
     *
     * @param lista   ocorrências
     * @param arquivo caminho do arquivo a gerar (as pastas são criadas se preciso)
     * @return quantidade de linhas de dados gravadas
     * @throws IOException se não conseguir gravar
     */
    public int exportarOcorrencias(Collection<Ocorrencia> lista, Path arquivo) throws IOException {
        List<String> linhas = new ArrayList<>();
        linhas.add("id_ocorrencia,dt_registro,nm_bairro,ds_logradouro,vl_latitude,vl_longitude,nm_tipo,"
                + "nr_gravidade,nr_peso_risco,indice_risco,nivel_risco,st_ocorrencia,dt_resolucao,qtd_fotos");
        for (Ocorrencia o : lista) {
            linhas.add(String.join(",",
                    String.valueOf(o.getId()),
                    DATA.format(o.getDataRegistro()),
                    texto(o.getBairro().getNome()),
                    texto(o.getLocalizacao().getLogradouro()),
                    String.format(Locale.ROOT, "%.6f", o.getLocalizacao().getLatitude()),
                    String.format(Locale.ROOT, "%.6f", o.getLocalizacao().getLongitude()),
                    texto(o.getTipo().getNome()),
                    String.valueOf(o.getGravidade()),
                    String.valueOf(o.getTipo().getPesoRisco()),
                    String.valueOf(o.calcularIndiceRisco()),
                    o.classificarRisco().name(),
                    o.getStatus().name(),
                    o.getDataResolucao() == null ? "" : DATA.format(o.getDataResolucao()),
                    String.valueOf(o.getFotos().size())));
        }
        gravar(arquivo, linhas);
        return lista.size();
    }

    /**
     * Exporta todas as medições dos sensores.
     *
     * @param sensores sensores
     * @param arquivo  caminho do arquivo a gerar
     * @return quantidade de linhas de dados gravadas
     * @throws IOException se não conseguir gravar
     */
    public int exportarMedicoes(Collection<Sensor> sensores, Path arquivo) throws IOException {
        List<String> linhas = new ArrayList<>();
        linhas.add("cd_serie,tp_sensor,nm_bairro,dt_medicao,vl_medicao,unidade,nivel_risco");
        int total = 0;
        for (Sensor s : sensores) {
            for (Medicao m : s.getMedicoes()) {
                linhas.add(String.join(",",
                        s.getCodigoSerie(),
                        s.getCodigoTipo(),
                        texto(s.getBairro().getNome()),
                        DATA.format(m.getDataMedicao()),
                        String.format(Locale.ROOT, "%.2f", m.getValor()),
                        s.getUnidade(),
                        m.getNivelRisco().name()));
                total++;
            }
        }
        gravar(arquivo, linhas);
        return total;
    }

    private static void gravar(Path arquivo, List<String> linhas) throws IOException {
        Path pasta = arquivo.toAbsolutePath().getParent();
        if (pasta != null) {
            Files.createDirectories(pasta);
        }
        Files.write(arquivo, linhas, StandardCharsets.UTF_8);
    }

    /** Coloca o texto entre aspas e escapa aspas internas, como manda o formato CSV. */
    private static String texto(String valor) {
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }
}
