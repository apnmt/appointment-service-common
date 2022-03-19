package de.apnmt.appointment.common.repository;

import de.apnmt.appointment.common.domain.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data SQL repository for the Service entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findAllByOrganizationId(Long organizationId);

}
