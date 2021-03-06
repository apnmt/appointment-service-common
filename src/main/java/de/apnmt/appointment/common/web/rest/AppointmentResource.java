package de.apnmt.appointment.common.web.rest;

import de.apnmt.appointment.common.domain.Appointment;
import de.apnmt.appointment.common.repository.AppointmentRepository;
import de.apnmt.appointment.common.service.AppointmentService;
import de.apnmt.appointment.common.service.dto.AppointmentDTO;
import de.apnmt.common.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link Appointment}.
 */
@RestController
@RequestMapping("/api")
public class AppointmentResource {

    private final Logger log = LoggerFactory.getLogger(AppointmentResource.class);

    private static final String ENTITY_NAME = "appointmentServiceAppointment";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AppointmentService appointmentService;

    private final AppointmentRepository appointmentRepository;

    public AppointmentResource(AppointmentService appointmentService, AppointmentRepository appointmentRepository) {
        this.appointmentService = appointmentService;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * {@code POST  /appointments} : Create a new appointment.
     *
     * @param appointmentDTO the appointmentDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new appointmentDTO, or with status {@code 400 (Bad Request)} if the appointment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/appointments")
    public ResponseEntity<AppointmentDTO> createAppointment(@Valid @RequestBody AppointmentDTO appointmentDTO) throws URISyntaxException {
        this.log.debug("REST request to save Appointment : {}", appointmentDTO);
        if (appointmentDTO.getId() != null) {
            throw new BadRequestAlertException("A new appointment cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AppointmentDTO result = this.appointmentService.save(appointmentDTO);
        return ResponseEntity
                .created(new URI("/api/appointments/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(this.applicationName, true, ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * {@code PUT  /appointments/:id} : Updates an existing appointment.
     *
     * @param id             the id of the appointmentDTO to save.
     * @param appointmentDTO the appointmentDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated appointmentDTO,
     * or with status {@code 400 (Bad Request)} if the appointmentDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the appointmentDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/appointments/{id}")
    public ResponseEntity<AppointmentDTO> updateAppointment(
            @PathVariable(value = "id", required = false) final Long id,
            @Valid @RequestBody AppointmentDTO appointmentDTO
    ) throws URISyntaxException {
        this.log.debug("REST request to update Appointment : {}, {}", id, appointmentDTO);
        if (appointmentDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, appointmentDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!this.appointmentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        AppointmentDTO result = this.appointmentService.save(appointmentDTO);
        return ResponseEntity
                .ok()
                .headers(HeaderUtil.createEntityUpdateAlert(this.applicationName, true, ENTITY_NAME, appointmentDTO.getId().toString()))
                .body(result);
    }

    /**
     * {@code GET  /appointments} : get all the appointments.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of appointments in body.
     */
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments(Pageable pageable) {
        this.log.debug("REST request to get a page of Appointments");
        Page<AppointmentDTO> page = this.appointmentService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /appointments/organization/:organizationId/employee/:employeeId} : get all the appointments for Employee and Organization.
     *
     * @param organizationId the id of the Organization.
     * @param employeeId     the id of the Employee.
     * @param start          start Date.
     * @param end            the end Date.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of appointments in body.
     */
    @GetMapping("/appointments/organization/{organizationId}/employee/{employeeId}")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments(@PathVariable Long organizationId, @PathVariable Long employeeId, @RequestParam LocalDateTime start, @RequestParam LocalDateTime end) {
        this.log.debug("Request to get all Appointments for Organization {}, Employee {}, between {} and {}", organizationId, employeeId, start, end);
        List<AppointmentDTO> appointments = this.appointmentService.findAllForOrganizationAndEmployee(organizationId, employeeId, start, end);
        return ResponseEntity.ok().body(appointments);
    }

    /**
     * {@code GET  /appointments/organization/:organizationId} : get all the appointments for Organization.
     *
     * @param organizationId the id of the Organization.
     * @param start          start Date.
     * @param end            the end Date.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of appointments in body.
     */
    @GetMapping("/appointments/organization/{organizationId}")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments(@PathVariable Long organizationId, @RequestParam LocalDateTime start, @RequestParam LocalDateTime end) {
        this.log.debug("Request to get all Appointments for Organization {}, between {} and {}", organizationId, start, end);
        List<AppointmentDTO> appointments = this.appointmentService.findAllForOrganization(organizationId, start, end);
        return ResponseEntity.ok().body(appointments);
    }

    /**
     * {@code GET  /appointments/:id} : get the "id" appointment.
     *
     * @param id the id of the appointmentDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the appointmentDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/appointments/{id}")
    public ResponseEntity<AppointmentDTO> getAppointment(@PathVariable Long id) {
        this.log.debug("REST request to get Appointment : {}", id);
        Optional<AppointmentDTO> appointmentDTO = this.appointmentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(appointmentDTO);
    }

    /**
     * {@code DELETE  /appointments/:id} : delete the "id" appointment.
     *
     * @param id the id of the appointmentDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        this.log.debug("REST request to delete Appointment : {}", id);
        this.appointmentService.delete(id);
        return ResponseEntity
                .noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(this.applicationName, true, ENTITY_NAME, id.toString()))
                .build();
    }

    /**
     * {@code DELETE  /appointments} : delete all appointments.
     *
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/appointments")
    public ResponseEntity<Void> deleteAppointments() {
        this.log.debug("REST request to delete all Appointments");
        this.appointmentService.deleteAll();
        return ResponseEntity
                .noContent()
                .build();
    }
}
