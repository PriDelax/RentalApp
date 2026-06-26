package rentalapp.DataAccessObject;

import rentalapp.Entity.Request;
import rentalapp.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RequestDAO {

    public List<Request> getAll() {
        List<Request> requests = new ArrayList<>();
        String sql = "SELECT * FROM \"Requests\" ORDER BY \"CreatedAt\" DESC";
        try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                requests.add(mapResultSetToRequest(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка заявок: " + e.getMessage());
            e.printStackTrace();
        }
        return requests;
    }

    public Request findById(int id) {
        Request request = null;
        String sql = "SELECT * FROM \"Requests\" WHERE \"Id\" = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    request = mapResultSetToRequest(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении заявки с id: " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return request;
    }

    public int add(Request request) {
        String sql = "INSERT INTO \"Requests\" (\"RoomId\",\"TenantId\",\"ContractorId\",\"Description\",\"Category\",\"Priority\",\"CurrentStatus\") VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            pstmt.setInt(1, request.getRoomId());
            pstmt.setInt(2, request.getTenantId());
            if (request.getContractorId() == 0){
                pstmt.setNull(3, Types.INTEGER);
            } else {
                pstmt.setInt(3, request.getContractorId());
            }
            pstmt.setString(4, request.getDescription());
            pstmt.setString(5, request.getCategory());
            pstmt.setString(6, request.getPriority());
            pstmt.setString(7, request.getCurrentStatus());
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
            System.err.println("Ошибка при добавлении новой заявки: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(Request request) {
        String sql = "UPDATE \"Requests\" SET \"RoomId\"=?, \"ContractorId\"=?, \"TenantId\"=?, " + "\"Description\"=?, \"Category\"=?, \"Priority\"=?, \"CurrentStatus\"=? WHERE \"Id\"=?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, request.getRoomId());
            if (request.getContractorId() > 0) {
                pstmt.setInt(2, request.getContractorId());
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }
            pstmt.setInt(3, request.getTenantId());
            pstmt.setString(4, request.getDescription());
            pstmt.setString(5, request.getCategory());
            pstmt.setString(6, request.getPriority());
            pstmt.setString(7, request.getCurrentStatus());
            pstmt.setInt(8, request.getId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении заявки: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM \"Requests\" WHERE \"Id\" = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении заявки: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private Request mapResultSetToRequest(ResultSet rs) throws SQLException {
        Request request = new Request();
        request.setId(rs.getInt("Id"));
        request.setRoomId(rs.getInt("RoomId"));
        request.setTenantId(rs.getInt("TenantId"));
        int contractorId = rs.getInt("ContractorId");
        request.setContractorId(rs.wasNull() ? 0 : contractorId);
        request.setDescription(rs.getString("Description"));
        request.setCategory(rs.getString("Category"));
        request.setPriority(rs.getString("Priority"));
        request.setCurrentStatus(rs.getString("CurrentStatus"));
        request.setCreatedAt(rs.getObject("CreatedAt", LocalDateTime.class));
        return request;
    }
}