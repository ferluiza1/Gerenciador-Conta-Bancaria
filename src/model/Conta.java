package model;

import exception.SaldoInsuficienteException;

public abstract class Conta {
   protected int numero;
   protected double saldo;
    protected String titular;

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public Conta(int numero, double saldo, String titular) {
        this.numero = numero;
        this.saldo = saldo;
        this.titular = titular;
    }

    abstract void sacar(double valor) throws SaldoInsuficienteException;

    public double depositar(double valor){
        return this.saldo += valor;
    }

    public void imprimirDados(){
        System.out.println("Titular: " + this.titular);
        System.out.println("Saldo da conta: " +this.saldo);
        System.out.println("Número da conta: " + this.numero);
    }


}
