/*
 * Copyright (C) 2023  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.iit.epas.tests;

import com.google.common.io.Resources;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import javax.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2Connection;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class Startup implements ApplicationRunner {

  @Autowired
  EntityManagerFactory entityManagerFactory;

  public static class DatasetImport implements Work {

    private DatabaseOperation operation;
    private final URL url;

    public DatasetImport(DatabaseOperation operation, URL url) {
      this.operation = operation;
      this.url = url;
    }

    @Override
    public void execute(Connection connection) {
      try {
        IDataSet dataSet = new FlatXmlDataSetBuilder()
            .setColumnSensing(true).build(url);
        operation.execute(new H2Connection(connection, ""), dataSet);
      } catch (DataSetException dse) {
        dse.printStackTrace();
      } catch (DatabaseUnitException due) {
        due.printStackTrace();
      } catch (SQLException sqle) {
        sqle.printStackTrace();
      }
    }
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    log.info("Inizializzazione del db per i test");
    Session session = (Session) entityManagerFactory.createEntityManager().getDelegate();
    session.doWork(
        new DatasetImport(
            DatabaseOperation.INSERT,
            Resources.getResource(
                Startup.class, "data/qualifications.xml")));
    //competenceCode
    session.doWork(
        new DatasetImport(
            DatabaseOperation.INSERT,
            Resources.getResource(Startup.class, "data/competence-codes.xml")));
    //competenceCode
    session.doWork(
        new DatasetImport(
            DatabaseOperation.INSERT,
            Resources.getResource(Startup.class, "data/stamp-modification-types.xml")));

    //competenceCode
    session.doWork(
        new DatasetImport(
            DatabaseOperation.INSERT,
            Resources.getResource(Startup.class, "data/roles.xml")));

    //office
    session.doWork(
        new DatasetImport(
            DatabaseOperation.INSERT,
            Resources.getResource(Startup.class, "data/office-with-deps.xml")));

    //workingTimeType workingTimeTypeDay
    session.doWork(
        new DatasetImport(
            DatabaseOperation.INSERT,
            Resources.getResource(Startup.class, "data/working-time-types.xml")));

    //lucchesi slim 2016-04
    session.doWork(
        new DatasetImport(
            DatabaseOperation.INSERT,
            Resources.getResource(Startup.class, "data/lucchesi-login-logout.xml")));

    log.info("Terminato inserimento dati nel db di test");
  }

}
