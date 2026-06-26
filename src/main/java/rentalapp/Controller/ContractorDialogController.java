package rentalapp.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import rentalapp.DataAccessObject.ContractorDAO;
import rentalapp.Entity.Contractor;

public class ContractorDialogController {
    @FXML private Label dialogTitle;
    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private TextField specialityField;
    @FXML private Spinner<Integer> experienceSpinner;
    @FXML private Spinner<Double> ratingSpinner;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveButton;
    @Setter private Stage dialogStage;
    @Setter private ContractorDAO contractorDAO;
    private Contractor contractorToEdit;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        setupSpinners();
    }

    public void setContractorToEdit(Contractor contractor) {
        this.isEditMode = true;
        this.contractorToEdit = contractor;
        dialogTitle.setText("✏️ Редактировать исполнителя");
        populateFields();
    }

    private void populateFields() {
        if (contractorToEdit == null) return;
        fullNameField.setText(contractorToEdit.getFullName());
        phoneField.setText(contractorToEdit.getPhoneNumber());
        specialityField.setText(contractorToEdit.getSpeciality());
        experienceSpinner.getValueFactory().setValue(contractorToEdit.getExperienceYears());
        ratingSpinner.getValueFactory().setValue(contractorToEdit.getRating());
        descriptionArea.setText(contractorToEdit.getDescription());
        saveButton.setText("Сохранить");
    }

    private void setupSpinners() {
        SpinnerValueFactory<Integer> experienceFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0);
        experienceSpinner.setValueFactory(experienceFactory);
        experienceSpinner.setEditable(true);
        SpinnerValueFactory<Double> ratingFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 5.0, 0.0, 0.1);
        ratingSpinner.setValueFactory(ratingFactory);
        ratingSpinner.setEditable(true);
    }
    @FXML private void handleSave() {
        if (fullNameField.getText().trim().isEmpty()) {
            showAlert("Ошибка", "Введите ФИО");
            return;
        }
        if (phoneField.getText().trim().isEmpty()) {
            showAlert("Ошибка", "Введите телефон");
            return;
        }
        if (specialityField.getText().trim().isEmpty()) {
            showAlert("Ошибка", "Введите специальность");
            return;
        }
        Contractor contractor = isEditMode ? contractorToEdit : new Contractor();
        contractor.setFullName(fullNameField.getText().trim());
        contractor.setPhoneNumber(phoneField.getText().trim());
        contractor.setSpeciality(specialityField.getText().trim());
        contractor.setExperienceYears(experienceSpinner.getValue());
        contractor.setRating(ratingSpinner.getValue());
        contractor.setDescription(descriptionArea.getText().trim());
        boolean success = isEditMode ? contractorDAO.update(contractor) : contractorDAO.add(contractor) > 0;
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