package local.example.restaurant_reservation.controller;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

import local.example.restaurant_reservation.dto.AvailabilityResponseDto;
import local.example.restaurant_reservation.service.AvailabilityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/restaurants/{restaurantId}/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public AvailabilityResponseDto getAvailability(@PathVariable Long restaurantId,
                                                   @RequestParam @NotNull LocalDate date) {
        return availabilityService.getAvailability(restaurantId, date);
    }
}
