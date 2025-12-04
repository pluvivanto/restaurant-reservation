package local.example.restaurant_reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import local.example.restaurant_reservation.dto.AvailabilityResponseDto;
import local.example.restaurant_reservation.dto.AvailabilityResponseDto.SlotAvailability;
import local.example.restaurant_reservation.model.Reservation;
import local.example.restaurant_reservation.model.ReservationStatusEnum;
import local.example.restaurant_reservation.model.Restaurant;
import local.example.restaurant_reservation.repository.ReservationRepository;
import local.example.restaurant_reservation.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

    private final RestaurantRepository restaurantRepository;
    private final ReservationRepository reservationRepository;

    public AvailabilityService(RestaurantRepository restaurantRepository,
            ReservationRepository reservationRepository) {
        this.restaurantRepository = restaurantRepository;
        this.reservationRepository = reservationRepository;
    }

    public AvailabilityResponseDto getAvailability(Long restaurantId, LocalDate date) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId);
        List<Reservation> reservations = reservationRepository.findByRestaurantAndDate(restaurantId, date);

        // Group reserved tables by hour (excluding cancelled)
        Map<LocalTime, Integer> reservedByHour = reservations.stream()
                .filter(r -> r.getStatus() != ReservationStatusEnum.CANCELLED)
                .collect(Collectors.groupingBy(
                        r -> r.getStartsAt().toLocalTime().withMinute(0).withSecond(0).withNano(0),
                        Collectors.summingInt(Reservation::getTableCount)));

        // Generate slots for each hour from open to close
        List<SlotAvailability> slots = new ArrayList<>();
        LocalTime current = restaurant.getOpenTime();
        LocalTime closeTime = restaurant.getCloseTime();

        while (current.isBefore(closeTime)) {
            int reserved = reservedByHour.getOrDefault(current, 0);
            int available = Math.max(restaurant.getTotalTables() - reserved, 0);
            slots.add(SlotAvailability.builder()
                    .startTime(current)
                    .availableTables(available)
                    .build());
            current = current.plusHours(1);
        }

        return AvailabilityResponseDto.builder()
                .restaurantId(restaurantId)
                .date(date)
                .slots(slots)
                .build();
    }
}
