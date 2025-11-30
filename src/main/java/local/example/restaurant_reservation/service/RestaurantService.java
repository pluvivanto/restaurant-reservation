package local.example.restaurant_reservation.service;

import java.util.List;

import local.example.restaurant_reservation.dto.RestaurantRequestDto;
import local.example.restaurant_reservation.dto.RestaurantResponseDto;
import local.example.restaurant_reservation.model.Restaurant;
import local.example.restaurant_reservation.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public List<RestaurantResponseDto> listRestaurants(int page, int size) {
        return restaurantRepository.findAll(page, size).stream().map(RestaurantResponseDto::fromEntity)
                .toList();
    }

    public RestaurantResponseDto getRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId);
        return RestaurantResponseDto.fromEntity(restaurant);
    }

    public RestaurantResponseDto createRestaurant(RestaurantRequestDto requestDto) {
        Restaurant toCreate = RestaurantRequestDto.toEntity(requestDto);
        Restaurant created = restaurantRepository.add(toCreate);
        return RestaurantResponseDto.fromEntity(created);
    }

    @Transactional
    public RestaurantResponseDto updateRestaurant(Long restaurantId,
                                                  RestaurantRequestDto requestDto) {
        Restaurant existing = restaurantRepository.findByIdForUpdate(restaurantId);
        existing.setName(requestDto.getName());
        existing.setAddress(requestDto.getAddress());
        existing.setPhone(requestDto.getPhone());
        existing.setOpenTime(requestDto.getOpenTime());
        existing.setCloseTime(requestDto.getCloseTime());
        existing.setTotalTables(requestDto.getTotalTables());
        Restaurant updated = restaurantRepository.update(existing);
        return RestaurantResponseDto.fromEntity(updated);
    }
}
