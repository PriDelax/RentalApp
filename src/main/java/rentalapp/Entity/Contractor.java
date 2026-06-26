package rentalapp.Entity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Contractor {
    private int id;
    private String fullName;
    private LocalDate birthDate;
    private String phoneNumber;
    private String speciality;
    private int experienceYears;
    private double rating;
    private String description;
    private LocalDateTime createdAt;
}