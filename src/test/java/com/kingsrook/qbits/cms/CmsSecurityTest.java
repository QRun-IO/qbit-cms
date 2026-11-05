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


import java.util.List;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qbits.cms.security.CmsRole;
import com.kingsrook.qbits.cms.security.CmsSecurity;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QPermissionDeniedException;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** Roles, permissions and their enforcement (spec section 6) with QQQ core
 ** permission enforcement on, as QQQ 4.2 will default.
 *******************************************************************************/
class CmsSecurityTest
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
    ** Role bundles.
    *******************************************************************************/
   @Test
   void testRolePermissions()
   {
      CmsQBitConfig config = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND)
         .withRole("cms-admins", CmsRole.ADMIN).withRole("writers", CmsRole.EDITOR).withRole("readers", CmsRole.VIEWER);

      assertThat(CmsSecurity.rolesFor(config, List.of("writers", "unknown"))).containsExactly(CmsRole.EDITOR);
      assertThat(CmsSecurity.rolesFor(config, null)).isEmpty();

      Set<String> admin = CmsSecurity.permissionsFor(config, Set.of(CmsRole.ADMIN));
      assertThat(admin).contains("cms_page.read", "cms_page.delete", "cms_page_type.edit", "cms.admin", "cms.publish");

      Set<String> editor = CmsSecurity.permissionsFor(config, Set.of(CmsRole.EDITOR));
      assertThat(editor).contains("cms_page.insert", "cms_page.edit", "cms_page.delete", "cms_page_type.read")
         .doesNotContain("cms_page_type.edit", "cms.publish", "cms.admin");

      assertThat(CmsSecurity.permissionsFor(config, Set.of(CmsRole.PUBLISHER))).contains("cms.publish").doesNotContain("cms.admin");

      Set<String> drafter = CmsSecurity.permissionsFor(config, Set.of(CmsRole.DRAFTER));
      assertThat(drafter).contains("cms_page.insert", "cms_page.edit").doesNotContain("cms_page.delete", "cms.publish");

      Set<String> viewer = CmsSecurity.permissionsFor(config, Set.of(CmsRole.VIEWER));
      assertThat(viewer).allMatch(p -> p.endsWith(".read")).contains("cms_menu_item.read");

      QSession session = new QSession().withPermission("host.thing");
      CmsSecurity.applySessionPermissions(session, List.of("readers"), config);
      assertThat(session.getPermissions()).contains("host.thing", "cms_page.read").doesNotContain("cms_page.insert");
   }



   /*******************************************************************************
    ** Enforced: a viewer reads but cannot write; an editor writes content but
    ** not reference data; a session without CMS permissions cannot read.
    *******************************************************************************/
   @Test
   void testEnforcement() throws Exception
   {
      CmsQBitConfig config    = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND);
      QInstance     qInstance = install(config);

      CmsTestSupport.session(qInstance, CmsSecurity.permissionsFor(config, Set.of(CmsRole.VIEWER)));
      assertThat(new QueryAction().execute(new QueryInput("cms_redirect_type")).getRecords()).hasSize(4);
      assertThatThrownBy(() -> insertSite("viewer")).isInstanceOf(QPermissionDeniedException.class);

      CmsTestSupport.session(qInstance, CmsSecurity.permissionsFor(config, Set.of(CmsRole.EDITOR)));
      Integer siteId = insertSite("editor");
      assertThat(siteId).isNotNull();
      assertThatThrownBy(() -> new UpdateAction().execute(new UpdateInput("cms_page_type").withRecord(new QRecord().withValue("id", 1).withValue("name", "X"))))
         .isInstanceOf(QPermissionDeniedException.class);

      CmsTestSupport.session(qInstance, CmsSecurity.permissionsFor(config, Set.of(CmsRole.DRAFTER)));
      assertThatThrownBy(() -> new DeleteAction().execute(new DeleteInput("cms_site").withPrimaryKey(siteId))).isInstanceOf(QPermissionDeniedException.class);

      CmsTestSupport.session(qInstance, Set.of());
      assertThatThrownBy(() -> new QueryAction().execute(new QueryInput("cms_page"))).isInstanceOf(QPermissionDeniedException.class);
   }



   /*******************************************************************************
    ** openReads: anyone reads, writes still need the table permission.
    *******************************************************************************/
   @Test
   void testOpenReads() throws Exception
   {
      CmsQBitConfig config    = new CmsQBitConfig().withBackendName(CmsTestSupport.BACKEND).withOpenReads(true);
      QInstance     qInstance = install(config);

      CmsTestSupport.session(qInstance, Set.of());
      assertThat(new QueryAction().execute(new QueryInput("cms_redirect_type")).getRecords()).hasSize(4);
      assertThatThrownBy(() -> insertSite("anonymous")).isInstanceOf(QPermissionDeniedException.class);

      CmsTestSupport.session(qInstance, Set.of("cms_site.insert"));
      assertThat(insertSite("writer")).isNotNull();
      assertThatThrownBy(() -> new DeleteAction().execute(new DeleteInput("cms_site").withPrimaryKey(1))).isInstanceOf(QPermissionDeniedException.class);
   }



   /*******************************************************************************
    ** A migrated, enforced install.
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
   private static Integer insertSite(String name) throws Exception
   {
      return (new InsertAction().execute(new InsertInput("cms_site").withRecord(new QRecord().withValue("name", name).withValue("shortName", name)))
         .getRecords().get(0).getValueInteger("id"));
   }
}
