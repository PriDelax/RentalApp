package rentalapp.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import rentalapp.DataAccessObject.ContractorDAO;
import rentalapp.Entity.Contractor;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ContractorsManagementController {
    @FXML private TextField searchField;
    @FXML private FlowPane contractorsFlowPane;
    private final ContractorDAO contractorDAO = new ContractorDAO();
    private List<Contractor> allContractors;
    @FXML public void initialize() {
        loadContractors();
    }

    private void loadContractors() {
        allContractors = contractorDAO.getAll();
        displayContractors(allContractors);
    }

    private void displayContractors(List<Contractor> contractors) {
        contractorsFlowPane.getChildren().clear();
        for (Contractor contractor : contractors) {
            VBox card = createContractorCard(contractor);
            contractorsFlowPane.getChildren().add(card);
        }
    }

    private VBox createContractorCard(Contractor contractor) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-width: 2px; " +
                        "-fx-padding: 15px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);"
        );
        card.setPrefWidth(250);
        Label iconLabel = new Label("🔧");
        iconLabel.setStyle("-fx-font-size: 32px; -fx-alignment: CENTER;");
        Label nameLabel = new Label(contractor.getFullName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label specialityLabel = new Label(contractor.getSpeciality());
        specialityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        HBox starsBox = new HBox(2);
        int fullStars = (int) Math.round(contractor.getRating());
        for (int i = 0; i < 5; i++) {
            Label star = new Label(i < fullStars ? "★" : "☆");
            star.setStyle(
                    "-fx-font-size: 18px; " +
                            "-fx-text-fill: " + (i < fullStars ? "#f39c12" : "#bdc3c7") + ";"
            );
            starsBox.getChildren().add(star);
        }
        Label experienceLabel = new Label("Опыт: " + contractor.getExperienceYears() + " лет");
        experienceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");
        Label phoneLabel = new Label("📞 " + contractor.getPhoneNumber());
        phoneLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3498db;");
        HBox buttons = new HBox(10);
        Button editBtn = new Button("✏️");
        editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand;");
        editBtn.setOnAction(e -> handleEditContractor(contractor));
        Button removeBtn = new Button("❌");
        removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> handleRemoveContractor(contractor));
        buttons.getChildren().addAll(editBtn, removeBtn);
        card.getChildren().addAll(
                iconLabel,
                nameLabel,
                specialityLabel,
                starsBox,
                experienceLabel,
                phoneLabel,
                buttons
        );
        return card;
    }
    @FXML private void handleAddContractor() {
        openContractorDialog(null);
    }

    private void handleEditContractor(Contractor contractor) {
        openContractorDialog(contractor);
    }

    private void openContractorDialog(Contractor contractor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/contractor_dialog.fxml"));
            Parent root = loader.load();
            ContractorDialogController controller = loader.getController();
            Stage dialogStage = new Stage();
            if (contractor != null) {
                controller.setContractorToEdit(contractor);
            }
            controller.setDialogStage(dialogStage);
            controller.setContractorDAO(contractorDAO);
            dialogStage.setTitle("Исполнитель");
            dialogStage.setScene(new Scene(root));
            dialogStage.getScene().getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(contractorsFlowPane.getScene().getWindow());
            dialogStage.showAndWait();
            handleRefresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRemoveContractor(Contractor contractor) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Удаление исполнителя");
        alert.setHeaderText("Удалить исполнителя?");
        alert.setContentText(contractor.getFullName());
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                contractorDAO.delete(contractor.getId());
                handleRefresh();
            }
        });
    }
    @FXML private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        List<Contractor> filtered = allContractors.stream().filter(c -> c.getFullName().toLowerCase().contains(query) || c.getSpeciality().toLowerCase().contains(query)).collect(Collectors.toList());
        displayContractors(filtered);
    }
    @FXML private void handleRefresh() {
        searchField.clear();
        loadContractors();
    }
}