package rentalapp.DataAccessObject;

import rentalapp.Entity.Room;
import rentalapp.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public List<Room> getAll() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM \"Rooms\" ORDER BY \"Id\"";
        try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка помещений: " + e.getMessage());
            e.printStackTrace();
        }
        return rooms;
    }

    public Room findById(int id) {
        Room room = null;
        String sql = "SELECT * FROM \"Rooms\" WHERE \"Id\" = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    room = mapResultSetToRoom(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении помещения с id: " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return room;
    }


    public List<Room> findByLandlordId(int landlordId) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM \"Rooms\" WHERE \"LandlordId\" = ? ORDER BY \"Id\"";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, landlordId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapResultSetToRoom(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске по арендодателю: " + e.getMessage());
            e.printStackTrace();
        }
        return rooms;
    }

    public int add(Room room) {
        String sql = "INSERT INTO \"Rooms\" (\"Address\",\"Description\",\"Type\",\"RoomsNumber\",\"Square\",\"Status\",\"LandlordId\") VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            pstmt.setString(1, room.getAddress());
            pstmt.setString(2, room.getDescription());
            pstmt.setString(3, room.getType());
            pstmt.setInt(4, room.getRoomsNumber());
            pstmt.setDouble(5, room.getSquare());
            pstmt.setString(6, room.getStatus());
            pstmt.setInt(7, room.getLandlordId());
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
            System.err.println("Ошибка при добавлении нового помещения: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(Room room) {
        String sql = "UPDATE \"Rooms\" SET \"Address\"=?, \"Description\"=?, \"Type\"=?, \"RoomsNumber\"=?, \"Square\"=?, \"Status\"=?, \"LandlordId\"=? WHERE \"Id\"=?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, room.getAddress());
            pstmt.setString(2, room.getDescription());
            pstmt.setString(3, room.getType());
            pstmt.setInt(4, room.getRoomsNumber());
            pstmt.setDouble(5, room.getSquare());
            pstmt.setString(6, room.getStatus());
            pstmt.setInt(7, room.getLandlordId());
            pstmt.setInt(8, room.getId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении помещения: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM \"Rooms\" WHERE \"Id\" = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении помещения: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getInt("Id"));
        room.setAddress(rs.getString("Address"));
        room.setDescription(rs.getString("Description"));
        room.setType(rs.getString("Type"));
        room.setRoomsNumber(rs.getInt("RoomsNumber"));
        room.setSquare(rs.getDouble("Square"));
        room.setStatus(rs.getString("Status"));
        room.setLandlordId(rs.getInt("LandlordId"));
        room.setCreatedAt(rs.getObject("CreatedAt", LocalDateTime.class));
        return room;
    }
}