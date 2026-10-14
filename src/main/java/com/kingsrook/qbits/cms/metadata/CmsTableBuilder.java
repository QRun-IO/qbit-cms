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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qbits.cms.CmsQBitConfig;
import com.kingsrook.qbits.cms.CmsTables;
import com.kingsrook.qbits.cms.core.model.CmsRedirectMatchType;
import com.kingsrook.qbits.cms.security.CmsOpenReadsPermissionChecker;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.ChildRecordListRenderer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.audits.AuditLevel;
import com.kingsrook.qqq.backend.core.model.metadata.audits.QAuditRules;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.PermissionLevel;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.ExposedJoin;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSTableBackendDetails;


/*******************************************************************************
 ** Builds the CMS tables of a config: one standard QQQ table per definition
 ** (fields from the entity, prefixed names, snake_case columns), a
 ** possible-value source per table, joins and child-record lists for child
 ** tables, associations, permission rules and field audit.
 **
 ** Every table is a normal, visible QQQ table (owner rule): reference tables
 ** and child tables are left out of the nav, not hidden.
 *******************************************************************************/
public class CmsTableBuilder
{
   /** The QQQ audit table; field audit is applied only when the host has it. */
   static final String AUDIT_TABLE = "audit";

   private final CmsQBitConfig config;



   /*******************************************************************************
    ** Constructor.
    *******************************************************************************/
   public CmsTableBuilder(CmsQBitConfig config)
   {
      this.config = config;
   }



   /*******************************************************************************
    ** The result of a build.
    **
    ** @param tables               the tables, by physical name
    ** @param possibleValueSources the possible-value sources
    ** @param joins                the joins
    ** @param widgets              the child-record list widgets
    *******************************************************************************/
   public record Built(Map<String, QTableMetaData> tables, List<QPossibleValueSource> possibleValueSources, List<QJoinMetaData> joins,
                       List<QWidgetMetaData> widgets)
   {
   }



   /*******************************************************************************
    ** Build every table of the config's features.
    *******************************************************************************/
   public Built build(QInstance qInstance) throws QException
   {
      List<CmsTableDefinition>    definitions = CmsTables.definitions(config);
      Set<String>                 logicals    = definitions.stream().map(CmsTableDefinition::logicalName).collect(Collectors.toSet());
      Map<String, QTableMetaData> tables      = new LinkedHashMap<>();
      List<QPossibleValueSource>  pvs         = new ArrayList<>();
      List<QJoinMetaData>         joins       = new ArrayList<>();
      List<QWidgetMetaData>       widgets     = new ArrayList<>();
      boolean                     audit       = qInstance.getTable(AUDIT_TABLE) != null;

      for(CmsTableDefinition definition : definitions)
      {
         tables.put(config.applyPrefix(definition.logicalName()), buildTable(definition, logicals, audit));
         pvs.add(QPossibleValueSource.newForTable(config.applyPrefix(definition.logicalName())));
      }
      if(logicals.contains(CmsTables.REDIRECT))
      {
         pvs.add(QPossibleValueSource.newForEnum(config.applyPrefix(CmsRedirectMatchType.NAME), CmsRedirectMatchType.values()));
      }

      for(CmsTableDefinition definition : definitions)
      {
         QTableMetaData parent = tables.get(config.applyPrefix(definition.logicalName()));
         for(CmsTableDefinition.Child child : definition.children())
         {
            if(logicals.contains(child.childLogicalName()))
            {
               addChild(parent, definition, child, joins, widgets);
            }
         }
      }
      return (new Built(tables, pvs, joins, widgets));
   }



