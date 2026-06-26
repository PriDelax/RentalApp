package rentalapp.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import rentalapp.DataAccessObject.*;
import rentalapp.Entity.*;
import rentalapp.util.Session;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class RequestsManagementController {
    @FXML private TableView<Request> requestsTable;
    @FXML private TableColumn<Request, String> descriptionColumn;
    @FXML private TableColumn<Request, String> categoryColumn;
    @FXML private TableColumn<Request, String> priorityColumn;
    @FXML private TableColumn<Request, String> currentStatusColumn;
    @FXML private TableColumn<Request, String> roomColumn;
    @FXML private TableColumn<Request, String> tenantColumn;
    @FXML private TableColumn<Request, String> contractorColumn;
    @FXML private TableColumn<Request, String> createdAtColumn;
    @FXML private TableColumn<Request, Void> actionsColumn;
    private final RequestDAO requestDAO = new RequestDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final TenantDAO tenantDAO = new TenantDAO();
    private final ContractorDAO contractorDAO = new ContractorDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final ObservableList<Request> requestsList = FXCollections.observableArrayList();
    @FXML public void initialize() {
        setupColumns();
        loadRequests("all");
        setupActionsColumn();
    }

    private void setupColumns() {
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        currentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("currentStatus"));
        roomColumn.setCellValueFactory(cellData -> {
            Request req = cellData.getValue();
            Room room = roomDAO.findById(req.getRoomId());
            return new javafx.beans.property.SimpleStringProperty(room != null ? room.getAddress() : "—");
        });
        tenantColumn.setCellValueFactory(cellData -> {
            Request req = cellData.getValue();
            Tenant tenant = tenantDAO.findById(req.getTenantId());
            return new javafx.beans.property.SimpleStringProperty(tenant != null ? tenant.getFullName() : "—");
        });
        contractorColumn.setCellValueFactory(cellData -> {
            Request req = cellData.getValue();
            int contractorId = req.getContractorId();
            if (contractorId == 0) {
                return new javafx.beans.property.SimpleStringProperty("Не назначен");
            }
            Contractor contractor = contractorDAO.findById(contractorId);
            return new javafx.beans.property.SimpleStringProperty(contractor != null ? contractor.getFullName() : "—");
        });
        createdAtColumn.setCellValueFactory(cellData -> {
            Request req = cellData.getValue();
            if (req.getCreatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(req.getCreatedAt().format(dateFormatter));
            }
            return new javafx.beans.property.SimpleStringProperty("—");
        });
        requestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadRequests(String statusFilter) {
        int landlordId = Session.getCurrentSession().getUserId();
        List<Room> landlordRooms = roomDAO.findByLandlordId(landlordId);
        List<Integer> roomIds = landlordRooms.stream().map(Room::getId).toList();
        List<Request> allRequests = requestDAO.getAll();
        List<Request> filteredRequests = allRequests.stream().filter(req -> roomIds.contains(req.getRoomId())).collect(Collectors.toList());
        if (!"all".equals(statusFilter)) {
            filteredRequests = filteredRequests.stream().filter(req -> statusFilter.equals(req.getCurrentStatus())).collect(Collectors.toList());
        }
        filteredRequests.sort((r1, r2) -> {
            int priority1 = getPriorityValue(r1.getPriority());
            int priority2 = getPriorityValue(r2.getPriority());
            return Integer.compare(priority1, priority2);
        });
        requestsList.setAll(filteredRequests);
        requestsTable.setItems(requestsList);
    }

    private int getPriorityValue(String priority) {
        if (priority == null) return 999;
        return switch (priority) {
            case "немедленные" -> 1;  // Самый высокий приоритет
            case "срочные" -> 2;
            case "несрочные" -> 3;
            default -> 999;
        };
    }
    @FXML private void filterAll() { loadRequests("all"); }
    @FXML private void filterNew() { loadRequests("новая"); }
    @FXML private void filterWorking() { loadRequests("в работе"); }
    @FXML private void filterDone() { loadRequests("закрыта"); }
    @FXML private void handleUpdate() {
        String currentFilter = getCurrentFilter();
        loadRequests(currentFilter);
    }

    private String getCurrentFilter() {
        return "all";
    }

    private void openRequestDialog(Request requestToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/request_dialog.fxml"));
            Parent root = loader.load();
            RequestDialogController controller = loader.getController();
            Stage dialogStage = new Stage();
            if (requestToEdit != null) {
                controller.setRequestToEdit(requestToEdit);
                controller.setEditMode(true);
                dialogStage.setTitle("Редактировать заявку");
            } else {
                dialogStage.setTitle("Новая заявка");
            }
            controller.setDialogStage(dialogStage);
            controller.setRequestDAO(requestDAO);
            controller.setRoomDAO(roomDAO);
            controller.setTenantDAO(tenantDAO);
            controller.loadData();
            dialogStage.setScene(new Scene(root));
            dialogStage.getScene().getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(requestsTable.getScene().getWindow());
            dialogStage.showAndWait();
            handleUpdate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateRequest() {
        openRequestDialog(null);
    }

    @FXML
    private void handleEdit() {
        Request selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите заявку для редактирования");
            return;
        }
        openRequestDialog(selected);
    }

    private void showAssignDialog(Request request) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/assign_contractor_dialog.fxml"));
            Parent root = loader.load();
            AssignContractorDialogController controller = loader.getController();
            Stage dialogStage = new Stage();
            controller.setDialogStage(dialogStage);
            controller.setRequestDAO(requestDAO);
            controller.setContractorDAO(new ContractorDAO());
            controller.setRequest(request);
            controller.loadData();
            dialogStage.setTitle("Назначить исполнителя");
            dialogStage.setScene(new Scene(root));
            dialogStage.getScene().getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(requestsTable.getScene().getWindow());
            dialogStage.showAndWait();
            handleUpdate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void changeStatus(Request request, String newStatus) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Смена статуса");
        alert.setHeaderText("Изменить статус заявки?");
        String statusText = switch (newStatus) {
            case "закрыта" -> "завершённая";
            case "новая" -> "новая";
            case "в работе" -> "в работе";
            default -> newStatus;
        };
        alert.setContentText("Заявка #" + request.getId() + "\n" + "Новый статус: " + statusText);
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                request.setCurrentStatus(newStatus);
                boolean success = requestDAO.update(request);
                if (success) {
                    handleUpdate();
                    showAlert("Успех", "Статус заявки изменён на \"" + statusText + "\"");
                } else {
                    showAlert("Ошибка", "Не удалось изменить статус");
                }
            }
        });
    }

    @FXML
    private void handleDelete() {
        Request selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Удаление заявки");
            alert.setHeaderText("Удалить заявку?");
            alert.setContentText("Описание: " + selected.getDescription());
            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    requestDAO.delete(selected.getId());
                    handleUpdate();
                    showAlert("Успех", "Заявка удалена");
                }
            });
        } else {
            showAlert("Внимание", "Выберите заявку для удаления");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<Request, Void>() {
            private final HBox buttonBox = new HBox(5);  // Контейнер для кнопок
            private final Button assignBtn = new Button("🔧");
            private final Button completeBtn = new Button("✅");
            private final Button reopenBtn = new Button("🔙");
            {
                setupButton(assignBtn, "#f39c12", "Назначить исполнителя");
                setupButton(completeBtn, "#27ae60", "Завершить заявку");
                setupButton(reopenBtn, "#e74c3c", "Вернуть в новые");
                assignBtn.setOnAction(e -> {
                    Request request = getTableView().getItems().get(getIndex());
                    showAssignDialog(request);
                });
                completeBtn.setOnAction(e -> {
                    Request request = getTableView().getItems().get(getIndex());
                    changeStatus(request, "закрыта");
                });
                reopenBtn.setOnAction(e -> {
                    Request request = getTableView().getItems().get(getIndex());
                    changeStatus(request, "новая");
                });
            }

            private void setupButton(Button btn, String color, String tooltip) {
                btn.setStyle(
                        "-fx-background-color: " + color + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-cursor: hand; " +
                                "-fx-padding: 5px 8px; " +
                                "-fx-background-radius: 3px; " +
                                "-fx-font-size: 14px;"
                );
                btn.setTooltip(new Tooltip(tooltip));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Request request = getTableView().getItems().get(getIndex());
                    buttonBox.getChildren().clear();
                    String status = request.getCurrentStatus();
                    if ("новая".equals(status)) {
                        buttonBox.getChildren().add(assignBtn);
                    } else if ("в работе".equals(status)) {
                        buttonBox.getChildren().addAll(completeBtn, reopenBtn);
                    } else if ("закрыта".equals(status)) {
                        buttonBox.getChildren().add(reopenBtn);
                    }
                    setGraphic(buttonBox);
                }
            }
        });
        actionsColumn.setSortable(false);
        actionsColumn.setPrefWidth(180.0);
    }
}