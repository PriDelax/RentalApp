package rentalapp.Entity;

import lombok.*;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Agreement {
    private int id;
    private int tenantId;
    private int roomId;
    private int landlordId;
    private LocalDate effectiveDate;
    private LocalDate determinationDate;
    private Integer arendaSum;
    private String paymentSchedule;
    private String status;
}