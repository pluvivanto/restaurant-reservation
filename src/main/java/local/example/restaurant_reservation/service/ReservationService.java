package local.example.restaurant_reservation.service;

import local.example.restaurant_reservation.dto.ReservationRequestDto;
import local.example.restaurant_reservation.dto.ReservationResponseDto;
import local.example.restaurant_reservation.dto.ReservationStatusUpdateRequestDto;
import local.example.restaurant_reservation.model.Customer;
import local.example.restaurant_reservation.model.Reservation;
import local.example.restaurant_reservation.repository.CustomerRepository;
import local.example.restaurant_reservation.repository.ReservationRepository;
import local.example.restaurant_reservation.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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
        restaurantRepository.findById(requestDto.getRestaurantId());
        Customer customer;
        try {
            customer = customerRepository.findByEmail(requestDto.getCustomerEmail());
        } catch (IllegalArgumentException ex) {
            customer = customerRepository.add(requestDto.toCustomer());
        }

        Reservation reservation = requestDto.toReservation(customer.getId());
        Reservation saved = reservationRepository.add(reservation);

        return ReservationResponseDto.fromEntity(saved);
    }

    public ReservationResponseDto getReservation(Long reservationId) {
        return ReservationResponseDto.fromEntity(reservationRepository.findById(reservationId));
    }

    public ReservationResponseDto updateStatus(Long reservationId,
                                               ReservationStatusUpdateRequestDto statusRequest) {
        Reservation reservation = reservationRepository.findById(reservationId);
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
}
