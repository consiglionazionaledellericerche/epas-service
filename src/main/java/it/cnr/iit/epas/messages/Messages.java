package it.cnr.iit.epas.messages;

import org.springframework.stereotype.Component;

@Component
public class Messages {

  //FIXME: da completare con il sistema di gestione delle stringhe esternalizzate
  //ed internazionalite di Spring
  public static String get(String messageId) {
    return messageId;
  }
}
