package rentalapp.DataAccessObject;

import rentalapp.Entity.Agreement;
import rentalapp.util.DatabaseHelper;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AgreementDAO {
    public List<Agreement> getAll() {
        List<Agreement> agreements = new ArrayList<>();
        String sql = "SELECT * FROM \"Agreements\" ORDER BY \"Id\"";
        try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                agreements.add(mapResultSetToAgreement(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка договоров: " + e.getMessage());
            e.printStackTrace();
        }
        return agreements;
    }

    public Agreement findById(int id) {
        Agreement agreement = null;
        String sql = "SELECT * FROM \"Agreements\" WHERE \"Id\" = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    agreement = mapResultSetToAgreement(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении договора с id: " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return agreement;
    }

    public List<Agreement> findByLandlordId(int landlordId) {
        List<Agreement> agreements = new ArrayList<>();
        String sql = "SELECT * FROM \"Agreements\" WHERE \"LandlordId\" = ? ORDER BY \"EffectiveDate\" DESC";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, landlordId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    agreements.add(mapResultSetToAgreement(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске по арендодателю: " + e.getMessage());
            e.printStackTrace();
        }
        return agreements;
    }

    public int add(Agreement agreement) {
        String sql = "INSERT INTO \"Agreements\" (\"TenantId\",\"RoomId\",\"LandlordId\",\"EffectiveDate\",\"DeterminationDate\",\"ArendaSum\",\"PaymentSchedule\",\"Status\") VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            pstmt.setInt(1, agreement.getTenantId());
            pstmt.setInt(2, agreement.getRoomId());
            pstmt.setInt(3, agreement.getLandlordId());
            pstmt.setObject(4, agreement.getEffectiveDate());
            pstmt.setObject(5, agreement.getDeterminationDate());
            pstmt.setInt(6, agreement.getArendaSum());
            pstmt.setString(7, agreement.getPaymentSchedule());
            pstmt.setString(8, agreement.getStatus());
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
            System.err.println("Ошибка при создании нового договора: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(Agreement agreement) {
        String sql = "UPDATE \"Agreements\" SET \"TenantId\"=?, \"RoomId\"=?, \"LandlordId\"=?, \"EffectiveDate\"=?, \"DeterminationDate\"=?, \"ArendaSum\"=?, \"PaymentSchedule\"=?, \"Status\"=? WHERE \"Id\"=?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, agreement.getTenantId());
            pstmt.setInt(2, agreement.getRoomId());
            pstmt.setInt(3, agreement.getLandlordId());
            pstmt.setObject(4, agreement.getEffectiveDate());
            pstmt.setObject(5, agreement.getDeterminationDate());
            pstmt.setInt(6, agreement.getArendaSum());
            pstmt.setString(7, agreement.getPaymentSchedule());
            pstmt.setString(8, agreement.getStatus());
            pstmt.setInt(9, agreement.getId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении условий договора: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM \"Agreements\" WHERE \"Id\" = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Ошибка при расторжении договора: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private Agreement mapResultSetToAgreement(ResultSet rs) throws SQLException {
        Agreement agreement = new Agreement();
        agreement.setId(rs.getInt("Id"));
        agreement.setTenantId(rs.getInt("TenantId"));
        agreement.setRoomId(rs.getInt("RoomId"));
        agreement.setLandlordId(rs.getInt("LandlordId"));
        agreement.setEffectiveDate(rs.getObject("EffectiveDate", LocalDate.class));
        agreement.setDeterminationDate(rs.getObject("DeterminationDate", LocalDate.class));
        agreement.setArendaSum(rs.getInt("ArendaSum"));
        agreement.setPaymentSchedule(rs.getString("PaymentSchedule"));
        agreement.setStatus(rs.getString("Status"));
        return agreement;
    }
}