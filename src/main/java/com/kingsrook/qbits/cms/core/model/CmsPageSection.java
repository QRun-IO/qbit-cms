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

package com.kingsrook.qbits.cms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DynamicDefaultValueBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;


/*******************************************************************************
 ** One ordered section of a page; its content is interpreted by its section
 ** type.
 **
 ** The physical table name is the configured prefix plus TABLE_NAME (for
 ** example cms_page_section); possible-value source names on the fields are
 ** logical and get the same prefix when the table is built.
 *******************************************************************************/
public class CmsPageSection extends QRecordEntity
{
   public static final String TABLE_NAME = "page_section";

   @QField(isEditable = false, isPrimaryKey = true)
   private Long id;

   @QField(isRequired = true, maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String name;

   @QField(maxLength = 255, valueTooLongBehavior = ValueTooLongBehavior.ERROR, label = "CSS Class Name")
   private String cssClassName;

   @QField(isRequired = true, possibleValueSourceName = "page_section_type", label = "Section Type")
   private Long sectionTypeId;

   @QField(gridColumns = 12)
   private String content;

   @QField(isRequired = true, defaultValue = "0")
   private Integer sortOrder;

   @QField(isRequired = true, possibleValueSourceName = "page", label = "Page")
   private Long pageId;

   @QField(isEditable = false, dynamicDefaultValueBehavior = DynamicDefaultValueBehavior.CREATE_DATE)
   private Instant createDate;

   @QField(isEditable = false, dynamicDefaultValueBehavior = DynamicDefaultValueBehavior.MODIFY_DATE)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public CmsPageSection()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public CmsPageSection(QRecord record) throws QException
   {
      populateFromQRecord(record);
   }



   /*******************************************************************************
    ** Getter for id
    *******************************************************************************/
   public Long getId()
   {
      return (this.id);
   }



   /*******************************************************************************
    ** Setter for id
    *******************************************************************************/
   public void setId(Long id)
   {
      this.id = id;
   }



   /*******************************************************************************
    ** Fluent setter for id
    *******************************************************************************/
   public CmsPageSection withId(Long id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for name
    *******************************************************************************/
   public String getName()
   {
      return (this.name);
   }



   /*******************************************************************************
    ** Setter for name
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Fluent setter for name
    *******************************************************************************/
   public CmsPageSection withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for cssClassName
    *******************************************************************************/
   public String getCssClassName()
   {
      return (this.cssClassName);
   }



   /*******************************************************************************
    ** Setter for cssClassName
    *******************************************************************************/
   public void setCssClassName(String cssClassName)
   {
      this.cssClassName = cssClassName;
   }



   /*******************************************************************************
    ** Fluent setter for cssClassName
    *******************************************************************************/
   public CmsPageSection withCssClassName(String cssClassName)
   {
      this.cssClassName = cssClassName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sectionTypeId
    *******************************************************************************/
   public Long getSectionTypeId()
   {
      return (this.sectionTypeId);
   }



   /*******************************************************************************
    ** Setter for sectionTypeId
    *******************************************************************************/
   public void setSectionTypeId(Long sectionTypeId)
   {
      this.sectionTypeId = sectionTypeId;
   }



   /*******************************************************************************
    ** Fluent setter for sectionTypeId
    *******************************************************************************/
   public CmsPageSection withSectionTypeId(Long sectionTypeId)
   {
      this.sectionTypeId = sectionTypeId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for content
    *******************************************************************************/
   public String getContent()
   {
      return (this.content);
   }



   /*******************************************************************************
    ** Setter for content
    *******************************************************************************/
   public void setContent(String content)
   {
      this.content = content;
   }



   /*******************************************************************************
    ** Fluent setter for content
    *******************************************************************************/
   public CmsPageSection withContent(String content)
   {
      this.content = content;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sortOrder
    *******************************************************************************/
   public Integer getSortOrder()
   {
      return (this.sortOrder);
   }



   /*******************************************************************************
    ** Setter for sortOrder
    *******************************************************************************/
   public void setSortOrder(Integer sortOrder)
   {
      this.sortOrder = sortOrder;
   }



   /*******************************************************************************
    ** Fluent setter for sortOrder
    *******************************************************************************/
   public CmsPageSection withSortOrder(Integer sortOrder)
   {
      this.sortOrder = sortOrder;
      return (this);
   }



   /*******************************************************************************
    ** Getter for pageId
    *******************************************************************************/
   public Long getPageId()
   {
      return (this.pageId);
   }



   /*******************************************************************************
    ** Setter for pageId
    *******************************************************************************/
   public void setPageId(Long pageId)
   {
      this.pageId = pageId;
   }



   /*******************************************************************************
    ** Fluent setter for pageId
    *******************************************************************************/
   public CmsPageSection withPageId(Long pageId)
   {
      this.pageId = pageId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for createDate
    *******************************************************************************/
   public Instant getCreateDate()
   {
      return (this.createDate);
   }



   /*******************************************************************************
    ** Setter for createDate
    *******************************************************************************/
   public void setCreateDate(Instant createDate)
   {
      this.createDate = createDate;
   }



   /*******************************************************************************
    ** Fluent setter for createDate
    *******************************************************************************/
   public CmsPageSection withCreateDate(Instant createDate)
   {
      this.createDate = createDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for modifyDate
    *******************************************************************************/
   public Instant getModifyDate()
   {
      return (this.modifyDate);
   }



   /*******************************************************************************
    ** Setter for modifyDate
    *******************************************************************************/
   public void setModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
   }



   /*******************************************************************************
    ** Fluent setter for modifyDate
    *******************************************************************************/
   public CmsPageSection withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }

}
