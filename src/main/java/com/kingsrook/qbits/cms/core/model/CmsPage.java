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
 ** A page of a site, made of ordered page sections; pages nest under a parent
 ** page.
 **
 ** The physical table name is the configured prefix plus TABLE_NAME (for
 ** example cms_page); possible-value source names on the fields are
 ** logical and get the same prefix when the table is built.
 *******************************************************************************/
public class CmsPage extends QRecordEntity
{
   public static final String TABLE_NAME = "page";

   @QField(isEditable = false, isPrimaryKey = true)
   private Long id;

   @QField(isRequired = true, maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String name;

   @QField(isRequired = true, maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String slug;

   @QField(isRequired = true, maxLength = 200, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String title;

   @QField(gridColumns = 12)
   private String content;

   @QField(isRequired = true, possibleValueSourceName = "page_type", label = "Page Type")
   private Long pageTypeId;

   @QField(maxLength = 50, valueTooLongBehavior = ValueTooLongBehavior.ERROR, defaultValue = "default")
   private String template;

   @QField(isRequired = true, defaultValue = "false")
   private Boolean isPublished;

   @QField()
   private Instant publishDate;

   @QField(isRequired = true, defaultValue = "0")
   private Integer sortOrder;

   @QField(possibleValueSourceName = "page", label = "Parent Page")
   private Long parentPageId;

   @QField(isRequired = true, possibleValueSourceName = "site", label = "Site")
   private Long siteId;

   @QField(maxLength = 500, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE, gridColumns = 12)
   private String metaDescription;

   @QField(maxLength = 255, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE, gridColumns = 12)
   private String metaKeywords;

   @QField(maxLength = 255, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE, label = "Open Graph Image", gridColumns = 12)
   private String ogImage;

   @QField(isEditable = false, dynamicDefaultValueBehavior = DynamicDefaultValueBehavior.CREATE_DATE)
   private Instant createDate;

   @QField(isEditable = false, dynamicDefaultValueBehavior = DynamicDefaultValueBehavior.MODIFY_DATE)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public CmsPage()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public CmsPage(QRecord record) throws QException
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
   public CmsPage withId(Long id)
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
   public CmsPage withName(String name)
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
   public CmsPage withSlug(String slug)
   {
      this.slug = slug;
      return (this);
   }



   /*******************************************************************************
    ** Getter for title
    *******************************************************************************/
   public String getTitle()
   {
      return (this.title);
   }



   /*******************************************************************************
    ** Setter for title
    *******************************************************************************/
   public void setTitle(String title)
   {
      this.title = title;
   }



   /*******************************************************************************
    ** Fluent setter for title
    *******************************************************************************/
   public CmsPage withTitle(String title)
   {
      this.title = title;
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
   public CmsPage withContent(String content)
   {
      this.content = content;
      return (this);
   }



   /*******************************************************************************
    ** Getter for pageTypeId
    *******************************************************************************/
   public Long getPageTypeId()
   {
      return (this.pageTypeId);
   }



   /*******************************************************************************
    ** Setter for pageTypeId
    *******************************************************************************/
   public void setPageTypeId(Long pageTypeId)
   {
      this.pageTypeId = pageTypeId;
   }



   /*******************************************************************************
    ** Fluent setter for pageTypeId
    *******************************************************************************/
   public CmsPage withPageTypeId(Long pageTypeId)
   {
      this.pageTypeId = pageTypeId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for template
    *******************************************************************************/
   public String getTemplate()
   {
      return (this.template);
   }



   /*******************************************************************************
    ** Setter for template
    *******************************************************************************/
   public void setTemplate(String template)
   {
      this.template = template;
   }



   /*******************************************************************************
    ** Fluent setter for template
    *******************************************************************************/
   public CmsPage withTemplate(String template)
   {
      this.template = template;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isPublished
    *******************************************************************************/
   public Boolean getIsPublished()
   {
      return (this.isPublished);
   }



   /*******************************************************************************
    ** Setter for isPublished
    *******************************************************************************/
   public void setIsPublished(Boolean isPublished)
   {
      this.isPublished = isPublished;
   }



   /*******************************************************************************
    ** Fluent setter for isPublished
    *******************************************************************************/
   public CmsPage withIsPublished(Boolean isPublished)
   {
      this.isPublished = isPublished;
      return (this);
   }



   /*******************************************************************************
    ** Getter for publishDate
    *******************************************************************************/
   public Instant getPublishDate()
   {
      return (this.publishDate);
   }



   /*******************************************************************************
    ** Setter for publishDate
    *******************************************************************************/
   public void setPublishDate(Instant publishDate)
   {
      this.publishDate = publishDate;
   }



   /*******************************************************************************
    ** Fluent setter for publishDate
    *******************************************************************************/
   public CmsPage withPublishDate(Instant publishDate)
   {
      this.publishDate = publishDate;
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
   public CmsPage withSortOrder(Integer sortOrder)
   {
      this.sortOrder = sortOrder;
      return (this);
   }



   /*******************************************************************************
    ** Getter for parentPageId
    *******************************************************************************/
   public Long getParentPageId()
   {
      return (this.parentPageId);
   }



   /*******************************************************************************
    ** Setter for parentPageId
    *******************************************************************************/
   public void setParentPageId(Long parentPageId)
   {
      this.parentPageId = parentPageId;
   }



   /*******************************************************************************
    ** Fluent setter for parentPageId
    *******************************************************************************/
   public CmsPage withParentPageId(Long parentPageId)
   {
      this.parentPageId = parentPageId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for siteId
    *******************************************************************************/
   public Long getSiteId()
   {
      return (this.siteId);
   }



   /*******************************************************************************
    ** Setter for siteId
    *******************************************************************************/
   public void setSiteId(Long siteId)
   {
      this.siteId = siteId;
   }



   /*******************************************************************************
    ** Fluent setter for siteId
    *******************************************************************************/
   public CmsPage withSiteId(Long siteId)
   {
      this.siteId = siteId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for metaDescription
    *******************************************************************************/
   public String getMetaDescription()
   {
      return (this.metaDescription);
   }



   /*******************************************************************************
    ** Setter for metaDescription
    *******************************************************************************/
   public void setMetaDescription(String metaDescription)
   {
      this.metaDescription = metaDescription;
   }



   /*******************************************************************************
    ** Fluent setter for metaDescription
    *******************************************************************************/
   public CmsPage withMetaDescription(String metaDescription)
   {
      this.metaDescription = metaDescription;
      return (this);
   }



   /*******************************************************************************
    ** Getter for metaKeywords
    *******************************************************************************/
   public String getMetaKeywords()
   {
      return (this.metaKeywords);
   }



   /*******************************************************************************
    ** Setter for metaKeywords
    *******************************************************************************/
   public void setMetaKeywords(String metaKeywords)
   {
      this.metaKeywords = metaKeywords;
   }



   /*******************************************************************************
    ** Fluent setter for metaKeywords
    *******************************************************************************/
   public CmsPage withMetaKeywords(String metaKeywords)
   {
      this.metaKeywords = metaKeywords;
      return (this);
   }



   /*******************************************************************************
    ** Getter for ogImage
    *******************************************************************************/
   public String getOgImage()
   {
      return (this.ogImage);
   }



   /*******************************************************************************
    ** Setter for ogImage
    *******************************************************************************/
   public void setOgImage(String ogImage)
   {
      this.ogImage = ogImage;
   }



   /*******************************************************************************
    ** Fluent setter for ogImage
    *******************************************************************************/
   public CmsPage withOgImage(String ogImage)
   {
      this.ogImage = ogImage;
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
   public CmsPage withCreateDate(Instant createDate)
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
   public CmsPage withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }

}
