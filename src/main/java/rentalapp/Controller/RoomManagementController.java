package rentalapp.Controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import rentalapp.DataAccessObject.RoomDAO;
import rentalapp.Entity.Room;
import javafx.scene.control.*;
import rentalapp.util.Session;
import java.io.IOException;
import java.util.List;

public class RoomManagementController {
    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, String> addressColumn;
    @FXML private TableColumn<Room, String> descriptionColumn;
    @FXML private TableColumn<Room, String> typeColumn;
    @FXML private TableColumn<Room, Integer> roomsNumberColumn;
    @FXML private TableColumn<Room, Double> squareColumn;
    @FXML private TableColumn<Room, String> statusColumn;
    @FXML private TableColumn<Room, Void> actionsColumn;
    private final RoomDAO roomDAO = new RoomDAO();
    @FXML public void initialize(){
        roomsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupColumns();
        loadRooms();
    }

    private void setupColumns(){
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        roomsNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomsNumber"));
        squareColumn.setCellValueFactory(new PropertyValueFactory<>("square"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        actionsColumn.setSortable(false);
        actionsColumn.setEditable(false);
    }

    private void loadRooms(){
        int landlordId = Session.getCurrentSession().getUserId();
        List<Room> rooms = roomDAO.findByLandlordId(landlordId);
        roomsTable.setItems(FXCollections.observableArrayList(rooms));
    }
    @FXML private void addRoom() throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/room_dialog.fxml"));
        Parent root = fxmlLoader.load();
        RoomDialogController roomDialogController = fxmlLoader.getController();
        Stage stage = new Stage();
        roomDialogController.setDialogStage(stage);
        roomDialogController.setRoomDAO(roomDAO);
        stage.setScene(new Scene(root));
        stage.getScene().getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        stage.setTitle("Добавить помещение");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(roomsTable.getScene().getWindow());
        stage.showAndWait();
        loadRooms();
    }
    @FXML private void handleUpdate(){
        loadRooms();
    }
    @FXML private void handleEdit() {
        Room selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Внимание");
            alert.setHeaderText("Ничего не выбрано");
            alert.setContentText("Выберите помещение в таблице для редактирования.");
            alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/room_dialog.fxml"));
            Parent root = loader.load();
            RoomDialogController controller = loader.getController();
            Stage dialogStage = new Stage();
            controller.setDialogStage(dialogStage);
            controller.setRoomDAO(roomDAO);
            controller.setRoomToEdit(selected);
            dialogStage.setTitle("Редактировать помещение");
            dialogStage.setScene(new Scene(root));
            dialogStage.getScene().getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(roomsTable.getScene().getWindow());
            dialogStage.showAndWait();
            loadRooms();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML private void handleDelete() {
        Room selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Внимание");
            alert.setHeaderText("Ничего не выбрано");
            alert.setContentText("Выберите помещение в таблице для удаления.");
            alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удалить помещение?");
        alert.setContentText(
                "Адрес: " + selected.getAddress() + "\n" +
                        "Это действие нельзя отменить."
        );
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                boolean success = roomDAO.delete(selected.getId());
                if (success) {
                    loadRooms();
                    System.out.println("Помещение удалено");
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Ошибка");
                    errorAlert.setHeaderText("Не удалось удалить");
                    errorAlert.setContentText("Возможно, есть связанные договоры.");
                    errorAlert.showAndWait();
                }
            }
        });
    }
}