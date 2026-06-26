package rentalapp.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.*;
import javafx.stage.Stage;
import lombok.Setter;
import rentalapp.DataAccessObject.RoomDAO;
import rentalapp.Entity.Room;
import rentalapp.util.Session;

public class RoomDialogController {
    @FXML private TextField addressField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private Spinner<Integer> roomsNumberSpinner;
    @FXML private Spinner<Double> squareSpinner;
    @FXML private ComboBox<String> statusCombo;
    @Setter
    private Stage dialogStage;
    @Setter
    private RoomDAO roomDAO;
    private Room roomToEdit;
    private boolean isEditMode = false;

    public void setRoomToEdit(Room room) {
        this.isEditMode = true;
        this.roomToEdit = room;
        populateFields();
    }
    @FXML
    public void initialize() {
        setupComboBoxes();
        setupSpinners();
        if (isEditMode && roomToEdit != null) {
            populateFields();
        }
    }

    private void setupComboBoxes() {
        typeCombo.getItems().addAll("студия", "евроремонт", "апартаменты", "общежитие");
        statusCombo.getItems().addAll("свободна", "занята", "ремонт");
        typeCombo.setValue("студия");
        statusCombo.setValue("свободна");
    }

    private void setupSpinners() {
        var roomsFactory = new IntegerSpinnerValueFactory(1, 10, 1);
        roomsNumberSpinner.setValueFactory(roomsFactory);
        var squareFactory = new DoubleSpinnerValueFactory(10.0, 500.0, 30.0, 0.5);
        squareSpinner.setValueFactory(squareFactory);
    }

    private void populateFields() {
        if (roomToEdit == null) return;
        addressField.setText(roomToEdit.getAddress());
        descriptionField.setText(roomToEdit.getDescription());
        typeCombo.setValue(roomToEdit.getType());
        roomsNumberSpinner.getValueFactory().setValue(roomToEdit.getRoomsNumber());
        squareSpinner.getValueFactory().setValue(roomToEdit.getSquare());
        statusCombo.setValue(roomToEdit.getStatus());
    }

    @FXML
    private void handleSave() {
        if (addressField.getText().trim().isEmpty()) {
            showAlert("Ошибка", "Адрес обязателен");
            return;
        }
        Room room = isEditMode ? roomToEdit : new Room();
        room.setAddress(addressField.getText().trim());
        room.setDescription(descriptionField.getText().trim());
        room.setType(typeCombo.getValue());
        room.setRoomsNumber(roomsNumberSpinner.getValue());
        room.setSquare(squareSpinner.getValue());
        room.setStatus(statusCombo.getValue());
        if (!isEditMode) {
            room.setLandlordId(Session.getCurrentSession().getUserId());
        }
        boolean success = isEditMode ? roomDAO.update(room) : roomDAO.add(room) > 0;
        if (success) {
            dialogStage.close();
        } else {
            showAlert("Ошибка", "Не удалось сохранить данные");
        }
    }
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}