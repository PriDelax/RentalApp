package rentalapp.Entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Payment {
    private int id;
    private int agreementId;
    private BigDecimal paymentSum;
    private LocalDate paymentDate;
    private String type;
    private String status;
    private LocalDateTime createdAt;
}