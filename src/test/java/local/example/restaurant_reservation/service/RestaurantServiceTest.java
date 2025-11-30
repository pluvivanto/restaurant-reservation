package local.example.restaurant_reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import local.example.restaurant_reservation.dto.RestaurantRequestDto;
import local.example.restaurant_reservation.dto.RestaurantResponseDto;
import local.example.restaurant_reservation.model.Restaurant;
import local.example.restaurant_reservation.repository.RestaurantRepository;
import net.datafaker.Faker;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    private static final Faker FAKER = new Faker(new Random(1));

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant existing;

    @BeforeEach
    void setUp() {
        existing = Restaurant.builder().id(1L).name(FAKER.company().name())
                .address(FAKER.address().streetAddress()).phone(FAKER.phoneNumber().cellPhone())
                .openTime(LocalTime.of(9, 0)).closeTime(LocalTime.of(22, 0)).totalTables(10)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(7)).build();
    }

    @Test
    void listRestaurants_ReturnsPagedDtos_WhenPageRequested() {
        // given
        when(restaurantRepository.findAll(eq(1), eq(5))).thenReturn(List.of(existing));

        // when
        List<RestaurantResponseDto> result = restaurantService.listRestaurants(1, 5);

        // then
        assertThat(result).hasSize(1);
        RestaurantResponseDto dto = result.getFirst();
        assertThat(dto.getId()).isEqualTo(existing.getId());
        assertThat(dto.getName()).isEqualTo(existing.getName());
        verify(restaurantRepository).findAll(1, 5);
    }

    @Test
    void getRestaurant_ThrowsNotFound_WhenMissing() {
        // given
        when(restaurantRepository.findById(42L)).thenThrow(new EmptyResultDataAccessException(1));

        // when + then
        assertThatThrownBy(() -> restaurantService.getRestaurant(42L))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    void createRestaurant_ReturnsDto_WhenCreated() {
        // given
        Restaurant created = Restaurant.builder().id(2L).name("New Spot").openTime(LocalTime.of(10, 0))
                .closeTime(LocalTime.of(20, 0)).totalTables(5).build();
        when(restaurantRepository.add(any(Restaurant.class))).thenReturn(created);

        RestaurantRequestDto request = new RestaurantRequestDto();
        request.setName(existing.getName());
        request.setAddress(existing.getAddress());
        request.setPhone(existing.getPhone());
        request.setOpenTime(existing.getOpenTime());
        request.setCloseTime(existing.getCloseTime());
        request.setTotalTables(existing.getTotalTables());

        // when
        RestaurantResponseDto response = restaurantService.createRestaurant(request);

        // then
        assertThat(response.getId()).isEqualTo(created.getId());
        assertThat(response.getName()).isEqualTo(created.getName());
    }

    @Test
    void updateRestaurant_ReturnsUpdated_WhenFound() {
        // given
        Restaurant updated = existing.toBuilder().name("Updated Name").totalTables(12).build();
        when(restaurantRepository.findByIdForUpdate(existing.getId())).thenReturn(existing);
        when(restaurantRepository.update(existing)).thenReturn(updated);

        RestaurantRequestDto request = new RestaurantRequestDto();
        request.setName(updated.getName());
        request.setAddress(updated.getAddress());
        request.setPhone(updated.getPhone());
        request.setOpenTime(updated.getOpenTime());
        request.setCloseTime(updated.getCloseTime());
        request.setTotalTables(updated.getTotalTables());

        // when
        RestaurantResponseDto response = restaurantService.updateRestaurant(existing.getId(), request);

        // then
        assertThat(response.getName()).isEqualTo("Updated Name");
        assertThat(response.getTotalTables()).isEqualTo(12);
        verify(restaurantRepository).update(existing);
    }
}
