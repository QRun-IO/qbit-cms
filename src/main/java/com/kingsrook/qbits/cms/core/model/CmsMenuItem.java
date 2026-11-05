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
 ** One entry of a menu: a link or route, ordered, optionally nested under a
 ** parent item.
 **
 ** The physical table name is the configured prefix plus TABLE_NAME (for
 ** example cms_menu_item); possible-value source names on the fields are
 ** logical and get the same prefix when the table is built.
 *******************************************************************************/
public class CmsMenuItem extends QRecordEntity
{
   public static final String TABLE_NAME = "menu_item";

   @QField(isEditable = false, isPrimaryKey = true)
   private Long id;

   @QField(isRequired = true, possibleValueSourceName = "menu", label = "Menu")
   private Long menuId;

   @QField(possibleValueSourceName = "menu_item", label = "Parent Item")
   private Long parentId;

   @QField(isRequired = true, maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String name;

   @QField(isRequired = true, maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String slug;

   @QField(isRequired = true, maxLength = 50, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String itemType;

   @QField(maxLength = 500, valueTooLongBehavior = ValueTooLongBehavior.ERROR, label = "Link URL", gridColumns = 12)
   private String linkUrl;

   @QField(maxLength = 200, valueTooLongBehavior = ValueTooLongBehavior.ERROR, gridColumns = 12)
   private String routePath;

   @QField(maxLength = 20, valueTooLongBehavior = ValueTooLongBehavior.ERROR, defaultValue = "_self")
   private String target;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String icon;

   @QField(maxLength = 255, valueTooLongBehavior = ValueTooLongBehavior.ERROR, gridColumns = 12)
   private String description;

   @QField(isRequired = true, defaultValue = "0")
   private Integer displayOrder;

   @QField(isRequired = true, defaultValue = "true")
   private Boolean isActive;

   @QField(maxLength = 200, valueTooLongBehavior = ValueTooLongBehavior.ERROR, label = "CSS Classes")
   private String cssClasses;

   @QField(isEditable = false, dynamicDefaultValueBehavior = DynamicDefaultValueBehavior.CREATE_DATE)
   private Instant createDate;

   @QField(isEditable = false, dynamicDefaultValueBehavior = DynamicDefaultValueBehavior.MODIFY_DATE)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public CmsMenuItem()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public CmsMenuItem(QRecord record) throws QException
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
   public CmsMenuItem withId(Long id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for menuId
    *******************************************************************************/
   public Long getMenuId()
   {
      return (this.menuId);
   }



   /*******************************************************************************
    ** Setter for menuId
    *******************************************************************************/
   public void setMenuId(Long menuId)
   {
      this.menuId = menuId;
   }



   /*******************************************************************************
    ** Fluent setter for menuId
    *******************************************************************************/
   public CmsMenuItem withMenuId(Long menuId)
   {
      this.menuId = menuId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for parentId
    *******************************************************************************/
   public Long getParentId()
   {
      return (this.parentId);
   }



   /*******************************************************************************
    ** Setter for parentId
    *******************************************************************************/
   public void setParentId(Long parentId)
   {
      this.parentId = parentId;
   }



   /*******************************************************************************
    ** Fluent setter for parentId
    *******************************************************************************/
   public CmsMenuItem withParentId(Long parentId)
   {
      this.parentId = parentId;
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
   public CmsMenuItem withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for slug
    *******************************************************************************/
   public String getSlug()
   {
      return (this.slug);
   }



   /*******************************************************************************
    ** Setter for slug
    *******************************************************************************/
   public void setSlug(String slug)
   {
      this.slug = slug;
   }



   /*******************************************************************************
    ** Fluent setter for slug
    *******************************************************************************/
   public CmsMenuItem withSlug(String slug)
   {
      this.slug = slug;
      return (this);
   }



   /*******************************************************************************
    ** Getter for itemType
    *******************************************************************************/
   public String getItemType()
   {
      return (this.itemType);
   }



   /*******************************************************************************
    ** Setter for itemType
    *******************************************************************************/
   public void setItemType(String itemType)
   {
      this.itemType = itemType;
   }



   /*******************************************************************************
    ** Fluent setter for itemType
    *******************************************************************************/
   public CmsMenuItem withItemType(String itemType)
   {
      this.itemType = itemType;
      return (this);
   }



   /*******************************************************************************
    ** Getter for linkUrl
    *******************************************************************************/
   public String getLinkUrl()
   {
      return (this.linkUrl);
   }



   /*******************************************************************************
    ** Setter for linkUrl
    *******************************************************************************/
   public void setLinkUrl(String linkUrl)
   {
      this.linkUrl = linkUrl;
   }



   /*******************************************************************************
    ** Fluent setter for linkUrl
    *******************************************************************************/
   public CmsMenuItem withLinkUrl(String linkUrl)
   {
      this.linkUrl = linkUrl;
      return (this);
   }



   /*******************************************************************************
    ** Getter for routePath
    *******************************************************************************/
   public String getRoutePath()
   {
      return (this.routePath);
   }



   /*******************************************************************************
    ** Setter for routePath
    *******************************************************************************/
   public void setRoutePath(String routePath)
   {
      this.routePath = routePath;
   }



   /*******************************************************************************
    ** Fluent setter for routePath
    *******************************************************************************/
   public CmsMenuItem withRoutePath(String routePath)
   {
      this.routePath = routePath;
      return (this);
   }



   /*******************************************************************************
    ** Getter for target
    *******************************************************************************/
   public String getTarget()
   {
      return (this.target);
   }



   /*******************************************************************************
    ** Setter for target
    *******************************************************************************/
   public void setTarget(String target)
   {
      this.target = target;
   }



   /*******************************************************************************
    ** Fluent setter for target
    *******************************************************************************/
   public CmsMenuItem withTarget(String target)
   {
      this.target = target;
      return (this);
   }



   /*******************************************************************************
    ** Getter for icon
    *******************************************************************************/
   public String getIcon()
   {
      return (this.icon);
   }



   /*******************************************************************************
    ** Setter for icon
    *******************************************************************************/
   public void setIcon(String icon)
   {
      this.icon = icon;
   }



   /*******************************************************************************
    ** Fluent setter for icon
    *******************************************************************************/
   public CmsMenuItem withIcon(String icon)
   {
      this.icon = icon;
      return (this);
   }



   /*******************************************************************************
    ** Getter for description
    *******************************************************************************/
   public String getDescription()
   {
      return (this.description);
   }



   /*******************************************************************************
    ** Setter for description
    *******************************************************************************/
   public void setDescription(String description)
   {
      this.description = description;
   }



   /*******************************************************************************
    ** Fluent setter for description
    *******************************************************************************/
   public CmsMenuItem withDescription(String description)
   {
      this.description = description;
      return (this);
   }



   /*******************************************************************************
    ** Getter for displayOrder
    *******************************************************************************/
   public Integer getDisplayOrder()
   {
      return (this.displayOrder);
   }



   /*******************************************************************************
    ** Setter for displayOrder
    *******************************************************************************/
   public void setDisplayOrder(Integer displayOrder)
   {
      this.displayOrder = displayOrder;
   }



   /*******************************************************************************
    ** Fluent setter for displayOrder
    *******************************************************************************/
   public CmsMenuItem withDisplayOrder(Integer displayOrder)
   {
      this.displayOrder = displayOrder;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isActive
    *******************************************************************************/
   public Boolean getIsActive()
   {
      return (this.isActive);
   }



   /*******************************************************************************
    ** Setter for isActive
    *******************************************************************************/
   public void setIsActive(Boolean isActive)
   {
      this.isActive = isActive;
   }



   /*******************************************************************************
    ** Fluent setter for isActive
    *******************************************************************************/
   public CmsMenuItem withIsActive(Boolean isActive)
   {
      this.isActive = isActive;
      return (this);
   }



   /*******************************************************************************
    ** Getter for cssClasses
    *******************************************************************************/
   public String getCssClasses()
   {
      return (this.cssClasses);
   }



   /*******************************************************************************
    ** Setter for cssClasses
    *******************************************************************************/
   public void setCssClasses(String cssClasses)
   {
      this.cssClasses = cssClasses;
   }



   /*******************************************************************************
    ** Fluent setter for cssClasses
    *******************************************************************************/
   public CmsMenuItem withCssClasses(String cssClasses)
   {
      this.cssClasses = cssClasses;
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
   public CmsMenuItem withCreateDate(Instant createDate)
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
   public CmsMenuItem withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }

}
