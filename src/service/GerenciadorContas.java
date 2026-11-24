package service;

import dao.ContaDAO;
import exception.SaldoInsuficienteException;
import model.ContaCorrente;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorContas {

    // === VERSÃO COM BANCO DE DADOS (ATIVA) ===
    private ContaDAO contaDAO = new ContaDAO();

    // === VERSÃO EM MEMÓRIA (COMENTADA) ===
    // private List<ContaCorrente> contas = new ArrayList<>();

    public ContaCorrente buscarConta(int numero) {
        // Versão com Banco de Dados:
        List<ContaCorrente> contas = contaDAO.listar();
        for (ContaCorrente c : contas) {
            if (c.getNumero() == numero) {
                return c;
            }
        }
        return null;

        /* Versão em Memória:
        for (ContaCorrente c : contas) {
            if (c.getNumero() == numero) {
                return c;
            }
        }
        return null;
        */
    }

    public String processadorSolicitacao(String mensagem) {
        String[] partes = mensagem.split(";");
        String operacao = partes[0].trim().toUpperCase();
        int numero = Integer.parseInt(partes[1].trim());
        ContaCorrente conta = buscarConta(numero);

        try {
            switch (operacao) {
                case "CRIAR":
                    if (conta != null) {
                        return "ERRO;CONTA_JA_EXISTE";
                    }
                    double saldoInicial = Double.parseDouble(partes[2].trim());
                    String titular = partes[3].trim();
                    ContaCorrente novaConta = new ContaCorrente(numero, saldoInicial, titular);

                    // Versão com Banco de Dados:
                    contaDAO.inserir(novaConta);

                    // Versão em Memória:
                    // contas.add(novaConta);

                    return "OK;CONTA_CRIADA";

                case "CONSULTAR":
                    if (conta == null) {
                        return "ERRO;CONTA_NAO_ENCONTRADA";
                    }
                    return "SALDO;" + conta.getSaldo();

                case "DEPOSITAR":
                    if (conta == null) {
                        return "ERRO;CONTA_NAO_ENCONTRADA";
                    }
                    double valorDep = Double.parseDouble(partes[2].trim());
                    conta.depositar(valorDep);

                    // Versão com Banco de Dados (se for usar em memória, basta comentar esta linha):
                    contaDAO.atualizarSaldo(numero, conta.getSaldo());

                    return "OK;DEPOSITO_REALIZADO";

                case "SACAR":
                    if (conta == null) {
                        return "ERRO;CONTA_NAO_ENCONTRADA";
                    }
                    double valorSq = Double.parseDouble(partes[2].trim());
                    conta.sacar(valorSq);

                    // Versão com Banco de Dados (se for usar em memória, basta comentar esta linha):
                    contaDAO.atualizarSaldo(numero, conta.getSaldo());

                    return "OK;SAQUE_REALIZADO";

                default:
                    return "ERRO;OPERACAO_INVALIDA";
            }
        } catch (SaldoInsuficienteException e) {
            return "ERRO;SALDO_INSUFICIENTE";
        }
    }
}