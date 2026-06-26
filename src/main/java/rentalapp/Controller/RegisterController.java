package rentalapp.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import rentalapp.AuthService;
import rentalapp.util.Validator;

public class RegisterController {

    @FXML private RadioButton tenantRadio;
    @FXML private RadioButton landlordRadio;
    @FXML private ToggleGroup roleGroup;
    @FXML private TextField loginField;
    @FXML private Label loginError;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordError;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label confirmPasswordError;
    @FXML private TextField fullNameField;
    @FXML private Label fullNameError;
    @FXML private TextField phoneField;
    @FXML private Label phoneError;
    @FXML private TextField emailField;
    @FXML private Label emailError;
    @FXML private TextField passportField;
    @FXML private Label passportError;
    @FXML private VBox paymentDetailsBox;
    @FXML private TextField paymentDetailsField;
    @FXML private Label paymentDetailsError;
    @FXML private Label generalError;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        roleGroup = new ToggleGroup();
        tenantRadio.setToggleGroup(roleGroup);
        landlordRadio.setToggleGroup(roleGroup);
        landlordRadio.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            paymentDetailsBox.setVisible(isSelected);
            paymentDetailsBox.setManaged(isSelected);
        });
        loginField.textProperty().addListener((obs, old, newVal) -> clearError(loginError));
        passwordField.textProperty().addListener((obs, old, newVal) -> clearError(passwordError));
        confirmPasswordField.textProperty().addListener((obs, old, newVal) -> clearError(confirmPasswordError));
    }

    @FXML
    private void handleRegister() {
        clearAllErrors();
        String login = loginField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String passport = passportField.getText().trim();
        String paymentDetails = paymentDetailsField.getText().trim();
        boolean hasErrors = false;
        if (!Validator.isValidLogin(login)) {
            showError(loginError, "Логин: 3-50 символов");
            hasErrors = true;
        }
        if (!Validator.isValidPassword(password)) {
            showError(passwordError, "Пароль: мин. 6 символов");
            hasErrors = true;
        }
        if (!Validator.doPasswordsMatch(password, confirmPassword)) {
            showError(confirmPasswordError, "Пароли не совпадают");
            hasErrors = true;
        }
        if (!Validator.isValidFullName(fullName)) {
            showError(fullNameError, "Введите ФИО");
            hasErrors = true;
        }
        if (!Validator.isValidPhone(phone)) {
            showError(phoneError, "Введите телефон");
            hasErrors = true;
        }
        if (!Validator.isValidEmail(email)) {
            showError(emailError, "Введите email");
            hasErrors = true;
        }
        if (!Validator.isValidPassport(passport)) {
            showError(passportError, "Введите паспортные данные");
            hasErrors = true;
        }
        if (landlordRadio.isSelected() && !Validator.isValidPaymentDetails(paymentDetails)) {
            showError(paymentDetailsError, "Введите реквизиты");
            hasErrors = true;
        }
        if (hasErrors) return;
        int newId;
        if (tenantRadio.isSelected()) {
            newId = authService.registerTenant(login, password, fullName, phone, email, passport);
        } else if (landlordRadio.isSelected()) {
            newId = authService.registerLandlord(login, password, fullName, phone, email, passport, paymentDetails);
        } else {
            showError(generalError, "Ошибка: не выбрана роль");
            return;
        }
        if (newId > 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Регистрация успешна");
            alert.setHeaderText("Аккаунт создан!");
            alert.setContentText("Теперь вы можете войти в систему.");
            alert.showAndWait();
            handleBack();
        } else {
            showError(generalError, "Не удалось зарегистрироваться. Логин занят?");
        }
    }
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Учёт аренды жилых помещений");
            stage.setResizable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(Label label, String msg) {
        label.setText(msg);
        label.setVisible(true);
    }
    private void clearError(Label label) {
        label.setText("");
        label.setVisible(false);
    }
    private void clearAllErrors() {
        clearError(loginError); clearError(passwordError); clearError(confirmPasswordError);
        clearError(fullNameError); clearError(phoneError); clearError(emailError);
        clearError(passportError); clearError(paymentDetailsError); clearError(generalError);
    }
}