package rentalapp.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import rentalapp.DataAccessObject.*;
import rentalapp.Entity.*;
import rentalapp.util.Session;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TenantRequestsController {
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<Request> requestsTable;
    @FXML private TableColumn<Request, String> dateColumn;
    @FXML private TableColumn<Request, String> categoryColumn;
    @FXML private TableColumn<Request, String> priorityColumn;
    @FXML private TableColumn<Request, String> descriptionColumn;
    @FXML private TableColumn<Request, String> contractorColumn;
    @FXML private TableColumn<Request, String> statusColumn;
    @FXML private TableColumn<Request, Void> actionsColumn;
    private final RequestDAO requestDAO = new RequestDAO();
    private final ContractorDAO contractorDAO = new ContractorDAO();
    private final AgreementDAO agreementDAO = new AgreementDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final ObservableList<Request> requestsList = FXCollections.observableArrayList();
    @FXML public void initialize() {
        setupColumns();
        loadRequests();
        setupFilters();
    }

    private void setupColumns() {
        dateColumn.setCellValueFactory(cellData -> {
            Request req = cellData.getValue();
            if (req.getCreatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(req.getCreatedAt().format(dateFormatter));
            }
            return new javafx.beans.property.SimpleStringProperty("—");
        });
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(priority);
                    String color = switch (priority) {
                        case "немедленные" -> "#e74c3c";
                        case "срочные" -> "#f39c12";
                        case "несрочные" -> "#27ae60";
                        default -> "#7f8c8d";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        contractorColumn.setCellValueFactory(cellData -> {
            Request req = cellData.getValue();
            if (req.getContractorId() > 0) {
                Contractor contractor = contractorDAO.findById(req.getContractorId());
                return new javafx.beans.property.SimpleStringProperty(contractor != null ? contractor.getFullName() : "—");
            }
            return new javafx.beans.property.SimpleStringProperty("Не назначен");
        });
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("currentStatus"));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    String color = switch (status) {
                        case "новая" -> "#f39c12";
                        case "в работе" -> "#3498db";
                        case "закрыта" -> "#27ae60";
                        case "отменена" -> "#e74c3c";
                        default -> "#7f8c8d";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });
        setupActionsColumn();
        requestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button cancelButton = new Button("❌ Отменить");
            {
                cancelButton.setStyle(
                        "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                                "-fx-cursor: hand; -fx-padding: 5px 10px; -fx-background-radius: 3px;"
                );
                cancelButton.setOnAction(e -> {
                    Request request = getTableView().getItems().get(getIndex());
                    handleCancelRequest(request);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Request request = getTableView().getItems().get(getIndex());
                    if ("новая".equals(request.getCurrentStatus())) {
                        setGraphic(cancelButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        actionsColumn.setSortable(false);
    }

    private void loadRequests() {
        int tenantId = Session.getCurrentSession().getUserId();
        List<Agreement> agreements = agreementDAO.getAll().stream().filter(agr -> agr.getTenantId() == tenantId).toList();
        List<Integer> roomIds = agreements.stream().map(Agreement::getRoomId).toList();
        List<Request> tenantRequests = requestDAO.getAll().stream().filter(req -> roomIds.contains(req.getRoomId())).sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt())).collect(Collectors.toList());
        requestsList.setAll(tenantRequests);
        applyFilters();
    }

    private void setupFilters() {
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        String selectedStatus = statusFilterCombo.getValue();
        if (selectedStatus == null || "Все".equals(selectedStatus)) {
            requestsTable.setItems(requestsList);
        } else {
            ObservableList<Request> filtered = requestsList.stream().filter(req -> selectedStatus.equals(req.getCurrentStatus())).collect(FXCollections::observableArrayList, ObservableList::add, ObservableList::addAll);
            requestsTable.setItems(filtered);
        }
    }

    @FXML private void handleCreateRequest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tenant_request_dialog.fxml"));
            Parent root = loader.load();
            TenantRequestDialogController controller = loader.getController();
            Stage dialogStage = new Stage();
            controller.setDialogStage(dialogStage);
            dialogStage.setTitle("🔧 Новая заявка");
            dialogStage.setScene(new Scene(root));
            dialogStage.getScene().getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(requestsTable.getScene().getWindow());
            dialogStage.showAndWait();
            handleRefresh();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть диалог создания заявки");
        }
    }

    private void handleCancelRequest(Request request) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Отмена заявки");
        alert.setHeaderText("Отменить заявку?");
        alert.setContentText("Вы уверены, что хотите отменить эту заявку?");
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                request.setCurrentStatus("отменена");
                if (requestDAO.update(request)) {
                    handleRefresh();
                    showAlert("Успех", "Заявка отменена");
                } else {
                    showAlert("Ошибка", "Не удалось отменить заявку");
                }
            }
        });
    }
    @FXML private void handleRefresh() {
        statusFilterCombo.setValue("Все");
        loadRequests();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}