package local.example.restaurant_reservation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

import local.example.restaurant_reservation.model.Restaurant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RestaurantRequestDto {

    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 500)
    private String address;

    @Size(max = 50)
    private String phone;

    @NotNull
    private LocalTime openTime;

    @NotNull
    private LocalTime closeTime;

    @NotNull
    @Min(1)
    @Max(1000)
    private Integer totalTables;

    public static Restaurant toEntity(RestaurantRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return Restaurant.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .phone(dto.getPhone())
                .openTime(dto.getOpenTime())
                .closeTime(dto.getCloseTime())
                .totalTables(dto.getTotalTables())
                .build();
    }
}
