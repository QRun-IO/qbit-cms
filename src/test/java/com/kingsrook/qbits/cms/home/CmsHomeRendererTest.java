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

package com.kingsrook.qbits.cms.home;


import java.util.Map;
import java.util.Set;
import com.kingsrook.qbits.cms.CmsQBitConfig;
import com.kingsrook.qbits.cms.CmsQBitProducer;
import com.kingsrook.qbits.cms.CmsStartupMode;
import com.kingsrook.qbits.cms.CmsTestSupport;
import com.kingsrook.qbits.cms.metadata.CmsTableDefinition;
import com.kingsrook.qbits.cms.security.CmsRole;
import com.kingsrook.qbits.cms.security.CmsSecurity;
import com.kingsrook.qqq.backend.core.actions.dashboard.RenderWidgetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.RawHTML;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** The CMS homes render real data read as the user, honest empty states, and
 ** leave out what the user may not read.
 *******************************************************************************/
class CmsHomeRendererTest
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
    ** Empty install: area homes say so honestly; settings show seeded types.
    *******************************************************************************/
   @Test
   void testEmptyInstall() throws Exception
   {
      CmsQBitConfig config    = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND);
      QInstance     qInstance = install(config);
      CmsTestSupport.session(qInstance, CmsSecurity.permissionsFor(config, Set.of(CmsRole.ADMIN)));

      String top = render(qInstance, "cmsHome");
      assertThat(top).contains("Sites", "Pages", "Navigation", "/app/cmsPages", "0 pages");

      String sites = render(qInstance, "cmsSitesHome");
      assertThat(sites).contains("Nothing yet", "Needs attention", "everything here is published and active", "New site", "New redirect");
   }



   /*******************************************************************************
    ** With content: counts, drafts, inactive rows and recently changed records.
    *******************************************************************************/
   @Test
   void testWithContent() throws Exception
   {
      CmsQBitConfig config    = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND);
      QInstance     qInstance = install(config);
      CmsTestSupport.session(qInstance, CmsSecurity.permissionsFor(config, Set.of(CmsRole.ADMIN)));

      Integer siteId = insert("cms_site", new QRecord().withValue("name", "Example").withValue("shortName", "ex"));
      insert("cms_page", new QRecord().withValue("name", "Home").withValue("slug", "home").withValue("title", "Welcome <home>")
         .withValue("pageTypeId", 1).withValue("siteId", siteId).withValue("isPublished", true));
      insert("cms_page", new QRecord().withValue("name", "Draft").withValue("slug", "draft").withValue("title", "Work in progress")
         .withValue("pageTypeId", 1).withValue("siteId", siteId));
      insert("cms_snippet", new QRecord().withValue("name", "Footer").withValue("shortName", "footer").withValue("siteId", siteId).withValue("isActive", false));

      String top = render(qInstance, "cmsHome");
      assertThat(top).contains("2 pages", "1 unpublished", "1 site");

      String pages = render(qInstance, "cmsPagesHome");
      assertThat(pages).contains("Welcome &lt;home&gt;", "Work in progress", "/app/cms_page/", "Unpublished pages", "Inactive snippets", "Recently changed", "New page");
      assertThat(pages).doesNotContain("Welcome <home>");

      CmsTestSupport.session(qInstance, CmsSecurity.permissionsFor(config, Set.of(CmsRole.VIEWER)));
      assertThat(render(qInstance, "cmsPagesHome")).contains("Work in progress").doesNotContain("New page");
   }



   /*******************************************************************************
    ** A user who may read nothing sees that, not zeros; DEGRADED shows the
    ** schema problem.
    *******************************************************************************/
   @Test
   void testNoAccessAndDegraded() throws Exception
   {
      CmsQBitConfig config    = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND);
      QInstance     qInstance = install(config);
      CmsTestSupport.session(qInstance, Set.of());
      assertThat(render(qInstance, "cmsHome")).contains("do not have access");
      assertThat(render(qInstance, "cmsNavigationHome")).contains("do not have access to Navigation");

      RDBMSBackendMetaData backend   = CmsTestSupport.h2Backend();
      QInstance            unmigrated = CmsTestSupport.blankInstance(backend);
      CmsQBitConfig        degraded   = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND).withStartupMode(CmsStartupMode.DEGRADED);
      new CmsQBitProducer().withQBitConfig(degraded).produce(unmigrated);
      CmsTestSupport.session(unmigrated, Set.of());
      assertThat(render(unmigrated, "cmsHome")).contains("not up to date");

      QWidgetMetaData orphan = new QWidgetMetaData().withName("x").withDefaultValue(CmsHomeRenderer.KEY_NAME_PREFIX, "nope");
      assertThatThrownBy(() -> new CmsHomeRenderer().render(new RenderWidgetInput().withWidgetMetaData(orphan))).hasMessageContaining("No CMS install");
   }



   /*******************************************************************************
    ** Pluralizing labels.
    *******************************************************************************/
   @Test
   void testPlural()
   {
      assertThat(CmsHomeRenderer.plural(1, "Page")).isEqualTo("1 page");
      assertThat(CmsHomeRenderer.plural(2, "Page Type")).isEqualTo("2 page types");
      assertThat(CmsTableDefinition.pluralize("Category")).isEqualTo("Categories");
      assertThat(CmsTableDefinition.pluralize("Key")).isEqualTo("Keys");
      assertThat(CmsHtml.escape(null)).isEmpty();
      assertThat(CmsHtml.escape("<a href='x'>&\"")).isEqualTo("&lt;a href=&#39;x&#39;&gt;&amp;&quot;");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QInstance install(CmsQBitConfig config) throws Exception
   {
      RDBMSBackendMetaData backend = CmsTestSupport.h2Backend();
      CmsTestSupport.migrate(backend, Map.of());
      QInstance qInstance = CmsTestSupport.blankInstance(backend);
      qInstance.setEnforcePermissionsInCoreActions(true);
      new CmsQBitProducer().withQBitConfig(config).produce(qInstance);
      return (qInstance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String render(QInstance qInstance, String widgetName) throws QException
   {
      RawHTML html = (RawHTML) new RenderWidgetAction().execute(new RenderWidgetInput().withWidgetMetaData(qInstance.getWidget(widgetName))).getWidgetData();
      return (html.getHtml());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Integer insert(String table, QRecord record) throws QException
   {
      return (new InsertAction().execute(new InsertInput(table).withRecord(record)).getRecords().get(0).getValueInteger("id"));
   }
}
