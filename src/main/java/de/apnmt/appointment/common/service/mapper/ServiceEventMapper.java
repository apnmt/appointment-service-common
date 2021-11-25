package de.apnmt.appointment.common.service.mapper;

import de.apnmt.appointment.common.domain.Service;
import de.apnmt.common.event.value.ServiceEventDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity {@link Service} and its DTO {@link ServiceEventDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface ServiceEventMapper extends EntityMapper<ServiceEventDTO, Service> {
}
