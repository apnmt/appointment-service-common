package de.apnmt.appointment.common.service.mapper;

import de.apnmt.appointment.common.domain.Appointment;
import de.apnmt.common.event.value.AppointmentEventDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity {@link Appointment} and its DTO {@link AppointmentEventDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface AppointmentEventMapper extends EntityMapper<AppointmentEventDTO, Appointment> {
}
