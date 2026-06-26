package rentalapp.DataAccessObject;

import rentalapp.Entity.Tenant;
import rentalapp.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TenantDAO {

    public List<Tenant> getAll() {
        List<Tenant> tenants = new ArrayList<>();
        String sql = "SELECT * FROM \"Tenants\" ORDER BY \"Id\"";
        try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tenants.add(mapResultSetToTenant(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка съёмщиков: " + e.getMessage());
            e.printStackTrace();
        }
        return tenants;
    }

    public Tenant findById(int id) {
        Tenant tenant = null;
        String sql = "SELECT * FROM \"Tenants\" WHERE \"Id\" = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    tenant = mapResultSetToTenant(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении съёмщика с id: " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return tenant;
    }

    public int add(Tenant tenant) {
        String sql = "INSERT INTO \"Tenants\" (\"Login\",\"Password\",\"FullName\",\"BirthDate\",\"PhoneNumber\",\"Email\",\"PassportDetails\") VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            pstmt.setString(1, tenant.getLogin());
            pstmt.setString(2, tenant.getPassword());
            pstmt.setString(3, tenant.getFullName());
            pstmt.setObject(4, tenant.getBirthDate());
            pstmt.setString(5, tenant.getPhoneNumber());
            pstmt.setString(6, tenant.getEmail());
            pstmt.setString(7, tenant.getPassportDetails());
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
            System.err.println("Ошибка при добавлении нового съёмщика: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(Tenant tenant) {
        String sql = "UPDATE \"Tenants\" SET \"Login\"=?, \"Password\"=?, \"FullName\"=?, \"BirthDate\"=?, \"PhoneNumber\"=?, \"Email\"=?, \"PassportDetails\"=? WHERE \"Id\"=?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tenant.getLogin());
            pstmt.setString(2, tenant.getPassword());
            pstmt.setString(3, tenant.getFullName());
            pstmt.setObject(4, tenant.getBirthDate());
            pstmt.setString(5, tenant.getPhoneNumber());
            pstmt.setString(6, tenant.getEmail());
            pstmt.setString(7, tenant.getPassportDetails());
            pstmt.setInt(8, tenant.getId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM \"Tenants\" WHERE \"Id\" = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении съёмщика: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public Tenant findByLogin(String login) {
        Tenant tenant = null;
        String sql = "SELECT * FROM \"Tenants\" WHERE \"Login\" = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, login);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTenant(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении съёмщика с логином: " + login + ": " + e.getMessage());
            e.printStackTrace();
        }
        return tenant;
    }

    private Tenant mapResultSetToTenant(ResultSet rs) throws SQLException {
        Tenant tenant = new Tenant();
        tenant.setId(rs.getInt("Id"));
        tenant.setLogin(rs.getString("Login"));
        tenant.setPassword(rs.getString("Password"));
        tenant.setFullName(rs.getString("FullName"));
        tenant.setBirthDate(rs.getObject("BirthDate", LocalDate.class));
        tenant.setPhoneNumber(rs.getString("PhoneNumber"));
        tenant.setEmail(rs.getString("Email"));
        tenant.setPassportDetails(rs.getString("PassportDetails"));
        tenant.setCreatedAt(rs.getObject("CreatedAt", LocalDateTime.class));
        return tenant;
    }
}