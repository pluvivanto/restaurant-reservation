package local.example.restaurant_reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import net.datafaker.Faker;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final Faker FAKER = new Faker(new Random(3));

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Restaurant restaurant;
    private Reservation reservation;
    private Customer customer;

    @BeforeEach
    void setUp() {
        restaurant = Restaurant.builder().id(1L).name(FAKER.company().name()).totalTables(10).build();

        customer = Customer.builder().id(2L).name(FAKER.name().fullName())
                .email(FAKER.internet().emailAddress()).phone(FAKER.phoneNumber().cellPhone()).build();

        reservation = Reservation.builder().id(3L).restaurantId(restaurant.getId())
                .customerId(customer.getId()).tableCount(2)
                .startsAt(OffsetDateTime.now(ZoneOffset.UTC).plusDays(2)
                        .truncatedTo(ChronoUnit.SECONDS))
                .status(ReservationStatusEnum.PENDING)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1)).build();
    }

    @Test
    void createReservation_CreatesCustomerAndReservation_WhenValid() {
        // given
        ReservationRequestDto requestDto = new ReservationRequestDto();
        requestDto.setRestaurantId(restaurant.getId());
        requestDto.setCustomerName(FAKER.name().fullName());
        requestDto.setCustomerPhone(FAKER.phoneNumber().cellPhone());
        requestDto.setCustomerEmail(FAKER.internet().emailAddress());
        requestDto.setTableCount(2);
        requestDto.setStartsAt(reservation.getStartsAt());

        when(restaurantRepository.findById(restaurant.getId())).thenReturn(restaurant);
        when(customerRepository.findByEmail(requestDto.getCustomerEmail()))
                .thenReturn(Optional.empty());
        when(customerRepository.add(any(Customer.class))).thenReturn(customer);
        when(reservationRepository.add(any(Reservation.class))).thenReturn(reservation);

        // when
        ReservationResponseDto response = reservationService.createReservation(requestDto);

        // then
        assertThat(response.getId()).isEqualTo(reservation.getId());
        assertThat(response.getCustomerId()).isEqualTo(customer.getId());
        assertThat(response.getRestaurantId()).isEqualTo(restaurant.getId());
        verify(customerRepository).add(any(Customer.class));
        verify(reservationRepository).add(any(Reservation.class));
    }

    @Test
    void cancelReservation_SetsStatusCancelled_WhenCalled() {
        // given
        when(reservationRepository.findById(reservation.getId())).thenReturn(reservation);
        Reservation cancelled = reservation.toBuilder().status(ReservationStatusEnum.CANCELLED).build();
        when(reservationRepository.update(any(Reservation.class))).thenReturn(cancelled);

        // when
        ReservationResponseDto response = reservationService.cancelReservation(reservation.getId());

        // then
        assertThat(response.getStatus()).isEqualTo(ReservationStatusEnum.CANCELLED);
        verify(reservationRepository).update(any(Reservation.class));
    }

    @Test
    void updateStatus_SavesStatus_WhenRequestValid() {
        // given
        when(reservationRepository.findById(reservation.getId())).thenReturn(reservation);
        ReservationStatusUpdateRequestDto statusUpdate = new ReservationStatusUpdateRequestDto();
        statusUpdate.setStatus(ReservationStatusEnum.CONFIRMED);
        Reservation confirmed = reservation.toBuilder().status(ReservationStatusEnum.CONFIRMED).build();
        when(reservationRepository.update(any(Reservation.class))).thenReturn(confirmed);

        // when
        ReservationResponseDto response =
                reservationService.updateStatus(reservation.getId(), statusUpdate);

        // then
        assertThat(response.getStatus()).isEqualTo(ReservationStatusEnum.CONFIRMED);
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).update(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ReservationStatusEnum.CONFIRMED);
    }

    @Test
    void listReservations_FiltersByDate_WhenProvided() {
        // given
        LocalDate date = reservation.getStartsAt().toLocalDate();
        when(reservationRepository.findByRestaurantAndDate(restaurant.getId(), date))
                .thenReturn(List.of(reservation));

        // when
        List<ReservationResponseDto> result =
                reservationService.listReservations(restaurant.getId(), date);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(reservation.getId());
        verify(reservationRepository).findByRestaurantAndDate(restaurant.getId(), date);
    }

    @Test
    void listReservations_ReturnsAll_WhenDateMissing() {
        // given
        when(reservationRepository.findByRestaurant(restaurant.getId()))
                .thenReturn(List.of(reservation));

        // when
        List<ReservationResponseDto> result =
                reservationService.listReservations(restaurant.getId(), null);

        // then
        assertThat(result).hasSize(1);
        verify(reservationRepository).findByRestaurant(restaurant.getId());
    }
}
