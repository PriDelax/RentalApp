package rentalapp.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import rentalapp.DataAccessObject.*;
import rentalapp.Entity.*;
import rentalapp.util.Session;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RequestDialogController {
    @FXML private ComboBox<Room> roomCombo;
    @FXML private ComboBox<Tenant> tenantCombo;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private TextArea descriptionArea;
    @Setter
    private Stage dialogStage;
    @Setter
    private RequestDAO requestDAO;
    @Setter
    private RoomDAO roomDAO;
    @Setter
    private TenantDAO tenantDAO;
    private final AgreementDAO agreementDAO = new AgreementDAO();
    private Request requestToEdit;
    private boolean isEditMode = false;
    @FXML private Button saveButton;
    @FXML public void initialize() {
        setupCategoryCombo();
        setupPriorityCombo();
    }

    public void setRequestToEdit(Request request) {
        this.isEditMode = true;
        this.requestToEdit = request;
    }

    public void loadData() {
        loadRooms();
        loadTenants();
        if (isEditMode && requestToEdit != null) {
            populateFields();
        }
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        if (saveButton != null) {
            saveButton.setText(editMode ? "Сохранить" : "Создать");
        }
    }

    private void populateFields() {
        if (requestToEdit == null) return;
        Room roomToSelect = roomCombo.getItems().stream().filter(r -> r.getId() == requestToEdit.getRoomId()).findFirst().orElse(null);
        roomCombo.setValue(roomToSelect);
        Tenant tenantToSelect = tenantCombo.getItems().stream().filter(t -> t.getId() == requestToEdit.getTenantId()).findFirst().orElse(null);
        tenantCombo.setValue(tenantToSelect);
        categoryCombo.setValue(requestToEdit.getCategory());
        priorityCombo.setValue(requestToEdit.getPriority());
        descriptionArea.setText(requestToEdit.getDescription());
    }

    private void loadRooms() {
        int landlordId = Session.getCurrentSession().getUserId();
        List<Room> rooms = roomDAO.findByLandlordId(landlordId);
        roomCombo.getItems().addAll(rooms);
        roomCombo.setCellFactory(listView -> new ListCell<Room>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                if (empty || room == null) {
                    setText(null);
                } else {
                    setText(room.getAddress());
                }
            }
        });
        roomCombo.setButtonCell(roomCombo.getCellFactory().call(null));
    }

    private void loadTenants() {
        int landlordId = Session.getCurrentSession().getUserId();
        List<Agreement> agreements = agreementDAO.findByLandlordId(landlordId);
        Set<Integer> tenantIds = agreements.stream().map(Agreement::getTenantId).collect(Collectors.toSet());
        List<Tenant> tenants = tenantIds.stream().map(tenantDAO::findById).filter(t -> t != null).toList();
        tenantCombo.getItems().addAll(tenants);
        tenantCombo.setCellFactory(listView -> new ListCell<Tenant>() {
            @Override
            protected void updateItem(Tenant tenant, boolean empty) {
                super.updateItem(tenant, empty);
                if (empty || tenant == null) {
                    setText(null);
                } else {
                    setText(tenant.getFullName());
                }
            }
        });
        tenantCombo.setButtonCell(tenantCombo.getCellFactory().call(null));
    }

    private void setupCategoryCombo() {
        categoryCombo.getItems().addAll("Сантехника", "Электрика", "Ремонт", "Уборка", "Другое");
        categoryCombo.setValue("Сантехника");
    }

    private void setupPriorityCombo() {
        priorityCombo.getItems().addAll("немедленные", "срочные", "несрочные");
        priorityCombo.setValue("срочные");
    }

    @FXML
    private void handleSave() {
        if (roomCombo.getValue() == null) {
            showAlert("Ошибка", "Выберите помещение");
            return;
        }
        if (tenantCombo.getValue() == null) {
            showAlert("Ошибка", "Выберите съёмщика");
            return;
        }
        if (descriptionArea.getText().trim().isEmpty()) {
            showAlert("Ошибка", "Введите описание проблемы");
            return;
        }
        Request request = isEditMode ? requestToEdit : new Request();
        request.setRoomId(roomCombo.getValue().getId());
        request.setTenantId(tenantCombo.getValue().getId());
        request.setDescription(descriptionArea.getText().trim());
        request.setCategory(categoryCombo.getValue());
        request.setPriority(priorityCombo.getValue());
        if (!isEditMode) {
            request.setContractorId(0);
            request.setCurrentStatus("новая");
        }
        boolean success = isEditMode? requestDAO.update(request) : requestDAO.add(request) > 0;
        if (success) {
            dialogStage.close();
        } else {
            showAlert("Ошибка", "Не удалось " + (isEditMode ? "сохранить" : "создать"));
        }
    }

    @FXML private void handleCancel() {
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