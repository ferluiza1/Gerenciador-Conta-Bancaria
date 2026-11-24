package rede;

import service.GerenciadorContas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorBanco {
    public static void main(String[] args) {
        int porta = 5000;
        GerenciadorContas gerenciador = new GerenciadorContas();

        try (ServerSocket servidor = new ServerSocket(porta)) {
            System.out.println("Servidor aguardando conexão na porta " + porta + "...");

            try (Socket cliente = servidor.accept();
                 BufferedReader entrada = new BufferedReader(
                         new InputStreamReader(cliente.getInputStream()));
                 PrintWriter saida = new PrintWriter(cliente.getOutputStream(), true)) {

                System.out.println("Cliente conectado!");
                String mensagem;

                while ((mensagem = entrada.readLine()) != null) {
                    if (mensagem.equalsIgnoreCase("SAIR")) {
                        break;
                    }
                    System.out.println("Solicitação recebida: " + mensagem);
                    String resposta = gerenciador.processadorSolicitacao(mensagem);
                    saida.println(resposta);
                }

                System.out.println("Cliente desconectado.");
            }
        } catch (IOException e) {
            System.out.println("Erro no servidor: " + e.getMessage());
        }
    }
}