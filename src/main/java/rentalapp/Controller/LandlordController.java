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
import rentalapp.AuthService;
import rentalapp.DataAccessObject.*;
import rentalapp.util.Session;

import java.io.IOException;

public class LandlordController {
    @FXML private Label userNameLabel;
    @FXML private Label roomsCountLabel;
    @FXML private Label agreementsCountLabel;
    @FXML private Label tenantsCountLabel;
    @FXML private StackPane contentArea;
    private final AuthService authService = new AuthService();
    private final RoomDAO roomDAO = new RoomDAO();
    private final AgreementDAO agreementDAO = new AgreementDAO();
    private final TenantDAO tenantDAO = new TenantDAO();
    @FXML
    public void initialize() {
        userNameLabel.setText(authService.getCurrentUserName());
        loadStatistics();
        System.out.println("Панель арендодателя загружена");
    }

    private void loadStatistics() {
        int landlordId = Session.getCurrentSession().getUserId();
        int roomsCount = roomDAO.findByLandlordId(landlordId).size();
        int agreementsCount = agreementDAO.findByLandlordId(landlordId).size();
        int tenantsCount = tenantDAO.getAll().size();
        roomsCountLabel.setText(String.valueOf(roomsCount));
        agreementsCountLabel.setText(String.valueOf(agreementsCount));
        tenantsCountLabel.setText(String.valueOf(tenantsCount));
    }
    @FXML
    private void showRooms() {
        loadContent("/fxml/landlord_rooms.fxml", "Мои помещения");
    }
    @FXML
    private void showAgreements() {
        loadContent("/fxml/landlord_agreements.fxml", "Мои договоры");
    }
    @FXML
    private void showTenants() {
        loadContent("/fxml/landlord_tenants.fxml", "Съёмщики");
    }
    @FXML
    private void showRequests() {
        loadContent("/fxml/landlord_requests.fxml", "Заявки на ремонт");
    }
    @FXML
    private void showContractors() {
        loadContent("/fxml/contractor_management.fxml", "Исполнители");
    }
    @FXML
    private void showFinance() {
        loadContent("/fxml/landlord_finance.fxml", "Финансы");
    }
    @FXML
    private void showProfile(){
        loadContent("/fxml/landlord_profile.fxml", "Профиль");
    }

    private void loadContent(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
            System.out.println("Загружено: " + title);
        } catch (Exception e) {
            System.err.println("⚠️ Файл " + fxmlPath + " ещё не создан");
            Label placeholder = new Label("🚧 Раздел \"" + title + "\" в разработке");
            placeholder.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d;");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(placeholder);
        }
    }
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Выход из системы");
        alert.setHeaderText("Вы действительно хотите выйти?");
        alert.setContentText("Все несохранённые изменения будут потеряны.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                authService.logout();
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) userNameLabel.getScene().getWindow();
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
                    stage.setScene(scene);
                    stage.setTitle("Учёт аренды жилых помещений");
                    stage.setResizable(false);
                    System.out.println("Выход выполнен, возвращено окно входа");
                } catch (IOException e) {
                    System.err.println("Ошибка при загрузке окна входа: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}