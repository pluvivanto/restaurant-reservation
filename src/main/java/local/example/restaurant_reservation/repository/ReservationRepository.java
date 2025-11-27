package local.example.restaurant_reservation.repository;

import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import local.example.restaurant_reservation.model.Reservation;
import local.example.restaurant_reservation.model.ReservationStatusEnum;

@Repository
public class ReservationRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ReservationRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Reservation add(Reservation reservation) {
        MapSqlParameterSource params =
                new MapSqlParameterSource().addValue("restaurantId", reservation.getRestaurantId())
                        .addValue("customerId", reservation.getCustomerId())
                        .addValue("tableCount", reservation.getTableCount())
                        .addValue("startsAt", reservation.getStartsAt(),
                                Types.TIMESTAMP_WITH_TIMEZONE)
                        .addValue("status", reservation.getStatus().name(), Types.VARCHAR);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(
                """
                        INSERT INTO reservation (restaurant_id, customer_id, table_count, starts_at, status)
                        VALUES (:restaurantId, :customerId, :tableCount, :startsAt, CAST(:status AS reservation_status))
                        """,
                params, keyHolder, new String[] {"id"});
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to insert reservation, no key was generated");
        }
        return findById(key.longValue());
    }

    public Reservation update(Reservation reservation) {
        Reservation nonNullReservation =
                Objects.requireNonNull(reservation, "reservation must not be null");
        BeanPropertySqlParameterSource params =
                new BeanPropertySqlParameterSource(nonNullReservation);
        namedParameterJdbcTemplate.update("""
                UPDATE reservation
                SET restaurant_id = :restaurantId,
                  customer_id = :customerId,
                  table_count = :tableCount,
                  starts_at = :startsAt,
                  status = CAST(:status AS reservation_status)
                WHERE id = :id
                """, params);
        return findById(reservation.getId());
    }

    public Reservation findById(Long reservationId) {
        try {
            SqlParameterSource params = new MapSqlParameterSource("id", reservationId);
            return namedParameterJdbcTemplate.queryForObject("""
                    SELECT *
                    FROM reservation
                    WHERE id = :id
                    """, params, new BeanPropertyRowMapper<>(Reservation.class));
        } catch (EmptyResultDataAccessException ex) {
            throw new IllegalArgumentException("Reservation %d not found".formatted(reservationId),
                    ex);
        }
    }

    public List<Reservation> findByRestaurantAndDate(Long restaurantId, LocalDate date) {
        MapSqlParameterSource params =
                new MapSqlParameterSource().addValue("restaurantId", restaurantId).addValue("date",
                        date);
        return namedParameterJdbcTemplate.query("""
                SELECT *
                FROM reservation
                WHERE restaurant_id = :restaurantId
                  AND CAST(starts_at AS DATE) = :date
                ORDER BY starts_at, id
                """, params, new BeanPropertyRowMapper<>(Reservation.class));
    }

    public List<Reservation> findByRestaurant(Long restaurantId) {
        MapSqlParameterSource params = new MapSqlParameterSource("restaurantId", restaurantId);
        return namedParameterJdbcTemplate.query("""
                SELECT *
                FROM reservation
                WHERE restaurant_id = :restaurantId
                ORDER BY starts_at, id
                """, params, new BeanPropertyRowMapper<>(Reservation.class));
    }

    public List<Reservation> findByStatusNot(Long restaurantId, ReservationStatusEnum status) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("restaurantId", restaurantId).addValue("status", status.name());
        return namedParameterJdbcTemplate.query("""
                SELECT *
                FROM reservation
                WHERE restaurant_id = :restaurantId
                  AND status <> CAST(:status AS reservation_status)
                ORDER BY starts_at, id
                """, params, new BeanPropertyRowMapper<>(Reservation.class));
    }

}
