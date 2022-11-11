package it.cnr.iit.epas.dto.v4.mapper;

import it.cnr.iit.epas.dto.v4.AbsenceShowTerseDto;
import it.cnr.iit.epas.dto.v4.PersonDayDto;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.absences.Absence;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PersonDayMapper {

  @Mapping(target = "personId", source = "person.id")
  PersonDayDto convert(PersonDay personDay);
  
  @Mapping(target = "justifiedType", source = "justifiedType.name")
  @Mapping(target = "externalId", source = "externalIdentifier")
  @Mapping(target = "justifiedTime", expression = "java(absence.justifiedTime())")
  AbsenceShowTerseDto convert(Absence absence);
}
