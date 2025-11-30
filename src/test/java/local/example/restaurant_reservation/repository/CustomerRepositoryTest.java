package local.example.restaurant_reservation.repository;

import local.example.restaurant_reservation.model.Customer;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CustomerRepositoryTest {

    private static final Faker FAKER = new Faker(new Random(5));

    @Autowired
    private CustomerRepository repository;

    @Test
    void save_AssignsIdAndCreatedAt_WhenSaving() {
        // given
        Customer customer = Customer.builder().name(FAKER.name().fullName())
                .phone(FAKER.phoneNumber().cellPhone()).email(FAKER.internet().emailAddress()).build();

        // when
        Customer saved = repository.add(customer);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        Customer reloaded = repository.findById(saved.getId());
        assertThat(reloaded.getEmail()).isEqualTo(saved.getEmail());
        assertThat(reloaded.getName()).isEqualTo(saved.getName());
    }

    @Test
    void findByEmail_IsCaseInsensitive_WhenMixedCase() {
        // given
        String mixedCaseEmail = FAKER.internet().emailAddress();
        repository.add(Customer.builder().name(FAKER.name().fullName())
                .phone(FAKER.phoneNumber().cellPhone()).email(mixedCaseEmail.toLowerCase()).build());

        // when
        Customer found = repository.findByEmail(mixedCaseEmail.toUpperCase());

        // then
        assertThat(found.getEmail()).isEqualTo(mixedCaseEmail.toLowerCase());
    }
}
