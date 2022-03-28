package de.apnmt.appointment.common.service;

import de.apnmt.appointment.common.domain.Appointment;
import de.apnmt.appointment.common.repository.AppointmentRepository;
import de.apnmt.appointment.common.service.dto.AppointmentDTO;
import de.apnmt.appointment.common.service.mapper.AppointmentEventMapper;
import de.apnmt.appointment.common.service.mapper.AppointmentMapper;
import de.apnmt.common.TopicConstants;
import de.apnmt.common.errors.HttpError;
import de.apnmt.common.event.ApnmtEvent;
import de.apnmt.common.event.ApnmtEventType;
import de.apnmt.common.event.value.AppointmentEventDTO;
import de.apnmt.common.sender.ApnmtEventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Status;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Appointment}.
 */
@Service
@Transactional
public class AppointmentService {

    private final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;

    private final AppointmentMapper appointmentMapper;

    private final ApnmtEventSender<AppointmentEventDTO> sender;

    private final AppointmentEventMapper appointmentEventMapper;

    public AppointmentService(AppointmentRepository appointmentRepository, AppointmentMapper appointmentMapper, ApnmtEventSender<AppointmentEventDTO> sender, AppointmentEventMapper appointmentEventMapper) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
        this.sender = sender;
        this.appointmentEventMapper = appointmentEventMapper;
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
        this.checkAvailability(appointment);
        appointment = this.appointmentRepository.save(appointment);
        this.sender.send(TopicConstants.APPOINTMENT_CHANGED_TOPIC, this.createEvent(appointment, ApnmtEventType.appointmentCreated));
        return this.appointmentMapper.toDto(appointment);
    }

    private ApnmtEvent<AppointmentEventDTO> createEvent(Appointment appointment, ApnmtEventType type) {
        return new ApnmtEvent<AppointmentEventDTO>().timestamp(LocalDateTime.now()).type(type).value(this.appointmentEventMapper.toDto(appointment));
    }

    private void checkAvailability(Appointment appointment) {
        LocalDateTime start = appointment.getStartAt().truncatedTo(ChronoUnit.DAYS);
        LocalDateTime end = start.plusDays(1);
        List<Appointment> appointments = this.appointmentRepository.findAllByOrganizationIdAndEmployeeIdAndStartAtAfterAndStartAtBefore(appointment.getOrganizationId(), appointment.getEmployeeId(), start, end);

        for (Appointment apnmt : appointments) {
            if (!(appointment.getStartAt().isAfter(apnmt.getEndAt()) || appointment.getEndAt().isBefore(apnmt.getStartAt()) || appointment.getStartAt().equals(apnmt.getEndAt()) || appointment.getEndAt().equals(apnmt.getStartAt()))) {
                throw new HttpError(Status.TOO_MANY_REQUESTS, "slot.not.available", "Slot from " + appointment.getStartAt() + " until " + appointment.getEndAt() + " for organization " + appointment.getOrganizationId() + " and employeeId " + appointment.getEmployeeId() + " is not available");
            }
        }
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
     * Get all the appointments for Employee and Organization.
     *
     * @param organizationId the id of the Organization.
     * @param employeeId     the id of the Employee.
     * @param start          start Date.
     * @param end            the end Date.
     * @return the list of entities.
     */
    public List<AppointmentDTO> findAllForOrganizationAndEmployee(Long organizationId, Long employeeId, LocalDateTime start, LocalDateTime end) {
        this.log.debug("Request to get all Appointments for Organization {}, Employee {}, between {} and {}", organizationId, employeeId, start, end);
        return this.appointmentRepository.findAllByOrganizationIdAndEmployeeIdAndStartAtAfterAndStartAtBefore(organizationId, employeeId, start, end).stream().map(this.appointmentMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Get all the appointments for Organization.
     *
     * @param organizationId the id of the Organization.
     * @param start          start Date.
     * @param end            the end Date.
     * @return the list of entities.
     */
    public List<AppointmentDTO> findAllForOrganization(Long organizationId, LocalDateTime start, LocalDateTime end) {
        this.log.debug("Request to get all Appointments for Organization {}, between {} and {}", organizationId, start, end);
        return this.appointmentRepository.findAllByOrganizationIdAndStartAtAfterAndStartAtBefore(organizationId, start, end).stream().map(this.appointmentMapper::toDto).collect(Collectors.toList());
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
        Optional<Appointment> maybe = this.appointmentRepository.findById(id);
        ApnmtEvent<AppointmentEventDTO> event;
        if (maybe.isPresent()) {
            event = this.createEvent(maybe.get(), ApnmtEventType.appointmentDeleted);
        } else {
            event = this.createEvent(new Appointment().id(id), ApnmtEventType.appointmentDeleted);
        }
        this.sender.send(TopicConstants.APPOINTMENT_CHANGED_TOPIC, event);
        this.appointmentRepository.deleteById(id);
    }
}
