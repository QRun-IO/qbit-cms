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


import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppSection;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.PermissionLevel;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryBackendModule;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSTableBackendDetails;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** CmsQBitProducer: what it produces, validation, namespaces and collisions
 ** (no database: startupMode SKIP).
 *******************************************************************************/
class CmsQBitProducerTest
{
   private static final List<String> CORE_TABLES = List.of("cms_site", "cms_domain", "cms_redirect", "cms_redirect_type", "cms_page", "cms_page_type",
      "cms_page_section", "cms_page_section_type", "cms_snippet", "cms_menu", "cms_menu_item");



   /*******************************************************************************
    ** The CORE feature: eleven standard tables named per the owner rule, a
    ** possible-value source each, child lists, apps with homes, and a valid
    ** instance.
    *******************************************************************************/
   @Test
   void testProduceCore() throws Exception
   {
      QInstance     qInstance = CmsTestSupport.blankInstance(CmsTestSupport.h2Backend());
      CmsQBitConfig config    = config();
      CmsQBitProducer producer = new CmsQBitProducer().withQBitConfig(config);
      assertThat(producer.isEnabled()).isTrue();
      assertThat(producer.getQBitConfig()).isSameAs(config);
      assertThat(producer.produce(qInstance).getEach(QTableMetaData.class)).isEmpty();
      new QInstanceValidator().validate(qInstance);

      assertThat(qInstance.getQBits()).hasSize(1);
      assertThat(qInstance.getQBits().values().iterator().next().getArtifactId()).isEqualTo("qbit-cms");
      for(String name : CORE_TABLES)
      {
         QTableMetaData table = qInstance.getTable(name);
         assertThat(table).as(name).isNotNull();
         assertThat(name).matches("^cms_[a-z][a-z_]*$");
         assertThat(((RDBMSTableBackendDetails) table.getBackendDetails()).getTableName()).isEqualTo(name);
         assertThat(table.getBackendName()).isEqualTo(CmsTestSupport.BACKEND);
         assertThat(table.getPermissionRules().getLevel()).isEqualTo(PermissionLevel.READ_INSERT_EDIT_DELETE_PERMISSIONS);
         assertThat(table.getIsHidden()).as(name + " is visible").isFalse();
         assertThat(table.getField("createDate").getBackendName()).isEqualTo("create_date");
         assertThat(qInstance.getPossibleValueSource(name)).as(name + " PVS").isNotNull();
      }
      assertThat(qInstance.getTable("cms_page").getField("pageTypeId").getBackendName()).isEqualTo("page_type_id");
      assertThat(qInstance.getTable("cms_page").getField("siteId").getPossibleValueSourceName()).isEqualTo("cms_site");
      assertThat(qInstance.getTable("cms_redirect").getField("matchType").getPossibleValueSourceName()).isEqualTo("cms_redirect_match_type");
      assertThat(qInstance.getTable("cms_page").getAssociations()).extracting("name").contains("sections");
      assertThat(qInstance.getTable("cms_menu").getAssociations()).extracting("name").contains("items");

      QAppMetaData top = qInstance.getApp("cms");
      assertThat(top.getLabel()).isEqualTo("CMS");
      assertThat(top.getWidgets()).containsExactly("cmsHome");
      assertThat(top.getChildren()).extracting("name").containsExactly("cmsSites", "cmsPages", "cmsNavigation");
      assertThat(qInstance.getApp("cmsPages").getWidgets()).containsExactly("cmsPagesHome");
      assertThat(qInstance.getWidget("cmsPagesHome")).isNotNull();

      /////////////////////////////////////////////////////////////////////
      // the nav lists main and reference tables only, never a child     //
      // table, and no processes                                         //
      /////////////////////////////////////////////////////////////////////
      List<String> navTables = qInstance.getApps().values().stream().filter(a -> a.getSections() != null)
         .flatMap(a -> a.getSections().stream()).flatMap(s -> s.getTables() == null ? java.util.stream.Stream.empty() : s.getTables().stream()).toList();
      assertThat(navTables).containsExactlyInAnyOrder("cms_site", "cms_redirect", "cms_redirect_type", "cms_page", "cms_snippet", "cms_page_type",
         "cms_page_section_type", "cms_menu");
      for(QAppMetaData app : qInstance.getApps().values())
      {
         for(QAppSection section : app.getSections() == null ? List.<QAppSection>of() : app.getSections())
         {
            assertThat(section.getProcesses()).as(app.getName()).isNullOrEmpty();
         }
      }
      assertThat(qInstance.getTable("cms_page").getSections()).extracting("name").contains("cms_page_cms_page_section_pageId");
   }



