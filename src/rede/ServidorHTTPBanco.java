package rede;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dao.ContaDAO;
import exception.SaldoInsuficienteException;
import model.ContaCorrente;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServidorHTTPBanco {

    // === VERSÃO EM MEMÓRIA (ATIVA) ===
    private static final List<ContaCorrente> contas = new ArrayList<>();

    // === VERSÃO COM BANCO DE DADOS (COMENTADA) ===
    // private static final ContaDAO contaDAO = new ContaDAO();

    public static void main(String[] args) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(8080), 0);

        servidor.createContext("/contas", ServidorHTTPBanco::tratarRequisicao);

        servidor.start();
        System.out.println("API Bancária rodando em http://localhost:8080/contas");
    }

    private static void tratarRequisicao(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();
        String caminho = troca.getRequestURI().getPath();

        if (caminho.endsWith("/") && caminho.length() > 1) {
            caminho = caminho.substring(0, caminho.length() - 1);
        }

        String[] partes = caminho.split("/");

        try {
            // ROTA: /contas
            if (partes.length == 2) {
                if (metodo.equals("GET")) {
                    listarContas(troca);
                } else if (metodo.equals("POST")) {
                    cadastrarConta(troca);
                } else {
                    enviarResposta(troca, 405, "{\"erro\": \"Método não permitido\"}");
                }
                return;
            }

            // ROTA: /contas/{numero}
            if (partes.length == 3) {
                int numero = Integer.parseInt(partes[2]);
                switch (metodo) {
                    case "GET" -> consultarConta(troca, numero);
                    case "PUT" -> atualizarConta(troca, numero);
                    case "DELETE" -> removerConta(troca, numero);
                    default -> enviarResposta(troca, 405, "{\"erro\": \"Método não permitido\"}");
                }
                return;
            }

            // ROTA: /contas/{numero}/depositos ou /contas/{numero}/saques
            if (partes.length == 4) {
                int numero = Integer.parseInt(partes[2]);
                String acao = partes[3];

                if (!metodo.equals("POST")) {
                    enviarResposta(troca, 405, "{\"erro\": \"Método não permitido\"}");
                    return;
                }

                if (acao.equals("depositos")) {
                    realizarDeposito(troca, numero);
                } else if (acao.equals("saques")) {
                    realizarSaque(troca, numero);
                } else {
                    enviarResposta(troca, 404, "{\"erro\": \"Endpoint não encontrado\"}");
                }
                return;
            }

            enviarResposta(troca, 404, "{\"erro\": \"Endpoint não encontrado\"}");

        } catch (NumberFormatException e) {
            enviarResposta(troca, 400, "{\"erro\": \"Número da conta ou valor inválido\"}");
        } catch (Exception e) {
            enviarResposta(troca, 500, "{\"erro\": \"Erro interno no servidor\"}");
        }
    }

    // === OPERAÇÕES DOS ENDPOINTS ===

    private static void listarContas(HttpExchange troca) throws IOException {
        // Versão em Memória:
        List<ContaCorrente> lista = contas;

        // Versão com Banco de Dados:
        // List<ContaCorrente> lista = contaDAO.listar();

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            json.append(contaParaJson(lista.get(i)));
            if (i < lista.size() - 1) json.append(",");
        }
        json.append("]");
        enviarResposta(troca, 200, json.toString());
    }

    private static void consultarConta(HttpExchange troca, int numero) throws IOException {
        ContaCorrente conta = buscarConta(numero);
        if (conta == null) {
            enviarResposta(troca, 404, "{\"erro\": \"Conta não encontrada\"}");
            return;
        }
        enviarResposta(troca, 200, contaParaJson(conta));
    }

    private static void cadastrarConta(HttpExchange troca) throws IOException {
        String corpo = lerCorpo(troca);
        String numStr = extrairValorJson(corpo, "numero");
        String titular = extrairValorJson(corpo, "titular");
        String saldoStr = extrairValorJson(corpo, "saldo");

        if (numStr == null || titular == null || titular.isBlank() || saldoStr == null) {
            enviarResposta(troca, 400, "{\"erro\": \"Campos obrigatórios: numero, titular e saldo\"}");
            return;
        }

        int numero = Integer.parseInt(numStr);
        double saldo = Double.parseDouble(saldoStr);

        if (saldo < 0) {
            enviarResposta(troca, 400, "{\"erro\": \"Saldo inicial não pode ser negativo\"}");
            return;
        }
        if (buscarConta(numero) != null) {
            enviarResposta(troca, 400, "{\"erro\": \"Já existe uma conta com esse número\"}");
            return;
        }

        ContaCorrente nova = new ContaCorrente(numero, saldo, titular);

        // Versão em Memória:
        contas.add(nova);

        // Versão com Banco de Dados:
        // contaDAO.inserir(nova);

        enviarResposta(troca, 201, contaParaJson(nova));
    }

    private static void atualizarConta(HttpExchange troca, int numero) throws IOException {
        ContaCorrente atual = buscarConta(numero);
        if (atual == null) {
            enviarResposta(troca, 404, "{\"erro\": \"Conta não encontrada\"}");
            return;
        }

        String corpo = lerCorpo(troca);
        String novoTitular = extrairValorJson(corpo, "titular");
        String novoSaldoStr = extrairValorJson(corpo, "saldo");

        String titularFinal = (novoTitular != null && !novoTitular.isBlank()) ? novoTitular : atual.getTitular();
        double saldoFinal = (novoSaldoStr != null) ? Double.parseDouble(novoSaldoStr) : atual.getSaldo();

        if (saldoFinal < 0) {
            enviarResposta(troca, 400, "{\"erro\": \"Saldo não pode ser negativo\"}");
            return;
        }

        ContaCorrente atualizada = new ContaCorrente(numero, saldoFinal, titularFinal);

        // Versão em Memória:
        int indice = buscarIndice(numero);
        contas.set(indice, atualizada);

        // Versão com Banco de Dados (atualiza o saldo no MySQL):
        // contaDAO.atualizarSaldo(numero, saldoFinal);

        enviarResposta(troca, 200, contaParaJson(atualizada));
    }

    private static void removerConta(HttpExchange troca, int numero) throws IOException {
        if (buscarConta(numero) == null) {
            enviarResposta(troca, 404, "{\"erro\": \"Conta não encontrada\"}");
            return;
        }

        // Versão em Memória:
        contas.remove(buscarIndice(numero));

        // Versão com Banco de Dados:
        // contaDAO.remover(numero);

        enviarResposta(troca, 200, "{\"mensagem\": \"Conta removida com sucesso\"}");
    }

    private static void realizarDeposito(HttpExchange troca, int numero) throws IOException {
        ContaCorrente conta = buscarConta(numero);
        if (conta == null) {
            enviarResposta(troca, 404, "{\"erro\": \"Conta não encontrada\"}");
            return;
        }

        String valorStr = extrairValorJson(lerCorpo(troca), "valor");
        if (valorStr == null) {
            enviarResposta(troca, 400, "{\"erro\": \"Campo 'valor' é obrigatório\"}");
            return;
        }

        double valor = Double.parseDouble(valorStr);
        if (valor <= 0) {
            enviarResposta(troca, 400, "{\"erro\": \"Valor do depósito deve ser positivo\"}");
            return;
        }

        conta.depositar(valor);

        // Versão com Banco de Dados:
        // contaDAO.atualizarSaldo(numero, conta.getSaldo());

        enviarResposta(troca, 200, contaParaJson(conta));
    }

    private static void realizarSaque(HttpExchange troca, int numero) throws IOException {
        ContaCorrente conta = buscarConta(numero);
        if (conta == null) {
            enviarResposta(troca, 404, "{\"erro\": \"Conta não encontrada\"}");
            return;
        }

        String valorStr = extrairValorJson(lerCorpo(troca), "valor");
        if (valorStr == null) {
            enviarResposta(troca, 400, "{\"erro\": \"Campo 'valor' é obrigatório\"}");
            return;
        }

        double valor = Double.parseDouble(valorStr);
        if (valor <= 0) {
            enviarResposta(troca, 400, "{\"erro\": \"Valor do saque deve ser positivo\"}");
            return;
        }

        try {
            conta.sacar(valor);

            // Versão com Banco de Dados:
            // contaDAO.atualizarSaldo(numero, conta.getSaldo());

            enviarResposta(troca, 200, contaParaJson(conta));
        } catch (SaldoInsuficienteException e) {
            enviarResposta(troca, 400, "{\"erro\": \"Saldo insuficiente\"}");
        }
    }

    // === MÉTODOS AUXILIARES ===

    private static ContaCorrente buscarConta(int numero) {
        // Versão em Memória:
        for (ContaCorrente c : contas) {
            if (c.getNumero() == numero) return c;
        }
        return null;

        /* Versão com Banco de Dados:
        for (ContaCorrente c : contaDAO.listar()) {
            if (c.getNumero() == numero) return c;
        }
        return null;
        */
    }

    private static int buscarIndice(int numero) {
        for (int i = 0; i < contas.size(); i++) {
            if (contas.get(i).getNumero() == numero) return i;
        }
        return -1;
    }

    private static String contaParaJson(ContaCorrente c) {
        return String.format(Locale.US,
                "{\"numero\": %d, \"titular\": \"%s\", \"saldo\": %.2f}",
                c.getNumero(), c.getTitular(), c.getSaldo());
    }

    private static String lerCorpo(HttpExchange troca) throws IOException {
        try (InputStream is = troca.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void enviarResposta(HttpExchange troca, int status, String json) throws IOException {
        troca.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        troca.sendResponseHeaders(status, bytes.length);
        troca.getResponseBody().write(bytes);
        troca.close();
    }

    private static String extrairValorJson(String json, String chave) {
        String busca = "\"" + chave + "\"";
        int idx = json.indexOf(busca);
        if (idx == -1) return null;

        int doisPontos = json.indexOf(":", idx + busca.length());
        if (doisPontos == -1) return null;

        int i = doisPontos + 1;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
        if (i >= json.length()) return null;

        if (json.charAt(i) == '"') {
            int fim = json.indexOf('"', i + 1);
            return (fim == -1) ? null : json.substring(i + 1, fim);
        } else {
            int fim = i;
            while (fim < json.length() && json.charAt(fim) != ',' && json.charAt(fim) != '}' && !Character.isWhitespace(json.charAt(fim))) {
                fim++;
            }
            return json.substring(i, fim);
        }
    }
}