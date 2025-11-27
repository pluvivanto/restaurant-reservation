package local.example.restaurant_reservation.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import local.example.restaurant_reservation.dto.ReservationRequestDto;
import local.example.restaurant_reservation.dto.ReservationResponseDto;
import local.example.restaurant_reservation.dto.ReservationStatusUpdateRequestDto;
import local.example.restaurant_reservation.model.Customer;
import local.example.restaurant_reservation.model.Reservation;
import local.example.restaurant_reservation.model.ReservationStatusEnum;
import local.example.restaurant_reservation.model.Restaurant;
import local.example.restaurant_reservation.repository.CustomerRepository;
import local.example.restaurant_reservation.repository.ReservationRepository;
import local.example.restaurant_reservation.repository.RestaurantRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              CustomerRepository customerRepository, RestaurantRepository restaurantRepository) {
        this.reservationRepository = reservationRepository;
        this.customerRepository = customerRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public ReservationResponseDto createReservation(ReservationRequestDto requestDto) {
        // Validate restaurant exists before creating a reservation
        findRestaurant(requestDto.getRestaurantId());
        Customer customer = customerRepository.findByEmail(requestDto.getCustomerEmail())
                .orElseGet(() -> customerRepository.add(requestDto.toCustomer()));

        Reservation reservation = requestDto.toReservation(customer.getId());
        Reservation saved = reservationRepository.add(reservation);

        return ReservationResponseDto.fromEntity(saved);
    }

    public ReservationResponseDto getReservation(Long reservationId) {
        return ReservationResponseDto.fromEntity(findReservation(reservationId));
    }

    public ReservationResponseDto cancelReservation(Long reservationId) {
        Reservation reservation = findReservation(reservationId);
        Reservation cancelled = reservation.toBuilder()
                .status(ReservationStatusEnum.CANCELLED)
                .build();
        reservationRepository.update(cancelled);
        return ReservationResponseDto.fromEntity(cancelled);
    }

    public ReservationResponseDto updateStatus(Long reservationId,
                                               ReservationStatusUpdateRequestDto statusRequest) {
        Reservation reservation = findReservation(reservationId);
        Reservation updated = reservation.toBuilder()
                .status(statusRequest.getStatus())
                .build();
        reservationRepository.update(updated);
        return ReservationResponseDto.fromEntity(updated);
    }

    public List<ReservationResponseDto> listReservations(Long restaurantId, LocalDate date) {
        List<Reservation> reservations = date == null
                ? reservationRepository.findByRestaurant(restaurantId)
                : reservationRepository.findByRestaurantAndDate(restaurantId, date);
        return reservations.stream().map(ReservationResponseDto::fromEntity).toList();
    }

    private Restaurant findRestaurant(Long restaurantId) {
        try {
            return restaurantRepository.findById(restaurantId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Restaurant with id %d not found".formatted(restaurantId), ex);
        }
    }

    private Reservation findReservation(Long reservationId) {
        try {
            return reservationRepository.findById(reservationId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Reservation with id %d not found".formatted(reservationId), ex);
        }
    }
}
