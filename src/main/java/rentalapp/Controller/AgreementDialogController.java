package rentalapp.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import rentalapp.DataAccessObject.*;
import rentalapp.Entity.*;
import rentalapp.util.Session;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AgreementDialogController {
    @FXML private ComboBox<Room> roomCombo;
    @FXML private ComboBox<Tenant> tenantCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField arendaSumField;
    @FXML private ComboBox<String> paymentScheduleCombo;
    @FXML private Button saveButton;
    @Setter private Stage dialogStage;
    @Setter private AgreementDAO agreementDAO = new AgreementDAO();
    @Setter private RoomDAO roomDAO;
    @Setter private TenantDAO tenantDAO;
    private Agreement agreementToEdit;
    private boolean isEditMode = false;
    @FXML public void initialize() {
        setupPaymentSchedule();
        startDatePicker.setValue(LocalDate.now());
    }

    public void loadData(){
        loadFreeRooms();
        loadTenants();
        if (isEditMode && agreementToEdit != null) {
            populateFields();
        }
    }

    public void setAgreementToEdit(Agreement agreement) {
        this.isEditMode = true;
        this.agreementToEdit = agreement;
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        if (saveButton != null) {
            saveButton.setText(editMode ? "Сохранить" : "Создать");
        }
    }

    private void populateFields() {
        if (agreementToEdit == null) return;
        Room roomToSelect = roomCombo.getItems().stream().filter(r -> r.getId() == agreementToEdit.getRoomId()).findFirst().orElse(null);
        roomCombo.setValue(roomToSelect);
        roomCombo.setDisable(true);
        Tenant tenantToSelect = tenantCombo.getItems().stream().filter(t -> t.getId() == agreementToEdit.getTenantId()).findFirst().orElse(null);
        tenantCombo.setValue(tenantToSelect);
        startDatePicker.setValue(agreementToEdit.getEffectiveDate());
        endDatePicker.setValue(agreementToEdit.getDeterminationDate());
        arendaSumField.setText(String.valueOf(agreementToEdit.getArendaSum()));
        paymentScheduleCombo.setValue(agreementToEdit.getPaymentSchedule());
    }

    private void loadFreeRooms() {
        int landlordId = Session.getCurrentSession().getUserId();
        List<Room> allRooms = roomDAO.findByLandlordId(landlordId);
        List<Room> freeRooms = allRooms.stream().filter(room -> "свободна".equals(room.getStatus())).toList();
        roomCombo.getItems().addAll(freeRooms);
        roomCombo.setCellFactory(listView -> new ListCell<Room>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                if (empty || room == null) {
                    setText(null);
                } else {
                    setText(room.getAddress() + " (" + room.getType() + ")");
                }
            }
        });
        roomCombo.setButtonCell(roomCombo.getCellFactory().call(null));
    }

    private void loadTenants() {
        int landlordId = Session.getCurrentSession().getUserId();
        AgreementDAO agreementDAO = new AgreementDAO();
        List<Agreement> agreements = agreementDAO.findByLandlordId(landlordId);
        List<Tenant> tenants = agreements.stream().map(agreement -> tenantDAO.findById(agreement.getTenantId())).filter(tenant -> tenant != null).distinct().toList();
        tenantCombo.getItems().addAll(tenants);
        tenantCombo.setCellFactory(listView -> new ListCell<Tenant>() {
            @Override
            protected void updateItem(Tenant tenant, boolean empty) {
                super.updateItem(tenant, empty);
                if (empty || tenant == null) {
                    setText(null);
                } else {
                    setText(tenant.getFullName() + " (" + tenant.getPhoneNumber() + ")");
                }
            }
        });
        tenantCombo.setButtonCell(tenantCombo.getCellFactory().call(null));
    }

    private void setupPaymentSchedule() {
        paymentScheduleCombo.getItems().addAll("Ежемесячно", "Ежеквартально", "Раз в полгода", "Ежегодно");
        paymentScheduleCombo.setValue("Ежемесячно");
    }

    @FXML
    private void handleCreate() {
        if (roomCombo.getValue() == null) {
            showAlert("Ошибка", "Выберите помещение");
            return;
        }
        if (tenantCombo.getValue() == null) {
            showAlert("Ошибка", "Выберите съёмщика");
            return;
        }
        if (startDatePicker.getValue() == null) {
            showAlert("Ошибка", "Выберите дату начала");
            return;
        }
        if (endDatePicker.getValue() == null) {
            showAlert("Ошибка", "Выберите дату окончания");
            return;
        }
        if (startDatePicker.getValue().isAfter(endDatePicker.getValue())) {
            showAlert("Ошибка", "Дата начала не может быть позже даты окончания");
            return;
        }
        int arendaAmount;
        try {
            arendaAmount = Integer.parseInt(arendaSumField.getText().trim());
            if (arendaAmount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректную сумму аренды (положительное число)");
            return;
        }
        Agreement newAgreement = isEditMode ? agreementToEdit : new Agreement();
        newAgreement.setRoomId(roomCombo.getValue().getId());
        newAgreement.setTenantId(tenantCombo.getValue().getId());
        newAgreement.setLandlordId(Session.getCurrentSession().getUserId());
        newAgreement.setEffectiveDate(startDatePicker.getValue());
        newAgreement.setDeterminationDate(endDatePicker.getValue());
        newAgreement.setArendaSum(arendaAmount);
        newAgreement.setPaymentSchedule(paymentScheduleCombo.getValue());
        if (!isEditMode) {
            newAgreement.setStatus("в силе");
        }
        boolean success;
        if (isEditMode) {
            success = agreementDAO.update(newAgreement);
        } else {
            success = agreementDAO.add(newAgreement) > 0;
        }
        if (success) {
            if (!isEditMode) {
                Room room = roomCombo.getValue();
                room.setStatus("занята");
                roomDAO.update(room);
            }
            dialogStage.close();
        } else {
            showAlert("Ошибка", "Не удалось " + (isEditMode ? "сохранить" : "создать"));
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