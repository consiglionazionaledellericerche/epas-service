package it.cnr.iit.epas.config;

import javax.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
    import org.hibernate.envers.AuditReaderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnversConfig {

  @Bean
  public AuditReader auditReader(EntityManager entityManager) {
    return AuditReaderFactory.get(entityManager);
  }
}