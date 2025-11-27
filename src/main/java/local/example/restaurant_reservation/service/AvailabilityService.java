package local.example.restaurant_reservation.service;

import java.time.LocalDate;
import java.util.List;

import local.example.restaurant_reservation.dto.AvailabilityResponseDto;
import local.example.restaurant_reservation.model.Reservation;
import local.example.restaurant_reservation.model.ReservationStatusEnum;
import local.example.restaurant_reservation.model.Restaurant;
import local.example.restaurant_reservation.repository.ReservationRepository;
import local.example.restaurant_reservation.repository.RestaurantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        Restaurant restaurant = findRestaurant(restaurantId);
        List<Reservation> reservations =
                reservationRepository.findByRestaurantAndDate(restaurantId, date);

        int reservedTables =
                reservations.stream().filter(r -> r.getStatus() != ReservationStatusEnum.CANCELLED)
                        .mapToInt(Reservation::getTableCount).sum();

        int availableTables = Math.max(restaurant.getTotalTables() - reservedTables, 0);
        AvailabilityResponseDto dto = new AvailabilityResponseDto();
        dto.setRestaurantId(restaurantId);
        dto.setDate(date);
        dto.setAvailableTables(availableTables);
        return dto;
    }

    private Restaurant findRestaurant(Long restaurantId) {
        try {
            return restaurantRepository.findById(restaurantId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Restaurant with id %d not found".formatted(restaurantId), ex);
        }
    }
}
