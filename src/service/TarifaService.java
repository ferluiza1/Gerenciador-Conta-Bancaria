package service;

import model.Conta;
import model.TarifaStrategy;

public class TarifaService {

    public static double calcularTarifaMensal(Conta conta, TarifaStrategy estrategia) {
        return estrategia.calcular(conta.getSaldo());
    }
}