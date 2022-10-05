package it.cnr.iit.epas.repo;

import it.cnr.iit.epas.models.Person;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends PagingAndSortingRepository<Person, Long>{

}
