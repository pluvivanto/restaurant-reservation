package local.example.restaurant_reservation.model;

import java.time.LocalTime;
import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    private Long id;

    private String name;

    private String address;

    private String phone;

    private LocalTime openTime;

    private LocalTime closeTime;

    private Integer totalTables;

    private OffsetDateTime createdAt;

}
