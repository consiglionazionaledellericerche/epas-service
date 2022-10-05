package it.cnr.iit.epas.dto.mapper;

import it.cnr.iit.epas.dto.PersonShowDto;
import it.cnr.iit.epas.models.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PersonShowMapper {

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "officeId", source = "office.id")
  PersonShowDto convert(Person person);

}
