package local.example.restaurant_reservation.model;

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
public class Reservation {

    private Long id;

    private Long restaurantId;

    private Long customerId;

    private Integer tableCount;

    private OffsetDateTime startsAt; // 1-hour slot start

    private ReservationStatusEnum status;

    private OffsetDateTime createdAt;
}
