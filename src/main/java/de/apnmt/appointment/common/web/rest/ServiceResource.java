package de.apnmt.appointment.common.web.rest;

import de.apnmt.appointment.common.domain.Service;
import de.apnmt.appointment.common.repository.ServiceRepository;
import de.apnmt.appointment.common.service.ServiceService;
import de.apnmt.appointment.common.service.dto.ServiceDTO;
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
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link Service}.
 */
@RestController
@RequestMapping("/api")
public class ServiceResource {

    private final Logger log = LoggerFactory.getLogger(ServiceResource.class);

    private static final String ENTITY_NAME = "appointmentServiceService";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ServiceService serviceService;

    private final ServiceRepository serviceRepository;

    public ServiceResource(ServiceService serviceService, ServiceRepository serviceRepository) {
        this.serviceService = serviceService;
        this.serviceRepository = serviceRepository;
    }

    /**
     * {@code POST  /services} : Create a new service.
     *
     * @param serviceDTO the serviceDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new serviceDTO, or with status {@code 400 (Bad Request)} if the service has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/services")
    public ResponseEntity<ServiceDTO> createService(@Valid @RequestBody ServiceDTO serviceDTO) throws URISyntaxException {
        this.log.debug("REST request to save Service : {}", serviceDTO);
        if (serviceDTO.getId() != null) {
            throw new BadRequestAlertException("A new service cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ServiceDTO result = this.serviceService.save(serviceDTO);
        return ResponseEntity
            .created(new URI("/api/services/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(this.applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /services/:id} : Updates an existing service.
     *
     * @param id the id of the serviceDTO to save.
     * @param serviceDTO the serviceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated serviceDTO,
     * or with status {@code 400 (Bad Request)} if the serviceDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the serviceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/services/{id}")
    public ResponseEntity<ServiceDTO> updateService(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ServiceDTO serviceDTO
    ) throws URISyntaxException {
        this.log.debug("REST request to update Service : {}, {}", id, serviceDTO);
        if (serviceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, serviceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!this.serviceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ServiceDTO result = this.serviceService.save(serviceDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(this.applicationName, true, ENTITY_NAME, serviceDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /services/:id} : Partial updates given fields of an existing service, field will ignore if it is null
     *
     * @param id the id of the serviceDTO to save.
     * @param serviceDTO the serviceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated serviceDTO,
     * or with status {@code 400 (Bad Request)} if the serviceDTO is not valid,
     * or with status {@code 404 (Not Found)} if the serviceDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the serviceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/services/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<ServiceDTO> partialUpdateService(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ServiceDTO serviceDTO
    ) throws URISyntaxException {
        this.log.debug("REST request to partial update Service partially : {}, {}", id, serviceDTO);
        if (serviceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, serviceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!this.serviceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ServiceDTO> result = this.serviceService.partialUpdate(serviceDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(this.applicationName, true, ENTITY_NAME, serviceDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /services} : get all the services.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of services in body.
     */
    @GetMapping("/services")
    public ResponseEntity<List<ServiceDTO>> getAllServices(Pageable pageable) {
        this.log.debug("REST request to get a page of Services");
        Page<ServiceDTO> page = this.serviceService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /services/organization/:id} : get all the services.
     *
     * @param id the organization id.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of services in body.
     */
    @GetMapping("/services/organization/{id}")
    public ResponseEntity<List<ServiceDTO>> getAllServices(@PathVariable Long id) {
        this.log.debug("REST request to get a page of Services");
        List<ServiceDTO> services = this.serviceService.findAll(id);
        return ResponseEntity.ok().body(services);
    }

    /**
     * {@code GET  /services/:id} : get the "id" service.
     *
     * @param id the id of the serviceDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the serviceDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceDTO> getService(@PathVariable Long id) {
        this.log.debug("REST request to get Service : {}", id);
        Optional<ServiceDTO> serviceDTO = this.serviceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(serviceDTO);
    }

    /**
     * {@code DELETE  /services/:id} : delete the "id" service.
     *
     * @param id the id of the serviceDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        this.log.debug("REST request to delete Service : {}", id);
        this.serviceService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(this.applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
