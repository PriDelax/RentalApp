package rentalapp.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import rentalapp.DataAccessObject.*;
import rentalapp.Entity.*;
import rentalapp.util.Session;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TenantAgreementsController {
    @FXML private TableView<Agreement> agreementsTable;
    @FXML private TableColumn<Agreement, String> roomColumn;
    @FXML private TableColumn<Agreement, String> landlordColumn;
    @FXML private TableColumn<Agreement, String> startDateColumn;
    @FXML private TableColumn<Agreement, String> endDateColumn;
    @FXML private TableColumn<Agreement, String> rentColumn;
    @FXML private TableColumn<Agreement, String> scheduleColumn;
    @FXML private TableColumn<Agreement, String> statusColumn;
    private final AgreementDAO agreementDAO = new AgreementDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final LandlordDAO landlordDAO = new LandlordDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    @FXML public void initialize() {
        setupColumns();
        loadAgreements();
    }

    private void setupColumns() {
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
        startDateColumn.setCellValueFactory(cellData -> {
            Agreement agr = cellData.getValue();
            if (agr.getEffectiveDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(agr.getEffectiveDate().format(dateFormatter));
            }
            return new javafx.beans.property.SimpleStringProperty("—");
        });
        endDateColumn.setCellValueFactory(cellData -> {
            Agreement agr = cellData.getValue();
            if (agr.getDeterminationDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(agr.getDeterminationDate().format(dateFormatter));
            }
            return new javafx.beans.property.SimpleStringProperty("—");
        });
        rentColumn.setCellValueFactory(cellData -> {
            Agreement agr = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(agr.getArendaSum() + " ₽");
        });
        scheduleColumn.setCellValueFactory(new PropertyValueFactory<>("paymentSchedule"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
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
                        case "в силе" -> "#27ae60";
                        case "расторгнут" -> "#e74c3c";
                        default -> "#7f8c8d";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });
        agreementsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadAgreements() {
        int tenantId = Session.getCurrentSession().getUserId();
        List<Agreement> tenantAgreements = agreementDAO.getAll().stream().filter(agr -> agr.getTenantId() == tenantId).sorted((a1, a2) -> a2.getEffectiveDate().compareTo(a1.getEffectiveDate())).collect(Collectors.toList());
        ObservableList<Agreement> agreementsList = FXCollections.observableArrayList(tenantAgreements);
        agreementsTable.setItems(agreementsList);
    }
}