package rentalapp.Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import rentalapp.DataAccessObject.*;
import rentalapp.Entity.*;
import rentalapp.util.Session;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AgreementsManagementController {
    @FXML private TableView<Agreement> agreementsTable;
    @FXML private TableColumn<Agreement, String> tenantColumn;
    @FXML private TableColumn<Agreement, String> roomColumn;
    @FXML private TableColumn<Agreement, String> landlordColumn;
    @FXML private TableColumn<Agreement, String> effectiveDateColumn;
    @FXML private TableColumn<Agreement, String> determinationDateColumn;
    @FXML private TableColumn<Agreement, String> arendaSumColumn;
    @FXML private TableColumn<Agreement, String> paymentScheduleColumn;
    @FXML private TableColumn<Agreement, String> statusColumn;
    @FXML private TableColumn<Agreement, Void> actionsColumn;
    @FXML private Label totalAgreementsLabel;
    @FXML private Label activeAgreementsLabel;
    @FXML private Label totalIncomeLabel;
    @FXML private Label expiringSoonLabel;
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> incomeBarChart;
    @FXML private TabPane tabPane;
    private final AgreementDAO agreementDAO = new AgreementDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final TenantDAO tenantDAO = new TenantDAO();
    private final LandlordDAO landlordDAO = new LandlordDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final ObservableList<Agreement> agreementsList = FXCollections.observableArrayList();
    @FXML public void initialize() {
        setupColumns();
        loadAgreements("all");
        setupActionsColumn();
        updateStatistics();
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null && "📊 Статистика".equals(newTab.getText())) {
                Platform.runLater(() -> {
                    updateCharts();
                });
            }
        });
    }

    @FXML
    private void updateCharts() {
        updateStatusPieChart();
        updateIncomeBarChart();
    }

    private void updateStatusPieChart() {
        int landlordId = Session.getCurrentSession().getUserId();
        List<Room> landlordRooms = roomDAO.findByLandlordId(landlordId);
        List<Integer> roomIds = landlordRooms.stream().map(Room::getId).toList();
        List<Agreement> allAgreements = agreementDAO.getAll();
        List<Agreement> landlordAgreements = allAgreements.stream().filter(agr -> roomIds.contains(agr.getRoomId())).toList();
        long activeCount = landlordAgreements.stream().filter(agr -> "в силе".equals(agr.getStatus())).count();
        long terminatedCount = landlordAgreements.stream().filter(agr -> "расторгнут".equals(agr.getStatus())).count();
        System.out.println("PieChart данные: В силе=" + activeCount + ", Расторгнут=" + terminatedCount);
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(new PieChart.Data("В силе", activeCount), new PieChart.Data("Расторгнут", terminatedCount));
        statusPieChart.setData(pieChartData);
    }

    private void updateIncomeBarChart() {
        int landlordId = Session.getCurrentSession().getUserId();
        List<Room> landlordRooms = roomDAO.findByLandlordId(landlordId);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Арендная плата");
        for (Room room : landlordRooms) {
            List<Agreement> activeAgreements = agreementDAO.getAll().stream().filter(agr -> agr.getRoomId() == room.getId()).filter(agr -> "в силе".equals(agr.getStatus())).toList();
            if (!activeAgreements.isEmpty()) {
                int totalIncome = activeAgreements.stream().mapToInt(Agreement::getArendaSum).sum();
                String shortAddress = room.getAddress().length() > 20 ? room.getAddress().substring(0, 20) + "..." : room.getAddress();
                series.getData().add(new XYChart.Data<>(shortAddress, totalIncome));
            }
        }
    }

    private void updateStatistics() {
        int landlordId = Session.getCurrentSession().getUserId();
        List<Room> landlordRooms = roomDAO.findByLandlordId(landlordId);
        List<Integer> roomIds = landlordRooms.stream().map(Room::getId).toList();
        List<Agreement> allAgreements = agreementDAO.getAll();
        List<Agreement> landlordAgreements = allAgreements.stream().filter(agr -> roomIds.contains(agr.getRoomId())).toList();
        int total = landlordAgreements.size();
        totalAgreementsLabel.setText(String.valueOf(total));
        List<Agreement> activeAgreements = landlordAgreements.stream().filter(agr -> "в силе".equals(agr.getStatus())).toList();
        activeAgreementsLabel.setText(String.valueOf(activeAgreements.size()));
        int totalIncome = activeAgreements.stream().mapToInt(Agreement::getArendaSum).sum();
        totalIncomeLabel.setText(String.format("%,d ₽", totalIncome).replace(',', ' '));
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);
        long expiringSoon = activeAgreements.stream().filter(agr -> agr.getDeterminationDate() != null)
                .filter(agr -> {
                    LocalDate endDate = agr.getDeterminationDate();
                    return (endDate.isEqual(today) || endDate.isAfter(today)) &&
                            endDate.isBefore(thirtyDaysLater);
                })
                .count();
        expiringSoonLabel.setText(String.valueOf(expiringSoon));
        if (expiringSoon > 0) {
            expiringSoonLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            expiringSoonLabel.setStyle("-fx-text-fill: #27ae60;");
        }
    }

    @FXML
    private void handleUpdate() {
        loadAgreements("all");
        updateStatistics();
    }

    private void setupColumns() {
        tenantColumn.setCellValueFactory(cellData -> {
            Agreement agr = cellData.getValue();
            Tenant tenant = tenantDAO.findById(agr.getTenantId());
            return new javafx.beans.property.SimpleStringProperty(tenant != null ? tenant.getFullName() : "—");
        });
        roomColumn.setCellValueFactory(cellData -> {
            Agreement agr = cellData.getValue();
            Room room = roomDAO.findById(agr.getRoomId());
            return new javafx.beans.property.SimpleStringProperty(room != null ? room.getAddress() : "—");
        });
        landlordColumn.setCellValueFactory(cellData -> {
            Agreement agr = cellData.getValue();
            Landlord landlord = landlordDAO.findById(agr.getLandlordId());
            return new javafx.beans.property.SimpleStringProperty(landlord != null ? landlord.getFullName() : "—");
        });
        effectiveDateColumn.setCellValueFactory(cellData -> {
            Agreement agr = cellData.getValue();
            if (agr.getEffectiveDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(agr.getEffectiveDate().format(dateFormatter));
            }
            return new javafx.beans.property.SimpleStringProperty("—");
        });

        determinationDateColumn.setCellValueFactory(cellData -> {
            Agreement agr = cellData.getValue();
            if (agr.getDeterminationDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(agr.getDeterminationDate().format(dateFormatter));
            }
            return new javafx.beans.property.SimpleStringProperty("—");
        });
        arendaSumColumn.setCellValueFactory(cellData -> {
            Agreement agr = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(agr.getArendaSum() + " ₽");
        });
        paymentScheduleColumn.setCellValueFactory(new PropertyValueFactory<>("paymentSchedule"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        agreementsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadAgreements(String statusFilter) {
        int landlordId = Session.getCurrentSession().getUserId();
        List<Room> landlordRooms = roomDAO.findByLandlordId(landlordId);
        List<Integer> roomIds = landlordRooms.stream().map(Room::getId).toList();
        List<Agreement> allAgreements = agreementDAO.getAll();
        List<Agreement> filteredAgreements = allAgreements.stream().filter(agr -> roomIds.contains(agr.getRoomId())).collect(Collectors.toList());
        if (!"all".equals(statusFilter)) {
            filteredAgreements = filteredAgreements.stream().filter(agr -> statusFilter.equals(agr.getStatus())).collect(Collectors.toList());
        }
        agreementsList.setAll(filteredAgreements);
        agreementsTable.setItems(agreementsList);
    }
    @FXML private void filterAll() { loadAgreements("all"); }
    @FXML private void filterActive() { loadAgreements("в силе"); }
    @FXML private void filterTerminate() { loadAgreements("расторгнут"); }
    @FXML private void handleCreateAgreement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/agreement_dialog.fxml"));
            Parent root = loader.load();
            AgreementDialogController controller = loader.getController();
            Stage dialogStage = new Stage();
            controller.setDialogStage(dialogStage);
            controller.setAgreementDAO(agreementDAO);
            controller.setRoomDAO(roomDAO);
            controller.setTenantDAO(tenantDAO);
            controller.loadData();
            dialogStage.setTitle("📄 Новый договор");
            dialogStage.setScene(new Scene(root));
            dialogStage.getScene().getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(agreementsTable.getScene().getWindow());
            dialogStage.showAndWait();
            handleUpdate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<Agreement, Void>() {
            private final Button terminateBtn = new Button("Расторгнуть");
            {
                terminateBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " + "-fx-cursor: hand; -fx-padding: 5px 10px; -fx-background-radius: 3px;");
                terminateBtn.setOnAction(e -> {
                    Agreement agreement = getTableView().getItems().get(getIndex());
                    terminateAgreement(agreement);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Agreement agreement = getTableView().getItems().get(getIndex());
                    if ("в силе".equals(agreement.getStatus())) {
                        setGraphic(terminateBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        actionsColumn.setSortable(false);
    }

    private void openAgreementDialog(Agreement agreementToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/agreement_dialog.fxml"));
            Parent root = loader.load();
            AgreementDialogController controller = loader.getController();
            Stage dialogStage = new Stage();
            if (agreementToEdit != null) {
                controller.setAgreementToEdit(agreementToEdit);
                controller.setEditMode(true);
                dialogStage.setTitle("️Редактировать договор");
            } else {
                dialogStage.setTitle("📄 Новый договор");
            }
            controller.setDialogStage(dialogStage);
            controller.setAgreementDAO(agreementDAO);
            controller.setRoomDAO(roomDAO);
            controller.setTenantDAO(tenantDAO);
            controller.loadData();
            dialogStage.setScene(new Scene(root));
            dialogStage.getScene().getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(agreementsTable.getScene().getWindow());
            dialogStage.showAndWait();
            handleUpdate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEdit() {
        Agreement selected = agreementsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Внимание");
            alert.setHeaderText("Ничего не выбрано");
            alert.setContentText("Выберите договор для редактирования.");
            alert.showAndWait();
            return;
        }
        if ("расторгнут".equals(selected.getStatus())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Нельзя редактировать");
            alert.setHeaderText("Расторгнутые договоры нельзя изменять");
            alert.setContentText("Этот договор был расторгнут и больше не может быть изменён.");
            alert.showAndWait();
            return;
        }
        openAgreementDialog(selected);
    }

    private void terminateAgreement(Agreement agreement) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Расторжение договора");
        alert.setHeaderText("Расторгнуть договор?");
        alert.setContentText("Помещение: " + roomDAO.findById(agreement.getRoomId()).getAddress() + "\n" + "После расторжения:\n" +
                        "- Дата окончания = сегодня\n" +
                        "- Сумма аренды = 0\n" +
                        "- Договор нельзя будет редактировать");
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                agreement.setStatus("расторгнут");
                agreement.setDeterminationDate(LocalDate.now());
                agreement.setArendaSum(0);
                boolean success = agreementDAO.update(agreement);
                if (success) {
                    Room room = roomDAO.findById(agreement.getRoomId());
                    room.setStatus("свободна");
                    roomDAO.update(room);
                    handleUpdate();
                    showAlert("Успех", "Договор расторгнут. Помещение свободно.");
                } else {
                    showAlert("Ошибка", "Не удалось расторгнуть договор");
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}