package rentalapp.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import rentalapp.DataAccessObject.TenantDAO;
import rentalapp.Entity.Tenant;
import rentalapp.util.Session;
import java.io.IOException;

public class TenantMainController {
    @FXML private Label tenantNameLabel;
    @FXML private StackPane contentArea;
    private final TenantDAO tenantDAO = new TenantDAO();
    @FXML public void initialize() {
        loadTenantInfo();
        showDashboard();
    }

    private void loadTenantInfo() {
        int tenantId = Session.getCurrentSession().getUserId();
        Tenant tenant = tenantDAO.findById(tenantId);
        if (tenant != null) {
            tenantNameLabel.setText(tenant.getFullName());
        }
    }
    @FXML private void showDashboard() {
        loadContent("/fxml/tenant_dashboard.fxml", "Главная");
    }
    @FXML private void showAgreements() {
        loadContent("/fxml/tenant_agreements.fxml", "Мои договоры");
    }
    @FXML private void showRequests() {
        loadContent("/fxml/tenant_requests.fxml", "Заявки на ремонт");
    }
    @FXML private void showPayments() {
        loadContent("/fxml/tenant_payments.fxml", "Мои платежи");
    }
    @FXML private void showProfile() {
        loadContent("/fxml/tenant_profile.fxml", "Профиль");
    }

    private void loadContent(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить: " + title);
        }
    }
    @FXML private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Выход");
        alert.setHeaderText("Выйти из системы?");
        alert.setContentText("Вы уверены, что хотите выйти?");
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) contentArea.getScene().getWindow();
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
                    stage.setScene(scene);
                    stage.setTitle("Вход в систему");
                    Session.getCurrentSession().setUserId(0);
                    Session.getCurrentSession().setRole(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}