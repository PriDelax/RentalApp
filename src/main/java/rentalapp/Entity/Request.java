package rentalapp.Entity;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Request {
    private int id;
    private int roomId;
    private int contractorId;
    private int tenantId;
    private String description;
    private String category;
    private String priority;
    private String currentStatus;
    private LocalDateTime createdAt;
}