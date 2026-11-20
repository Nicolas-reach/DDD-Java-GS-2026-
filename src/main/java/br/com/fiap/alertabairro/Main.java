package br.com.fiap.alertabairro;

import br.com.fiap.alertabairro.dominio.excecao.RegraNegocioException;
import br.com.fiap.alertabairro.dominio.monitoramento.Alerta;
import br.com.fiap.alertabairro.dominio.monitoramento.Medicao;
import br.com.fiap.alertabairro.dominio.monitoramento.Sensor;
import br.com.fiap.alertabairro.dominio.ocorrencia.HistoricoStatus;
import br.com.fiap.alertabairro.dominio.ocorrencia.Ocorrencia;
import br.com.fiap.alertabairro.dominio.ocorrencia.StatusOcorrencia;
import br.com.fiap.alertabairro.dominio.territorio.Bairro;
import br.com.fiap.alertabairro.dominio.usuario.Permissao;
import br.com.fiap.alertabairro.dominio.usuario.Usuario;
import br.com.fiap.alertabairro.infraestrutura.BancoEmMemoria;
import br.com.fiap.alertabairro.infraestrutura.DadosSimulados;
import br.com.fiap.alertabairro.infraestrutura.ExportadorCsv;
import br.com.fiap.alertabairro.infraestrutura.RelogioSimulado;
import br.com.fiap.alertabairro.notificacao.CanalNotificacao;
import br.com.fiap.alertabairro.notificacao.NotificacaoPainel;
import br.com.fiap.alertabairro.notificacao.NotificacaoSmsSimulada;
import br.com.fiap.alertabairro.servico.AlertaService;
import br.com.fiap.alertabairro.servico.AnaliseService;
import br.com.fiap.alertabairro.servico.MonitoramentoService;
import br.com.fiap.alertabairro.servico.OcorrenciaService;
import br.com.fiap.alertabairro.servico.PontoRecorrente;
import br.com.fiap.alertabairro.servico.ResumoLeituras;
import br.com.fiap.alertabairro.util.Formato;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class Main {

    // Ids da base simulada (iguais aos do script 02_inserir_dados_simulados.sql)
    private static final long ANA = 1;
    private static final long BRUNO = 2;
    private static final long CARLA = 3;
    private static final long DIEGO = 4;
    private static final long ELISA = 5;
    private static final long FABIO = 6;
    private static final long RUA_GARCAS = 1;
    private static final long PONTE = 2;
    private static final long ESCOLA = 3;
    private static final long PRACA = 6;
    private static final long NASCENTE = 7;
    private static final long ALAGAMENTO = 1;
    private static final long ARVORE = 4;
    private static final long LIXO = 5;

    private static final PrintStream OUT = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    /** Os canais escrevem aqui; a demonstração mostra o conteúdo logo depois de cada ação. */
    private static final ByteArrayOutputStream BUFFER_CANAIS = new ByteArrayOutputStream();

    private static RelogioSimulado relogio;
    private static BancoEmMemoria banco;
    private static AlertaService alertas;
    private static OcorrenciaService ocorrencias;
    private static MonitoramentoService monitoramento;
    private static AnaliseService analise;

    private Main() {
    }

    /**
     * Ponto de entrada.
     *
     * @param args não usados
     * @throws IOException se não conseguir gravar os arquivos CSV
     */
    public static void main(String[] args) throws IOException {
        montarAplicacao();

        OUT.println("=".repeat(78));
        OUT.println(" AlertaBairro - Tecnologia comunitária para prevenção de riscos urbanos");
        OUT.println(" Demonstração do domínio em Java (dados simulados)");
        OUT.println("=".repeat(78));

        etapa1BaseSimulada();
        etapa2ChuvaESensores();
        etapa3MoradoresRegistram();
        etapa4RegrasDeNegocio();
        etapa5Acompanhamento();
        etapa6Inteligencia();
        etapa7Integracao();

        OUT.println();
        OUT.println("=".repeat(78));
        OUT.println(" Fim da demonstração. " + Alerta.AVISO_NAO_OFICIAL);
        OUT.println("=".repeat(78));
    }

    /** Liga as peças: repositórios, canais e serviços (injeção de dependência manual). */
    private static void montarAplicacao() {
        relogio = new RelogioSimulado(LocalDateTime.of(2026, 11, 18, 17, 0), ZoneId.of("America/Sao_Paulo"));
        banco = new BancoEmMemoria();
        DadosSimulados.carregar(banco);

        PrintStream saidaCanais = new PrintStream(BUFFER_CANAIS, true, StandardCharsets.UTF_8);
        List<CanalNotificacao> canais = List.of(
                new NotificacaoPainel(saidaCanais),
                new NotificacaoSmsSimulada(saidaCanais));

        alertas = new AlertaService(banco.alertas(), banco.ocorrencias(), banco.usuarios(), canais, relogio);
        ocorrencias = new OcorrenciaService(banco.ocorrencias(), banco.usuarios(), banco.localizacoes(),
                banco.tipos(), alertas, relogio);
        monitoramento = new MonitoramentoService(banco.sensores(), alertas, relogio);
        analise = new AnaliseService(banco.ocorrencias(), banco.bairros());
    }

    // ------------------------------------------------------------------ etapas

    private static void etapa1BaseSimulada() {
        titulo("1. Base simulada carregada (mesmos dados do script SQL)");
        OUT.printf("  %d bairros, %d locais, %d usuários, %d tipos de ocorrência, %d sensores, %d ocorrências"
                        + " históricas%n",
                banco.bairros().contar(), banco.localizacoes().contar(), banco.usuarios().contar(),
                banco.tipos().contar(), banco.sensores().contar(), banco.ocorrencias().contar());

        subtitulo("Bairros monitorados");
        banco.bairros().listarTodos().forEach(b -> OUT.println("  - " + b.resumo()));

        subtitulo("Usuários: mesma classe-mãe, permissões diferentes (herança + polimorfismo)");
        for (Usuario u : banco.usuarios().listarTodos()) {
            OUT.printf("  - %-13s %-19s %s%n", u.getNome(), u.getPerfil().getDescricao(), u.descreverPapel());
            StringBuilder permissoes = new StringBuilder("      ");
            for (Permissao p : Permissao.values()) {
                permissoes.append(p.getDescricao()).append(": ").append(u.temPermissao(p) ? "sim" : "não")
                        .append("   ");
            }
            OUT.println(permissoes.toString().stripTrailing());
        }

        subtitulo("Sensores: cada tipo classifica a leitura do seu jeito");
        monitoramento.listarSensores().forEach(s -> OUT.println("  - " + s.resumo()));
    }

    private static void etapa2ChuvaESensores() {
        titulo("2. 18/11, fim de tarde: começa a chover (módulo de monitoramento)");
        leitura("AB-PL-001", 8);
        leitura("AB-NA-001", 92);

        relogio.avancarMinutos(30);
        leitura("AB-PL-001", 34);
        leitura("AB-NA-001", 138);

        relogio.avancarMinutos(30);
        leitura("AB-PL-001", 58);
        leitura("AB-NA-001", 171);

        relogio.avancarMinutos(10);
        leitura("AB-NA-001", 207);
    }

    private static void etapa3MoradoresRegistram() {
        titulo("3. Moradores registram ocorrências (módulo do morador)");

        relogio.avancarMinutos(10);
        registro(ocorrencias.registrar(ANA, RUA_GARCAS, ALAGAMENTO,
                "Alagou de novo a esquina da Rua das Garças, água na altura do joelho", 4,
                "/uploads/ocorrencias/9/foto-1.jpg", "Esquina alagada"));

        relogio.avancarMinutos(5);
        registro(ocorrencias.registrar(BRUNO, PONTE, ALAGAMENTO,
                "Ponte do Córrego Azul coberta pela água, carros dando meia-volta", 5));

        relogio.avancarMinutos(5);
        registro(ocorrencias.registrar(ELISA, ESCOLA, ALAGAMENTO,
                "Água acumulada na frente da escola na hora da saída dos alunos", 3));

        relogio.avancarMinutos(5);
        registro(ocorrencias.registrar(CARLA, PRACA, ARVORE,
                "Galho pendurado na fiação da praça central, balançando com o vento", 2));

        subtitulo("Ana consulta o protocolo que recebeu");
        Ocorrencia protocolo = ocorrencias.consultarProtocolo(9);
        OUT.println("  " + protocolo.resumo());
        protocolo.getFotos().forEach(f -> OUT.println("    " + f.resumo()));

        Bairro beiraRio = banco.bairros().buscarPorIdOuFalhar(1);
        subtitulo("Registros do " + beiraRio.getNome() + " visíveis para os moradores");
        ocorrencias.listarDoBairro(beiraRio).forEach(o -> OUT.println("  " + o.resumo()));
    }

    private static void etapa4RegrasDeNegocio() {
        titulo("4. Regras de negócio barrando dados e ações inválidas");
        relogio.avancarMinutos(5);

        tentar("Gravidade fora da escala de 1 a 5",
                () -> ocorrencias.registrar(DIEGO, NASCENTE, ALAGAMENTO, "Água subindo na margem do córrego", 7));
        tentar("Relato curto demais para alguém entender o problema",
                () -> ocorrencias.registrar(DIEGO, NASCENTE, ALAGAMENTO, "alagou", 3));
        tentar("Morador tenta mudar o status de uma ocorrência",
                () -> ocorrencias.atualizarStatus(9, ANA, StatusOcorrencia.EM_ANALISE, "Eu vi que alagou"));
        tentar("Agente tenta descartar uma ocorrência (só o administrador pode)",
                () -> ocorrencias.atualizarStatus(11, ELISA, StatusOcorrencia.DESCARTADA, "Parece repetida"));
        tentar("Administrador tenta pular etapas: Aberta direto para Resolvida",
                () -> ocorrencias.atualizarStatus(10, FABIO, StatusOcorrencia.RESOLVIDA, "Resolvido"));
        tentar("Sensor em manutenção envia leitura",
                () -> monitoramento.registrarMedicao("AB-PL-002", 20));
        tentar("Leitura impossível de 950 cm (falha do equipamento)",
                () -> monitoramento.registrarMedicao("AB-NA-002", 950));
        tentar("Consulta de protocolo que não existe",
                () -> ocorrencias.consultarProtocolo(999));
        tentar("Morador tenta encerrar um alerta",
                () -> alertas.encerrar(1, BRUNO));
        tentar("Anexar foto a uma ocorrência já resolvida",
                () -> ocorrencias.consultarProtocolo(1)
                        .adicionarFoto("/uploads/ocorrencias/1/foto-2.jpg", "Foto atrasada", relogio.agora()));
        tentar("Conta desativada tenta registrar ocorrência", () -> {
            Usuario diego = banco.usuarios().buscarPorIdOuFalhar(DIEGO);
            diego.desativar();
            try {
                ocorrencias.registrar(DIEGO, NASCENTE, ALAGAMENTO, "Água subindo na margem do córrego", 3);
            } finally {
                diego.ativar();
            }
        });
    }

    private static void etapa5Acompanhamento() {
        titulo("5. Acompanhamento com histórico (módulo administrativo)");

        relogio.avancarMinutos(10);
        historico(ocorrencias.atualizarStatus(9, ELISA, StatusOcorrencia.EM_ANALISE,
                "Agente confirmou o alagamento no local"));
        relogio.avancarMinutos(5);
        historico(ocorrencias.atualizarStatus(10, ELISA, StatusOcorrencia.EM_ANALISE,
                "Ponte sinalizada com cones pela comunidade"));
        relogio.avancarMinutos(5);
        historico(ocorrencias.atualizarStatus(9, FABIO, StatusOcorrencia.EM_ATENDIMENTO,
                "Subprefeitura acionada para desobstruir os bueiros"));

        relogio.avancarMinutos(5);
        registro(ocorrencias.registrar(BRUNO, PONTE, LIXO, "Teste do aplicativo, pode desconsiderar", 1));
        historico(ocorrencias.atualizarStatus(13, FABIO, StatusOcorrencia.DESCARTADA,
                "Registro de teste, sem problema no local"));

        subtitulo("20:30 - a chuva passa e o córrego baixa");
        relogio.avancarMinutos(85);
        leitura("AB-PL-001", 4);
        leitura("AB-NA-001", 118);

        relogio.avancarMinutos(30);
        historico(ocorrencias.atualizarStatus(9, FABIO, StatusOcorrencia.RESOLVIDA,
                "Água escoou e a via foi liberada"));
        historico(ocorrencias.atualizarStatus(10, FABIO, StatusOcorrencia.RESOLVIDA,
                "Nível do córrego normalizou e a ponte foi liberada"));

        Bairro beiraRio = banco.bairros().buscarPorIdOuFalhar(1);
        int encerrados = alertas.encerrarAtivosDoBairro(beiraRio, ELISA);
        OUT.printf("  %s  Elisa Prado encerrou %d alerta(s) ativo(s) do %s%n",
                hora(), encerrados, beiraRio.getNome());

        subtitulo("Histórico completo do protocolo #9 (transparência para a comunidade)");
        Ocorrencia o9 = ocorrencias.consultarProtocolo(9);
        OUT.println("  Registrada em " + Formato.dataHora(o9.getDataRegistro()) + " por "
                + o9.getAutor().getNome() + " | resolvida em " + Formato.dataHora(o9.getDataResolucao()));
        o9.getHistorico().forEach(h -> OUT.println("    " + h.resumo()));

        subtitulo("Alertas emitidos na noite");
        alertas.listarTodos().forEach(a -> OUT.println("  " + a.resumo()));
    }

    private static void etapa6Inteligencia() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fim = relogio.agora().toLocalDate();
        titulo("6. Módulo de inteligência (" + Formato.data(inicio) + " a " + Formato.data(fim) + ")");

        subtitulo("Ocorrências válidas por bairro (descartadas ficam de fora)");
        Map<Bairro, Long> porBairro = analise.contarPorBairro(inicio, fim);
        porBairro.forEach((bairro, qtd) -> OUT.printf("  %-22s %2d ocorrência(s) | %s por 10 mil habitantes%n",
                bairro.getNome(), qtd, Formato.numero(analise.taxaPorDezMilHabitantes(bairro, inicio, fim))));

        subtitulo("Ocorrências por tipo");
        OUT.println("  " + AnaliseService.formatar(analise.contarPorTipo(inicio, fim), t -> t.getNome()));

        subtitulo("Pontos recorrentes (2 ou mais registros no mesmo local)");
        for (PontoRecorrente p : analise.pontosRecorrentes(2)) {
            OUT.printf("  %-58s %d registros | maior risco %2d | último em %s%n",
                    p.local().getDescricaoCurta(), p.quantidade(), p.maiorIndiceRisco(),
                    Formato.diaHora(p.ultimaOcorrencia()));
        }

        subtitulo("Ocorrências ainda abertas, da mais arriscada para a menos (onde agir primeiro)");
        ocorrencias.listarAtivasPorRisco().forEach(o -> OUT.println("  " + o.resumo()));

        subtitulo("Índice de risco ativo por bairro (soma dos índices das ocorrências abertas)");
        OUT.println("  " + AnaliseService.formatar(analise.indiceRiscoAtivoPorBairro(), Bairro::getNome));

        subtitulo("Resumo das leituras dos sensores");
        for (Sensor s : monitoramento.listarSensores()) {
            ResumoLeituras r = monitoramento.resumirLeituras(s);
            if (r.quantidade() == 0) {
                OUT.printf("  %s  sem leituras (%s)%n", r.codigoSerie(), s.getStatus().getDescricao());
                continue;
            }
            OUT.printf("  %s  %2d leituras | mín %s | máx %s | média %s %s | %d em atenção/crítico%n",
                    r.codigoSerie(), r.quantidade(), Formato.numero(r.minimo()), Formato.numero(r.maximo()),
                    Formato.numero(r.media()), r.unidade(), r.leiturasEmAlerta());
        }
    }

    private static void etapa7Integracao() throws IOException {
        titulo("7. Integração com as outras disciplinas");
        ExportadorCsv exportador = new ExportadorCsv();
        Path arquivoOcorrencias = Path.of("saida", "ocorrencias.csv");
        Path arquivoMedicoes = Path.of("saida", "medicoes.csv");
        int linhasOcorrencias = exportador.exportarOcorrencias(ocorrencias.listarTodas(), arquivoOcorrencias);
        int linhasMedicoes = exportador.exportarMedicoes(monitoramento.listarSensores(), arquivoMedicoes);
        OUT.printf("  Data Science: %s (%d linhas) e %s (%d linhas) gerados para o notebook%n",
                arquivoOcorrencias, linhasOcorrencias, arquivoMedicoes, linhasMedicoes);
        OUT.println("  Database Design: cada classe do domínio corresponde a uma tabela do modelo lógico");
        OUT.println("    Bairro -> BAIRRO | Localizacao -> LOCALIZACAO | Usuario (3 subclasses) -> USUARIO.tp_perfil");
        OUT.println("    TipoOcorrencia -> TIPO_OCORRENCIA | Ocorrencia -> OCORRENCIA | FotoOcorrencia -> FOTO_OCORRENCIA");
        OUT.println("    HistoricoStatus -> HISTORICO_STATUS | Sensor (2 subclasses) -> SENSOR.tp_sensor");
        OUT.println("    Medicao -> MEDICAO | Alerta -> ALERTA");
        OUT.println("  Dynamic Programming: os pontos recorrentes e as ocorrências ativas são os trechos a evitar"
                + " nas rotas");
    }

    // ------------------------------------------------------------- auxiliares

    private static void leitura(String codigoSerie, double valor) {
        int alertasAntes = alertas.listarTodos().size();
        Medicao m = monitoramento.registrarMedicao(codigoSerie, valor);
        OUT.printf("  %s  %-9s %-11s -> %-8s %s%n", hora(), codigoSerie,
                Formato.numero(valor) + " " + m.getSensor().getUnidade(), m.getNivelRisco().getDescricao(),
                m.descrever());
        mostrarNotificacoes();
        if (m.getNivelRisco().exigeAlerta() && alertas.listarTodos().size() == alertasAntes) {
            OUT.println("      (sem novo alerta: já existe um alerta ativo deste sensor com nível igual ou maior)");
        }
    }

    private static void registro(Ocorrencia o) {
        String fotos = o.getFotos().isEmpty() ? "" : " | " + o.getFotos().size() + " foto";
        OUT.printf("  %s  Protocolo #%d por %s (%s): %s em %s | gravidade %d x peso %d = risco %d (%s)%s%n",
                hora(), o.getId(), o.getAutor().getNome(), o.getAutor().getPerfil().getDescricao(),
                o.getTipo().getNome(), o.getLocalizacao().getLogradouro(), o.getGravidade(),
                o.getTipo().getPesoRisco(), o.calcularIndiceRisco(), o.classificarRisco().getDescricao(), fotos);
        mostrarNotificacoes();
    }

    private static void historico(HistoricoStatus h) {
        OUT.printf("  #%d  %s%n", h.getOcorrencia().getId(), h.resumo());
    }

    /**
     * Executa uma ação que deve ser barrada e mostra a exceção.
     * Um único catch de {@link RegraNegocioException} trata também as subclasses
     * AcessoNegadoException e RecursoNaoEncontradoException (polimorfismo).
     */
    private static void tentar(String descricao, Runnable acao) {
        try {
            acao.run();
            OUT.println("  [PERMITIDO] " + descricao);
        } catch (RegraNegocioException e) {
            OUT.println("  [BLOQUEADO] " + descricao);
            OUT.println("      " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        mostrarNotificacoes();
    }

    private static void mostrarNotificacoes() {
        String texto = BUFFER_CANAIS.toString(StandardCharsets.UTF_8);
        if (!texto.isEmpty()) {
            OUT.print(texto);
            BUFFER_CANAIS.reset();
        }
    }

    private static String hora() {
        return HORA.format(relogio.agora());
    }

    private static void titulo(String texto) {
        OUT.println();
        OUT.println(texto);
        OUT.println("-".repeat(78));
    }

    private static void subtitulo(String texto) {
        OUT.println();
        OUT.println("  > " + texto);
    }
}
