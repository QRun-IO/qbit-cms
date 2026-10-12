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


import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qbits.cms.CmsQBitConfig;
import com.kingsrook.qbits.cms.CmsRuntime;
import com.kingsrook.qbits.cms.CmsTables;
import com.kingsrook.qbits.cms.metadata.CmsAreas;
import com.kingsrook.qbits.cms.metadata.CmsTableDefinition;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionEnforcement;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QPermissionDeniedException;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.RawHTML;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaDataInterface;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** The CMS homes. One renderer serves the top app's home and every area's
 ** home; the widget's default values say which (namePrefix and area). Each
 ** home owns its data and shows only real data read as the current user, so
 ** a table the user may not read is left out instead of shown as zero:
 **
 ** - the top home: one card per area with its main tables' counts (and draft
 **   pages), linking to the area; no per-record lists, so nothing duplicates
 **   an area home;
 ** Reads always run with PermissionEnforcement.ENFORCE: a widget renders
 ** inside a trusted scope, and these counts must still be the user's own.
 **
 ** - an area home: a tile per main table (count, drafts or inactive rows),
 **   the area's recently changed records, and its settings tables.
 *******************************************************************************/
public class CmsHomeRenderer extends AbstractWidgetRenderer
{
   public static final String KEY_NAME_PREFIX = "namePrefix";
   public static final String KEY_AREA        = "area";

   static final int RECENT_ROWS = 8;

