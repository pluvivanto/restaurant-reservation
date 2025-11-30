package local.example.restaurant_reservation.repository;

import local.example.restaurant_reservation.model.Customer;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class CustomerRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public CustomerRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Customer add(Customer customer) {
        Customer nonNullCustomer = Objects.requireNonNull(customer, "customer must not be null");
        SqlParameterSource params = new BeanPropertySqlParameterSource(nonNullCustomer);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            namedParameterJdbcTemplate.update("""
                    INSERT INTO customer (name, phone, email)
                    VALUES (:name, :phone, :email)
                    """, params, keyHolder, new String[] {"id"});
        } catch (DuplicateKeyException ex) {
            throw new DuplicateKeyException(
                    "Customer with email %s already exists".formatted(nonNullCustomer.getEmail()),
                    ex);
        }
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to insert customer, no key was generated");
        }
        return findById(key.longValue());
    }

    public Customer findByEmail(String email) {
        try {
            SqlParameterSource params = new MapSqlParameterSource("email", email);
            return namedParameterJdbcTemplate.queryForObject("""
                    SELECT *
                    FROM customer
                    WHERE lower(email) = lower(:email)
                    """, params, new BeanPropertyRowMapper<>(Customer.class));
        } catch (EmptyResultDataAccessException ex) {
            throw new IllegalArgumentException("Customer with email %s not found".formatted(email),
                    ex);
        }
    }

    public Customer findById(Long id) {
        try {
            SqlParameterSource params = new MapSqlParameterSource("id", id);
            return namedParameterJdbcTemplate.queryForObject("""
                    SELECT *
                    FROM customer
                    WHERE id = :id
                    """, params, new BeanPropertyRowMapper<>(Customer.class));
        } catch (EmptyResultDataAccessException ex) {
            throw new IllegalArgumentException("Customer %d not found".formatted(id), ex);
        }
    }
}
