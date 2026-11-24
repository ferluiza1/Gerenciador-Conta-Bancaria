package rede;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClienteBanco {
    public static void main(String[] args) {
        String host = "localhost";
        int porta = 5000;

        try (Socket socket = new Socket(host, porta);
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado ao servidor bancário!");

            while (true) {
                System.out.println("\n=== MENU BANCO ===");
                System.out.println("1 - Criar conta");
                System.out.println("2 - Consultar saldo");
                System.out.println("3 - Depositar");
                System.out.println("4 - Sacar");
                System.out.println("0 - Sair");
                System.out.print("Escolha uma opção: ");

                int opcao = scanner.nextInt();
                scanner.nextLine(); // limpa a quebra de linha

                if (opcao == 0) {
                    saida.println("SAIR");
                    System.out.println("Encerrando conexão...");
                    break;
                }

                String mensagem = "";

                switch (opcao) {
                    case 1:
                        System.out.print("Número da conta: ");
                        int numCriar = scanner.nextInt();
                        System.out.print("Saldo inicial: ");
                        double saldoInicial = scanner.nextDouble();
                        scanner.nextLine(); // limpa a quebra de linha
                        System.out.print("Nome do titular: ");
                        String titular = scanner.nextLine();
                        mensagem = "CRIAR;" + numCriar + ";" + saldoInicial + ";" + titular;
                        break;

                    case 2:
                        System.out.print("Número da conta: ");
                        int numCons = scanner.nextInt();
                        mensagem = "CONSULTAR;" + numCons;
                        break;

                    case 3:
                        System.out.print("Número da conta: ");
                        int numDep = scanner.nextInt();
                        System.out.print("Valor para depositar: ");
                        double valorDep = scanner.nextDouble();
                        mensagem = "DEPOSITAR;" + numDep + ";" + valorDep;
                        break;

                    case 4:
                        System.out.print("Número da conta: ");
                        int numSaq = scanner.nextInt();
                        System.out.print("Valor para sacar: ");
                        double valorSaq = scanner.nextDouble();
                        mensagem = "SACAR;" + numSaq + ";" + valorSaq;
                        break;

                    default:
                        System.out.println("Opção inválida!");
                        continue;
                }

                saida.println(mensagem);
                String resposta = entrada.readLine();
                System.out.println("Servidor: " + resposta);
            }

        } catch (IOException e) {
            System.out.println("Erro ao conectar no servidor: " + e.getMessage());
        }
    }
}
