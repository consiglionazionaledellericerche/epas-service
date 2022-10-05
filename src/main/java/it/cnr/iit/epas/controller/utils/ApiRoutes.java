package it.cnr.iit.epas.controller.utils;

public class ApiRoutes {

  private static final String ONLY_DIGITS_REGEX = "^\\d+$";
  //private static final String ALPHANUMERIC_SPECIALS_REGEX = "^\\d*[a-zA-Z\\W].*$";

  public static final String ID_REGEX = "{id:" + ONLY_DIGITS_REGEX + "}";

  public static final String LIST = "";
  public static final String SHOW = "/" + ID_REGEX;
  public static final String CREATE = "/create";
  public static final String UPDATE = "/update";
  public static final String PATCH = "/patch/" + ID_REGEX;
  public static final String DELETE = "/delete/" + ID_REGEX;
  public static final String ALL = "/all";                      // used for a ligthweight list

}
