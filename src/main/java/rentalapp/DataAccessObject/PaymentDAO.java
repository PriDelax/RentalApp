package rentalapp.DataAccessObject;

import rentalapp.Entity.Payment;
import rentalapp.util.DatabaseHelper;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public List<Payment> getAll() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM \"Payments\" ORDER BY \"PaymentDate\" DESC";
        try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка платежей: " + e.getMessage());
            e.printStackTrace();
        }
        return payments;
    }

    public Payment findById(int id) {
        Payment payment = null;
        String sql = "SELECT * FROM \"Payments\" WHERE \"Id\" = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    payment = mapResultSetToPayment(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении платежа с id: " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return payment;
    }

    public int add(Payment payment) {
        String sql = "INSERT INTO \"Payments\" (\"AgreementId\",\"PaymentSum\",\"PaymentDate\",\"Type\",\"Status\") VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            pstmt.setInt(1, payment.getAgreementId());
            pstmt.setBigDecimal(2, payment.getPaymentSum());
            pstmt.setObject(3, payment.getPaymentDate());
            pstmt.setString(4, payment.getType());
            pstmt.setString(5, payment.getStatus());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0){
                throw new SQLException("Добавление не удалось, 0 затронутых строк");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()){
                if (generatedKeys.next()){
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении нового платежа: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM \"Payments\" WHERE \"Id\" = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении платежа: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getInt("Id"));
        payment.setAgreementId(rs.getInt("AgreementId"));
        payment.setPaymentSum(rs.getBigDecimal("PaymentSum"));
        payment.setPaymentDate(rs.getObject("PaymentDate", LocalDate.class));
        payment.setType(rs.getString("Type"));
        payment.setStatus(rs.getString("Status"));
        payment.setCreatedAt(rs.getObject("CreatedAt", LocalDateTime.class));
        return payment;
    }
}