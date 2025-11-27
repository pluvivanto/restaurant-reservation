package local.example.restaurant_reservation.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvailabilityResponseDto {

    private Long restaurantId;

    private LocalDate date;

    private Integer availableTables;
}
