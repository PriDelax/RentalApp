package rentalapp.Entity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Landlord {
    private int id;
    private String login;
    private String password;
    private String fullName;
    private LocalDate birthDate;
    private String phoneNumber;
    private String email;
    private String passportDetails;
    private String paymentDetails;
    private LocalDateTime createdAt;
}