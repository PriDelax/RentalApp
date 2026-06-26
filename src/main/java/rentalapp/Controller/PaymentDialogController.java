package rentalapp.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import rentalapp.DataAccessObject.*;
import rentalapp.Entity.*;
import rentalapp.util.Session;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentDialogController {
    @FXML private ComboBox<Agreement> agreementCombo;
    @FXML private TextField amountField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Button saveButton;
    @Setter private Stage dialogStage;
    @Setter private PaymentDAO paymentDAO;
    @Setter private AgreementDAO agreementDAO;
    @Setter private RoomDAO roomDAO;
    @Setter private TenantDAO tenantDAO;
    @FXML public void initialize() {
        setupTypeCombo();
        setupStatusCombo();
        datePicker.setValue(LocalDate.now());
    }

    public void loadData() {
        loadAgreements();
    }

    private void loadAgreements() {
        int landlordId = Session.getCurrentSession().getUserId();
        List<Agreement> allAgreements = agreementDAO.getAll();
        List<Agreement> activeAgreements = allAgreements.stream().filter(agr -> agr.getLandlordId() == landlordId).filter(agr -> "в силе".equals(agr.getStatus())).toList();
        agreementCombo.getItems().addAll(activeAgreements);
        agreementCombo.setCellFactory(listView -> new ListCell<Agreement>() {
            @Override
            protected void updateItem(Agreement agreement, boolean empty) {
                super.updateItem(agreement, empty);
                if (empty || agreement == null) {
                    setText(null);
                } else {
                    Room room = roomDAO.findById(agreement.getRoomId());
                    Tenant tenant = tenantDAO.findById(agreement.getTenantId());
                    String roomAddress = room != null ? room.getAddress() : "Н/Д";
                    String tenantName = tenant != null ? tenant.getFullName() : "Н/Д";
                    setText("Договор #" + agreement.getId() + " | " + roomAddress + " | " + tenantName);
                }
            }
        });
        agreementCombo.setButtonCell(agreementCombo.getCellFactory().call(null));
    }

    private void setupTypeCombo() {
        typeCombo.getItems().addAll("аренда", "пени", "залог");
        typeCombo.setValue("аренда");
    }

    private void setupStatusCombo() {
        statusCombo.getItems().addAll("оплачено", "не оплачено", "частично");
        statusCombo.setValue("не оплачено");
    }
    @FXML private void handleCreate() {
        if (agreementCombo.getValue() == null) {
            showAlert("Ошибка", "Выберите договор");
            return;
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректную сумму (положительное число)");
            return;
        }
        if (datePicker.getValue() == null) {
            showAlert("Ошибка", "Выберите дату платежа");
            return;
        }
        Payment payment = new Payment();
        payment.setAgreementId(agreementCombo.getValue().getId());
        payment.setPaymentSum(amount);
        payment.setPaymentDate(datePicker.getValue());
        payment.setType(typeCombo.getValue());
        payment.setStatus(statusCombo.getValue());
        int newId = paymentDAO.add(payment);
        if (newId > 0) {
            dialogStage.close();
        } else {
            showAlert("Ошибка", "Не удалось создать платёж");
        }
    }
    @FXML private void handleCancel() {
        dialogStage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}