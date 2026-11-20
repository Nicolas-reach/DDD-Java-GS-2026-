# AlertaBairro: Domain Driven Design em Java

FIAP · Global Solution 2026.2 · CitizenTech · Engenharia de Software (2º ano)

| Nome completo | RM |
|---|---|
| `[Alexandre Constantino Furtado Junior]` | `[RM567188]` |
| `[Leonardo Batista de Souza]` | `[RM568558]` |
| `[Matheus Freitas dos Santos]` | `[RM567337]` |
| `[Nicolas Forcione de Oliveira e Souza]` | `[RM566998]` |


## Ficha da CitizenTech

| Campo | Conteúdo |
| --- | --- |
| Nome | AlertaBairro: tecnologia comunitária para prevenção de riscos urbanos |
| Cidade e bairro | [São Paulo-SP] |
| Cidadão beneficiado | Moradores de áreas com histórico de alagamento, perto de córregos e encostas |
| Problema, em uma frase | Os relatos de alagamentos e bueiros obstruídos ficam espalhados, e ninguém consegue ver onde e quando os riscos se repetem |
| Como funciona | Moradores registram ocorrências, sensores (simulados) enviam nível de água e chuva, e a plataforma emite alertas comunitários e mostra os pontos críticos |
| Indicador de impacto | Pontos recorrentes identificados e tempo entre o registro e a resolução de cada ocorrência |
| ODS conectados | 9 (metas 9.1, 9.c), 11 (meta 11.5), 13 (metas 13.1, 13.3) |

## Como executar

**IntelliJ IDEA (recomendado):** File › Open › selecione a pasta `alertabairro-java` (ela tem o `pom.xml`). Abra `src/main/java/br/com/fiap/alertabairro/Main.java` e clique em ▶ Run.

**Terminal (Java 17 ou superior):**

```bash
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
java -cp out br.com.fiap.alertabairro.Main
```

**Maven:** `mvn package` e depois `java -jar target/alertabairro-1.0.0.jar`.

O programa não pede nada pelo teclado. Ele simula uma tarde de chuva em 18/11/2026 e mostra cada módulo funcionando. No fim, grava `saida/ocorrencias.csv` e `saida/medicoes.csv`.

## Estrutura dos pacotes

```
br.com.fiap.alertabairro
├── Main                      demonstração de ponta a ponta
├── dominio                   EntidadeBase, Validacao, NivelRisco
│   ├── territorio            Bairro, Localizacao
│   ├── usuario               Usuario (abstrata) → Morador, AgenteComunitario, Administrador
│   ├── ocorrencia            Ocorrencia (raiz do agregado), FotoOcorrencia, HistoricoStatus, TipoOcorrencia
│   ├── monitoramento         Sensor (abstrata) → SensorNivelAgua, Pluviometro; Medicao; Alerta
│   └── excecao               RegraNegocioException → AcessoNegadoException, RecursoNaoEncontradoException
├── repositorio               Repositorio<T> (interface), RepositorioMemoria<T>
├── servico                   OcorrenciaService, MonitoramentoService, AlertaService, AnaliseService
├── notificacao               CanalNotificacao (interface) → NotificacaoPainel, NotificacaoSmsSimulada
├── infraestrutura            BancoEmMemoria, DadosSimulados, ExportadorCsv, RelogioSimulado
└── util                      Formato
```

## Documentação

A documentação das classes (JavaDoc) está em `docs/javadoc/index.html`. Para gerar de novo:

```bash
javadoc -encoding UTF-8 -docencoding UTF-8 -charset UTF-8 -d docs/javadoc -sourcepath src/main/java -subpackages br.com.fiap.alertabairro
```

## Dados

Todos os dados são simulados: nomes, e-mails, telefones, endereços e medições são fictícios. A base é a mesma do script `02_inserir_dados_simulados.sql` do Database Design.

Os alertas são indicadores comunitários experimentais e **não substituem a Defesa Civil** (em emergência, ligue 199).

## Vídeo 

Link YouTube: https://youtu.be/QnSib0EasHE
