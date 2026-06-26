package rentalapp.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import rentalapp.DataAccessObject.ContractorDAO;
import rentalapp.DataAccessObject.RequestDAO;
import rentalapp.Entity.Contractor;
import rentalapp.Entity.Request;
import java.util.List;

public class AssignContractorDialogController {
    @FXML private Label requestLabel;
    @FXML private ComboBox<Contractor> contractorCombo;
    @Setter private Stage dialogStage;
    @Setter private RequestDAO requestDAO;
    @Setter private ContractorDAO contractorDAO;
    private Request requestToEdit;
    @FXML public void initialize() {}

    public void loadData(){
        setupContractorCombo();
    }

    public void setRequest(Request request) {
        this.requestToEdit = request;
        requestLabel.setText(request.getDescription());
    }

    private void setupContractorCombo() {
        List<Contractor> contractors = contractorDAO.getAll();
        contractorCombo.getItems().addAll(contractors);
        contractorCombo.setCellFactory(listView -> new ListCell<Contractor>() {
            @Override
            protected void updateItem(Contractor contractor, boolean empty) {
                super.updateItem(contractor, empty);
                if (empty || contractor == null) {
                    setText(null);
                } else {
                    setText(contractor.getFullName() + " (" + contractor.getSpeciality() + ")");
                }
            }
        });
        contractorCombo.setButtonCell(contractorCombo.getCellFactory().call(null));
    }
    @FXML
    private void handleAssign() {
        Contractor selected = contractorCombo.getValue();
        if (selected == null) {
            showAlert("Ошибка", "Выберите исполнителя");
            return;
        }
        requestToEdit.setContractorId(selected.getId());
        requestToEdit.setCurrentStatus("в работе");
        boolean success = requestDAO.update(requestToEdit);
        if (success) {
            dialogStage.close();
        } else {
            showAlert("Ошибка", "Не удалось назначить исполнителя");
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