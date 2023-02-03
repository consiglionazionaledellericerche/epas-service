package it.cnr.iit.epas.messages;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Configurazione dei file properties contenenti
 * i messaggi di testo dell'applicazione.
 *
 * @author Cristian Lucchesi
 *
 */
@Configuration
public class MessagesConfiguration {

  @Bean
  MessageSource messageSource() {
    ReloadableResourceBundleMessageSource messageSource = 
        new ReloadableResourceBundleMessageSource();
    messageSource.setBasenames(
        "classpath:/messages/messages.it.properties");
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
  }
}