   private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US).withZone(ZoneOffset.UTC);



   /*******************************************************************************
    ** Render the home named by the widget's default values.
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      QWidgetMetaDataInterface widget     = input.getWidgetMetaData();
      Map<String, Serializable> defaults  = widget.getDefaultValues();
      String                    prefix    = defaults == null ? null : (String) defaults.get(KEY_NAME_PREFIX);
      String                    areaKey   = defaults == null ? null : (String) defaults.get(KEY_AREA);
      Optional<CmsQBitConfig>   config    = CmsRuntime.findConfig(QContext.getQInstance(), prefix);
      if(config.isEmpty())
      {
         throw (new QException("No CMS install with name prefix " + prefix));
      }

      StringBuilder html = new StringBuilder();
      CmsRuntime    runtime = config.get().getRuntime();
      if(runtime != null && !runtime.getSchemaProblems().isEmpty())
      {
         html.append(CmsHtml.warning("The CMS database is not up to date, so some screens may fail: "
            + CmsHtml.escape(String.join(" ", runtime.getSchemaProblems()))));
      }

      Optional<CmsAreas.Area> area = areaKey == null ? Optional.empty() : CmsAreas.area(areaKey);
      html.append(area.isPresent() ? renderArea(config.get(), area.get()) : renderTop(config.get()));
      return (new RenderWidgetOutput(new RawHTML(widget.getLabel(), html.toString())));
   }



   /*******************************************************************************
    ** The top home: a card per area.
    *******************************************************************************/
   String renderTop(CmsQBitConfig config) throws QException
   {
      List<CmsHtml.Tile> tiles = new ArrayList<>();
      for(CmsAreas.Area area : CmsAreas.areas(config))
      {
         List<String> notes = new ArrayList<>();
         long         total = 0;
         boolean      any   = false;
         for(String logical : area.mainTables())
         {
            Long count = count(config.applyPrefix(logical), null);
            if(count != null)
            {
               any = true;
               total += count;
               notes.add(CmsHtml.link(tablePath(config, logical), plural(count, label(logical))) + draftNote(config, logical));
            }
         }
         if(any)
         {
            tiles.add(new CmsHtml.Tile(area.label(), String.valueOf(total), "/app/" + area.appName(config), notes));
         }
      }
      if(tiles.isEmpty())
      {
         return (CmsHtml.empty("You do not have access to any CMS area."));
      }
      return (CmsHtml.tiles(tiles));
   }



   /*******************************************************************************
    ** An area home.
    *******************************************************************************/
   String renderArea(CmsQBitConfig config, CmsAreas.Area area) throws QException
   {
      StringBuilder      html   = new StringBuilder();
      List<CmsHtml.Tile> tiles  = new ArrayList<>();
      List<QRecord>      recent = new ArrayList<>();
      for(String logical : area.mainTables())
      {
         String table = config.applyPrefix(logical);
         Long   count = count(table, null);
         if(count == null)
         {
            continue;
         }
         List<String> notes = new ArrayList<>();
         String       draft = draftNote(config, logical);
         if(!draft.isEmpty())
         {
            notes.add(draft.substring(2));
         }
         else if(hasField(table, "isActive"))
         {
            Long inactive = count(table, new QQueryFilter(new QFilterCriteria("isActive", QCriteriaOperator.EQUALS, false)));
            if(inactive != null && inactive > 0)
            {
               notes.add(CmsHtml.muted(inactive + " inactive"));
            }
         }
         notes.add(CmsHtml.link(tablePath(config, logical) + "/create", "New " + label(logical).toLowerCase(Locale.ROOT)));
         tiles.add(new CmsHtml.Tile(pluralLabel(logical), String.valueOf(count), tablePath(config, logical), notes));
         recent.addAll(recent(table));
      }
      if(tiles.isEmpty())
      {
         return (CmsHtml.empty("You do not have access to " + CmsHtml.escape(area.label()) + "."));
      }
      html.append(CmsHtml.tiles(tiles));

      html.append(CmsHtml.heading("Recently changed"));
      recent.sort(Comparator.comparing((QRecord r) -> r.getValueInstant("modifyDate"), Comparator.nullsLast(Comparator.reverseOrder())));
      if(recent.isEmpty())
      {
         html.append(CmsHtml.empty("Nothing yet. Records you create appear here."));
      }
      else
      {
         List<CmsHtml.Row> rows = new ArrayList<>();
         for(QRecord record : recent.subList(0, Math.min(RECENT_ROWS, recent.size())))
         {
            String table   = record.getTableName();
            String logical = table.substring(config.getTableNamePrefix().length());
            String title   = record.getRecordLabel() != null ? record.getRecordLabel() : label(logical) + " " + record.getValue("id");
            Instant when   = record.getValueInstant("modifyDate");
            rows.add(new CmsHtml.Row(title, tablePath(config, logical) + "/" + record.getValue("id"), label(logical) + (when == null ? "" : " · " + DATE.format(when))));
         }
         html.append(CmsHtml.rows(rows));
      }

      List<String> settings = new ArrayList<>();
      for(String logical : area.settingsTables())
      {
         Long count = count(config.applyPrefix(logical), null);
         if(count != null)
         {
            settings.add(CmsHtml.link(tablePath(config, logical), plural(count, label(logical))));
         }
      }
      if(!settings.isEmpty())
      {
         html.append(CmsHtml.heading("Settings"));
         html.append("<div style=\"display:flex;flex-wrap:wrap;gap:6px 18px\">");
         settings.forEach(s -> html.append("<span>").append(s).append("</span>"));
         html.append("</div>");
      }
      return (html.toString());
   }



   /*******************************************************************************
    ** ", N drafts" for a table with an isPublished field (empty otherwise).
    *******************************************************************************/
   private String draftNote(CmsQBitConfig config, String logical) throws QException
   {
      String table = config.applyPrefix(logical);
      if(!hasField(table, "isPublished"))
      {
         return ("");
      }
      Long drafts = count(table, new QQueryFilter(new QFilterCriteria("isPublished", QCriteriaOperator.EQUALS, false)));
      return (drafts == null || drafts == 0 ? "" : ", " + CmsHtml.muted(drafts + (drafts == 1 ? " draft" : " drafts")));
   }



   /*******************************************************************************
    ** The most recently changed records of a table, read as the current user.
    *******************************************************************************/
   private List<QRecord> recent(String table) throws QException
   {
      try
      {
         QueryInput queryInput = new QueryInput(table)
            .withFilter(new QQueryFilter().withOrderBy(new QFilterOrderBy("modifyDate", false)).withLimit(RECENT_ROWS))
            .withShouldGenerateDisplayValues(true);
         queryInput.setPermissionEnforcement(PermissionEnforcement.ENFORCE);
         return (new QueryAction().execute(queryInput).getRecords());
      }
      catch(QPermissionDeniedException e)
      {
         return (List.of());
      }
   }



   /*******************************************************************************
    ** A count read as the current user, or null when the user may not read it.
    *******************************************************************************/
   private Long count(String table, QQueryFilter filter) throws QException
   {
      try
      {
         CountInput countInput = new CountInput(table).withFilter(filter);
         countInput.setPermissionEnforcement(PermissionEnforcement.ENFORCE);
         Integer count = new CountAction().execute(countInput).getCount();
         return (count == null ? 0L : count.longValue());
      }
      catch(QPermissionDeniedException e)
      {
         return (null);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean hasField(String table, String field)
   {
      QTableMetaData tableMetaData = QContext.getQInstance().getTable(table);
      return (tableMetaData != null && tableMetaData.getFields().containsKey(field));
   }



   /*******************************************************************************
    ** The admin path of a table (Next routes tables at /app/name).
    *******************************************************************************/
   static String tablePath(CmsQBitConfig config, String logical)
   {
      return ("/app/" + config.applyPrefix(logical));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String label(String logical)
   {
      return (CmsTables.definition(logical).map(CmsTableDefinition::label).orElse(logical));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String pluralLabel(String logical)
   {
      return (pluralize(label(logical)));
   }



   /*******************************************************************************
    ** "1 page" / "3 pages".
    *******************************************************************************/
   static String plural(long count, String label)
   {
      String lower = label.toLowerCase(Locale.ROOT);
      return (count + " " + (count == 1 ? lower : pluralize(lower)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   static String pluralize(String word)
   {
      return (word.endsWith("y") && !word.endsWith("ay") ? word.substring(0, word.length() - 1) + "ies" : word + "s");
   }
}
