package br.com.fiap.alertabairro.infraestrutura;

import br.com.fiap.alertabairro.dominio.monitoramento.Pluviometro;
import br.com.fiap.alertabairro.dominio.monitoramento.Sensor;
import br.com.fiap.alertabairro.dominio.monitoramento.SensorNivelAgua;
import br.com.fiap.alertabairro.dominio.ocorrencia.Ocorrencia;
import br.com.fiap.alertabairro.dominio.ocorrencia.StatusOcorrencia;
import br.com.fiap.alertabairro.dominio.ocorrencia.TipoOcorrencia;
import br.com.fiap.alertabairro.dominio.territorio.Bairro;
import br.com.fiap.alertabairro.dominio.territorio.Localizacao;
import br.com.fiap.alertabairro.dominio.usuario.Administrador;
import br.com.fiap.alertabairro.dominio.usuario.AgenteComunitario;
import br.com.fiap.alertabairro.dominio.usuario.Morador;
import br.com.fiap.alertabairro.dominio.usuario.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Carrega a base SIMULADA da demonstração. Os dados são os mesmos do script
 * {@code 02_inserir_dados_simulados.sql} do Database Design (mesmos ids, nomes e valores),
 * até a ocorrência 8 e a medição 14.
 * <p>
 * Nomes, e-mails, telefones, endereços e medições são fictícios.
 */
public final class DadosSimulados {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DadosSimulados() {
    }

