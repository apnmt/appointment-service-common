package de.apnmt.appointment.common.service.mapper;

import de.apnmt.appointment.common.domain.Appointment;
import de.apnmt.appointment.common.service.dto.AppointmentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity {@link Appointment} and its DTO {@link AppointmentDTO}.
 */
@Mapper(componentModel = "spring", uses = { CustomerMapper.class, ServiceMapper.class })
public interface AppointmentMapper extends EntityMapper<AppointmentDTO, Appointment> {
    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "service", source = "service", qualifiedByName = "id")
    AppointmentDTO toDto(Appointment s);
}
