package rentalapp.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import rentalapp.DataAccessObject.*;
import rentalapp.Entity.*;
import rentalapp.util.Session;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TenantPaymentsController {
    @FXML private Label paidAmountLabel;
    @FXML private Label pendingAmountLabel;
    @FXML private Label partialAmountLabel;
    @FXML private Label totalAmountLabel;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<Payment> paymentsTable;
    @FXML private TableColumn<Payment, String> dateColumn;
    @FXML private TableColumn<Payment, String> roomColumn;
    @FXML private TableColumn<Payment, String> amountColumn;
    @FXML private TableColumn<Payment, String> typeColumn;
    @FXML private TableColumn<Payment, String> statusColumn;
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final AgreementDAO agreementDAO = new AgreementDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final ObservableList<Payment> paymentsList = FXCollections.observableArrayList();
    @FXML public void initialize() {
        setupColumns();
        loadPayments();
        setupFilters();
    }

    private void setupColumns() {
        dateColumn.setCellValueFactory(cellData -> {
            Payment payment = cellData.getValue();
            if (payment.getPaymentDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(payment.getPaymentDate().format(dateFormatter));
            }
            return new javafx.beans.property.SimpleStringProperty("—");
        });
        roomColumn.setCellValueFactory(cellData -> {
            Payment payment = cellData.getValue();
            Agreement agreement = agreementDAO.findById(payment.getAgreementId());
            if (agreement != null) {
                Room room = roomDAO.findById(agreement.getRoomId());
                return new javafx.beans.property.SimpleStringProperty(room != null ? room.getAddress() : "—");
            }
            return new javafx.beans.property.SimpleStringProperty("—");
        });
        amountColumn.setCellValueFactory(cellData -> {
            Payment payment = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(payment.getPaymentSum().intValue() + " ₽");
        });
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
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
                        case "оплачено" -> "#27ae60";
                        case "не оплачено" -> "#e74c3c";
                        case "частично" -> "#f39c12";
                        default -> "#7f8c8d";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });
        paymentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadPayments() {
        int tenantId = Session.getCurrentSession().getUserId();
        List<Agreement> agreements = agreementDAO.getAll().stream().filter(agr -> agr.getTenantId() == tenantId).toList();
        List<Integer> agreementIds = agreements.stream().map(Agreement::getId).toList();
        List<Payment> tenantPayments = paymentDAO.getAll().stream().filter(payment -> agreementIds.contains(payment.getAgreementId())).sorted((p1, p2) -> p2.getPaymentDate().compareTo(p1.getPaymentDate())).collect(Collectors.toList());
        paymentsList.setAll(tenantPayments);
        updateStatistics();
        applyFilters();
    }

    private void updateStatistics() {
        int paid = paymentsList.stream().filter(p -> "оплачено".equals(p.getStatus())).mapToInt(p -> p.getPaymentSum().intValue()).sum();
        int pending = paymentsList.stream().filter(p -> "не оплачено".equals(p.getStatus())).mapToInt(p -> p.getPaymentSum().intValue()).sum();
        int partial = paymentsList.stream().filter(p -> "частично".equals(p.getStatus())).mapToInt(p -> p.getPaymentSum().intValue()).sum();
        int total = paid + pending + partial;
        paidAmountLabel.setText(formatMoney(paid));
        pendingAmountLabel.setText(formatMoney(pending));
        partialAmountLabel.setText(formatMoney(partial));
        totalAmountLabel.setText(formatMoney(total));
    }

    private void setupFilters() {
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        String selectedStatus = statusFilterCombo.getValue();
        if (selectedStatus == null || "Все".equals(selectedStatus)) {
            paymentsTable.setItems(paymentsList);
        } else {
            ObservableList<Payment> filtered = paymentsList.stream().filter(p -> selectedStatus.equals(p.getStatus())).collect(FXCollections::observableArrayList, ObservableList::add, ObservableList::addAll);
            paymentsTable.setItems(filtered);
        }
    }
    @FXML private void handleRefresh() {
        statusFilterCombo.setValue("Все");
        loadPayments();
    }

    private String formatMoney(int amount) {
        return String.format("%,d ₽", amount).replace(',', ' ');
    }
}