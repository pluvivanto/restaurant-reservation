package local.example.restaurant_reservation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

import local.example.restaurant_reservation.model.Customer;
import local.example.restaurant_reservation.model.Reservation;
import local.example.restaurant_reservation.model.ReservationStatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationRequestDto {

    @NotNull
    private Long restaurantId;

    @NotBlank
    private String customerName;

    @NotBlank
    private String customerPhone;

    @Email
    private String customerEmail;

    @NotNull
    @Min(1)
    private Integer tableCount;

    @NotNull
    private OffsetDateTime startsAt;

    public Reservation toReservation(Long customerId) {
        return Reservation.builder()
                .restaurantId(restaurantId)
                .customerId(customerId)
                .tableCount(tableCount)
                .startsAt(startsAt)
                .status(ReservationStatusEnum.PENDING)
                .build();
    }

    public Customer toCustomer() {
        return Customer.builder()
                .name(customerName)
                .phone(customerPhone)
                .email(customerEmail)
                .build();
    }
}
