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
import java.util.Optional;
import com.kingsrook.qbits.cms.core.model.CmsDomain;
import com.kingsrook.qbits.cms.core.model.CmsMenu;
import com.kingsrook.qbits.cms.core.model.CmsMenuItem;
import com.kingsrook.qbits.cms.core.model.CmsPage;
import com.kingsrook.qbits.cms.core.model.CmsPageSection;
import com.kingsrook.qbits.cms.core.model.CmsPageSectionType;
import com.kingsrook.qbits.cms.core.model.CmsPageType;
import com.kingsrook.qbits.cms.core.model.CmsRedirect;
import com.kingsrook.qbits.cms.core.model.CmsRedirectType;
import com.kingsrook.qbits.cms.core.model.CmsSite;
import com.kingsrook.qbits.cms.core.model.CmsSnippet;
import com.kingsrook.qbits.cms.metadata.CmsTableDefinition;
import com.kingsrook.qbits.cms.metadata.CmsTableDefinition.Kind;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;


/*******************************************************************************
 ** The CMS tables: logical names (the physical name is the configured prefix
 ** plus the logical name, CmsQBitConfig.applyPrefix) and their definitions.
 ** Java code names CMS tables only through these constants and the config, so
 ** a host can choose any prefix (spec section 2.4).
 *******************************************************************************/
public final class CmsTables
{
   public static final String SITE              = CmsSite.TABLE_NAME;
   public static final String DOMAIN            = CmsDomain.TABLE_NAME;
   public static final String REDIRECT          = CmsRedirect.TABLE_NAME;
   public static final String REDIRECT_TYPE     = CmsRedirectType.TABLE_NAME;
   public static final String PAGE              = CmsPage.TABLE_NAME;
   public static final String PAGE_TYPE         = CmsPageType.TABLE_NAME;
   public static final String PAGE_SECTION      = CmsPageSection.TABLE_NAME;
   public static final String PAGE_SECTION_TYPE = CmsPageSectionType.TABLE_NAME;
   public static final String SNIPPET           = CmsSnippet.TABLE_NAME;
   public static final String MENU              = CmsMenu.TABLE_NAME;
   public static final String MENU_ITEM         = CmsMenuItem.TABLE_NAME;

