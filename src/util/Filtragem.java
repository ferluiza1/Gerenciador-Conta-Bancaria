package util;

import model.ContaCorrente;

import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.groupingBy;

public class Filtragem {
    public static void filtragemContas10000(List<ContaCorrente> contas){
        System.out.println("Contas acima de 10.000:");
         contas.stream()
                .filter(c -> c.getSaldo() >10000)
                .forEach(c -> System.out.println("Titular: " + c.getTitular() + "| Saldo: " + c.getSaldo() + "| Número da conta: " + c.getNumero()));
    }

    public static void exibirSaldoTotal(List<ContaCorrente> contas){
        System.out.println("Saldo total de todas as contas:");
       double saldoTotal = contas.stream()
                .map(c-> c.getSaldo())
                .reduce(0.0,(subtotal,saldo) -> subtotal+saldo);

        System.out.println(" Saldo Total do Banco: \n");
        System.out.println("O valor total armazenado é: R$ " + saldoTotal);
    }
    public static void agruparContasPorFaixa(List<ContaCorrente> contas) {
        System.out.println(" Contas Agrupadas por Faixa de Saldo:");

        var contasAgrupadas = contas.stream()
                .collect(groupingBy(c -> {
                    if (c.getSaldo() <= 5000.0) {
                        return "(a) Até R$ 5000";
                    } else if (c.getSaldo() <= 10000.0) {
                        return "(b) De R$ 5001 a R$ 10000";
                    } else {
                        return "(c) Acima de R$ 10000";
                    }
                }));

        contasAgrupadas.forEach((faixa, lista) -> {
            System.out.println("\n" + faixa + ":");
            lista.forEach(c -> System.out.println("  -> " + c.getTitular() + " | Saldo: R$ " + c.getSaldo()));
        });
    }




}
