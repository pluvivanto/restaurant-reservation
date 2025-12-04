package local.example.restaurant_reservation.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import local.example.restaurant_reservation.dto.AvailabilityResponseDto;
import local.example.restaurant_reservation.dto.AvailabilityResponseDto.SlotAvailability;
import local.example.restaurant_reservation.service.AvailabilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AvailabilityController.class)
class AvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvailabilityService availabilityService;

    @Test
    void getAvailability_ReturnsDto_WhenDateProvided() throws Exception {
        // given
        Long restaurantId = 1L;
        LocalDate date = LocalDate.now().plusDays(2);
        AvailabilityResponseDto dto = AvailabilityResponseDto.builder()
                .restaurantId(restaurantId)
                .date(date)
                .slots(List.of(
                        SlotAvailability.builder().startTime(LocalTime.of(10, 0)).availableTables(5).build(),
                        SlotAvailability.builder().startTime(LocalTime.of(11, 0)).availableTables(3).build()))
                .build();
        when(availabilityService.getAvailability(restaurantId, date)).thenReturn(dto);

        // when
        mockMvc
                .perform(get("/restaurants/{restaurantId}/availability", restaurantId).param("date",
                        date.toString()))
                // then
                .andExpect(status().isOk()).andExpect(jsonPath("$.restaurantId").value(restaurantId))
                .andExpect(jsonPath("$.slots[0].startTime").value("10:00:00"))
                .andExpect(jsonPath("$.slots[0].availableTables").value(5));

        verify(availabilityService).getAvailability(restaurantId, date);
    }
}
