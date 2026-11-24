package service;

import exception.SaldoInsuficienteException;
import model.Conta;
import model.ContaCorrente;

import java.util.List;

public class AtendimentoBancario implements Runnable {

    private ContaCorrente conta;
    private int tipoOperacao;
    private double valor;


    public AtendimentoBancario(ContaCorrente conta, int tipoOperacao, double valor) {
        this.conta = conta;
        this.tipoOperacao = tipoOperacao;
        this.valor = valor;
    }

    @Override
    public void run() {
        String nomeThread = Thread.currentThread().getName();
        System.out.println("-> " + nomeThread + " iniciou o atendimento para conta " + conta.getNumero());

        try {
            Thread.sleep(2000);

            switch (tipoOperacao) {
                case 1:
                    System.out.println(nomeThread + "----Consulta Saldo----\n Conta:" + conta.getNumero() + "\n Saldo: " + conta.getSaldo());
                    break;
                case 2:
                    conta.depositar(valor);
                    System.out.println(nomeThread + " [Depósito] Conta: " + conta.getNumero() + " | Valor: R$ " + valor);
                    break;
                case 3:
                    try {
                        conta.sacar(valor);
                        System.out.println(nomeThread + " [Saque] Conta: " + conta.getNumero() + " | Valor: R$ " + valor);
                    } catch (SaldoInsuficienteException e) {
                        System.out.println(nomeThread + " [Falha no Saque] Conta: " + conta.getNumero() + " | Erro: " + e.getMessage());
                    }
                    break;
                case 4:
                    System.out.println(nomeThread + " [Consulta Dados] Titular: " + conta.getTitular() + " | Conta: " + conta.getNumero());
                    break;
                default:
                    System.out.println(nomeThread + " [Erro] Operação inválida.");
            }
        } catch (InterruptedException e) {
            System.out.println(nomeThread + " foi interrompida!");
        }

        System.out.println("-> " + nomeThread + " TERMINOU o atendimento da conta: " + conta.getNumero() + "\n");
    }
}
