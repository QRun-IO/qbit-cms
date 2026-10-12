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
import java.util.Set;
import com.kingsrook.qbits.cms.CmsFeature;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;


/*******************************************************************************
 ** Everything the builder needs to know about one CMS table beyond its
 ** entity: label, icon, record label, screen sections, child tables, unique
 ** keys and which fields hold long text.
 **
 ** @param logicalName       the unprefixed table name, e.g. "page"
 ** @param entityClass       the entity whose fields the table has
 ** @param feature           the feature that owns the table
 ** @param label             the table label
 ** @param icon              a Material icon name
 ** @param kind              main table, child table or reference data
 ** @param recordLabelFields fields of the record label (joined by spaces)
 ** @param sections          screen sections (the system dates section is added)
 ** @param children          child tables shown as lists on this table's records
 ** @param uniqueKeys        unique keys (field names)
 ** @param textFields        fields stored as TEXT (long, unbounded content)
 *******************************************************************************/
public record CmsTableDefinition(String logicalName, Class<? extends QRecordEntity> entityClass, CmsFeature feature, String label, String icon,
                                 Kind kind, List<String> recordLabelFields, List<Section> sections, List<Child> children,
                                 List<List<String>> uniqueKeys, Set<String> textFields)
{
   /*******************************************************************************
    ** How a table appears in the admin.
    *******************************************************************************/
   public enum Kind
   {
      /** In the nav of its area; has its own home counts. */
      MAIN,

      /** Not in the nav: edited as a child list on its parent's record. */
      CHILD,

      /** Reference data in the area's Settings section; admin-editable only. */
      REFERENCE
   }



   /*******************************************************************************
    ** A screen section.
    **
    ** @param name   the section name
    ** @param label  the section label
    ** @param icon   a Material icon name
    ** @param tier   T1 (primary) or T2
    ** @param fields the fields, in order
    *******************************************************************************/
   public record Section(String name, String label, String icon, Tier tier, List<String> fields)
   {
   }



   /*******************************************************************************
    ** A child table shown as a list on this table's records.
    **
    ** @param childLogicalName the child's logical table name
    ** @param joinField        the child's field holding this table's id
    ** @param label            the list label
    ** @param associationName  the association name (API and nested writes), or null
    ** @param orderBy          the child field to order by
    *******************************************************************************/
   public record Child(String childLogicalName, String joinField, String label, String associationName, String orderBy)
   {
   }



   /*******************************************************************************
    ** Start a definition.
    *******************************************************************************/
   public static Builder builder(String logicalName, Class<? extends QRecordEntity> entityClass, CmsFeature feature, String label, String icon, Kind kind)
   {
      return (new Builder(logicalName, entityClass, feature, label, icon, kind));
   }



   /*******************************************************************************
    ** Fluent builder for a definition.
    *******************************************************************************/
   public static final class Builder
   {
      private final String                       logicalName;
      private final Class<? extends QRecordEntity> entityClass;
      private final CmsFeature                   feature;
      private final String                       label;
      private final String                       icon;
      private final Kind                         kind;
      private       List<String>                 recordLabelFields = List.of("name");
      private final List<Section>                sections          = new ArrayList<>();
      private final List<Child>                  children          = new ArrayList<>();
      private final List<List<String>>           uniqueKeys        = new ArrayList<>();
      private       Set<String>                  textFields        = Set.of();



      /*******************************************************************************
       ** Constructor.
       *******************************************************************************/
      private Builder(String logicalName, Class<? extends QRecordEntity> entityClass, CmsFeature feature, String label, String icon, Kind kind)
      {
         this.logicalName = logicalName;
         this.entityClass = entityClass;
         this.feature = feature;
         this.label = label;
         this.icon = icon;
         this.kind = kind;
      }



      /*******************************************************************************
       ** The record label fields.
       *******************************************************************************/
      public Builder recordLabel(String... fields)
      {
         this.recordLabelFields = List.of(fields);
         return (this);
      }



      /*******************************************************************************
       ** A screen section.
       *******************************************************************************/
      public Builder section(String name, String label, String icon, Tier tier, String... fields)
      {
         this.sections.add(new Section(name, label, icon, tier, List.of(fields)));
         return (this);
      }



      /*******************************************************************************
       ** A child list.
       *******************************************************************************/
      public Builder child(String childLogicalName, String joinField, String label, String associationName, String orderBy)
      {
         this.children.add(new Child(childLogicalName, joinField, label, associationName, orderBy));
         return (this);
      }



      /*******************************************************************************
       ** A unique key.
       *******************************************************************************/
      public Builder unique(String... fields)
      {
         this.uniqueKeys.add(List.of(fields));
         return (this);
      }



      /*******************************************************************************
       ** Fields stored as TEXT.
       *******************************************************************************/
      public Builder text(String... fields)
      {
         this.textFields = Set.of(fields);
         return (this);
      }



      /*******************************************************************************
       ** The definition.
       *******************************************************************************/
      public CmsTableDefinition build()
      {
         return (new CmsTableDefinition(logicalName, entityClass, feature, label, icon, kind, recordLabelFields, List.copyOf(sections), List.copyOf(children),
            List.copyOf(uniqueKeys), textFields));
      }
   }
}
