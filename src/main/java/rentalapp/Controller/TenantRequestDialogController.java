package rentalapp.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import rentalapp.DataAccessObject.*;
import rentalapp.Entity.*;
import rentalapp.util.Session;

public class TenantRequestDialogController {
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private TextArea descriptionArea;
    @Setter
    private Stage dialogStage;
    private final RequestDAO requestDAO = new RequestDAO();
    private final AgreementDAO agreementDAO = new AgreementDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    @FXML public void initialize() {
        categoryCombo.getItems().addAll("Сантехника", "Электрика", "Ремонт", "Уборка", "Другое");
        categoryCombo.setValue("Сантехника");
        priorityCombo.getItems().addAll("немедленные", "срочные", "несрочные");
        priorityCombo.setValue("срочные");
    }
    @FXML private void handleCreate() {
        if (descriptionArea.getText().trim().isEmpty()) {
            showAlert("Ошибка", "Введите описание проблемы");
            return;
        }
        int tenantId = Session.getCurrentSession().getUserId();
        var agreements = agreementDAO.getAll().stream().filter(agr -> agr.getTenantId() == tenantId).filter(agr -> "в силе".equals(agr.getStatus())).toList();
        if (agreements.isEmpty()) {
            showAlert("Ошибка", "У вас нет активных договоров");
            return;
        }
        int roomId = agreements.get(0).getRoomId();
        Request request = new Request();
        request.setRoomId(roomId);
        request.setTenantId(tenantId);
        request.setContractorId(0);
        request.setDescription(descriptionArea.getText().trim());
        request.setCategory(categoryCombo.getValue());
        request.setPriority(priorityCombo.getValue());
        request.setCurrentStatus("новая");
        if (requestDAO.add(request) > 0) {
            dialogStage.close();
        } else {
            showAlert("Ошибка", "Не удалось создать заявку");
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