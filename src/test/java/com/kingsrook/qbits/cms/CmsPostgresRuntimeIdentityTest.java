/*
 * Copyright 2025 Kingsrook, LLC
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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qbits.cms.security.CmsRole;
import com.kingsrook.qbits.cms.security.CmsSecurity;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QPermissionDeniedException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.module.postgres.model.metadata.PostgreSQLBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** The install as production runs it on PostgreSQL 17: a schema OWNER runs the
 ** changelog (Liquibase 4.33.0) with a prefix that cannot collide with a host's own
 ** CMS tables, and the application runs as a DML-only role that owns nothing, cannot
 ** read migration history and cannot run DDL.  The startup check must work for that
 ** role (catalog verification), and the QQQ permission ceilings must hold on top of
 ** the database grants.
 *******************************************************************************/
@Testcontainers(disabledWithoutDocker = true)
class CmsPostgresRuntimeIdentityTest
{
   private static final String OWNER   = "qcms_owner";
   private static final String RUNTIME = "qcms_runtime";
   private static final String PREFIX  = "qcms_";

   @Container
   private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");



   /*******************************************************************************
    ** Owner, runtime role, owner-run migration (twice), DML-only grants.
    *******************************************************************************/
   @BeforeAll
   static void provision() throws Exception
   {
      try(Connection admin = connect(POSTGRES.getUsername(), POSTGRES.getPassword(), "postgres"); Statement statement = admin.createStatement())
      {
         statement.execute("CREATE ROLE " + OWNER + " LOGIN PASSWORD 'owner-synthetic'");
         statement.execute("CREATE ROLE " + RUNTIME + " LOGIN PASSWORD 'runtime-synthetic'");
         statement.execute("CREATE DATABASE cmsdb OWNER " + OWNER);
         statement.execute("REVOKE ALL ON DATABASE cmsdb FROM PUBLIC");
         statement.execute("GRANT CONNECT ON DATABASE cmsdb TO " + RUNTIME);
      }
      try(Connection owner = connect(OWNER, "owner-synthetic", "cmsdb"); Statement statement = owner.createStatement())
      {
         statement.execute("REVOKE ALL ON SCHEMA public FROM PUBLIC");
         statement.execute("GRANT USAGE ON SCHEMA public TO " + RUNTIME);
      }

      ConnectionManager.resetConnectionProviders();
      PostgreSQLBackendMetaData ownerBackend = backend(OWNER, "owner-synthetic");
      CmsTestSupport.migrate(ownerBackend, Map.of("cms.tablePrefix", PREFIX));
      CmsTestSupport.migrate(ownerBackend, Map.of("cms.tablePrefix", PREFIX));

      try(Connection owner = connect(OWNER, "owner-synthetic", "cmsdb"); Statement statement = owner.createStatement())
      {
         statement.execute("DO $$ DECLARE t text; BEGIN FOR t IN SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename LIKE '" + PREFIX
            + "%' LOOP EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON %I TO " + RUNTIME + "', t); END LOOP; END $$");
      }
   }



   /*******************************************************************************
    ** Clear the context.
    *******************************************************************************/
   @AfterEach
   void afterEach()
   {
      QContext.clear();
   }



   /*******************************************************************************
    ** The original history check cannot run as the DML-only role and must fail
    ** closed; the catalog check starts it.
    *******************************************************************************/
   @Test
   void testStartupCheckAsRuntimeRole() throws Exception
   {
      CmsQBitConfig history = config().withSchemaVerification(CmsSchemaVerification.CHANGELOG_HISTORY);
      assertThatThrownBy(() -> new CmsQBitProducer().withQBitConfig(history).produce(runtimeInstance()))
         .isInstanceOf(QException.class).hasMessageContaining("not up to date");

      CmsQBitConfig catalog = config().withSchemaVerification(CmsSchemaVerification.CATALOG);
      new CmsQBitProducer().withQBitConfig(catalog).produce(runtimeInstance());
      assertThat(catalog.getRuntime().getSchemaProblems()).isEmpty();
   }



   /*******************************************************************************
    ** The catalog check names a missing table and a missing column, and refuses to
    ** start in FAIL_FAST mode.
    *******************************************************************************/
   @Test
   void testCatalogCheckFindsDrift() throws Exception
   {
      CmsQBitConfig unmigrated = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND).withTableNamePrefix("zcms_").withNamePrefix("zcms")
         .withPermissionBaseName("zcms").withSchemaVerification(CmsSchemaVerification.CATALOG);
      assertThatThrownBy(() -> new CmsQBitProducer().withQBitConfig(unmigrated).produce(runtimeInstance()))
         .isInstanceOf(QException.class).hasMessageContaining("zcms_page is missing");

