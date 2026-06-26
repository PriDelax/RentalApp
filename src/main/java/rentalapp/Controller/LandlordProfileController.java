package rentalapp.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.Node;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import rentalapp.DataAccessObject.LandlordDAO;
import rentalapp.Entity.Landlord;
import rentalapp.util.Session;
import java.time.format.DateTimeFormatter;

public class LandlordProfileController {
    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField passportField;
    @FXML private TextField paymentDetailsField;
    @FXML private TextField birthDateField;
    @FXML private TextField createdAtField;
    private final LandlordDAO landlordDAO = new LandlordDAO();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private Landlord currentLandlord;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    @FXML public void initialize() {
        loadProfile();
    }

    private void loadProfile() {
        int landlordId = Session.getCurrentSession().getUserId();
        currentLandlord = landlordDAO.findById(landlordId);
        if (currentLandlord != null) {
            fullNameField.setText(currentLandlord.getFullName());
            phoneField.setText(currentLandlord.getPhoneNumber());
            emailField.setText(currentLandlord.getEmail());
            passportField.setText(currentLandlord.getPassportDetails());
            paymentDetailsField.setText(currentLandlord.getPaymentDetails());
            if (currentLandlord.getBirthDate() != null) {
                birthDateField.setText(currentLandlord.getBirthDate().format(dateFormatter));
            } else {
                birthDateField.setText("Не указана");
            }
            if (currentLandlord.getCreatedAt() != null) {
                createdAtField.setText(currentLandlord.getCreatedAt().format(dateFormatter));
            } else {
                createdAtField.setText("—");
            }
        }
    }
    @FXML private void handleSave() {
        if (phoneField.getText().trim().isEmpty()) {
            showAlert("Ошибка", "Введите номер телефона");
            return;
        }
        if (emailField.getText().trim().isEmpty()) {
            showAlert("Ошибка", "Введите email");
            return;
        }
        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Ошибка", "Введите корректный email");
            return;
        }
        if (paymentDetailsField.getText().trim().isEmpty()) {
            showAlert("Ошибка", "Введите реквизиты для оплаты");
            return;
        }
        currentLandlord.setPhoneNumber(phoneField.getText().trim());
        currentLandlord.setEmail(emailField.getText().trim());
        currentLandlord.setPaymentDetails(paymentDetailsField.getText().trim());
        if (landlordDAO.update(currentLandlord)) {
            showAlert("Успех", "Данные успешно сохранены");
        } else {
            showAlert("Ошибка", "Не удалось сохранить данные");
        }
    }
    @FXML private void handleChangePassword() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("🔑 Смена пароля");
        dialog.setHeaderText("Введите новый пароль");
        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Текущий пароль");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Новый пароль");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Подтвердите пароль");
        ButtonType saveButton = new ButtonType("Сохранить", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);
        Node saveButtonNode = dialog.getDialogPane().lookupButton(saveButton);
        saveButtonNode.setDisable(true);
        oldPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validateFields(oldPasswordField, newPasswordField, confirmPasswordField, saveButtonNode));
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validateFields(oldPasswordField, newPasswordField, confirmPasswordField, saveButtonNode));
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validateFields(oldPasswordField, newPasswordField, confirmPasswordField, saveButtonNode));
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Текущий пароль:"), 0, 0);
        grid.add(oldPasswordField, 1, 0);
        grid.add(new Label("Новый пароль:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Подтверждение:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButton) {
                String dbPassword = currentLandlord.getPassword();
                String inputPassword = oldPasswordField.getText();
                String newPass = newPasswordField.getText();
                String confirmPass = confirmPasswordField.getText();
                if (!passwordEncoder.matches(inputPassword, dbPassword)) {
                    showAlert("Ошибка", "Неверный текущий пароль");
                    return null;
                }
                if (!newPass.equals(confirmPass)) {
                    showAlert("Ошибка", "Пароли не совпадают");
                    return null;
                }
                if (newPass.length() < 6) {
                    showAlert("Ошибка", "Пароль должен быть не менее 6 символов");
                    return null;
                }
                String hashedPassword = passwordEncoder.encode(newPass);
                currentLandlord.setPassword(hashedPassword);
                if (landlordDAO.update(currentLandlord)) {
                    showAlert("Успех", "Пароль успешно изменён");
                } else {
                    showAlert("Ошибка", "Не удалось изменить пароль");
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void validateFields(PasswordField oldPass, PasswordField newPass,
                                PasswordField confirmPass, Node saveButton) {
        boolean allFilled = !oldPass.getText().isEmpty()
                && !newPass.getText().isEmpty()
                && !confirmPass.getText().isEmpty();
        saveButton.setDisable(!allFilled);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}