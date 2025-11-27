package local.example.restaurant_reservation.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import local.example.restaurant_reservation.dto.AvailabilityResponseDto;
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
        AvailabilityResponseDto dto = new AvailabilityResponseDto();
        dto.setRestaurantId(restaurantId);
        dto.setDate(date);
        dto.setAvailableTables(5);
        when(availabilityService.getAvailability(restaurantId, date)).thenReturn(dto);

        // when
        mockMvc
                .perform(get("/restaurants/{restaurantId}/availability", restaurantId).param("date",
                        date.toString()))
                // then
                .andExpect(status().isOk()).andExpect(jsonPath("$.restaurantId").value(restaurantId))
                .andExpect(jsonPath("$.availableTables").value(5));

        verify(availabilityService).getAvailability(restaurantId, date);
    }
}
