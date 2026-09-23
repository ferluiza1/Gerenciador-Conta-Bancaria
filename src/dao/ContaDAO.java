package dao;

import model.ContaCorrente;
import model.TarifaTransferenciaStrategy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ContaDAO {

    public void inserir(ContaCorrente conta) {
        String sql = "INSERT INTO contas (numero, titular, saldo) VALUES (?, ?, ?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, conta.getNumero());
            stmt.setString(2, conta.getTitular());
            stmt.setDouble(3, conta.getSaldo());
            stmt.executeUpdate();
            System.out.println("Conta cadastrada no banco de dados com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao inserir no banco: " + e.getMessage());
        }
    }

    public List<ContaCorrente> listar() {
        List<ContaCorrente> lista = new ArrayList<>();
        String sql = "SELECT * FROM contas";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int numero = rs.getInt("numero");
                String titular = rs.getString("titular");
                double saldo = rs.getDouble("saldo");

                ContaCorrente conta = new ContaCorrente(numero, saldo, titular);
                lista.add(conta);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao carregar dados do banco: " + e.getMessage());
        }
        return lista;
    }

    public void atualizarSaldo(int numero, double novoSaldo) {
        String sql = "UPDATE contas SET saldo = ? WHERE numero = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, novoSaldo);
            stmt.setInt(2, numero);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar saldo no banco: " + e.getMessage());
        }
    }

    public void remover(int numero) {
        String sql = "DELETE FROM contas WHERE numero = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, numero);
            stmt.executeUpdate();
            System.out.println("Conta removida do banco com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao remover conta: " + e.getMessage());
        }
    }

    public void transferirComTarifa(int numOrigem, int numDestino, double valor, double tarifa) {
        String sqlSaque = "UPDATE contas SET saldo = saldo - ? WHERE numero = ?";
        String sqlDeposito = "UPDATE contas SET saldo = saldo + ? WHERE numero = ?";
        String sqlHistorico = "INSERT INTO transferencias (id_origem, id_destino, valor_transferido, valor_tarifa) VALUES (?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = Conexao.getConnection();
            conn.setAutoCommit(false);

            double totalDescontado = valor + tarifa;

            try (PreparedStatement stmtSaque = conn.prepareStatement(sqlSaque)) {
                stmtSaque.setDouble(1, totalDescontado);
                stmtSaque.setInt(2, numOrigem);
                stmtSaque.executeUpdate();
            }

            try (PreparedStatement stmtDeposito = conn.prepareStatement(sqlDeposito)) {
                stmtDeposito.setDouble(1, valor);
                stmtDeposito.setInt(2, numDestino);
                stmtDeposito.executeUpdate();
            }

            try (PreparedStatement stmtHist = conn.prepareStatement(sqlHistorico)) {
                stmtHist.setInt(1, numOrigem);
                stmtHist.setInt(2, numDestino);
                stmtHist.setDouble(3, valor);
                stmtHist.setDouble(4, tarifa);
                stmtHist.executeUpdate();
            }

            conn.commit();
            System.out.println("Transferência realizada com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro na transferência. Desfazendo transação...");
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
            }
        }
    }

    public void listarHistoricoTransferencias() {
        String sql = "SELECT * FROM transferencias ORDER BY data_transferencia DESC";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n HISTÓRICO DE TRANSFERÊNCIAS");
            while(rs.next()) {
                System.out.printf("Id: %d |Data: %s | Origem: %d -> Destino: %d | Valor: R$ %.2f | Tarifa: R$ %.2f\n",
                        rs.getInt("id"),
                        rs.getTimestamp("data_transferencia"),
                        rs.getInt("id_origem"),
                        rs.getInt("id_destino"),
                        rs.getDouble("valor_transferido"),
                        rs.getDouble("valor_tarifa"));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao carregar histórico: " + e.getMessage());
        }
    }
}
