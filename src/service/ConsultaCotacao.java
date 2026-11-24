package service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ConsultaCotacao {

    public static void main(String[] args) {
        consultarCotacao("USD-BRL");
    }

    public static void consultarCotacao(String parMoedas) {
        String url = "https://economia.awesomeapi.com.br/json/last/" + parMoedas;

        try {
            // 1. Cria o cliente e a requisição HTTP GET
            HttpClient cliente = HttpClient.newHttpClient();
            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            // 2. Envia a requisição e recebe a resposta como String
            HttpResponse<String> resposta = cliente.send(
                    requisicao,
                    HttpResponse.BodyHandlers.ofString()
            );

            // 3. Verifica o código de status HTTP
            if (resposta.statusCode() == 200) {
                String json = resposta.body();

                // 4. Interpreta os dados do JSON manualmente (sem Gson)
                String nome = extrairCampo(json, "name");
                String valorCompra = extrairCampo(json, "bid");
                String maxima = extrairCampo(json, "high");
                String minima = extrairCampo(json, "low");
                String data = extrairCampo(json, "create_date");

                // 5. Exibe de maneira organizada
                System.out.println("=== COTAÇÃO ATUAL ===");
                System.out.println("Moeda:  " + nome);
                System.out.println("Compra: R$ " + valorCompra);
                System.out.println("Máxima: R$ " + maxima);
                System.out.println("Mínima: R$ " + minima);
                System.out.println("Data:   " + data);
            } else {
                System.out.println("Erro HTTP: " + resposta.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Erro de comunicação com a API: " + e.getMessage());
        }
    }

    // Método auxiliar para extrair o valor de uma chave no JSON sem biblioteca externa
    private static String extrairCampo(String json, String chave) {
        String busca = "\"" + chave + "\":\"";
        int inicio = json.indexOf(busca);
        if (inicio == -1) {
            return "Não encontrado";
        }
        inicio += busca.length();
        int fim = json.indexOf("\"", inicio);
        return json.substring(inicio, fim);
    }
}