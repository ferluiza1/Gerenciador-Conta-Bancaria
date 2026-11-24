package model;

import exception.SaldoInsuficienteException;

public class ContaCorrente extends Conta {

    public ContaCorrente(int numero, double saldo, String titular) {
        super(numero, saldo, titular);
    }

    @Override
    public synchronized void sacar(double valor) throws SaldoInsuficienteException {
        if (valor > getSaldo()) {
            throw new SaldoInsuficienteException("Erro:Saldo insuficiente");
        }
        if (valor <= 0) {
            throw new SaldoInsuficienteException("Erro:O valor tem que ser maior que zero");
        }
        // A pausa de 0.5 segundos que vai gerar o erro de concorrência
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//        }
        saldo -= valor;
        System.out.println(Thread.currentThread().getName() + " sacou R$ " + valor + ". Saldo atual: R$ " + getSaldo());
    }
}

