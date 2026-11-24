package model;

import exception.SaldoInsuficienteException;

public class ContaConjunta extends ContaCorrente{
    private String titlar2;

    public ContaConjunta(int numero, double saldo, String titular, String titlar2) {
        super(numero, saldo, titular);
        this.titlar2 = titlar2;
    }
    public String getTitlar2() {
        return titlar2;
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