   private static final List<CmsTableDefinition> DEFINITIONS = List.of(
      CmsTableDefinition.builder(SITE, CmsSite.class, CmsFeature.CORE, "Site", "language", Kind.MAIN)
         .recordLabel("name")
         .section("site", "Site", "language", Tier.T1, "id", "name", "shortName", "description")
         .child(DOMAIN, "siteId", "Domains", null, "name")
         .unique("name")
         .build(),
      CmsTableDefinition.builder(DOMAIN, CmsDomain.class, CmsFeature.CORE, "Domain", "dns", Kind.CHILD)
         .recordLabel("fqdn")
         .section("domain", "Domain", "dns", Tier.T1, "id", "fqdn", "siteId", "isPrimary", "name", "shortName", "description")
         .unique("shortName").unique("name").unique("fqdn")
         .build(),
      CmsTableDefinition.builder(REDIRECT, CmsRedirect.class, CmsFeature.CORE, "Redirect", "open_in_new", Kind.MAIN)
         .section("redirect", "Redirect", "open_in_new", Tier.T1, "id", "name", "sourcePath", "targetPath", "redirectTypeId", "siteId", "description")
         .section("matching", "Matching", "tune", Tier.T2, "matchType", "priority", "preserveQueryParams", "isActive", "expiryDate")
         .section("usage", "Usage", "query_stats", Tier.T2, "hitCount", "lastHitDate")
         .build(),
      CmsTableDefinition.builder(REDIRECT_TYPE, CmsRedirectType.class, CmsFeature.CORE, "Redirect Type", "alt_route", Kind.REFERENCE)
         .section("redirectType", "Redirect Type", "alt_route", Tier.T1, "id", "name", "code", "httpStatusCode", "isActive", "sortOrder", "description")
         .unique("code")
         .build(),
      CmsTableDefinition.builder(PAGE, CmsPage.class, CmsFeature.CORE, "Page", "description", Kind.MAIN)
         .recordLabel("title")
         .section("page", "Page", "description", Tier.T1, "id", "title", "name", "slug", "siteId", "pageTypeId", "template", "isPublished", "publishDate")
         .section("content", "Content", "notes", Tier.T2, "content")
         .section("structure", "Structure", "account_tree", Tier.T2, "parentPageId", "sortOrder")
         .section("seo", "SEO", "travel_explore", Tier.T2, "metaDescription", "metaKeywords", "ogImage")
         .child(PAGE_SECTION, "pageId", "Sections", "sections", "sortOrder")
         .child(PAGE, "parentPageId", "Child Pages", null, "sortOrder")
         .unique("slug", "siteId").unique("slug", "parentPageId")
         .text("content")
         .build(),
      CmsTableDefinition.builder(PAGE_TYPE, CmsPageType.class, CmsFeature.CORE, "Page Type", "layers", Kind.REFERENCE)
         .section("pageType", "Page Type", "layers", Tier.T1, "id", "name", "code", "isActive", "sortOrder", "description")
         .unique("name").unique("code")
         .build(),
      CmsTableDefinition.builder(PAGE_SECTION, CmsPageSection.class, CmsFeature.CORE, "Page Section", "view_agenda", Kind.CHILD)
         .section("pageSection", "Page Section", "view_agenda", Tier.T1, "id", "name", "pageId", "sectionTypeId", "sortOrder", "cssClassName")
         .section("content", "Content", "notes", Tier.T2, "content")
         .text("content")
         .build(),
      CmsTableDefinition.builder(PAGE_SECTION_TYPE, CmsPageSectionType.class, CmsFeature.CORE, "Page Section Type", "category", Kind.REFERENCE)
         .section("pageSectionType", "Page Section Type", "category", Tier.T1, "id", "name", "code", "isActive", "sortOrder", "description")
         .unique("code").unique("name")
         .build(),
      CmsTableDefinition.builder(SNIPPET, CmsSnippet.class, CmsFeature.CORE, "Snippet", "code", Kind.MAIN)
         .section("snippet", "Snippet", "code", Tier.T1, "id", "name", "shortName", "siteId", "isActive", "sortOrder", "cssClassName")
         .section("content", "Content", "notes", Tier.T2, "content")
         .unique("shortName", "siteId")
         .text("content")
         .build(),
      CmsTableDefinition.builder(MENU, CmsMenu.class, CmsFeature.CORE, "Menu", "menu", Kind.MAIN)
         .section("menu", "Menu", "menu", Tier.T1, "id", "name", "slug", "menuType", "siteId", "isActive", "description")
         .child(MENU_ITEM, "menuId", "Items", "items", "displayOrder")
         .unique("slug", "siteId")
         .build(),
      CmsTableDefinition.builder(MENU_ITEM, CmsMenuItem.class, CmsFeature.CORE, "Menu Item", "link", Kind.CHILD)
         .section("menuItem", "Menu Item", "link", Tier.T1, "id", "name", "slug", "menuId", "parentId", "itemType", "displayOrder", "isActive")
         .section("target", "Link", "open_in_new", Tier.T2, "linkUrl", "routePath", "target")
         .section("display", "Display", "palette", Tier.T2, "icon", "cssClasses", "description")
         .child(MENU_ITEM, "parentId", "Child Items", null, "displayOrder")
         .build());



   /*******************************************************************************
    ** Not instantiable.
    *******************************************************************************/
   private CmsTables()
   {
   }



   /*******************************************************************************
    ** Every table definition, in a stable order.
    *******************************************************************************/
   public static List<CmsTableDefinition> definitions()
   {
      return (DEFINITIONS);
   }



   /*******************************************************************************
    ** The definitions of the features a config turns on.
    *******************************************************************************/
   public static List<CmsTableDefinition> definitions(CmsQBitConfig config)
   {
      return (DEFINITIONS.stream().filter(d -> config.hasFeature(d.feature())).toList());
   }



   /*******************************************************************************
    ** One definition by logical name.
    *******************************************************************************/
   public static Optional<CmsTableDefinition> definition(String logicalName)
   {
      return (DEFINITIONS.stream().filter(d -> d.logicalName().equals(logicalName)).findFirst());
   }
}
