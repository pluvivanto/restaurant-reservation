package local.example.restaurant_reservation.controller;

import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import local.example.restaurant_reservation.dto.RestaurantRequestDto;
import local.example.restaurant_reservation.dto.RestaurantResponseDto;
import local.example.restaurant_reservation.service.RestaurantService;

@RestController
@RequestMapping("/restaurants")
@Validated
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public List<RestaurantResponseDto> listRestaurants(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        return restaurantService.listRestaurants(page, size);
    }

    @GetMapping("/{restaurantId}")
    public RestaurantResponseDto getRestaurant(@PathVariable Long restaurantId) {
        return restaurantService.getRestaurant(restaurantId);
    }

    @PostMapping
    public RestaurantResponseDto createRestaurant(
            @Valid @RequestBody RestaurantRequestDto requestDto) {
        return restaurantService.createRestaurant(requestDto);
    }

    @PutMapping("/{restaurantId}")
    public RestaurantResponseDto updateRestaurant(@PathVariable Long restaurantId,
            @RequestBody RestaurantRequestDto requestDto) {
        return restaurantService.updateRestaurant(restaurantId, requestDto);
    }

}
