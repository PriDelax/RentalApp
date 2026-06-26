package rentalapp.Controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import rentalapp.DataAccessObject.*;
import rentalapp.Entity.Agreement;
import rentalapp.Entity.Tenant;
import rentalapp.util.Session;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TenantsManagementController {
    @FXML private TableView<Tenant> tenantsTable;
    @FXML private TableColumn<Tenant, String> fullNameColumn;
    @FXML private TableColumn<Tenant, Object> birthDateColumn;
    @FXML private TableColumn<Tenant, String> phoneNumberColumn;
    @FXML private TableColumn<Tenant, String> emailColumn;
    private final TenantDAO tenantDAO = new TenantDAO();
    private final AgreementDAO agreementDAO = new AgreementDAO();

    @FXML public void initialize() {
        tenantsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupColumns();
        loadTenants();
    }

    private void setupColumns() {
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    private void loadTenants() {
        int landlordId = Session.getCurrentSession().getUserId();
        List<Tenant> tenants = getTenantsByLandlordId(landlordId).collect(Collectors.toList());
        tenantsTable.setItems(FXCollections.observableArrayList(tenants));
    }

    private Stream<Tenant> getTenantsByLandlordId(int landlordId) {
        List<Agreement> agreements = agreementDAO.findByLandlordId(landlordId);
        Set<Integer> tenantIds = agreements.stream().map(Agreement::getTenantId).collect(Collectors.toSet());
        return tenantIds.stream().map(tenantDAO::findById).filter(t -> t != null);
    }

    @FXML
    private void handleRefresh() {
        loadTenants();
    }
}