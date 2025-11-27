package local.example.restaurant_reservation.repository;

import java.util.Objects;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import local.example.restaurant_reservation.model.Customer;

@Repository
public class CustomerRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public CustomerRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Customer add(Customer customer) {
        Customer nonNullCustomer = Objects.requireNonNull(customer, "customer must not be null");
        SqlParameterSource parameters = new BeanPropertySqlParameterSource(nonNullCustomer);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update("""
                INSERT INTO customer (name, phone, email)
                VALUES (:name, :phone, :email)
                """, parameters, keyHolder, new String[]{"id"});
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to insert customer, no key was generated");
        }
        return findById(key.longValue()).orElseThrow();
    }

    public Optional<Customer> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        try {
            SqlParameterSource params = new MapSqlParameterSource("email", email);
            Customer customer = namedParameterJdbcTemplate.queryForObject("""
                    SELECT *
                    FROM customer
                    WHERE lower(email) = lower(:email)
                    """, params, new BeanPropertyRowMapper<>(Customer.class));
            return Optional.ofNullable(customer);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<Customer> findById(Long id) {
        try {
            SqlParameterSource params = new MapSqlParameterSource("id", id);
            Customer customer = namedParameterJdbcTemplate.queryForObject("""
                    SELECT *
                    FROM customer
                    WHERE id = :id
                    """, params, new BeanPropertyRowMapper<>(Customer.class));
            return Optional.ofNullable(customer);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }
}
