package local.example.restaurant_reservation.repository;

import java.util.List;
import java.util.Objects;

import local.example.restaurant_reservation.model.Restaurant;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class RestaurantRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public RestaurantRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Restaurant findById(Long id) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("id", id);
        return namedParameterJdbcTemplate.queryForObject("SELECT * FROM restaurant WHERE id=:id",
                parameterSource, new BeanPropertyRowMapper<>(Restaurant.class));
    }

    public Restaurant findByIdForUpdate(Long id) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("id", id);
        return namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM restaurant WHERE id=:id FOR UPDATE",
                parameterSource,
                new BeanPropertyRowMapper<>(Restaurant.class));
    }

    public Restaurant add(Restaurant restaurant) {
        Restaurant nonNullRestaurant =
                Objects.requireNonNull(restaurant, "restaurant must not be null");
        SqlParameterSource params = new BeanPropertySqlParameterSource(nonNullRestaurant);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsUpdated = namedParameterJdbcTemplate.update("""
                INSERT INTO restaurant
                (name, address, phone, open_time, close_time, total_tables)
                VALUES (:name, :address, :phone, :openTime, :closeTime, :totalTables)
                """, params, keyHolder, new String[] {"id"});
        Number key = keyHolder.getKey();
        if (rowsUpdated == 0 || key == null) {
            throw new IllegalStateException("Failed to insert restaurant");
        }
        return findById(key.longValue());
    }

    public Restaurant update(Restaurant restaurant) {
        Restaurant nonNullRestaurant =
                Objects.requireNonNull(restaurant, "restaurant must not be null");
        SqlParameterSource params = new BeanPropertySqlParameterSource(nonNullRestaurant);
        namedParameterJdbcTemplate.update("""
                UPDATE restaurant
                SET name=:name, address=:address, phone=:phone,
                  open_time=:openTime, close_time=:closeTime, total_tables=:totalTables
                WHERE id=:id
                """, params);
        return findById(restaurant.getId());
    }

    public List<Restaurant> findAll(int page, int size) {
        SqlParameterSource params = new MapSqlParameterSource().addValue("limit", size)
                .addValue("offset", (long) page * size);
        return namedParameterJdbcTemplate.query("""
                SELECT *
                FROM restaurant
                ORDER BY name, id
                LIMIT :limit OFFSET :offset
                """, params, new BeanPropertyRowMapper<>(Restaurant.class));
    }
}
