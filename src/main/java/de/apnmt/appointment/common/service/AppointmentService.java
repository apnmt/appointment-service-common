package de.apnmt.appointment.common.service;

import de.apnmt.appointment.common.domain.Appointment;
import de.apnmt.appointment.common.repository.AppointmentRepository;
import de.apnmt.appointment.common.service.dto.AppointmentDTO;
import de.apnmt.appointment.common.service.error.SlotNotAvailableException;
import de.apnmt.appointment.common.service.mapper.AppointmentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link Appointment}.
 */
@Service
@Transactional
public class AppointmentService {

    private final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;

    private final AppointmentMapper appointmentMapper;

    public AppointmentService(AppointmentRepository appointmentRepository, AppointmentMapper appointmentMapper) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
    }

    /**
     * Save a appointment.
     *
     * @param appointmentDTO the entity to save.
     * @return the persisted entity.
     */
    public AppointmentDTO save(AppointmentDTO appointmentDTO) {
        this.log.debug("Request to save Appointment : {}", appointmentDTO);
        Appointment appointment = this.appointmentMapper.toEntity(appointmentDTO);
        checkAvailability(appointment);
        appointment = this.appointmentRepository.save(appointment);
        return this.appointmentMapper.toDto(appointment);
    }

    private void checkAvailability(Appointment appointment) {
        LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        LocalDateTime end = start.plusDays(1);
        List<Appointment> appointments = this.appointmentRepository.findAllByOrganizationIdAndEmployeeIdAndStartAtAfterAndStartAtBefore(appointment.getOrganizationId(), appointment.getEmployeeId(), start, end);

        for (Appointment apnmt : appointments) {
            if (!(appointment.getStartAt().isAfter(apnmt.getEndAt()) || appointment.getEndAt().isBefore(apnmt.getStartAt()) || appointment.getStartAt().equals(apnmt.getEndAt()) || appointment.getEndAt().equals(apnmt.getStartAt()))) {
                throw new SlotNotAvailableException();
            }
        }
    }

    /**
     * Partially update a appointment.
     *
     * @param appointmentDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<AppointmentDTO> partialUpdate(AppointmentDTO appointmentDTO) {
        this.log.debug("Request to partially update Appointment : {}", appointmentDTO);

        return this.appointmentRepository
                .findById(appointmentDTO.getId())
                .map(
                        existingAppointment -> {
                            this.appointmentMapper.partialUpdate(existingAppointment, appointmentDTO);

                            return existingAppointment;
                        }
                )
                .map(this.appointmentRepository::save)
                .map(this.appointmentMapper::toDto);
    }

    /**
     * Get all the appointments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<AppointmentDTO> findAll(Pageable pageable) {
        this.log.debug("Request to get all Appointments");
        return this.appointmentRepository.findAll(pageable).map(this.appointmentMapper::toDto);
    }

    /**
     * Get one appointment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<AppointmentDTO> findOne(Long id) {
        this.log.debug("Request to get Appointment : {}", id);
        return this.appointmentRepository.findById(id).map(this.appointmentMapper::toDto);
    }

    /**
     * Delete the appointment by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        this.log.debug("Request to delete Appointment : {}", id);
        this.appointmentRepository.deleteById(id);
    }
}
