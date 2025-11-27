package local.example.restaurant_reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;
import local.example.restaurant_reservation.model.Reservation;
import local.example.restaurant_reservation.model.ReservationStatusEnum;
import net.datafaker.Faker;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ReservationRepositoryTest {

  private static final Faker FAKER = new Faker();

  @Autowired
  private ReservationRepository repository;

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  private Long restaurantId;
  private Long otherRestaurantId;
  private Long customerId;

  @BeforeEach
  void setUp() {
    restaurantId = insertRestaurant();
    otherRestaurantId = insertRestaurant();
    customerId = insertCustomer();
  }

  @Test
  void add_AssignsIdAndCreatedAt_WhenSaving() {
    // given
    OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
    Reservation reservation =
        Reservation.builder().restaurantId(restaurantId)
            .customerId(customerId).tableCount(3)
            .startsAt(startTime)
            .status(ReservationStatusEnum.PENDING).build();

    // when
    Reservation saved = repository.add(reservation);

    // then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(repository.findById(saved.getId())).isNotNull();
  }

  @Test
  void findById_Throws_WhenMissing() {
    // when + then
    assertThatThrownBy(() -> repository.findById(999L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("999");
  }

  @Test
  void findByRestaurantAndDate_FiltersResults_WhenDateMatches() {
    // given
    OffsetDateTime targetTime =
        OffsetDateTime.now(ZoneOffset.UTC).plusDays(2)
            .truncatedTo(ChronoUnit.SECONDS);
    OffsetDateTime otherTime = targetTime.plusDays(1);
    Reservation target = repository.add(Reservation.builder().restaurantId(restaurantId)
        .customerId(customerId).tableCount(2).startsAt(targetTime)
        .status(ReservationStatusEnum.CONFIRMED).build());
    repository.add(Reservation.builder().restaurantId(restaurantId)
        .customerId(customerId)
        .tableCount(2).startsAt(otherTime)
        .status(ReservationStatusEnum.CONFIRMED).build());
    repository.add(Reservation.builder().restaurantId(otherRestaurantId)
        .customerId(customerId)
        .tableCount(2).startsAt(targetTime)
        .status(ReservationStatusEnum.CONFIRMED).build());
    LocalDate date = targetTime.toLocalDate();

    // when
    List<Reservation> result = repository.findByRestaurantAndDate(restaurantId, date);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getId()).isEqualTo(target.getId());
  }

  private Long insertRestaurant() {
    MapSqlParameterSource params =
        new MapSqlParameterSource().addValue("name", FAKER.company().name())
            .addValue("address",
                FAKER.address().streetAddress())
            .addValue("phone", FAKER.phoneNumber().cellPhone())
            .addValue("openTime", java.time.LocalTime.of(9, 0))
            .addValue("closeTime",
                java.time.LocalTime.of(22, 0))
            .addValue("totalTables", 20);
    return jdbcTemplate.queryForObject(
        """
            INSERT INTO restaurant (name, address, phone, open_time, close_time, total_tables)
            VALUES (:name, :address, :phone, :openTime, :closeTime, :totalTables)
            RETURNING id
            """,
        params, Long.class);
  }

  private Long insertCustomer() {
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("name", FAKER.name().fullName())
            .addValue("phone", FAKER.phoneNumber().cellPhone())
            .addValue("email", FAKER.internet().emailAddress());
    return jdbcTemplate.queryForObject("""
        INSERT INTO customer (name, phone, email)
        VALUES (:name, :phone, :email)
        RETURNING id
        """, params, Long.class);
  }
}
