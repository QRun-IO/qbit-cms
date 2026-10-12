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
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qbits.cms.security.CmsRole;
import com.kingsrook.qbits.cms.security.CmsSecurity;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** Installing the qbit into a blank QQQ instance on H2: the host runs the
 ** changelog, produces the qbit (FAIL_FAST schema check), and the CMS tables
 ** work as ordinary QQQ tables.
 *******************************************************************************/
class CmsInstallTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void afterEach()
   {
      QContext.clear();
   }



   /*******************************************************************************
    ** Fresh install: schema check passes, generic reference data is seeded,
    ** and content round-trips, including children through associations.
    *******************************************************************************/
   @Test
   void testFreshInstall() throws Exception
   {
      RDBMSBackendMetaData backend = CmsTestSupport.h2Backend();
      CmsTestSupport.migrate(backend, Map.of());

      QInstance     qInstance = CmsTestSupport.blankInstance(backend);
      CmsQBitConfig config    = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND);
      new CmsQBitProducer().withQBitConfig(config).produce(qInstance);
      new QInstanceValidator().validate(qInstance);
      assertThat(config.getRuntime().getSchemaProblems()).isEmpty();

      CmsTestSupport.session(qInstance, CmsSecurity.permissionsFor(config, Set.of(CmsRole.ADMIN)));
      assertThat(count("cms_redirect_type")).isEqualTo(4);
      assertThat(count("cms_page_type")).isEqualTo(4);
      assertThat(count("cms_page_section_type")).isEqualTo(9);

      Integer siteId = insert("cms_site", new QRecord().withValue("name", "Example").withValue("shortName", "example"));
      insert("cms_domain", new QRecord().withValue("name", "www").withValue("shortName", "www").withValue("fqdn", "www.example.com")
         .withValue("isPrimary", true).withValue("siteId", siteId));
      Integer pageTypeId    = idOf("cms_page_type", "code", "STATIC");
      Integer sectionTypeId = idOf("cms_page_section_type", "code", "MARKDOWN");

      QRecord page = new QRecord().withValue("name", "About").withValue("slug", "about").withValue("title", "About us")
         .withValue("pageTypeId", pageTypeId).withValue("siteId", siteId).withValue("content", "x".repeat(5000))
         .withAssociatedRecord("sections", new QRecord().withValue("name", "Intro").withValue("sectionTypeId", sectionTypeId).withValue("sortOrder", 1)
            .withValue("content", "{\"content\": \"# Hello\"}"))
         .withAssociatedRecord("sections", new QRecord().withValue("name", "Body").withValue("sectionTypeId", sectionTypeId).withValue("sortOrder", 2));
      Integer pageId = insert("cms_page", page);

      QRecord stored = new GetAction().execute(new GetInput("cms_page").withPrimaryKey(pageId).withIncludeAssociations(true)).getRecord();
      assertThat(stored.getValueString("content")).hasSize(5000);
      assertThat(stored.getValueBoolean("isPublished")).isFalse();
      assertThat(stored.getValue("createDate")).isNotNull();
      assertThat(stored.getAssociatedRecords().get("sections")).hasSize(2);

      Integer menuId = insert("cms_menu", new QRecord().withValue("name", "Header").withValue("slug", "header").withValue("menuType", "HEADER").withValue("siteId", siteId)
         .withAssociatedRecord("items", new QRecord().withValue("name", "About").withValue("slug", "about").withValue("itemType", "PAGE").withValue("routePath", "/about")));
      assertThat(count("cms_menu_item")).isEqualTo(1);
      assertThat(menuId).isNotNull();
   }



   /*******************************************************************************
    ** A host can choose any prefix; the changelog property and the config agree.
    *******************************************************************************/
   @Test
   void testCustomPrefix() throws Exception
   {
      RDBMSBackendMetaData backend = CmsTestSupport.h2Backend();
      CmsTestSupport.migrate(backend, Map.of("cms.tablePrefix", "acme_cms_"));

      QInstance     qInstance = CmsTestSupport.blankInstance(backend);
      CmsQBitConfig config    = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND).withTableNamePrefix("acme_cms_");
      new CmsQBitProducer().withQBitConfig(config).produce(qInstance);
      assertThat(qInstance.getTable("acme_cms_page")).isNotNull();
      assertThat(qInstance.getTable("cms_page")).isNull();

      CmsTestSupport.session(qInstance, CmsSecurity.permissionsFor(config, Set.of(CmsRole.ADMIN)));
      assertThat(count("acme_cms_redirect_type")).isEqualTo(4);
   }



   /*******************************************************************************
    ** Running the changelog again changes nothing (the guard runs every time
    ** and passes for tables this changelog created).
    *******************************************************************************/
   @Test
   void testRerunIsNoop() throws Exception
   {
      RDBMSBackendMetaData backend = CmsTestSupport.h2Backend();
      CmsTestSupport.migrate(backend, Map.of());
      CmsTestSupport.migrate(backend, Map.of());

      QInstance     qInstance = CmsTestSupport.blankInstance(backend);
      CmsQBitConfig config    = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND);
      new CmsQBitProducer().withQBitConfig(config).produce(qInstance);
      CmsTestSupport.session(qInstance, CmsSecurity.permissionsFor(config, Set.of(CmsRole.ADMIN)));
      assertThat(count("cms_redirect_type")).isEqualTo(4);
   }



   /*******************************************************************************
    ** An existing table with a qbit name halts the install; nothing is
    ** overwritten.
    *******************************************************************************/
   @Test
   void testExistingTableHalts() throws Exception
   {
      RDBMSBackendMetaData backend = CmsTestSupport.h2Backend();
      try(Connection connection = ConnectionManager.getConnection(backend); Statement statement = connection.createStatement())
      {
         statement.execute("CREATE TABLE cms_page (id INT PRIMARY KEY, title VARCHAR(10))");
         statement.execute("INSERT INTO cms_page VALUES (1, 'theirs')");
      }
      assertThatThrownBy(() -> CmsTestSupport.migrate(backend, Map.of())).hasStackTraceContaining("was not created by qbit-cms");

      try(Connection connection = ConnectionManager.getConnection(backend); Statement statement = connection.createStatement())
      {
         var resultSet = statement.executeQuery("SELECT title FROM cms_page WHERE id = 1");
         assertThat(resultSet.next()).isTrue();
         assertThat(resultSet.getString(1)).isEqualTo("theirs");
      }
   }



   /*******************************************************************************
    ** Reference data is optional.
    *******************************************************************************/
   @Test
   void testSeedOff() throws Exception
   {
      RDBMSBackendMetaData backend = CmsTestSupport.h2Backend();
      CmsTestSupport.migrate(backend, Map.of("cms.seedReferenceData", "false"));

      QInstance     qInstance = CmsTestSupport.blankInstance(backend);
      CmsQBitConfig config    = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND);
      new CmsQBitProducer().withQBitConfig(config).produce(qInstance);
      CmsTestSupport.session(qInstance, CmsSecurity.permissionsFor(config, Set.of(CmsRole.ADMIN)));
      assertThat(count("cms_redirect_type")).isZero();
   }



   /*******************************************************************************
    ** Without the changelog, FAIL_FAST refuses to start and names the problem;
    ** DEGRADED starts and records it.
    *******************************************************************************/
   @Test
   void testSchemaCheck() throws Exception
   {
      RDBMSBackendMetaData backend = CmsTestSupport.h2Backend();

      CmsQBitConfig failFast = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND);
      assertThatThrownBy(() -> new CmsQBitProducer().withQBitConfig(failFast).produce(CmsTestSupport.blankInstance(backend)))
         .isInstanceOf(QException.class)
         .hasMessageContaining("not up to date")
         .hasMessageContaining("DATABASECHANGELOG");

      CmsQBitConfig degraded = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND).withStartupMode(CmsStartupMode.DEGRADED);
      new CmsQBitProducer().withQBitConfig(degraded).produce(CmsTestSupport.blankInstance(backend));
      assertThat(degraded.getRuntime().getSchemaProblems()).isNotEmpty();

      ////////////////////////////////////////////////////////////////
      // changelog run without CORE (cms.features): tables missing  //
      ////////////////////////////////////////////////////////////////
      CmsTestSupport.migrate(backend, Map.of("cms.features", "MEDIA"));
      CmsQBitConfig partial = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND).withStartupMode(CmsStartupMode.DEGRADED);
      new CmsQBitProducer().withQBitConfig(partial).produce(CmsTestSupport.blankInstance(backend));
      assertThat(partial.getRuntime().getSchemaProblems()).anyMatch(p -> p.contains("1.0.0-create-page") && p.contains("CORE"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Integer insert(String table, QRecord record) throws QException
   {
      List<QRecord> records = new InsertAction().execute(new InsertInput(table).withRecord(record)).getRecords();
      assertThat(records.get(0).getErrors()).as(table).isEmpty();
      return (records.get(0).getValueInteger("id"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static int count(String table) throws QException
   {
      return (new CountAction().execute(new CountInput(table)).getCount());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Integer idOf(String table, String field, String value) throws QException
   {
      List<QRecord> records = new QueryAction().execute(new QueryInput(table).withFilter(new QQueryFilter(new QFilterCriteria(field, QCriteriaOperator.EQUALS, value)))).getRecords();
      return (records.get(0).getValueInteger("id"));
   }
}
