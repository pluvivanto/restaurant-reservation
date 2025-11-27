package local.example.restaurant_reservation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
import local.example.restaurant_reservation.dto.RestaurantRequestDto;
import local.example.restaurant_reservation.dto.RestaurantResponseDto;
import local.example.restaurant_reservation.service.RestaurantService;
import net.datafaker.Faker;

@WebMvcTest(RestaurantController.class)
class RestaurantControllerTest {

    private static final Faker FAKER = new Faker(new Random(2));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RestaurantService restaurantService;

    private RestaurantResponseDto existing;

    @BeforeEach
    void setUp() {
        existing = new RestaurantResponseDto();
        existing.setId(1L);
        existing.setName(FAKER.company().name());
        existing.setAddress(FAKER.address().streetAddress());
        existing.setPhone(FAKER.phoneNumber().cellPhone());
        existing.setOpenTime(LocalTime.of(9, 0));
        existing.setCloseTime(LocalTime.of(22, 0));
        existing.setTotalTables(FAKER.number().numberBetween(5, 25));
        existing.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(FAKER.number().numberBetween(1, 30)));
    }

    @Test
    void listRestaurants_ReturnsPagedDtos_WhenCalledWithPageAndSize() throws Exception {
        // given
        when(restaurantService.listRestaurants(1, 5)).thenReturn(List.of(existing));

        // when
        mockMvc.perform(get("/restaurants").param("page", "1").param("size", "5"))
                // then
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(existing.getId()))
                .andExpect(jsonPath("$[0].name").value(existing.getName()))
                .andExpect(jsonPath("$[0].totalTables").value(existing.getTotalTables()));

        verify(restaurantService).listRestaurants(1, 5);
    }

    @Test
    void getRestaurant_ReturnsDto_WhenFound() throws Exception {
        // given
        when(restaurantService.getRestaurant(existing.getId())).thenReturn(existing);

        // when
        mockMvc.perform(get("/restaurants/{id}", existing.getId()))
                // then
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(existing.getId()))
                .andExpect(jsonPath("$.name").value(existing.getName()))
                .andExpect(jsonPath("$.openTime").value("09:00:00"));
    }

    @Test
    void getRestaurant_Returns404_WhenMissing() throws Exception {
        // given
        when(restaurantService.getRestaurant(42L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "missing"));

        // when
        mockMvc.perform(get("/restaurants/{id}", 42L))
                // then
                .andExpect(status().isNotFound());
    }

    @Test
    void createRestaurant_Returns400_WhenNameMissing() throws Exception {
        // given
        RestaurantRequestDto requestDto = new RestaurantRequestDto();
        requestDto.setAddress(FAKER.address().streetAddress());
        requestDto.setPhone(FAKER.phoneNumber().cellPhone());
        requestDto.setOpenTime(LocalTime.of(9, 0));
        requestDto.setCloseTime(LocalTime.of(22, 0));
        requestDto.setTotalTables(FAKER.number().numberBetween(5, 25));
        String requestBody = Objects.requireNonNull(objectMapper.writeValueAsString(requestDto));

        // when
        mockMvc
                .perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRestaurant_ForwardsRequest_WhenValid() throws Exception {
        // given
        RestaurantResponseDto updated = new RestaurantResponseDto();
        updated.setId(existing.getId());
        updated.setName(FAKER.company().name() + " Updated");
        updated.setAddress(FAKER.address().fullAddress());
        updated.setPhone(existing.getPhone());
        updated.setOpenTime(existing.getOpenTime());
        updated.setCloseTime(existing.getCloseTime());
        updated.setTotalTables(FAKER.number().numberBetween(10, 30));
        when(restaurantService.updateRestaurant(eq(existing.getId()), any(RestaurantRequestDto.class)))
                .thenReturn(updated);

        RestaurantRequestDto requestDto = new RestaurantRequestDto();
        requestDto.setName(updated.getName());
        requestDto.setAddress(updated.getAddress());
        requestDto.setPhone(FAKER.phoneNumber().cellPhone());
        requestDto.setOpenTime(LocalTime.of(8, 0));
        requestDto.setCloseTime(LocalTime.of(23, 0));
        requestDto.setTotalTables(updated.getTotalTables());
        String requestJson = Objects.requireNonNull(objectMapper.writeValueAsString(requestDto));

        // when
        mockMvc
                .perform(put("/restaurants/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE).content(requestJson))
                // then
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value(updated.getName()))
                .andExpect(jsonPath("$.totalTables").value(updated.getTotalTables()));

        ArgumentCaptor<RestaurantRequestDto> captor =
                ArgumentCaptor.forClass(RestaurantRequestDto.class);
        verify(restaurantService).updateRestaurant(eq(existing.getId()), captor.capture());

        RestaurantRequestDto captured = captor.getValue();
        assertThat(captured.getName()).isEqualTo(requestDto.getName());
        assertThat(captured.getAddress()).isEqualTo(requestDto.getAddress());
        assertThat(captured.getOpenTime()).isEqualTo(requestDto.getOpenTime());
        assertThat(captured.getCloseTime()).isEqualTo(requestDto.getCloseTime());
        assertThat(captured.getTotalTables()).isEqualTo(requestDto.getTotalTables());
    }
}
