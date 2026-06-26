package rentalapp.DataAccessObject;

import rentalapp.Entity.Landlord;
import rentalapp.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LandlordDAO {

    public List<Landlord> getAll() {
        List<Landlord> landlords = new ArrayList<>();
        String sql = "SELECT * FROM \"Landlords\" ORDER BY \"Id\"";
        try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                landlords.add(mapResultSetToLandlord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка арендодателей: " + e.getMessage());
            e.printStackTrace();
        }
        return landlords;
    }

    public Landlord findById(int id) {
        Landlord landlord = null;
        String sql = "SELECT * FROM \"Landlords\" WHERE \"Id\" = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    landlord = mapResultSetToLandlord(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении арендодателя с id: " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return landlord;
    }

    public int add(Landlord landlord) {
        String sql = "INSERT INTO \"Landlords\" (\"Login\",\"Password\",\"FullName\",\"BirthDate\",\"PhoneNumber\",\"Email\",\"PassportDetails\",\"PaymentDetails\") VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            pstmt.setString(1, landlord.getLogin());
            pstmt.setString(2, landlord.getPassword());
            pstmt.setString(3, landlord.getFullName());
            pstmt.setObject(4, landlord.getBirthDate());
            pstmt.setString(5, landlord.getPhoneNumber());
            pstmt.setString(6, landlord.getEmail());
            pstmt.setString(7, landlord.getPassportDetails());
            pstmt.setString(8, landlord.getPaymentDetails());
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
            System.err.println("Ошибка при добавлении нового арендодателя: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(Landlord landlord) {
        String sql = "UPDATE \"Landlords\" SET \"Login\"=?, \"Password\"=?, \"FullName\"=?, \"BirthDate\"=?, \"PhoneNumber\"=?, \"Email\"=?, \"PassportDetails\"=?, \"PaymentDetails\"=? WHERE \"Id\"=?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, landlord.getLogin());
            pstmt.setString(2, landlord.getPassword());
            pstmt.setString(3, landlord.getFullName());
            pstmt.setObject(4, landlord.getBirthDate());
            pstmt.setString(5, landlord.getPhoneNumber());
            pstmt.setString(6, landlord.getEmail());
            pstmt.setString(7, landlord.getPassportDetails());
            pstmt.setString(8, landlord.getPaymentDetails());
            pstmt.setInt(9, landlord.getId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM \"Landlords\" WHERE \"Id\" = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении арендодателя: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public Landlord findByLogin(String login) {
        Landlord landlord = null;
        String sql = "SELECT * FROM \"Landlords\" WHERE \"Login\" = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, login);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLandlord(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении арендодателя с логином: " + login + ": " + e.getMessage());
            e.printStackTrace();
        }
        return landlord;
    }

    private Landlord mapResultSetToLandlord(ResultSet rs) throws SQLException {
        Landlord landlord = new Landlord();
        landlord.setId(rs.getInt("Id"));
        landlord.setLogin(rs.getString("Login"));
        landlord.setPassword(rs.getString("Password"));
        landlord.setFullName(rs.getString("FullName"));
        landlord.setBirthDate(rs.getObject("BirthDate", LocalDate.class));
        landlord.setPhoneNumber(rs.getString("PhoneNumber"));
        landlord.setEmail(rs.getString("Email"));
        landlord.setPassportDetails(rs.getString("PassportDetails"));
        landlord.setPaymentDetails(rs.getString("PaymentDetails"));
        landlord.setCreatedAt(rs.getObject("CreatedAt", LocalDateTime.class));
        return landlord;
    }
}