   /*******************************************************************************
    ** One table.
    *******************************************************************************/
   QTableMetaData buildTable(CmsTableDefinition definition, Set<String> logicals, boolean audit) throws QException
   {
      String         name  = config.applyPrefix(definition.logicalName());
      QTableMetaData table = new QTableMetaData()
         .withName(name)
         .withLabel(definition.pluralLabel())
         .withIcon(new QIcon(definition.icon()))
         .withBackendName(config.getBackendName())
         .withBackendDetails(new RDBMSTableBackendDetails().withTableName(name))
         .withPrimaryKeyField("id")
         .withFieldsFromEntity(definition.entityClass())
         .withRecordLabelFormat(String.join(" ", definition.recordLabelFields().stream().map(f -> "%s").toList()))
         .withRecordLabelFields(definition.recordLabelFields())
         .withPermissionRules(permissionRules());

      for(QFieldMetaData field : table.getFields().values())
      {
         String pvsName = field.getPossibleValueSourceName();
         if(pvsName != null && logicals.contains(pvsName))
         {
            field.setPossibleValueSourceName(config.applyPrefix(pvsName));
         }
         else if(pvsName != null)
         {
            //////////////////////////////////////////////////////////////////
            // a reference to a table of a feature that is off: no lookup   //
            //////////////////////////////////////////////////////////////////
            field.setPossibleValueSourceName(null);
         }
         if(definition.textFields().contains(field.getName()))
         {
            field.setType(QFieldType.TEXT);
         }
      }
      if(CmsTables.REDIRECT.equals(definition.logicalName()))
      {
         table.getField("matchType").setPossibleValueSourceName(config.applyPrefix(CmsRedirectMatchType.NAME));
      }
      QInstanceEnricher.setInferredFieldBackendNames(table);

      for(List<String> uniqueKey : definition.uniqueKeys())
      {
         table.withUniqueKey(new UniqueKey(uniqueKey));
      }
      for(CmsTableDefinition.Section section : definition.sections())
      {
         table.addSection(new QFieldSection(section.name(), section.label(), new QIcon(section.icon()), section.tier(), section.fields()));
      }
      table.addSection(new QFieldSection("dates", "Dates", new QIcon("calendar_month"), Tier.T3, List.of("createDate", "modifyDate")));

      if(audit)
      {
         table.withAuditRules(new QAuditRules().withAuditLevel(AuditLevel.FIELD));
      }
      return (table);
   }



   /*******************************************************************************
    ** Permission rules for every CMS table: the table name plus .read, .insert,
    ** .edit and .delete; with openReads, reads need nothing.
    *******************************************************************************/
   QPermissionRules permissionRules()
   {
      QPermissionRules rules = new QPermissionRules().withLevel(PermissionLevel.READ_INSERT_EDIT_DELETE_PERMISSIONS);
      if(Boolean.TRUE.equals(config.getOpenReads()))
      {
         rules.withCustomPermissionChecker(new QCodeReference(CmsOpenReadsPermissionChecker.class));
      }
      return (rules);
   }



   /*******************************************************************************
    ** A child table: a join, a child-record list on the parent's screen, an
    ** exposed join (reports and filters) and, when named, an association.
    *******************************************************************************/
   private void addChild(QTableMetaData parent, CmsTableDefinition parentDefinition, CmsTableDefinition.Child child, List<QJoinMetaData> joins,
                         List<QWidgetMetaData> widgets)
   {
      String childTable = config.applyPrefix(child.childLogicalName());
      String joinName   = parent.getName() + "_" + childTable + "_" + child.joinField();

      QJoinMetaData join = new QJoinMetaData()
         .withName(joinName)
         .withLeftTable(parent.getName())
         .withRightTable(childTable)
         .withType(JoinType.ONE_TO_MANY)
         .withJoinOn(new JoinOn("id", child.joinField()))
         .withOrderBy(new QFilterOrderBy(child.orderBy()));
      joins.add(join);

      QWidgetMetaData widget = ChildRecordListRenderer.widgetMetaDataBuilder(join)
         .withName(joinName)
         .withLabel(child.label())
         .withMaxRows(50)
         .withCanAddChildRecord(true)
         .withDefaultValuesForNewChildRecordsFromParentFields(Map.of(child.joinField(), "id"))
         .getWidgetMetaData();
      widgets.add(widget);

      String icon = CmsTables.definition(child.childLogicalName()).map(CmsTableDefinition::icon).orElse(parentDefinition.icon());
      parent.addSection(new QFieldSection(joinName, child.label(), new QIcon(icon), Tier.T2, List.of()).withWidgetName(joinName));
      if(!childTable.equals(parent.getName()))
      {
         parent.withExposedJoin(new ExposedJoin().withLabel(child.label()).withJoinPath(List.of(joinName)).withJoinTable(childTable));
      }
      if(child.associationName() != null)
      {
         parent.withAssociation(new Association().withName(child.associationName()).withAssociatedTableName(childTable).withJoinName(joinName));
      }
   }
}
