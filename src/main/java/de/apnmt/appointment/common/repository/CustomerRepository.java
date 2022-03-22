package de.apnmt.appointment.common.repository;

import de.apnmt.appointment.common.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data SQL repository for the Customer entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findAllByOrganizationId(Long organizationId);

}
