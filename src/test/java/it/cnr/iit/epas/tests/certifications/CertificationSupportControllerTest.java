package it.cnr.iit.epas.tests.certifications;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.common.collect.ImmutableSet;
import it.cnr.iit.epas.dao.CompetenceCodeDao;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.models.Competence;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.absences.definitions.DefaultAbsenceType;
import it.cnr.iit.epas.tests.db.h2support.H2Examples;
import it.cnr.iit.epas.tests.db.h2support.base.H2AbsenceSupport;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Servizi per epas-certifications-service: accesso riservato al client di servizio e calcolo
 * delle righe dell'attestato del mese.
 */
@Transactional
@SpringBootTest
@TestPropertySource(properties = "epas.security.oauth2.resourceserverEnabled=true")
class CertificationSupportControllerTest {

  private static final String BASE = "/rest/v4/certifications";

  @Inject
  private WebApplicationContext context;
  @Inject
  private H2Examples h2Examples;
  @Inject
  private H2AbsenceSupport h2AbsenceSupport;
  @Inject
  private AbsenceService absenceService;
  @Inject
  private CompetenceCodeDao competenceCodeDao;
  @Inject
  private EntityManager em;

  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  private static RequestPostProcessor client(String clientId) {
    return jwt().jwt(token -> token.claim("azp", clientId));
  }

  private static RequestPostProcessor certificationsClient() {
    return client("epas-certifications-service");
  }

  @Test
  void onlyCertificationsClientCanCallTheEndpoints() throws Exception {
    mvc.perform(get(BASE + "/generalSettings").with(certificationsClient()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.timeoutAttestati").isNumber());
    mvc.perform(get(BASE + "/generalSettings").with(client("epas-workflow-service")))
        .andExpect(status().isForbidden());
    mvc.perform(get(BASE + "/generalSettings")).andExpect(status().isUnauthorized());
    mvc.perform(get("/rest/v4/workflow/generalSettings").with(certificationsClient()))
        .andExpect(status().isForbidden());
  }

  @Test
  void personAndOffice() throws Exception {
    Person person = h2Examples.normalEmployee(LocalDate.of(2016, 1, 1), Optional.empty());
    person.setNumber("1234");
    em.flush();

    mvc.perform(get(BASE + "/people/" + person.getId()).with(certificationsClient()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.number").value("1234"))
        .andExpect(jsonPath("$.officeCodeId").value(person.getOffice().getCodeId()));
    mvc.perform(get(BASE + "/offices/" + person.getOffice().getId())
        .with(certificationsClient()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.codeId").value(person.getOffice().getCodeId()));
    mvc.perform(get(BASE + "/people/0").with(certificationsClient()))
        .andExpect(status().isNotFound());
  }

  @Test
  void monthRowsGroupConsecutiveAbsences() throws Exception {
    // competence-codes.xml non valorizza version, che nel db reale ha default 0
    em.createNativeQuery("update competence_codes set version = 0 where code = 'S1'")
        .executeUpdate();
    em.clear();
    absenceService.enumInitializator();
    Person person = h2Examples.normalEmployee(LocalDate.of(2016, 1, 1), Optional.empty());
    h2AbsenceSupport.multipleAllDayInstances(person, DefaultAbsenceType.A_31, ImmutableSet.of(
        LocalDate.of(2016, 11, 7), LocalDate.of(2016, 11, 8), LocalDate.of(2016, 11, 10)));
    Competence competence = new Competence(person, competenceCodeDao.getCompetenceCodeByCode("S1"),
        2016, 11);
    competence.setValueApproved(12);
    em.persist(competence);
    em.flush();

    mvc.perform(get(BASE + "/people/" + person.getId() + "/monthCertifications")
        .param("year", "2016").param("month", "11").with(certificationsClient()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.certificationType == 'ABSENCE')].content",
            Matchers.containsInAnyOrder("31;7;8", "31;10;10")))
        .andExpect(jsonPath("$[?(@.certificationType == 'COMPETENCE')].content",
            Matchers.contains("S1;12")))
        .andExpect(jsonPath("$[?(@.certificationType == 'MEAL')].content",
            Matchers.contains("0;0")));
  }

  @Test
  void monthWithoutDataHasOnlyTheMealRow() throws Exception {
    Person person = h2Examples.normalEmployee(LocalDate.of(2016, 1, 1), Optional.empty());
    mvc.perform(get(BASE + "/people/" + person.getId() + "/monthCertifications")
        .param("year", "2016").param("month", "11").with(certificationsClient()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].certificationType").value("MEAL"));
  }
}
