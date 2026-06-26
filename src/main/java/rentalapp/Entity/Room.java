package rentalapp.Entity;

import lombok.*;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Room {
    private int id;
    private String address;
    private String description;
    private String type;
    private int roomsNumber;
    private double square;
    private String status;
    private int landlordId;
    private LocalDateTime createdAt;
}