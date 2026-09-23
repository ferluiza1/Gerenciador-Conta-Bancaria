package model;

public enum TarifaTransferenciaStrategy {

    FIXA {
        @Override
        public double calcular(double valorTransferencia) {
            return 5;
        }
    },

    PERCENTUAL {
        @Override
        public double calcular(double valorTransferencia) {
            return valorTransferencia * 0.01;
        }
    },

    ISENTA {
        @Override
        public double calcular(double valorTransferencia) {
            return 0;
        }
    };

    public abstract double calcular(double valorTransferencia);
}