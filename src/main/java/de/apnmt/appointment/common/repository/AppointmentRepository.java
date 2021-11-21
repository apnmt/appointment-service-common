package de.apnmt.appointment.common.repository;

import de.apnmt.appointment.common.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data SQL repository for the Appointment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByOrganizationIdAndEmployeeIdAndStartAtAfterAndStartAtBefore(Long organizationId, Long employeeId, LocalDateTime start, LocalDateTime end);

}
