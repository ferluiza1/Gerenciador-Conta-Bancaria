package service;

import model.TarifaTransferenciaStrategy;

public class TarifaTransferenciaService {
    public static double calcularTarifa(double valorTransferencia, TarifaTransferenciaStrategy estrategia) {
        return estrategia.calcular(valorTransferencia);
    }
}