   /*******************************************************************************
    ** Validation lists every problem.
    *******************************************************************************/
   @Test
   void testValidation()
   {
      assertThat(new CmsQBitProducer().isEnabled()).isFalse();
      assertThatThrownBy(() -> new CmsQBitProducer().produce(new QInstance())).hasMessageContaining("no config");

      QInstance qInstance = CmsTestSupport.blankInstance(CmsTestSupport.h2Backend());
      qInstance.addBackend(new QBackendMetaData().withName("memory").withBackendType(MemoryBackendModule.class));
      assertThatThrownBy(() -> produce(qInstance, new CmsQBitConfig())).hasMessageContaining("backendName is required");
      assertThatThrownBy(() -> produce(qInstance, config().withBackendName("nope"))).hasMessageContaining("not a backend");
      assertThatThrownBy(() -> produce(qInstance, config().withBackendName("memory"))).hasMessageContaining("must be an RDBMS backend");
      assertThatThrownBy(() -> produce(qInstance, config().withTableNamePrefix("CMS"))).hasMessageContaining("tableNamePrefix");
      assertThatThrownBy(() -> produce(qInstance, config().withNamePrefix("9x"))).hasMessageContaining("namePrefix");
      assertThatThrownBy(() -> produce(qInstance, config().withPermissionBaseName(""))).hasMessageContaining("permissionBaseName");
      assertThatThrownBy(() -> produce(qInstance, config().withParentAppName("content"))).hasMessageContaining("parentAppName content");
      assertThatThrownBy(() -> produce(qInstance, config().withFeatures(EnumSet.of(CmsFeature.CORE, CmsFeature.MEDIA)))).hasMessageContaining("MEDIA is not available");
      assertThatThrownBy(() -> produce(qInstance, config().withFeatures(Set.of()))).hasMessageContaining("must include CORE");
      assertThatThrownBy(() -> produce(qInstance, config().withRoleMapping(null))).hasMessageContaining("roleMapping");
      assertThatThrownBy(() -> produce(qInstance, config().withStartupMode(null))).hasMessageContaining("startupMode");
      assertThat(qInstance.getQBits()).isNullOrEmpty();
   }



   /*******************************************************************************
    ** A second install needs a namespace, which becomes its prefixes.
    *******************************************************************************/
   @Test
   void testNamespaceAndCollisions() throws Exception
   {
      QInstance qInstance = CmsTestSupport.blankInstance(CmsTestSupport.h2Backend());
      produce(qInstance, config());

      assertThatThrownBy(() -> produce(qInstance, config()))
         .isInstanceOf(QException.class)
         .hasMessageContaining("collide")
         .hasMessageContaining("table cms_site");

      CmsQBitConfig second = config();
      new CmsQBitProducer().withQBitConfig(second).withNamespace("siteTwo").produce(qInstance);
      assertThat(second.getTableNamePrefix()).isEqualTo("site_two_");
      assertThat(second.getNamePrefix()).isEqualTo("siteTwo");
      assertThat(second.getPermissionBaseName()).isEqualTo("site_two");
      assertThat(qInstance.getTable("site_two_page")).isNotNull();
      assertThat(qInstance.getApp("siteTwo")).isNotNull();
      assertThat(qInstance.getApp("siteTwoPages")).isNotNull();
      assertThat(qInstance.getQBits()).hasSize(2);
      assertThat(CmsRuntime.findConfig(qInstance, "siteTwo")).containsSame(second);
      new QInstanceValidator().validate(qInstance);
   }



   /*******************************************************************************
    ** The host can nest the CMS under one of its apps.
    *******************************************************************************/
   @Test
   void testParentApp() throws Exception
   {
      QInstance qInstance = CmsTestSupport.blankInstance(CmsTestSupport.h2Backend());
      qInstance.addApp(new QAppMetaData().withName("content").withLabel("Content"));
      produce(qInstance, config().withParentAppName("content").withAppName("cmsApp").withAppLabel("Website"));
      assertThat(qInstance.getApp("content").getChildren()).extracting("name").containsExactly("cmsApp");
      assertThat(qInstance.getApp("cmsApp").getLabel()).isEqualTo("Website");
      new QInstanceValidator().validate(qInstance);
   }



   /*******************************************************************************
    ** With openReads, tables keep their rule names and get the open-reads
    ** checker.
    *******************************************************************************/
   @Test
   void testOpenReadsRules() throws Exception
   {
      QInstance qInstance = CmsTestSupport.blankInstance(CmsTestSupport.h2Backend());
      produce(qInstance, config().withOpenReads(true));
      assertThat(qInstance.getTable("cms_page").getPermissionRules().getCustomPermissionChecker()).isNotNull();
   }



   /*******************************************************************************
    ** Naming constants follow the owner rule.
    *******************************************************************************/
   @Test
   void testNaming()
   {
      CmsQBitConfig config = config();
      assertThat(CmsQBitConfig.DEFAULT_TABLE_NAME_PREFIX).isEqualTo("cms_").matches("^[a-z][a-z0-9_]*_$");
      assertThat(config.applyPrefix("page")).isEqualTo("cms_page");
      assertThat(config.name("Pages")).isEqualTo("cmsPages");
      assertThat(config.permission("admin")).isEqualTo("cms.admin");
      assertThat(config.getEffectiveAppName()).isEqualTo("cms");
      assertThat(config.toString()).contains("tableNamePrefix=cms_");
      assertThat(config.getDefaultBackendNameForTables()).isEqualTo(CmsTestSupport.BACKEND);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static CmsQBitConfig config()
   {
      return (new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND).withStartupMode(CmsStartupMode.SKIP));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void produce(QInstance qInstance, CmsQBitConfig config) throws QException
   {
      new CmsQBitProducer().withQBitConfig(config).produce(qInstance);
   }
}
