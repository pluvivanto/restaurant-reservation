package local.example.restaurant_reservation.dto;

import java.time.LocalTime;
import java.time.OffsetDateTime;

import local.example.restaurant_reservation.model.Restaurant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RestaurantResponseDto {

    private Long id;

    private String name;

    private String address;

    private String phone;

    private LocalTime openTime;

    private LocalTime closeTime;

    private Integer totalTables;

    private OffsetDateTime createdAt;

    public static RestaurantResponseDto fromEntity(Restaurant restaurant) {
        if (restaurant == null) {
            return null;
        }
        RestaurantResponseDto dto = new RestaurantResponseDto();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setAddress(restaurant.getAddress());
        dto.setPhone(restaurant.getPhone());
        dto.setOpenTime(restaurant.getOpenTime());
        dto.setCloseTime(restaurant.getCloseTime());
        dto.setTotalTables(restaurant.getTotalTables());
        dto.setCreatedAt(restaurant.getCreatedAt());
        return dto;
    }

    public static Restaurant toEntity(RestaurantResponseDto dto) {
        if (dto == null) {
            return null;
        }
        return Restaurant.builder().id(dto.getId()).name(dto.getName()).address(dto.getAddress())
                .phone(dto.getPhone()).openTime(dto.getOpenTime()).closeTime(dto.getCloseTime())
                .totalTables(dto.getTotalTables()).createdAt(dto.getCreatedAt()).build();
    }
}
