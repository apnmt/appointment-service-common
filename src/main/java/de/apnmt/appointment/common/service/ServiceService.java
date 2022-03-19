package de.apnmt.appointment.common.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.apnmt.appointment.common.repository.ServiceRepository;
import de.apnmt.appointment.common.service.dto.ServiceDTO;
import de.apnmt.appointment.common.service.mapper.ServiceEventMapper;
import de.apnmt.appointment.common.service.mapper.ServiceMapper;
import de.apnmt.common.TopicConstants;
import de.apnmt.common.event.ApnmtEvent;
import de.apnmt.common.event.ApnmtEventType;
import de.apnmt.common.event.value.ServiceEventDTO;
import de.apnmt.common.sender.ApnmtEventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Service}.
 */
@Service
@Transactional
public class ServiceService {

    private final Logger log = LoggerFactory.getLogger(ServiceService.class);

    private final ServiceRepository serviceRepository;

    private final ServiceMapper serviceMapper;

    private final ServiceEventMapper serviceEventMapper;

    private final ApnmtEventSender<ServiceEventDTO> sender;

    public ServiceService(ServiceRepository serviceRepository, ServiceMapper serviceMapper, ServiceEventMapper serviceEventMapper, ApnmtEventSender<ServiceEventDTO> sender) {
        this.serviceRepository = serviceRepository;
        this.serviceMapper = serviceMapper;
        this.serviceEventMapper = serviceEventMapper;
        this.sender = sender;
    }

    /**
     * Save a service.
     *
     * @param serviceDTO the entity to save.
     * @return the persisted entity.
     */
    public ServiceDTO save(ServiceDTO serviceDTO) {
        this.log.debug("Request to save Service : {}", serviceDTO);
        de.apnmt.appointment.common.domain.Service service = this.serviceMapper.toEntity(serviceDTO);
        service = this.serviceRepository.save(service);
        this.sender.send(TopicConstants.SERVICE_CHANGED_TOPIC, this.createEvent(service, ApnmtEventType.serviceCreated));
        return this.serviceMapper.toDto(service);
    }

    /**
     * Partially update a service.
     *
     * @param serviceDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ServiceDTO> partialUpdate(ServiceDTO serviceDTO) {
        this.log.debug("Request to partially update Service : {}", serviceDTO);

        return this.serviceRepository.findById(serviceDTO.getId()).map(existingService -> {
            this.serviceMapper.partialUpdate(existingService, serviceDTO);

            return existingService;
        }).map(this.serviceRepository::save).map(service -> {
            this.sender.send(TopicConstants.SERVICE_CHANGED_TOPIC, this.createEvent(service, ApnmtEventType.serviceCreated));
            return service;
        }).map(this.serviceMapper::toDto);
    }

    /**
     * Get all the services.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ServiceDTO> findAll(Pageable pageable) {
        this.log.debug("Request to get all Services");
        return this.serviceRepository.findAll(pageable).map(this.serviceMapper::toDto);
    }

    /**
     * Get all the services by organizationId.
     *
     * @param organizationId the organization id.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ServiceDTO> findAll(Long organizationId) {
        this.log.debug("Request to get all Services for Organization {}", organizationId);
        return this.serviceRepository.findAllByOrganizationId(organizationId).stream().map(this.serviceMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Get one service by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ServiceDTO> findOne(Long id) {
        this.log.debug("Request to get Service : {}", id);
        return this.serviceRepository.findById(id).map(this.serviceMapper::toDto);
    }

    /**
     * Delete the service by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        this.log.debug("Request to delete Service : {}", id);
        Optional<de.apnmt.appointment.common.domain.Service> maybe = this.serviceRepository.findById(id);
        ApnmtEvent<ServiceEventDTO> event;
        if (maybe.isPresent()) {
            event = this.createEvent(maybe.get(), ApnmtEventType.serviceDeleted);
        } else {
            event = this.createEvent(new de.apnmt.appointment.common.domain.Service().id(id), ApnmtEventType.serviceDeleted);
        }
        this.sender.send(TopicConstants.SERVICE_CHANGED_TOPIC, event);
        this.serviceRepository.deleteById(id);
    }

    private ApnmtEvent<ServiceEventDTO> createEvent(de.apnmt.appointment.common.domain.Service service, ApnmtEventType type) {
        return new ApnmtEvent<ServiceEventDTO>().timestamp(LocalDateTime.now()).type(type).value(this.serviceEventMapper.toDto(service));
    }
}
