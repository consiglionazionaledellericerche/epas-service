package it.cnr.iit.epas.dto.v4.mapper;

import it.cnr.iit.epas.dto.v4.PersonDayDto;
import it.cnr.iit.epas.models.PersonDay;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PersonDayMapper {

  @Mapping(target = "personId", source = "person.id")
  PersonDayDto convert(PersonDay personDay);
}
