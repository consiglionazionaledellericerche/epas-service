package it.cnr.iit.epas.dto.mapper;

import it.cnr.iit.epas.dto.PersonShowTerseDto;
import it.cnr.iit.epas.models.Person;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonShowTerseMapper {

  PersonShowTerseDto convert(Person person);

}
