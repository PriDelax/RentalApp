package rentalapp.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import rentalapp.DataAccessObject.AgreementDAO;
import rentalapp.Entity.Agreement;
import rentalapp.util.Session;
import java.time.*;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaymentCalendarController {
    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private VBox dayDetails;
    @FXML private ListView<String> paymentsList;
    private final AgreementDAO agreementDAO = new AgreementDAO();
    private LocalDate currentDate;
    private List<Agreement> activeAgreements;
    @FXML public void initialize() {
        currentDate = LocalDate.now();
        loadActiveAgreements();
        buildCalendar();
    }

    private void loadActiveAgreements() {
        int landlordId = Session.getCurrentSession().getUserId();
        List<Agreement> allAgreements = agreementDAO.getAll();
        activeAgreements = allAgreements.stream().filter(agr -> agr.getLandlordId() == landlordId).filter(agr -> "в силе".equals(agr.getStatus())).toList();
        System.out.println("📅 Загружено активных договоров: " + activeAgreements.size());
    }

    private void buildCalendar() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7);
            calendarGrid.getColumnConstraints().add(col);
        }
        updateMonthYearLabel();
        String[] daysOfWeek = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-alignment: CENTER;");
            calendarGrid.add(dayLabel, i, 0);
        }
        YearMonth yearMonth = YearMonth.of(currentDate.getYear(), currentDate.getMonth());
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() - 1;
        int row = 1;
        int col = dayOfWeek;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDay = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), day);
            VBox dayCell = createDayCell(currentDay, day);
            calendarGrid.add(dayCell, col, row);
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createDayCell(LocalDate date, int dayOfMonth) {
        VBox cell = new VBox(5);
        cell.setStyle("-fx-padding: 5px; -fx-border-color: #e0e0e0; -fx-border-width: 1px; " + "-fx-background-radius: 5px; -fx-alignment: TOP_CENTER;");
        cell.setPrefHeight(80);
        Label dayLabel = new Label(String.valueOf(dayOfMonth));
        dayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        List<PaymentInfo> payments = getPaymentsForDate(date);
        HBox indicators = new HBox(5);
        indicators.setAlignment(javafx.geometry.Pos.CENTER);
        for (PaymentInfo payment : payments) {
            Label indicator = new Label("●");
            indicator.setTooltip(new Tooltip(payment.roomAddress + "\n" + "Сумма: " + payment.amount + " ₽\n" + "Статус: " + payment.status));
            String color = switch (payment.status.toLowerCase()) {
                case "оплачено" -> "#27ae60";
                case "просрочено" -> "#e74c3c";
                default -> "#f39c12";
            };
            indicator.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 16px; -fx-cursor: hand;");
            indicator.setOnMouseClicked(e -> showDayDetails(date, payments));
            indicators.getChildren().add(indicator);
        }
        if (date.equals(LocalDate.now())) {
            cell.setStyle(cell.getStyle() + " -fx-background-color: #e3f2fd;");
        }
        cell.setOnMouseClicked(e -> showDayDetails(date, payments));
        cell.getChildren().addAll(dayLabel, indicators);
        return cell;
    }

    private List<PaymentInfo> getPaymentsForDate(LocalDate date) {
        List<PaymentInfo> payments = new ArrayList<>();
        for (Agreement agreement : activeAgreements) {
            if ((date.isEqual(agreement.getEffectiveDate()) || date.isAfter(agreement.getEffectiveDate())) &&
                    date.isBefore(agreement.getDeterminationDate())) {
                if (isPaymentDate(date, agreement)) {
                    PaymentInfo payment = new PaymentInfo();
                    payment.roomAddress = agreement.getRoomId() + "";
                    payment.amount = agreement.getArendaSum();
                    payment.status = getPaymentStatus(date, agreement);
                    payment.agreement = agreement;
                    payments.add(payment);
                }
            }
        }
        return payments;
    }

    private boolean isPaymentDate(LocalDate date, Agreement agreement) {
        LocalDate startDate = agreement.getEffectiveDate();
        int dayOfMonth = startDate.getDayOfMonth();
        return date.getDayOfMonth() == dayOfMonth;
    }

    private String getPaymentStatus(LocalDate date, Agreement agreement) {
        if (date.isBefore(LocalDate.now())) {
            return "Просрочено";
        } else if (date.isEqual(LocalDate.now())) {
            return "Ожидается сегодня";
        } else {
            return "Ожидается";
        }
    }

    private void showDayDetails(LocalDate date, List<PaymentInfo> payments) {
        if (payments.isEmpty()) {
            dayDetails.setVisible(false);
            return;
        }
        dayDetails.setVisible(true);
        paymentsList.getItems().clear();
        String dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy", new java.util.Locale("ru")));
        for (PaymentInfo payment : payments) {
            String item = String.format("%s\n💰 %d ₽\n📊 %s",
                    payment.roomAddress,
                    payment.amount,
                    payment.status);
            paymentsList.getItems().add(item);
        }
    }

    private void updateMonthYearLabel() {
        String monthName = currentDate.getMonth().getDisplayName(TextStyle.FULL, new Locale("ru"));
        monthYearLabel.setText(monthName.substring(0, 1).toUpperCase() + monthName.substring(1) + " " + currentDate.getYear());
    }
    @FXML
    private void previousMonth() {
        currentDate = currentDate.minusMonths(1);
        buildCalendar();
    }
    @FXML
    private void nextMonth() {
        currentDate = currentDate.plusMonths(1);
        buildCalendar();
    }
    @FXML
    private void goToToday() {
        currentDate = LocalDate.now();
        buildCalendar();
    }

    private static class PaymentInfo {
        String roomAddress;
        int amount;
        String status;
        Agreement agreement;
    }
}