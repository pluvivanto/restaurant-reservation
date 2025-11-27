package local.example.restaurant_reservation.dto;

import java.time.OffsetDateTime;

import local.example.restaurant_reservation.model.Reservation;
import local.example.restaurant_reservation.model.ReservationStatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationResponseDto {

    private Long id;

    private Long restaurantId;

    private Long customerId;

    private Integer tableCount;

    private OffsetDateTime startsAt;

    private ReservationStatusEnum status;

    private OffsetDateTime createdAt;

    public static ReservationResponseDto fromEntity(Reservation reservation) {
        if (reservation == null) {
            return null;
        }
        ReservationResponseDto dto = new ReservationResponseDto();
        dto.setId(reservation.getId());
        dto.setRestaurantId(reservation.getRestaurantId());
        dto.setCustomerId(reservation.getCustomerId());
        dto.setTableCount(reservation.getTableCount());
        dto.setStartsAt(reservation.getStartsAt());
        dto.setStatus(reservation.getStatus());
        dto.setCreatedAt(reservation.getCreatedAt());
        return dto;
    }
}
