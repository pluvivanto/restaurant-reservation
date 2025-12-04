package local.example.restaurant_reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import local.example.restaurant_reservation.dto.ReservationRequestDto;
import local.example.restaurant_reservation.dto.ReservationResponseDto;
import local.example.restaurant_reservation.dto.ReservationStatusUpdateRequestDto;
import local.example.restaurant_reservation.model.ReservationStatusEnum;
import local.example.restaurant_reservation.service.ReservationService;
import net.datafaker.Faker;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

        private static final Faker FAKER = new Faker(new Random(4));

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private ReservationService reservationService;

        private ReservationResponseDto reservation;
        private String customerName;
        private String customerPhone;
        private String customerEmail;
        private OffsetDateTime startsAt;

        @BeforeEach
        void setUp() {
                customerName = FAKER.name().fullName();
                customerPhone = FAKER.phoneNumber().cellPhone();
                customerEmail = FAKER.internet().emailAddress();
                startsAt = OffsetDateTime.now(ZoneOffset.UTC).plusDays(7);

                reservation = new ReservationResponseDto();
                reservation.setId(10L);
                reservation.setRestaurantId(1L);
                reservation.setCustomerId(2L);
                reservation.setTableCount(3);
                reservation.setStartsAt(startsAt);
                reservation.setStatus(ReservationStatusEnum.PENDING);
        }

        @Test
        void createReservation_ReturnsDto_WhenRequestValid() throws Exception {
                // given
                ReservationRequestDto request = new ReservationRequestDto();
                request.setRestaurantId(1L);
                request.setCustomerName(customerName);
                request.setCustomerPhone(customerPhone);
                request.setCustomerEmail(customerEmail);
                request.setTableCount(3);
                request.setStartsAt(reservation.getStartsAt());
                when(reservationService.createReservation(any(ReservationRequestDto.class)))
                                .thenReturn(reservation);
                String requestJson = java.util.Objects.requireNonNull(objectMapper.writeValueAsString(request));

                // when
                mockMvc
                                .perform(post("/reservations").contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(requestJson))
                                // then
                                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(reservation.getId()))
                                .andExpect(jsonPath("$.restaurantId").value(reservation.getRestaurantId()));
        }

        @Test
        void getReservation_ReturnsDto_WhenExists() throws Exception {
                // given
                when(reservationService.getReservation(reservation.getId())).thenReturn(reservation);

                // when
                mockMvc.perform(get("/reservations/{id}", reservation.getId()))
                                // then
                                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(reservation.getId()))
                                .andExpect(jsonPath("$.status").value(reservation.getStatus().name()));
        }

        @Test
        void updateStatus_ReturnsUpdatedDto_WhenStatusValid() throws Exception {
                // given
                ReservationStatusUpdateRequestDto statusRequest = new ReservationStatusUpdateRequestDto();
                statusRequest.setStatus(ReservationStatusEnum.CONFIRMED);
                ReservationResponseDto confirmed = new ReservationResponseDto();
                confirmed.setId(reservation.getId());
                confirmed.setRestaurantId(reservation.getRestaurantId());
                confirmed.setStatus(ReservationStatusEnum.CONFIRMED);
                when(reservationService.updateStatus(eq(reservation.getId()),
                                any(ReservationStatusUpdateRequestDto.class))).thenReturn(confirmed);
                String statusRequestJson = java.util.Objects
                                .requireNonNull(objectMapper.writeValueAsString(statusRequest));

                // when
                mockMvc
                                .perform(post("/reservations/{id}/status", reservation.getId())
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(statusRequestJson))
                                // then
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(ReservationStatusEnum.CONFIRMED.name()));
        }

        @Test
        void listReservations_ReturnsFilteredList_WhenDateProvided() throws Exception {
                // given
                LocalDate date = reservation.getStartsAt().toLocalDate();
                when(reservationService.listReservations(reservation.getRestaurantId(), date, 0, 20))
                                .thenReturn(List.of(reservation));

                // when
                mockMvc
                                .perform(get("/restaurants/{id}/reservations", reservation.getRestaurantId()).param(
                                                "date",
                                                date.toString()))
                                // then
                                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(reservation.getId()));

                verify(reservationService).listReservations(reservation.getRestaurantId(), date, 0, 20);
        }
}
