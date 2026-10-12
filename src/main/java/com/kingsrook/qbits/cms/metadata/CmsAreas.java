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

package com.kingsrook.qbits.cms.metadata;


import java.util.List;
import java.util.Optional;
import com.kingsrook.qbits.cms.CmsFeature;
import com.kingsrook.qbits.cms.CmsQBitConfig;
import com.kingsrook.qbits.cms.CmsTables;


/*******************************************************************************
 ** The CMS areas: one sub-app each, with its own home. An area lists only its
 ** main tables in the nav, plus a Settings section for reference data; child
 ** tables are edited on their parents' records (owner rule).
 *******************************************************************************/
public final class CmsAreas
{
   /*******************************************************************************
    ** One area.
    **
    ** @param key            the name suffix (sub-app cmsSites, home cmsSitesHome)
    ** @param label          the label
    ** @param icon           a Material icon name
    ** @param feature        the feature that owns the area
    ** @param mainTables     logical names of the tables in the nav
    ** @param settingsTables logical names of the reference tables
    *******************************************************************************/
   public record Area(String key, String label, String icon, CmsFeature feature, List<String> mainTables, List<String> settingsTables)
   {
      /*******************************************************************************
       ** The sub-app name.
       *******************************************************************************/
      public String appName(CmsQBitConfig config)
      {
         return (config.name(key));
      }



      /*******************************************************************************
       ** The home widget name.
       *******************************************************************************/
      public String homeWidgetName(CmsQBitConfig config)
      {
         return (config.name(key + "Home"));
      }
   }



   private static final List<Area> AREAS = List.of(
      new Area("Sites", "Sites", "language", CmsFeature.CORE, List.of(CmsTables.SITE, CmsTables.REDIRECT), List.of(CmsTables.REDIRECT_TYPE)),
      new Area("Pages", "Pages", "description", CmsFeature.CORE, List.of(CmsTables.PAGE, CmsTables.SNIPPET), List.of(CmsTables.PAGE_TYPE, CmsTables.PAGE_SECTION_TYPE)),
      new Area("Navigation", "Navigation", "menu", CmsFeature.CORE, List.of(CmsTables.MENU), List.of()));



   /*******************************************************************************
    ** Not instantiable.
    *******************************************************************************/
   private CmsAreas()
   {
   }



   /*******************************************************************************
    ** The areas of the config's features.
    *******************************************************************************/
   public static List<Area> areas(CmsQBitConfig config)
   {
      return (AREAS.stream().filter(a -> config.hasFeature(a.feature())).toList());
   }



   /*******************************************************************************
    ** One area by key.
    *******************************************************************************/
   public static Optional<Area> area(String key)
   {
      return (AREAS.stream().filter(a -> a.key().equals(key)).findFirst());
   }



   /*******************************************************************************
    ** The top app's home widget name.
    *******************************************************************************/
   public static String topHomeWidgetName(CmsQBitConfig config)
   {
      return (config.name("Home"));
   }
}
