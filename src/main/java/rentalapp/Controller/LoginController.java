package rentalapp.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import rentalapp.AuthService;
import rentalapp.util.Session;

public class LoginController {
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Hyperlink registerLink;
    private final AuthService authService = new AuthService();
    @FXML
    public void initialize() {
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
    }
    @FXML
    private void handleLogin() {
        String login = loginField.getText().trim();
        String password = passwordField.getText();
        if (login.isEmpty() || password.isEmpty()) {
            showError("Введите логин и пароль");
            return;
        }
        boolean success = authService.login(login, password);
        if (success) {
            openMainWindow();
        } else {
            showError("Неверный логин или пароль");
            passwordField.clear();
        }
    }
    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Регистрация");
            stage.setResizable(false);
        } catch (Exception e) {
            System.err.println("Ошибка открытия окна регистрации: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openMainWindow() {
        try {
            String fxmlFile;
            String title;
            if ("LANDLORD".equals(Session.getCurrentUserRole())) {
                fxmlFile = "/fxml/landlord_main.fxml";
                title = "Панель арендодателя";
            } else if ("TENANT".equals(Session.getCurrentUserRole())) {
                fxmlFile = "/fxml/tenant_main.fxml";
                title = "Личный кабинет съёмщика";
            } else {
                showError("Неизвестная роль пользователя");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) loginField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.setResizable(true);
        } catch (Exception e) {
            System.err.println("Ошибка открытия главного окна: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        javafx.animation.KeyValue keyValue = new javafx.animation.KeyValue(errorLabel.visibleProperty(), false);
        javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), event -> errorLabel.setVisible(false));
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(keyFrame);
        timeline.play();
    }
}