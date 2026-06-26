package rentalapp;

import rentalapp.DataAccessObject.*;
import rentalapp.Entity.*;
import rentalapp.util.PasswordHasher;
import rentalapp.util.Session;

public class AuthService {
    private final TenantDAO tenantDAO = new TenantDAO();
    private final LandlordDAO landlordDAO = new LandlordDAO();

    public boolean login(String login, String password) {
        Tenant tenant = tenantDAO.findByLogin(login);
        if (tenant != null && PasswordHasher.verify(password, tenant.getPassword())) {
            Session.setCurrentSession(new Session(tenant.getId(), tenant.getFullName(), "TENANT"));
            System.out.println("Вход выполнен (Съёмщик): " + tenant.getFullName());
            return true;
        }
        Landlord landlord = landlordDAO.findByLogin(login);
        if (landlord != null && PasswordHasher.verify(password, landlord.getPassword())) {
            Session.setCurrentSession(new Session(landlord.getId(), landlord.getFullName(), "LANDLORD"));
            System.out.println("Вход выполнен (Арендодатель): " + landlord.getFullName());
            return true;
        }
        System.err.println("Неверный логин или пароль");
        return false;
    }

    public int registerTenant(String login, String password, String fullName, String phone, String email, String passport) {
        if (tenantDAO.findByLogin(login) != null) {
            System.err.println("Логин уже занят");
            return -1;
        }
        Tenant tenant = new Tenant();
        tenant.setLogin(login);
        tenant.setPassword(PasswordHasher.hash(password));
        tenant.setFullName(fullName);
        tenant.setPhoneNumber(phone);
        tenant.setEmail(email);
        tenant.setPassportDetails(passport);
        int newId = tenantDAO.add(tenant);
        System.out.println("Зарегистрирован новый съёмщик (ID: " + newId + ")");
        return newId;
    }

    public int registerLandlord(String login, String password, String fullName, String phone, String email, String passport, String paymentDetails) {
        if (landlordDAO.findByLogin(login) != null) {
            System.err.println("Логин уже занят");
            return -1;
        }
        Landlord landlord = new Landlord();
        landlord.setLogin(login);
        landlord.setPassword(PasswordHasher.hash(password));
        landlord.setFullName(fullName);
        landlord.setPhoneNumber(phone);
        landlord.setEmail(email);
        landlord.setPassportDetails(passport);
        landlord.setPaymentDetails(paymentDetails);
        int newId = landlordDAO.add(landlord);
        System.out.println("Зарегистрирован новый арендодатель (ID: " + newId + ")");
        return newId;
    }

    public void logout() {
        Session.logout();
        System.out.println("Выход из системы выполнен");
    }

    public String getCurrentUserName() {
        Session session = Session.getCurrentSession();
        return session != null ? session.getUserName() : "Гость";
    }
}