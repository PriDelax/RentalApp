package rentalapp.DataAccessObject;

import rentalapp.Entity.Contractor;
import rentalapp.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ContractorDAO {
    public List<Contractor> getAll() {
        List<Contractor> contractors = new ArrayList<>();
        String sql = "SELECT * FROM \"Contractors\" ORDER BY \"Id\"";
        try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                contractors.add(mapResultSetToContractor(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка исполнителей: " + e.getMessage());
            e.printStackTrace();
        }
        return contractors;
    }

    public Contractor findById(int id) {
        Contractor contractor = null;
        String sql = "SELECT * FROM \"Contractors\" WHERE \"Id\" = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    contractor = mapResultSetToContractor(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении исполнителя с id: " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return contractor;
    }

    public int add(Contractor contractor) {
        String sql = "INSERT INTO \"Contractors\" (\"FullName\",\"BirthDate\",\"PhoneNumber\",\"Speciality\",\"ExperienceYears\",\"Rating\",\"Description\") VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, contractor.getFullName());
            pstmt.setObject(2, contractor.getBirthDate());
            pstmt.setString(3, contractor.getPhoneNumber());
            pstmt.setString(4, contractor.getSpeciality());
            pstmt.setInt(5, contractor.getExperienceYears());
            pstmt.setDouble(6, contractor.getRating());
            pstmt.setString(7, contractor.getDescription());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Добавление не удалось");
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) return generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(Contractor contractor) {
        String sql = "UPDATE \"Contractors\" SET \"FullName\"=?, \"BirthDate\"=?, \"PhoneNumber\"=?, \"Speciality\"=? WHERE \"Id\"=?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, contractor.getFullName());
            pstmt.setObject(2, contractor.getBirthDate());
            pstmt.setString(3, contractor.getPhoneNumber());
            pstmt.setString(4, contractor.getSpeciality());
            pstmt.setInt(5, contractor.getId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM \"Contractors\" WHERE \"Id\" = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении исполнителя: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private Contractor mapResultSetToContractor(ResultSet rs) throws SQLException {
        Contractor contractor = new Contractor();
        contractor.setId(rs.getInt("Id"));
        contractor.setFullName(rs.getString("FullName"));
        contractor.setBirthDate(rs.getObject("BirthDate", LocalDate.class));
        contractor.setPhoneNumber(rs.getString("PhoneNumber"));
        contractor.setSpeciality(rs.getString("Speciality"));
        contractor.setExperienceYears(rs.getInt("ExperienceYears"));
        contractor.setRating(rs.getDouble("Rating"));
        contractor.setDescription(rs.getString("Description"));
        contractor.setCreatedAt(rs.getObject("CreatedAt", LocalDateTime.class));
        return contractor;
    }
}