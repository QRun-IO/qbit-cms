/*
 * Copyright 2026 Kingsrook, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kingsrook.qbits.cms;


import java.sql.Connection;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;


/*******************************************************************************
 ** Test fixtures: a blank QQQ instance on a fresh in-memory H2 database (the
 ** "any running QQQ system" a host starts from), the qbit's changelog run the
 ** way a host runs it, and sessions with chosen permissions.
 *******************************************************************************/
public final class CmsTestSupport
{
   public static final String BACKEND = "rdbms";



   /*******************************************************************************
    ** Not instantiable.
    *******************************************************************************/
   private CmsTestSupport()
   {
   }



   /*******************************************************************************
    ** A fresh H2 backend (its own database). QQQ caches connection providers
    ** by backend name, so the cache is reset first.
    *******************************************************************************/
   public static RDBMSBackendMetaData h2Backend()
   {
      ConnectionManager.resetConnectionProviders();
      return (new RDBMSBackendMetaData()
         .withName(BACKEND)
         .withVendor("h2")
         .withHostName("mem")
         .withDatabaseName("cms_" + UUID.randomUUID().toString().replace("-", ""))
         .withUsername("sa"));
   }



   /*******************************************************************************
    ** A blank instance with one RDBMS backend and mock authentication.
    *******************************************************************************/
   public static QInstance blankInstance(RDBMSBackendMetaData backend)
   {
      QInstance qInstance = new QInstance();
      qInstance.addBackend(backend);
      qInstance.withInstanceDefaultAuthentication(new QAuthenticationMetaData().withName("mock").withType(QAuthenticationType.MOCK));
      return (qInstance);
   }



   /*******************************************************************************
    ** Run the qbit's changelog against a backend, as a host's Liquibase would,
    ** with the given changelog properties.
    *******************************************************************************/
   public static void migrate(RDBMSBackendMetaData backend, Map<String, String> properties) throws Exception
   {
      Connection connection = ConnectionManager.getConnection(backend);
      Database   database   = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
      try(Liquibase liquibase = new Liquibase(CmsQBitConfig.CHANGELOG, new ClassLoaderResourceAccessor(), database))
      {
         properties.forEach(liquibase::setChangeLogParameter);
         liquibase.update(new Contexts(), new LabelExpression());
      }
   }



   /*******************************************************************************
    ** Start a context on an instance with a session holding these permissions.
    *******************************************************************************/
   public static QSession session(QInstance qInstance, Collection<String> permissions) throws Exception
   {
      QSession session = new QSession().withPermissions(permissions);
      QContext.init(qInstance, session);
      return (session);
   }
}