      try(Connection owner = connect(OWNER, "owner-synthetic", "cmsdb"); Statement statement = owner.createStatement())
      {
         statement.execute("ALTER TABLE " + PREFIX + "page RENAME COLUMN title TO title_old");
         try
         {
            CmsQBitConfig drifted = config().withSchemaVerification(CmsSchemaVerification.CATALOG);
            assertThatThrownBy(() -> new CmsQBitProducer().withQBitConfig(drifted).produce(runtimeInstance()))
               .isInstanceOf(QException.class).hasMessageContaining(PREFIX + "page.title is missing");
         }
         finally
         {
            statement.execute("ALTER TABLE " + PREFIX + "page RENAME COLUMN title_old TO title");
         }
      }
   }



   /*******************************************************************************
    ** The runtime role can do DML and nothing else: no DDL, no migration history.
    *******************************************************************************/
   @Test
   void testRuntimeRoleIsDmlOnly() throws Exception
   {
      try(Connection runtime = connect(RUNTIME, "runtime-synthetic", "cmsdb"); Statement statement = runtime.createStatement())
      {
         assertThatThrownBy(() -> statement.execute("CREATE TABLE intruder (id int)")).isInstanceOf(SQLException.class).hasMessageContaining("permission denied");
         assertThatThrownBy(() -> statement.execute("ALTER TABLE " + PREFIX + "site ADD COLUMN x int")).isInstanceOf(SQLException.class);
         assertThatThrownBy(() -> statement.execute("DROP TABLE " + PREFIX + "site")).isInstanceOf(SQLException.class);
         assertThatThrownBy(() -> statement.execute("TRUNCATE " + PREFIX + "site")).isInstanceOf(SQLException.class);
         assertThatThrownBy(() -> statement.executeQuery("SELECT * FROM databasechangelog")).isInstanceOf(SQLException.class).hasMessageContaining("permission denied");
      }
   }



   /*******************************************************************************
    ** Role ceilings over the DML grants, through QQQ's own actions on the native PG
    ** backend as the runtime role: an admin writes, a viewer reads only, an editor
    ** cannot change reference data, and a user with no CMS permission is refused.
    *******************************************************************************/
   @Test
   void testRoleCeilingsAsRuntimeRole() throws Exception
   {
      CmsQBitConfig config    = config().withSchemaVerification(CmsSchemaVerification.CATALOG);
      QInstance     instance  = runtimeInstance();
      instance.setEnforcePermissionsInCoreActions(true);
      new CmsQBitProducer().withQBitConfig(config).produce(instance);

      CmsTestSupport.session(instance, CmsSecurity.permissionsFor(config, Set.of(CmsRole.ADMIN)));
      Integer siteId = new InsertAction().execute(new InsertInput(PREFIX + "site").withRecord(new QRecord().withValue("name", "Example").withValue("shortName", "ex")))
         .getRecords().get(0).getValueInteger("id");
      assertThat(siteId).isNotNull();

      CmsTestSupport.session(instance, CmsSecurity.permissionsFor(config, Set.of(CmsRole.VIEWER)));
      assertThat(new QueryAction().execute(new QueryInput(PREFIX + "site")).getRecords()).isNotEmpty();
      assertThatThrownBy(() -> new InsertAction().execute(new InsertInput(PREFIX + "site").withRecord(new QRecord().withValue("name", "V").withValue("shortName", "v"))))
         .isInstanceOf(QPermissionDeniedException.class);

      CmsTestSupport.session(instance, CmsSecurity.permissionsFor(config, Set.of(CmsRole.EDITOR)));
      assertThatThrownBy(() -> new InsertAction().execute(new InsertInput(PREFIX + "page_type").withRecord(new QRecord().withValue("name", "X"))))
         .isInstanceOf(QPermissionDeniedException.class);

      CmsTestSupport.session(instance, Set.of());
      assertThatThrownBy(() -> new QueryAction().execute(new QueryInput(PREFIX + "site"))).isInstanceOf(QPermissionDeniedException.class);
      assertThatThrownBy(() -> new QueryAction().execute(new QueryInput(PREFIX + "page"))).isInstanceOf(QPermissionDeniedException.class);
   }



   /*******************************************************************************
    ** The configuration under test: a prefix of its own, never the default.
    *******************************************************************************/
   private static CmsQBitConfig config()
   {
      return (new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND).withTableNamePrefix(PREFIX).withNamePrefix("qcms").withPermissionBaseName("qcms"));
   }



   /*******************************************************************************
    ** An application instance on the runtime role.
    *******************************************************************************/
   private static QInstance runtimeInstance()
   {
      ConnectionManager.resetConnectionProviders();
      return (CmsTestSupport.blankInstance(backend(RUNTIME, "runtime-synthetic")));
   }



   /*******************************************************************************
    ** A native PostgreSQL backend as a named role.
    *******************************************************************************/
   private static PostgreSQLBackendMetaData backend(String user, String password)
   {
      return (new PostgreSQLBackendMetaData()
         .withName(CmsTestSupport.BACKEND)
         .withHostName(POSTGRES.getHost())
         .withPort(POSTGRES.getFirstMappedPort())
         .withDatabaseName("cmsdb")
         .withUsername(user)
         .withPassword(password));
   }



   /*******************************************************************************
    ** A JDBC connection as a named role.
    *******************************************************************************/
   private static Connection connect(String user, String password, String database) throws SQLException
   {
      return (DriverManager.getConnection("jdbc:postgresql://" + POSTGRES.getHost() + ":" + POSTGRES.getFirstMappedPort() + "/" + database, user, password));
   }
}
