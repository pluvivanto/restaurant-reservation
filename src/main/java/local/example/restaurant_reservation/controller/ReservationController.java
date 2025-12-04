package local.example.restaurant_reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.util.List;

import local.example.restaurant_reservation.dto.ReservationRequestDto;
import local.example.restaurant_reservation.dto.ReservationResponseDto;
import local.example.restaurant_reservation.dto.ReservationStatusUpdateRequestDto;
import local.example.restaurant_reservation.service.ReservationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Validated
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ReservationResponseDto createReservation(
            @Valid @RequestBody ReservationRequestDto reservationRequestDto) {
        return reservationService.createReservation(reservationRequestDto);
    }

    @GetMapping("/reservations/{reservationId}")
    public ReservationResponseDto getReservation(@PathVariable Long reservationId) {
        return reservationService.getReservation(reservationId);
    }

    @PostMapping("/reservations/{reservationId}/status")
    public ReservationResponseDto updateReservationStatus(@PathVariable Long reservationId,
            @Valid @RequestBody ReservationStatusUpdateRequestDto statusRequestDto) {
        return reservationService.updateStatus(reservationId, statusRequestDto);
    }

    @GetMapping("/restaurants/{restaurantId}/reservations")
    public List<ReservationResponseDto> listReservations(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        return reservationService.listReservations(restaurantId, date, page, size);
    }
}
