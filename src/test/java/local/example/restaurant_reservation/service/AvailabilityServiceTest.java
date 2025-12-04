package local.example.restaurant_reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.datafaker.Faker;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;

import local.example.restaurant_reservation.dto.AvailabilityResponseDto;
import local.example.restaurant_reservation.model.Reservation;
import local.example.restaurant_reservation.model.ReservationStatusEnum;
import local.example.restaurant_reservation.model.Restaurant;
import local.example.restaurant_reservation.repository.ReservationRepository;
import local.example.restaurant_reservation.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

        private static final Faker FAKER = new Faker(new Random(6));

        @Mock
        private RestaurantRepository restaurantRepository;

        @Mock
        private ReservationRepository reservationRepository;

        @InjectMocks
        private AvailabilityService availabilityService;

        private Restaurant restaurant;
        private LocalDate date;
        private Reservation reserved;
        private Reservation cancelled;

        @BeforeEach
        void setUp() {
                restaurant = Restaurant.builder()
                                .id(1L)
                                .name(FAKER.company().name())
                                .totalTables(10)
                                .openTime(LocalTime.of(10, 0))
                                .closeTime(LocalTime.of(14, 0))
                                .build();
                date = LocalDate.now().plusDays(1);
                OffsetDateTime slotTime = date.atTime(10, 0).atOffset(ZoneOffset.UTC);
                reserved = Reservation.builder()
                                .restaurantId(restaurant.getId())
                                .tableCount(4)
                                .startsAt(slotTime)
                                .status(ReservationStatusEnum.CONFIRMED)
                                .build();
                cancelled = Reservation.builder()
                                .restaurantId(restaurant.getId())
                                .tableCount(3)
                                .startsAt(slotTime)
                                .status(ReservationStatusEnum.CANCELLED)
                                .build();
        }

        @Test
        void getAvailability_ComputesAvailableTables_WhenReservationsExist() {
                // given
                when(restaurantRepository.findById(restaurant.getId())).thenReturn(restaurant);
                when(reservationRepository.findByRestaurantAndDate(restaurant.getId(), date))
                                .thenReturn(List.of(reserved, cancelled));

                // when
                AvailabilityResponseDto response = availabilityService.getAvailability(restaurant.getId(), date);

                // then
                assertThat(response.getSlots()).hasSize(4); // 10:00, 11:00, 12:00, 13:00
                assertThat(response.getSlots().get(0).getAvailableTables()).isEqualTo(6); // 10 - 4 reserved
                assertThat(response.getSlots().get(1).getAvailableTables()).isEqualTo(10); // no reservations at 11:00
                assertThat(response.getRestaurantId()).isEqualTo(restaurant.getId());
                assertThat(response.getDate()).isEqualTo(date);
                verify(reservationRepository).findByRestaurantAndDate(restaurant.getId(), date);
        }
}
