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
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public ReservationResponseDto createReservation(ReservationRequestDto requestDto) {
        var restaurant = restaurantRepository.findByIdForUpdate(requestDto.getRestaurantId());
        Customer customer;
        try {
            customer = customerRepository.findByEmail(requestDto.getCustomerEmail());
        } catch (IllegalArgumentException ex) {
            customer = customerRepository.add(requestDto.toCustomer());
        }

        int reservedTables =
                reservationRepository.sumReservedTablesForSlot(restaurant.getId(),
                        requestDto.getStartsAt());
        if (reservedTables + requestDto.getTableCount() > restaurant.getTotalTables()) {
            throw new IllegalStateException("Not enough tables available for the requested slot");
        }

        Reservation reservation = requestDto.toReservation(customer.getId());
        Reservation saved = reservationRepository.add(reservation);

        return ReservationResponseDto.fromEntity(saved);
    }

    public ReservationResponseDto getReservation(Long reservationId) {
        return ReservationResponseDto.fromEntity(reservationRepository.findById(reservationId));
    }

    @Transactional
    public ReservationResponseDto updateStatus(Long reservationId,
                                               ReservationStatusUpdateRequestDto statusRequest) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId);
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
