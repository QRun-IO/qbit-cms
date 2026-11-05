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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qbits.cms.security.CmsRole;
import com.kingsrook.qbits.cms.security.CmsSecurity;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.module.postgres.model.metadata.PostgreSQLBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** The install on PostgreSQL (the production database): the changelog creates
 ** the kof22-shaped schema (identity ids, the same column types and
 ** nullability), the schema check passes, and QQQ reads and writes it.
 ** Skipped when Docker is not available.
 *******************************************************************************/
@Testcontainers(disabledWithoutDocker = true)
class CmsPostgresInstallTest
{
   @Container
   private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void afterEach()
   {
      QContext.clear();
   }



   /*******************************************************************************
    ** Fresh install on PostgreSQL with the default prefix.
    *******************************************************************************/
   @Test
   void testInstallOnPostgres() throws Exception
   {
      ConnectionManager.resetConnectionProviders();
      PostgreSQLBackendMetaData backend = new PostgreSQLBackendMetaData()
         .withName(CmsTestSupport.BACKEND)
         .withHostName(POSTGRES.getHost())
         .withPort(POSTGRES.getFirstMappedPort())
         .withDatabaseName(POSTGRES.getDatabaseName())
         .withUsername(POSTGRES.getUsername())
         .withPassword(POSTGRES.getPassword());
      CmsTestSupport.migrate(backend, Map.of());

      QInstance     qInstance = CmsTestSupport.blankInstance(backend);
      CmsQBitConfig config    = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND);
      new CmsQBitProducer().withQBitConfig(config).produce(qInstance);
      assertThat(config.getRuntime().getSchemaProblems()).isEmpty();

      try(Connection connection = ConnectionManager.getConnection(backend); Statement statement = connection.createStatement())
      {
         assertThat(columns(statement, "cms_page")).contains(
            "id bigint NO", "slug character varying(100) NO", "title character varying(200) NO", "content text YES", "is_published boolean NO",
            "publish_date timestamp without time zone YES", "parent_page_id bigint YES", "create_date timestamp without time zone NO");
         try(ResultSet resultSet = statement.executeQuery("SELECT pg_get_serial_sequence('cms_page', 'id'), "
            + "(SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_name = 'cms_page' AND constraint_type = 'FOREIGN KEY')"))
         {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getString(1)).isEqualTo("public.cms_page_id_seq");
            assertThat(resultSet.getInt(2)).isEqualTo(3);
         }
      }

      CmsTestSupport.session(qInstance, CmsSecurity.permissionsFor(config, Set.of(CmsRole.ADMIN)));
      Integer siteId = new InsertAction().execute(new InsertInput("cms_site").withRecord(new QRecord().withValue("name", "Example").withValue("shortName", "ex")))
         .getRecords().get(0).getValueInteger("id");
      Integer pageId = new InsertAction().execute(new InsertInput("cms_page").withRecord(new QRecord().withValue("name", "Home").withValue("slug", "home")
         .withValue("title", "Home").withValue("pageTypeId", 1).withValue("siteId", siteId)
         .withAssociatedRecord("sections", new QRecord().withValue("name", "Hero").withValue("sectionTypeId", 2).withValue("content", "{}")))).getRecords().get(0).getValueInteger("id");
      QRecord page = new GetAction().execute(new GetInput("cms_page").withPrimaryKey(pageId).withIncludeAssociations(true)).getRecord();
      assertThat(page.getAssociatedRecords().get("sections")).hasSize(1);
      assertThat(page.getValue("createDate")).isNotNull();
   }



   /*******************************************************************************
    ** "name type nullable" for each column of a table.
    *******************************************************************************/
   private static List<String> columns(Statement statement, String table) throws Exception
   {
      List<String> rs = new ArrayList<>();
      try(ResultSet resultSet = statement.executeQuery("SELECT column_name, data_type, character_maximum_length, is_nullable FROM information_schema.columns "
         + "WHERE table_name = '" + table + "' ORDER BY ordinal_position"))
      {
         while(resultSet.next())
         {
            Object length = resultSet.getObject(3);
            rs.add(resultSet.getString(1) + " " + resultSet.getString(2) + (length == null ? "" : "(" + length + ")") + " " + resultSet.getString(4));
         }
      }
      return (rs);
   }
}
