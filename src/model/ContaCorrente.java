package model;

import exception.SaldoInsuficienteException;

public class ContaCorrente extends Conta{

    public ContaCorrente(int numero, double saldo, String titular) {
        super(numero, saldo, titular);
    }
    @Override
    public void sacar(double valor) throws SaldoInsuficienteException {
        if(valor> getSaldo()){
            throw new SaldoInsuficienteException("Erro:Saldo insuficiente");
        } if(valor <=0){
            throw new SaldoInsuficienteException("Erro:O valor tem que ser maior que zero");
        }
         saldo-=valor;
        }
    }


