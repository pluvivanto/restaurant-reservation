package local.example.restaurant_reservation.dto;

import jakarta.validation.constraints.NotNull;
import local.example.restaurant_reservation.model.ReservationStatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationStatusUpdateRequestDto {

    @NotNull
    private ReservationStatusEnum status;
}
