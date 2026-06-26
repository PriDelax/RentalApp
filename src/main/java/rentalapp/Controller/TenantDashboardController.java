package rentalapp.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import rentalapp.DataAccessObject.*;
import rentalapp.Entity.*;
import rentalapp.util.Session;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TenantDashboardController {
    @FXML private Label activeAgreementLabel;
    @FXML private Label openRequestsLabel;
    @FXML private Label nextPaymentLabel;
    @FXML private TableView<Request> recentRequestsTable;
    @FXML private TableColumn<Request, String> dateColumn;
    @FXML private TableColumn<Request, String> descriptionColumn;
    @FXML private TableColumn<Request, String> statusColumn;
    private final AgreementDAO agreementDAO = new AgreementDAO();
    private final RequestDAO requestDAO = new RequestDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    @FXML public void initialize() {
        setupTable();
        loadStatistics();
        loadRecentRequests();
    }

    private void setupTable() {
        dateColumn.setCellValueFactory(cellData -> {
            Request req = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(req.getCreatedAt() != null ? req.getCreatedAt().format(dateFormatter) : "—");
        });
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("currentStatus"));
        statusColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
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
    }

    private void loadStatistics() {
        int tenantId = Session.getCurrentSession().getUserId();
        List<Agreement> activeAgreements = agreementDAO.getAll().stream().filter(agr -> agr.getTenantId() == tenantId).filter(agr -> "в силе".equals(agr.getStatus())).toList();
        activeAgreementLabel.setText(String.valueOf(activeAgreements.size()));
        List<Integer> tenantRoomIds = activeAgreements.stream().map(Agreement::getRoomId).toList();
        List<Request> openRequests = requestDAO.getAll().stream().filter(req -> tenantRoomIds.contains(req.getRoomId())).filter(req -> !"закрыта".equals(req.getCurrentStatus())).toList();
        openRequestsLabel.setText(String.valueOf(openRequests.size()));
        LocalDate nextPayment = null;
        for (Agreement agr : activeAgreements) {
            LocalDate today = LocalDate.now();
            LocalDate next = agr.getEffectiveDate();
            while (next.isBefore(today) || next.isEqual(today)) {
                next = next.plusMonths(1);
            }
            if (nextPayment == null || next.isBefore(nextPayment)) {
                nextPayment = next;
            }
        }
        if (nextPayment != null) {
            nextPaymentLabel.setText(nextPayment.format(dateFormatter));
        } else {
            nextPaymentLabel.setText("—");
        }
    }
    private void loadRecentRequests() {
        int tenantId = Session.getCurrentSession().getUserId();
        List<Agreement> activeAgreements = agreementDAO.getAll().stream().filter(agr -> agr.getTenantId() == tenantId).filter(agr -> "в силе".equals(agr.getStatus())).toList();
        List<Integer> tenantRoomIds = activeAgreements.stream().map(Agreement::getRoomId).toList();
        List<Request> recentRequests = requestDAO.getAll().stream().filter(req -> tenantRoomIds.contains(req.getRoomId())).sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt())).limit(5).collect(Collectors.toList());
        ObservableList<Request> requestsList = FXCollections.observableArrayList(recentRequests);
        recentRequestsTable.setItems(requestsList);
    }
}