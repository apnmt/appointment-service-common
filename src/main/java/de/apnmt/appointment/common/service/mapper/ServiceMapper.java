package de.apnmt.appointment.common.service.mapper;

import de.apnmt.appointment.common.domain.Service;
import de.apnmt.appointment.common.service.dto.ServiceDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for the entity {@link Service} and its DTO {@link ServiceDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface ServiceMapper extends EntityMapper<ServiceDTO, Service> {
    @Named("id")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ServiceDTO toDtoId(Service service);
}
