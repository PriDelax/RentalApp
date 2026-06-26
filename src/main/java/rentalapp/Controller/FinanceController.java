package rentalapp.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import rentalapp.DataAccessObject.*;
import rentalapp.Entity.*;
import rentalapp.util.Session;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FinanceController {
    @FXML private Label receivedAmountLabel;
    @FXML private Label pendingAmountLabel;
    @FXML private Label overdueAmountLabel;
    @FXML private Label totalAmountLabel;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<PaymentRecord> paymentsTable;
    @FXML private TableColumn<PaymentRecord, LocalDate> paymentDateColumn;
    @FXML private TableColumn<PaymentRecord, String> agreementColumn;
    @FXML private TableColumn<PaymentRecord, String> roomColumn;
    @FXML private TableColumn<PaymentRecord, String> tenantColumn;
    @FXML private TableColumn<PaymentRecord, Integer> amountColumn;
    @FXML private TableColumn<PaymentRecord, String> statusColumn;
    @FXML private TableColumn<PaymentRecord, LocalDate> paidDateColumn;
    @FXML private LineChart<String, Number> incomeLineChart;
    private final AgreementDAO agreementDAO = new AgreementDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final TenantDAO tenantDAO = new TenantDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final ObservableList<PaymentRecord> paymentsList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private String currentStatusFilter = "все";
    private LocalDate currentFromDate;
    private LocalDate currentToDate;
    @FXML public void initialize() {
        setupColumns();
        loadPaymentsWithFilters();
        updateStatistics();
        updateIncomeChart();
    }

    private void setupColumns() {
        paymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        paymentDateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.format(dateFormatter));
            }
        });
        agreementColumn.setCellValueFactory(new PropertyValueFactory<>("agreementInfo"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomAddress"));
        tenantColumn.setCellValueFactory(new PropertyValueFactory<>("tenantName"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : String.format("%,d ₽", amount).replace(',', ' '));
            }
        });
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                } else {
                    String statusLower = status.toLowerCase();
                    setText(status);
                    String color = switch (statusLower) {
                        case "оплачено" -> "#27ae60";
                        case "не оплачено" -> "#e74c3c";
                        case "частично" -> "#f39c12";
                        default -> "#7f8c8d";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });
        paidDateColumn.setCellValueFactory(new PropertyValueFactory<>("paidDate"));
        paidDateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.format(dateFormatter));
            }
        });
        paymentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void updateStatistics() {
        int received = paymentsList.stream().filter(p -> "оплачено".equals(p.status)).mapToInt(p -> p.amount).sum();
        int pending = paymentsList.stream().filter(p -> "не оплачено".equals(p.status)).mapToInt(p -> p.amount).sum();
        int partially = paymentsList.stream().filter(p -> "частично".equals(p.status)).mapToInt(p -> p.amount).sum();
        int total = received + pending + partially;
        receivedAmountLabel.setText(formatMoney(received));
        pendingAmountLabel.setText(formatMoney(pending));
        overdueAmountLabel.setText(formatMoney(partially));
        totalAmountLabel.setText(formatMoney(total));
    }

    private void updateIncomeChart() {
        Map<String, Integer> incomeByMonth = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.of(now.getYear(), now.getMonth()).minusMonths(i);
            String monthKey = ym.getMonth().getDisplayName(
                    java.time.format.TextStyle.SHORT, new Locale("ru")) + " " + ym.getYear();
            incomeByMonth.put(monthKey, 0);
        }
        for (PaymentRecord payment : paymentsList) {
            if ("оплачено".equals(payment.status) && payment.paidDate != null) {
                String monthKey = payment.paidDate.getMonth().getDisplayName(
                        java.time.format.TextStyle.SHORT, new Locale("ru")) + " " + payment.paidDate.getYear();
                incomeByMonth.merge(monthKey, payment.amount, Integer::sum);
            }
        }
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Доход");
        for (Map.Entry<String, Integer> entry : incomeByMonth.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        incomeLineChart.getData().clear();
        incomeLineChart.getData().add(series);
        incomeLineChart.setLegendVisible(false);
    }
    @FXML private void handleMarkPaid() {
        openPaymentDialog();
    }

    private void openPaymentDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/payment_dialog.fxml"));
            Parent root = loader.load();
            PaymentDialogController controller = loader.getController();
            Stage dialogStage = new Stage();
            controller.setDialogStage(dialogStage);
            controller.setPaymentDAO(paymentDAO);
            controller.setAgreementDAO(agreementDAO);
            controller.setRoomDAO(roomDAO);
            controller.setTenantDAO(tenantDAO);
            controller.loadData();
            dialogStage.setTitle("💰 Новый платёж");
            dialogStage.setScene(new Scene(root));
            dialogStage.getScene().getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(paymentsTable.getScene().getWindow());
            dialogStage.showAndWait();
            handleRefresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML private void applyFilters() {
        String selectedStatus = statusFilterCombo.getValue();
        currentStatusFilter = selectedStatus != null ? selectedStatus.toLowerCase() : "все";
        currentFromDate = fromDatePicker.getValue();
        currentToDate = toDatePicker.getValue();
        loadPaymentsWithFilters();
    }

    private void loadPaymentsWithFilters() {
        int landlordId = Session.getCurrentSession().getUserId();
        List<Room> landlordRooms = roomDAO.findByLandlordId(landlordId);
        List<Integer> roomIds = landlordRooms.stream().map(Room::getId).toList();
        List<Agreement> allAgreements = agreementDAO.getAll();
        List<Agreement> landlordAgreements = allAgreements.stream().filter(agr -> roomIds.contains(agr.getRoomId())).toList();
        List<Payment> allPayments = paymentDAO.getAll();
        paymentsList.clear();
        for (Payment payment : allPayments) {
            boolean isLandlordPayment = landlordAgreements.stream().anyMatch(agr -> agr.getId() == payment.getAgreementId());
            if (!isLandlordPayment) continue;
            if (!"все".equals(currentStatusFilter) && !payment.getStatus().toLowerCase().equals(currentStatusFilter)) {
                continue;
            }
            if (currentFromDate != null && payment.getPaymentDate().isBefore(currentFromDate)) {
                continue;
            }
            if (currentToDate != null && payment.getPaymentDate().isAfter(currentToDate)) {
                continue;
            }
            PaymentRecord record = new PaymentRecord();
            record.paymentDate = payment.getPaymentDate();
            record.agreementId = payment.getAgreementId();
            record.amount = payment.getPaymentSum().intValue();
            record.status = payment.getStatus();
            record.paidDate = "оплачено".equalsIgnoreCase(payment.getStatus()) ? LocalDate.now() : null;
            Agreement agreement = agreementDAO.findById(payment.getAgreementId());
            if (agreement != null) {
                Room room = roomDAO.findById(agreement.getRoomId());
                Tenant tenant = tenantDAO.findById(agreement.getTenantId());
                record.roomAddress = room != null ? room.getAddress() : "Н/Д";
                record.tenantName = tenant != null ? tenant.getFullName() : "Н/Д";
                record.agreementInfo = "Договор #" + agreement.getId();
            }
            paymentsList.add(record);
        }
        paymentsTable.setItems(paymentsList);
        updateStatistics();
    }
    @FXML
    private void resetFilters() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        statusFilterCombo.setValue("все");
        currentStatusFilter = "все";
        currentFromDate = null;
        currentToDate = null;
        loadPaymentsWithFilters();
    }
    @FXML
    private void handleRefresh() {
        resetFilters();
        updateIncomeChart();
    }

    private String formatMoney(int amount) {
        return String.format("%,d ₽", amount).replace(',', ' ');
    }
    @Setter
    @Getter
    public static class PaymentRecord {
        public LocalDate paymentDate;
        public int agreementId;
        public int roomId;
        public int tenantId;
        public int amount;
        public String status;
        public LocalDate paidDate;
        public String roomAddress;
        public String tenantName;
        public String agreementInfo;
    }
}