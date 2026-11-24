package controller;


import dao.ContaDAO;
import exception.ContaIDesconhecidaException;
import exception.ContaPropriaException;
import model.ContaConjunta;
import model.ContaCorrente;
import exception.SaldoInsuficienteException;
import model.TarifaStrategy;
import model.TarifaTransferenciaStrategy;
import service.AtendimentoBancario;
import service.TarifaService;
import service.TarifaTransferenciaService;
import util.Filtragem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

public class Main {

    public static void salvarContas(List<ContaCorrente> contas) {
        try {
            List<String> linhasParaSalvar = new ArrayList<>();

            for (ContaCorrente c : contas) {
                String linha = c.getNumero() + "," + c.getTitular() + "," + c.getSaldo();
                linhasParaSalvar.add(linha);
            }

            Path caminhoSaida = Paths.get("contas_atualizadas.txt");
            Files.write(caminhoSaida, linhasParaSalvar);

            System.out.println("(Arquivo contas_atualizadas.txt atualizado com sucesso!)");

        } catch (Exception e) {
            System.out.println("Erro ao salvar o arquivo: " + e.getMessage());
        }
    }

    public static void carregarContasDoArquivo(List<ContaCorrente> contas) {

        Path caminho = Paths.get("contas_atualizadas.txt");

        if (!Files.exists(caminho)) {
            System.out.println("Arquivo não encontrado. O banco começará vazio.");
            return;
        }

        try {
            List<String> linhas = Files.readAllLines(caminho);

            for (String linha : linhas) {

                String[] dados = linha.split(",");

                int numero = Integer.parseInt(dados[0].trim());
                String titular = dados[1].trim();
                double saldo = Double.parseDouble(dados[2].trim());

                ContaCorrente conta = new ContaCorrente(numero, saldo,titular );
                contas.add(conta);
            }
            System.out.println("Contas carregadas do arquivo com sucesso!");

        } catch (Exception e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);
        ContaDAO dao = new ContaDAO();

        List<ContaCorrente> contas = dao.listar();

        if (contas.isEmpty()) {
            System.out.println("Banco de dados vazio. Importando dados do arquivo TXT...");
            carregarContasDoArquivo(contas);

            for (ContaCorrente c : contas) {
                dao.inserir(c);
            }
            System.out.println("Importação para o Banco de Dados concluída!");

        } else {
            salvarContas(contas);
        }

        System.out.println("Foram carregadas " + contas.size() + " contas com sucesso\n");

        int opcao = -1;
        while (opcao != 0) {
            System.out.println("1 - Adicionar Conta");
            System.out.println("2 - Depositar");
            System.out.println("3 - Sacar");
            System.out.println("4 - Filtrar contas > R$ 10.000");
            System.out.println("5 - Ver saldo total do banco");
            System.out.println("6 - Agrupar contas por faixa");
            System.out.println("7 - Listar todas as contas ordenadas por saldo");
            System.out.println("8 - Listar todas as contas ordenadas por nome");
            System.out.println("9 - Listar todas as contas com saldo maior que 5000 e pares");
            System.out.println("10 - Aplicar tarifa na conta");
            System.out.println("11 - Listar todas as contas do Banco de Dados");
            System.out.println("12 - Transferir entre contas");
            System.out.println("13 - Remover conta");
            System.out.println("14 - Ver histórico de transferências");
            System.out.println("15 - Atendimento Bancário");
            System.out.println("16 - Conta Conjunta");
            System.out.println("0 - Sair");
            opcao = sc.nextInt();
            switch (opcao) {
                case 1:
                    System.out.println("Digite o nome do titular:");
                    String titular = sc.next();
                    System.out.println("Digite o número da conta:");
                    int numero = sc.nextInt();
                    System.out.println("Digite o saldo da conta:");
                    double saldo = sc.nextDouble();

                    ContaCorrente novaConta = new ContaCorrente(numero, saldo, titular);
                    contas.add(novaConta);
                    System.out.println("Conta cadastrada com sucesso!");
                    dao.inserir(novaConta);
                    salvarContas(contas);
                    break;

                case 2:
                System.out.println("Digite o número da conta para depósito:");
                int numeroBuscado = sc.nextInt();
                ContaCorrente contaParaDeposito = null;

                for (ContaCorrente t : contas) {
                    if (t.getNumero() == numeroBuscado) {
                        contaParaDeposito = t;
                        break;
                    }
                }

                if (contaParaDeposito != null) {
                    System.out.print("Digite o valor para depositar: ");
                    double valorDeposito = sc.nextDouble();
                    contaParaDeposito.depositar(valorDeposito);
                    dao.atualizarSaldo(contaParaDeposito.getNumero(), contaParaDeposito.getSaldo());
                    salvarContas(contas);
                    System.out.println("Depósito salvo!");
                } else {
                    System.out.println("Conta não encontrada!");
                }
                break;

                case 3:
                    System.out.println("Digite o número da conta para saque:");
                    numeroBuscado = sc.nextInt();
                    ContaCorrente contaParaSaque = null;

                    for (ContaCorrente t : contas) {
                        if (t.getNumero() == numeroBuscado) {
                            contaParaSaque = t;
                            break;
                        }
                    }

                    if (contaParaSaque != null) {
                        System.out.print("Digite o valor para saque: ");
                        double valorSaque = sc.nextDouble();
                        try {
                            contaParaSaque.sacar(valorSaque);

                            dao.atualizarSaldo(contaParaSaque.getNumero(), contaParaSaque.getSaldo());
                            salvarContas(contas);
                            System.out.println("Saque salvo!");
                        } catch (SaldoInsuficienteException e) {
                            System.out.println(e.getMessage());
                        }
                    } else {
                        System.out.println("Conta não encontrada!");
                    }
                    break;
                case 4:
                    Filtragem.filtragemContas10000(contas);
                    break;
                case 5:
                    Filtragem.exibirSaldoTotal(contas);
                    break;

                case 6:
                    Filtragem.agruparContasPorFaixa(contas);
                    break;
                case 7:
                    Comparator<ContaCorrente> ordenarSaldo = (f1,f2) -> Double.compare(f2.getSaldo(),f1.getSaldo());
                    contas.sort(ordenarSaldo);
                    System.out.println("\n Lista de Contas Ordenadas por Saldo ");
                    for (ContaCorrente c : contas) {
                        System.out.println("Titular: " + c.getTitular() + " | Saldo: " + c.getSaldo());
                    }
                    break;
                case 8:
                    Comparator<ContaCorrente> ordernarNome = (c1,c2) -> c1.getTitular().compareToIgnoreCase(c2.getTitular());
                    contas.sort(ordernarNome);
                    System.out.println("\n Lista de Contas (Ordem Alfabética)");
                    for (ContaCorrente c : contas) {
                        System.out.println("Titular: " + c.getTitular() + " | Saldo: " + c.getSaldo());
                    }
                    break;
                case 9:
                    Predicate<ContaCorrente> saldoMaior5000 = c -> c.getSaldo() > 5000.0;
                    Predicate<ContaCorrente> numeroPar = c -> c.getNumero() % 2 == 0;

                    System.out.println("\n Contas com Saldo > R$ 5.000 ");
                    contas.stream()
                            .filter(saldoMaior5000)
                            .forEach(c -> c.imprimirDados());

                    System.out.println("\n Contas com Número Par ");
                    contas.stream()
                            .filter(numeroPar)
                            .forEach(c -> c.imprimirDados());

                    System.out.println("\n Número Par E Saldo > 5000");
                    contas.stream()
                            .filter(numeroPar.and(saldoMaior5000))
                            .forEach(c -> c.imprimirDados());
                    break;
                case 10:
                    System.out.println("Digite o número da conta para aplicar a tarifa:");
                    int numContaAlvo = sc.nextInt();
                    ContaCorrente contaAlvo = null;

                    for (ContaCorrente c : contas) {
                        if (c.getNumero() == numContaAlvo) {
                            contaAlvo = c;
                            break;
                        }
                    }

                    if (contaAlvo != null) {
                        System.out.println("Escolha a tarifa:");
                        System.out.println("1 - Fixa (Desconta R$ 10,00)");
                        System.out.println("2 - Percentual (Desconta 1% do saldo)");
                        System.out.println("3 - Isenta");
                        int opTarifa = sc.nextInt();

                        TarifaStrategy estrategia = TarifaStrategy.ISENTA;
                        if (opTarifa == 1) {
                            estrategia = TarifaStrategy.FIXA;
                        } else if (opTarifa == 2) {
                            estrategia = TarifaStrategy.PERCENTUAL;
                        }

                        double valorCalculado = TarifaService.calcularTarifaMensal(contaAlvo, estrategia);

                        if (valorCalculado > 0) {
                            try {
                                contaAlvo.sacar(valorCalculado);
                                salvarContas(contas);
                                dao.atualizarSaldo(contaAlvo.getNumero(), contaAlvo.getSaldo());
                                System.out.printf("Tarifa de R$ %.2f cobrada e salva no BD! Novo saldo: R$ %.2f\n", valorCalculado, contaAlvo.getSaldo());
                            } catch (SaldoInsuficienteException e) {
                                System.out.println("Erro: " + e.getMessage());
                            }
                        } else {
                            System.out.println("Conta isenta. Nenhuma tarifa foi cobrada.");
                        }

                    } else {
                        System.out.println("Conta não encontrada!");
                    }
                    break;
                case 11:
                    System.out.println("\n--- CONTAS SALVAS NO BANCO DE DADOS ---");
                    List<ContaCorrente> contasDoBanco = dao.listar();

                    if (contasDoBanco.isEmpty()) {
                        System.out.println("Nenhuma conta encontrada no banco.");
                    } else {
                        for (ContaCorrente c : contasDoBanco) {
                            System.out.println("Número: " + c.getNumero() +
                                    " | Titular: " + c.getTitular() +
                                    " | Saldo: R$ " + c.getSaldo());
                        }
                    }
                    break;
                case 12:
                    System.out.println("Digite a conta de ORIGEM:");
                    int numOrigem = sc.nextInt();
                    System.out.println("Digite a conta de DESTINO:");
                    int numDestino = sc.nextInt();
                    System.out.println("Digite o valor da transferência:");
                    double valorTransferencia = sc.nextDouble();

                    System.out.println("Escolha a tarifa a ser aplicada:");
                    System.out.println("1 - Isenta (Sem taxa)");
                    System.out.println("2 - Fixa (Taxa de R$ 5,00)");
                    System.out.println("3 - Percentual (Taxa de 1%)");
                    int opTipo = sc.nextInt();

                    TarifaTransferenciaStrategy tipo = TarifaTransferenciaStrategy.ISENTA;
                    if (opTipo == 2) tipo = TarifaTransferenciaStrategy.FIXA;
                    if (opTipo == 3) tipo = TarifaTransferenciaStrategy.PERCENTUAL;

                    double tarifaCalculada = TarifaTransferenciaService.calcularTarifa(valorTransferencia, tipo);

                    boolean origemExiste = false;
                    boolean destinoExiste = false;
                    ContaCorrente contaOrigem = null;

                    List<ContaCorrente> contasDoTipo = dao.listar();

                    for (ContaCorrente c : contasDoTipo) {
                        if (c.getNumero() == numOrigem) {
                            origemExiste = true;
                            contaOrigem = c;
                        }
                        if (c.getNumero() == numDestino) {
                            destinoExiste = true;
                        }
                    }

                    try {
                        if (!origemExiste || !destinoExiste) {
                            throw new ContaIDesconhecidaException("Erro: A conta de origem ou destino não existe no banco de dados.");
                        }

                        if (numOrigem == numDestino) {
                            throw new ContaPropriaException("Erro: Não é possível realizar uma transferência para a própria conta.");
                        }

                        if (valorTransferencia <= 0) {
                            throw new SaldoInsuficienteException("Erro: O valor da transferência tem que ser maior que zero.");
                        }


                        if ((valorTransferencia + tarifaCalculada) > contaOrigem.getSaldo()) {
                            throw new SaldoInsuficienteException("Erro: Saldo insuficiente para cobrir o valor + tarifa.");
                        }


                        dao.transferirComTarifa(numOrigem, numDestino, valorTransferencia, tarifaCalculada);
                        contas = dao.listar();
                        salvarContas(contas);

                        System.out.printf("Tarifa cobrada na operação: R$ %.2f\n", tarifaCalculada);

                    } catch (ContaIDesconhecidaException | ContaPropriaException | SaldoInsuficienteException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 13:
                    System.out.println("Digite o número da conta que deseja EXCLUIR:");
                    int numeroExcluir = sc.nextInt();

                    dao.remover(numeroExcluir);

                    contas.removeIf(c -> c.getNumero() == numeroExcluir);

                    salvarContas(contas);
                    break;
                case 14:
                    dao.listarHistoricoTransferencias();
                    break;
                case 15:
                    System.out.println("Digite o número da conta para o atendimento:");
                    int numeroConta = sc.nextInt();
                    ContaCorrente contaEscolhida = null;

                    for (ContaCorrente c : contas) {
                        if (c.getNumero() == numeroConta) {
                            contaEscolhida = c;
                            break;
                        }
                    }

                    if (contaEscolhida != null) {
                        System.out.println("Digite o valor que deseja depositar nesta sessão:");
                        double valorDep = sc.nextDouble();

                        System.out.println("Digite o valor que deseja sacar nesta sessão:");
                        double valorSaq = sc.nextDouble();

                        System.out.println("\n--- DISPARANDO 4 CAIXAS AO MESMO TEMPO ---");

                        Runnable a1 = new AtendimentoBancario(contaEscolhida, 1, 0);
                        Runnable a2 = new AtendimentoBancario(contaEscolhida, 2, valorDep);
                        Runnable a3 = new AtendimentoBancario(contaEscolhida, 3, valorSaq);
                        Runnable a4 = new AtendimentoBancario(contaEscolhida, 4, 0);

                        Thread t1 = new Thread(a1, "Caixa-01");
                        Thread t2 = new Thread(a2, "Caixa-02");
                        Thread t3 = new Thread(a3, "Caixa-03");
                        Thread t4 = new Thread(a4, "Caixa-04");

                        t1.start();
                        t2.start();
                        t3.start();
                        t4.start();

                        try {
                            t1.join();
                            t2.join();
                            t3.join();
                            t4.join();
                        } catch (InterruptedException e) {
                            System.out.println("Erro: " + e.getMessage());
                        }

                        dao.atualizarSaldo(contaEscolhida.getNumero(), contaEscolhida.getSaldo());
                        salvarContas(contas);

                        System.out.println("\nTodos os atendimentos foram concluídos!");

                    } else {
                        System.out.println("Erro: Conta não encontrada.");
                    }
                    break;
                case 16:
                    System.out.println("\n--CONTA CONJUNTA--");
                    System.out.println("Digite o número da conta:");
                    int numConta = sc.nextInt();
                    sc.nextLine();
                    System.out.println("Digite o nome do primeiro titular:");
                    String titular1 = sc.nextLine();

                    System.out.println("Digite o nome do segundo titular:");
                    String titular2 = sc.nextLine();

                    System.out.println("Digite o saldo inicial da conta:");
                    double saldoInicial = sc.nextDouble();
                    System.out.println("Digite o valor que ambos tentarão sacar ao mesmo tempo:");
                    double valorSaque = sc.nextDouble();
                    ContaConjunta contaFamilia = new ContaConjunta(numConta, saldoInicial, titular1, titular2);
                    Runnable tarefaA = new AtendimentoBancario(contaFamilia, 3, valorSaque);
                    Runnable tarefaB = new AtendimentoBancario(contaFamilia, 3, valorSaque);

                    Thread threadA = new Thread(tarefaA, titular1);
                    Thread threadB = new Thread(tarefaB, titular2);
                    threadA.start();
                    threadB.start();

                    try {
                        threadA.join();
                        threadB.join();
                    } catch (InterruptedException e) {
                        System.out.println("Erro: " + e.getMessage());
                    }

                    System.out.println("\nSaldo Final da Conta Conjunta: R$ " + contaFamilia.getSaldo());
                    break;


                case 0:
                    System.out.println("Saiu!");
                    break;
            }
        }
    }
}