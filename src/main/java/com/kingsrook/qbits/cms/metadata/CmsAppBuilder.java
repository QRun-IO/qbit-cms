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


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qbits.cms.CmsQBitConfig;
import com.kingsrook.qbits.cms.home.CmsHomeRenderer;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppSection;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;


/*******************************************************************************
 ** Builds the CMS app tree: one top app with its home, and one sub-app per
 ** area with its own home. The nav of an area lists its main tables and a
 ** Settings section of reference tables; processes are never in the nav.
 ** The qbit never sets the instance's home app (the host owns its landing
 ** page).
 *******************************************************************************/
public class CmsAppBuilder
{
   private final CmsQBitConfig config;



   /*******************************************************************************
    ** Constructor.
    *******************************************************************************/
   public CmsAppBuilder(CmsQBitConfig config)
   {
      this.config = config;
   }



   /*******************************************************************************
    ** The result: the apps (top first) and their home widgets.
    *******************************************************************************/
   public record Built(List<QAppMetaData> apps, List<QWidgetMetaData> widgets)
   {
   }



   /*******************************************************************************
    ** Build the apps and homes.
    *******************************************************************************/
   public Built build()
   {
      List<QAppMetaData>    apps    = new ArrayList<>();
      List<QWidgetMetaData> widgets = new ArrayList<>();

      String       topHome = CmsAreas.topHomeWidgetName(config);
      QAppMetaData top     = new QAppMetaData()
         .withName(config.getEffectiveAppName())
         .withLabel(config.getAppLabel())
         .withIcon(new QIcon("web"))
         .withWidgets(List.of(topHome));
      apps.add(top);
      widgets.add(homeWidget(topHome, config.getAppLabel(), null));

      for(CmsAreas.Area area : CmsAreas.areas(config))
      {
         QAppMetaData sub = new QAppMetaData()
            .withName(area.appName(config))
            .withLabel(area.label())
            .withIcon(new QIcon(area.icon()))
            .withWidgets(List.of(area.homeWidgetName(config)))
            .withSection(new QAppSection().withName(area.appName(config)).withLabel(area.label()).withIcon(new QIcon(area.icon()))
               .withTables(area.mainTables().stream().map(config::applyPrefix).toList()));
         if(!area.settingsTables().isEmpty())
         {
            sub.withSection(new QAppSection().withName(area.appName(config) + "Settings").withLabel("Settings").withIcon(new QIcon("settings"))
               .withTables(area.settingsTables().stream().map(config::applyPrefix).toList()));
         }
         top.withChild(sub);
         apps.add(sub);
         widgets.add(homeWidget(area.homeWidgetName(config), area.label(), area.key()));
      }
      return (new Built(apps, widgets));
   }



   /*******************************************************************************
    ** A home widget (full width; one renderer, told which home by its defaults).
    *******************************************************************************/
   private QWidgetMetaData homeWidget(String name, String label, String areaKey)
   {
      QWidgetMetaData widget = new QWidgetMetaData()
         .withName(name)
         .withLabel(label)
         .withType(WidgetType.HTML.getType())
         .withCodeReference(new QCodeReference(CmsHomeRenderer.class))
         .withGridColumns(12)
         .withIsCard(true)
         .withDefaultValue(CmsHomeRenderer.KEY_NAME_PREFIX, config.getNamePrefix());
      if(areaKey != null)
      {
         widget.withDefaultValue(CmsHomeRenderer.KEY_AREA, areaKey);
      }
      return (widget);
   }
}