    /**
     * Preenche o banco em memória com a base simulada.
     *
     * @param banco repositórios a preencher (vazios)
     */
    public static void carregar(BancoEmMemoria banco) {
        // BAIRRO
        Bairro beiraRio = banco.bairros().salvar(new Bairro("Jardim Beira-Rio", "São Paulo", "SP", 18_500));
        Bairro esperanca = banco.bairros().salvar(new Bairro("Vila Nova Esperança", "São Paulo", "SP", 22_300));
        Bairro nascentes = banco.bairros().salvar(new Bairro("Parque das Nascentes", "São Paulo", "SP", 14_800));

        // TIPO_OCORRENCIA
        TipoOcorrencia alagamento = banco.tipos().salvar(
                new TipoOcorrencia("Alagamento", "Acúmulo de água na via ou em imóveis", 3));
        TipoOcorrencia bueiro = banco.tipos().salvar(
                new TipoOcorrencia("Bueiro obstruído", "Boca de lobo entupida por lixo, folhas ou terra", 2));
        TipoOcorrencia deslizamento = banco.tipos().salvar(
                new TipoOcorrencia("Deslizamento de terra", "Movimento de terra ou rachaduras em encosta", 3));
        TipoOcorrencia arvore = banco.tipos().salvar(
                new TipoOcorrencia("Queda de árvore", "Árvore ou galho caído bloqueando via ou fiação", 2));
        banco.tipos().salvar(
                new TipoOcorrencia("Lixo acumulado", "Lixo descartado em via pública ou perto de córrego", 1));

        // LOCALIZACAO
        Localizacao garcas = banco.localizacoes().salvar(new Localizacao(
                "Rua das Garças, 120", "Esquina com a Av. Ribeirinha", -23.491200, -46.432100, beiraRio));
        Localizacao ponte = banco.localizacoes().salvar(new Localizacao(
                "Av. Ribeirinha, 900", "Ponte sobre o Córrego Azul", -23.493500, -46.428700, beiraRio));
        Localizacao escola = banco.localizacoes().salvar(new Localizacao(
                "Rua do Porto, 45", "Em frente à escola municipal", -23.489900, -46.435400, beiraRio));
        Localizacao pontoOnibus = banco.localizacoes().salvar(new Localizacao(
                "Rua Esperança, 300", "Ponto de ônibus da praça", -23.512300, -46.451800, esperanca));
        Localizacao viela = banco.localizacoes().salvar(new Localizacao(
                "Travessa São João, 12", "Escadaria da viela", -23.514700, -46.449200, esperanca));
        Localizacao praca = banco.localizacoes().salvar(new Localizacao(
                "Rua das Palmeiras, 510", "Praça central", -23.510800, -46.454600, esperanca));
        Localizacao nascente = banco.localizacoes().salvar(new Localizacao(
                "Rua da Nascente, 88", "Margem do Córrego da Mata", -23.530400, -46.470200, nascentes));
        Localizacao encosta = banco.localizacoes().salvar(new Localizacao(
                "Rua Projetada A, 20", "Encosta atrás da UBS", -23.532900, -46.467500, nascentes));

        // USUARIO (um objeto de cada subclasse: herança na prática)
        Usuario ana = banco.usuarios().salvar(new Morador(
                "Ana Souza", "ana.souza@exemplo.com.br", "(11) 90000-0001", beiraRio, dt("2026-09-01 10:00")));
        Usuario bruno = banco.usuarios().salvar(new Morador(
                "Bruno Lima", "bruno.lima@exemplo.com.br", "(11) 90000-0002", beiraRio, dt("2026-09-03 14:20")));
        Usuario carla = banco.usuarios().salvar(new Morador(
                "Carla Mendes", "carla.mendes@exemplo.com.br", "(11) 90000-0003", esperanca, dt("2026-09-10 09:15")));
        Usuario diego = banco.usuarios().salvar(new Morador(
                "Diego Rocha", "diego.rocha@exemplo.com.br", null, nascentes, dt("2026-09-22 19:40")));
        Usuario elisa = banco.usuarios().salvar(new AgenteComunitario(
                "Elisa Prado", "elisa.prado@exemplo.com.br", "(11) 90000-0005", beiraRio, dt("2026-08-15 08:00")));
        Usuario fabio = banco.usuarios().salvar(new Administrador(
                "Fábio Nunes", "fabio.nunes@exemplo.com.br", "(11) 90000-0006", null, dt("2026-08-01 08:00")));

        // SENSOR
        Sensor nivelAzul = banco.sensores().salvar(
                new SensorNivelAgua("AB-NA-001", ponte, 150, 200, LocalDate.of(2026, 10, 1)));
        Sensor chuvaBeiraRio = banco.sensores().salvar(
                new Pluviometro("AB-PL-001", escola, 25, 50, LocalDate.of(2026, 10, 1)));
        Sensor nivelMata = banco.sensores().salvar(
                new SensorNivelAgua("AB-NA-002", nascente, 120, 180, LocalDate.of(2026, 10, 15)));
        Sensor chuvaEsperanca = banco.sensores().salvar(
                new Pluviometro("AB-PL-002", praca, 25, 50, LocalDate.of(2026, 10, 15)));
        chuvaEsperanca.colocarEmManutencao();

        // MEDICAO histórica (chuva de 03/11 e de 08/11)
        leituras(nivelAzul, "2026-11-03 17:00", 80, "2026-11-03 18:00", 140, "2026-11-03 18:30", 175,
                "2026-11-03 19:00", 210, "2026-11-03 20:00", 160, "2026-11-04 08:00", 90);
        leituras(chuvaBeiraRio, "2026-11-03 17:00", 12, "2026-11-03 18:00", 38, "2026-11-03 18:30", 62,
                "2026-11-03 19:00", 30);
        leituras(nivelMata, "2026-11-08 16:00", 70, "2026-11-08 17:00", 130, "2026-11-08 18:00", 150,
                "2026-11-08 19:00", 100);

        // OCORRENCIA histórica, com fotos e histórico de status
        Ocorrencia o1 = nova(banco, ana, garcas, alagamento,
                "Rua das Garças tomada pela água após chuva forte, carros parados", 4, "2026-11-03 18:40");
        o1.adicionarFoto("/uploads/ocorrencias/1/foto-1.jpg", "Rua das Garças alagada", dt("2026-11-03 18:41"));
        o1.alterarStatus(StatusOcorrencia.EM_ANALISE, elisa, "Agente confirmou o alagamento no local", dt("2026-11-03 19:30"));
        o1.alterarStatus(StatusOcorrencia.RESOLVIDA, fabio, "Água escoou e a via foi liberada", dt("2026-11-04 10:00"));

        Ocorrencia o2 = nova(banco, bruno, ponte, alagamento,
                "Água do córrego subiu e passou por cima da ponte", 5, "2026-11-03 19:05");
        o2.adicionarFoto("/uploads/ocorrencias/2/foto-1.jpg", "Ponte coberta pela água", dt("2026-11-03 19:06"));
        o2.alterarStatus(StatusOcorrencia.EM_ANALISE, elisa, "Nível do córrego confirmado pelo sensor", dt("2026-11-03 19:40"));
        o2.alterarStatus(StatusOcorrencia.RESOLVIDA, fabio, "Nível do córrego voltou ao normal", dt("2026-11-04 09:30"));

        Ocorrencia o3 = nova(banco, ana, escola, bueiro,
                "Bueiro em frente à escola entupido de folhas e lixo", 3, "2026-11-05 07:50");
        o3.alterarStatus(StatusOcorrencia.EM_ANALISE, elisa, "Agente foi ao local", dt("2026-11-05 09:00"));
        o3.alterarStatus(StatusOcorrencia.EM_ATENDIMENTO, fabio, "Limpeza solicitada ao serviço de zeladoria", dt("2026-11-06 14:00"));

        Ocorrencia o4 = nova(banco, carla, pontoOnibus, bueiro,
                "Boca de lobo do ponto de ônibus com lixo acumulado", 2, "2026-11-06 12:15");
        o4.alterarStatus(StatusOcorrencia.EM_ANALISE, fabio, "Registro conferido pela associação", dt("2026-11-07 10:00"));

        Ocorrencia o5 = nova(banco, carla, viela, deslizamento,
                "Rachadura nova na escadaria da viela depois da chuva", 4, "2026-11-07 08:20");
        o5.adicionarFoto("/uploads/ocorrencias/5/foto-1.jpg", "Rachadura na escadaria", dt("2026-11-07 08:21"));
        o5.alterarStatus(StatusOcorrencia.EM_ANALISE, fabio, "Encaminhado para avaliação da Defesa Civil", dt("2026-11-07 09:00"));

        nova(banco, diego, nascente, alagamento,
                "Margem do córrego alagada, água chegando nas casas", 3, "2026-11-08 17:30");

        Ocorrencia o7 = nova(banco, diego, encosta, deslizamento,
                "Terra deslizou na encosta atrás da UBS", 5, "2026-11-08 18:10");
        o7.adicionarFoto("/uploads/ocorrencias/7/foto-1.jpg", "Terra na encosta", dt("2026-11-08 18:12"));
        o7.alterarStatus(StatusOcorrencia.EM_ANALISE, elisa, "Agente a caminho", dt("2026-11-08 18:30"));
        o7.alterarStatus(StatusOcorrencia.EM_ATENDIMENTO, fabio, "Defesa Civil acionada pelo telefone 199", dt("2026-11-08 19:00"));

        Ocorrencia o8 = nova(banco, carla, praca, arvore,
                "Galho grande caiu na praça e bloqueou a calçada", 3, "2026-11-10 06:45");
        o8.alterarStatus(StatusOcorrencia.EM_ANALISE, elisa, "Agente confirmou o galho caído", dt("2026-11-10 07:30"));
        o8.alterarStatus(StatusOcorrencia.RESOLVIDA, fabio, "Galho removido pela equipe", dt("2026-11-10 15:00"));
    }

    private static Ocorrencia nova(BancoEmMemoria banco, Usuario autor, Localizacao local, TipoOcorrencia tipo,
                                   String descricao, int gravidade, String quando) {
        return banco.ocorrencias().salvar(new Ocorrencia(autor, local, tipo, descricao, gravidade, dt(quando)));
    }

    /** Registra pares (data, valor) em ordem no sensor. */
    private static void leituras(Sensor sensor, Object... pares) {
        for (int i = 0; i < pares.length; i += 2) {
            sensor.registrarLeitura(((Number) pares[i + 1]).doubleValue(), dt((String) pares[i]));
        }
    }

    private static LocalDateTime dt(String texto) {
        return LocalDateTime.parse(texto, FORMATO);
    }
}
