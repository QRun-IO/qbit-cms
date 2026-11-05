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


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qbits.cms.security.CmsRole;
import com.kingsrook.qbits.cms.security.CmsSecurity;
import com.kingsrook.qqq.backend.core.actions.dashboard.RenderWidgetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Child-record lists on parent records render and serialize (the record
 ** screens' Sections, Child Pages, Domains and Items lists).
 *******************************************************************************/
class CmsChildListTest
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
    ** Every child list renders to JSON for a real parent record.
    *******************************************************************************/
   @Test
   void testChildListsSerialize() throws Exception
   {
      RDBMSBackendMetaData backend = CmsTestSupport.h2Backend();
      CmsTestSupport.migrate(backend, Map.of());
      QInstance     qInstance = CmsTestSupport.blankInstance(backend);
      CmsQBitConfig config    = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND);
      new CmsQBitProducer().withQBitConfig(config).produce(qInstance);
      CmsTestSupport.session(qInstance, CmsSecurity.permissionsFor(config, Set.of(CmsRole.ADMIN)));

      Integer siteId = new InsertAction().execute(new InsertInput("cms_site").withRecord(new QRecord().withValue("name", "S").withValue("shortName", "s")))
         .getRecords().get(0).getValueInteger("id");
      new InsertAction().execute(new InsertInput("cms_page").withRecord(new QRecord().withValue("name", "P").withValue("slug", "p").withValue("title", "P")
         .withValue("pageTypeId", 1).withValue("siteId", siteId)
         .withAssociatedRecord("sections", new QRecord().withValue("name", "A").withValue("sectionTypeId", 1))));
      new InsertAction().execute(new InsertInput("cms_menu").withRecord(new QRecord().withValue("name", "M").withValue("slug", "m").withValue("menuType", "HEADER")
         .withValue("siteId", siteId).withAssociatedRecord("items", new QRecord().withValue("name", "I").withValue("slug", "i").withValue("itemType", "LINK"))));

      for(String widget : List.of("cms_page_cms_page_section_pageId", "cms_page_cms_page_parentPageId", "cms_site_cms_domain_siteId",
         "cms_menu_cms_menu_item_menuId", "cms_menu_item_cms_menu_item_parentId"))
      {
         RenderWidgetOutput output = new RenderWidgetAction().execute(new RenderWidgetInput().withWidgetMetaData(qInstance.getWidget(widget))
            .withUrlParams(new HashMap<>(Map.of("id", "1"))));
         assertThat(JsonUtils.toJson(output.getWidgetData())).as(widget).isNotEmpty();
      }
   }
}
