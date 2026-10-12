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
 ** A public URL redirect: requests matching the source path go to the target
 ** path.
 **
 ** The physical table name is the configured prefix plus TABLE_NAME (for
 ** example cms_redirect); possible-value source names on the fields are
 ** logical and get the same prefix when the table is built.
 *******************************************************************************/
public class CmsRedirect extends QRecordEntity
{
   public static final String TABLE_NAME = "redirect";

   @QField(isEditable = false, isPrimaryKey = true)
   private Long id;

   @QField(isRequired = true, maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String name;

   @QField(maxLength = 500, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE)
   private String description;

   @QField(isRequired = true, maxLength = 500, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String sourcePath;

   @QField(isRequired = true, maxLength = 500, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String targetPath;

   @QField(isRequired = true, possibleValueSourceName = "redirect_type", label = "Redirect Type")
   private Long redirectTypeId;

   @QField(isRequired = true, maxLength = 20, valueTooLongBehavior = ValueTooLongBehavior.ERROR, defaultValue = "EXACT")
   private String matchType;

   @QField(isRequired = true, defaultValue = "true")
   private Boolean isActive;

   @QField(isRequired = true, defaultValue = "0")
   private Integer priority;

   @QField(isRequired = true, defaultValue = "true")
   private Boolean preserveQueryParams;

   @QField(isEditable = false, defaultValue = "0")
   private Long hitCount;

   @QField(isEditable = false)
   private Instant lastHitDate;

   @QField()
   private Instant expiryDate;

   @QField(isRequired = true, possibleValueSourceName = "site", label = "Site")
   private Long siteId;

   @QField(isEditable = false, dynamicDefaultValueBehavior = DynamicDefaultValueBehavior.CREATE_DATE)
   private Instant createDate;

   @QField(isEditable = false, dynamicDefaultValueBehavior = DynamicDefaultValueBehavior.MODIFY_DATE)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public CmsRedirect()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public CmsRedirect(QRecord record) throws QException
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
   public CmsRedirect withId(Long id)
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
   public CmsRedirect withName(String name)
   {
      this.name = name;
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
   public CmsRedirect withDescription(String description)
   {
      this.description = description;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sourcePath
    *******************************************************************************/
   public String getSourcePath()
   {
      return (this.sourcePath);
   }



   /*******************************************************************************
    ** Setter for sourcePath
    *******************************************************************************/
   public void setSourcePath(String sourcePath)
   {
      this.sourcePath = sourcePath;
   }



   /*******************************************************************************
    ** Fluent setter for sourcePath
    *******************************************************************************/
   public CmsRedirect withSourcePath(String sourcePath)
   {
      this.sourcePath = sourcePath;
      return (this);
   }



   /*******************************************************************************
    ** Getter for targetPath
    *******************************************************************************/
   public String getTargetPath()
   {
      return (this.targetPath);
   }



   /*******************************************************************************
    ** Setter for targetPath
    *******************************************************************************/
   public void setTargetPath(String targetPath)
   {
      this.targetPath = targetPath;
   }



   /*******************************************************************************
    ** Fluent setter for targetPath
    *******************************************************************************/
   public CmsRedirect withTargetPath(String targetPath)
   {
      this.targetPath = targetPath;
      return (this);
   }



   /*******************************************************************************
    ** Getter for redirectTypeId
    *******************************************************************************/
   public Long getRedirectTypeId()
   {
      return (this.redirectTypeId);
   }



   /*******************************************************************************
    ** Setter for redirectTypeId
    *******************************************************************************/
   public void setRedirectTypeId(Long redirectTypeId)
   {
      this.redirectTypeId = redirectTypeId;
   }



   /*******************************************************************************
    ** Fluent setter for redirectTypeId
    *******************************************************************************/
   public CmsRedirect withRedirectTypeId(Long redirectTypeId)
   {
      this.redirectTypeId = redirectTypeId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for matchType
    *******************************************************************************/
   public String getMatchType()
   {
      return (this.matchType);
   }



   /*******************************************************************************
    ** Setter for matchType
    *******************************************************************************/
   public void setMatchType(String matchType)
   {
      this.matchType = matchType;
   }



   /*******************************************************************************
    ** Fluent setter for matchType
    *******************************************************************************/
   public CmsRedirect withMatchType(String matchType)
   {
      this.matchType = matchType;
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
   public CmsRedirect withIsActive(Boolean isActive)
   {
      this.isActive = isActive;
      return (this);
   }



   /*******************************************************************************
    ** Getter for priority
    *******************************************************************************/
   public Integer getPriority()
   {
      return (this.priority);
   }



   /*******************************************************************************
    ** Setter for priority
    *******************************************************************************/
   public void setPriority(Integer priority)
   {
      this.priority = priority;
   }



   /*******************************************************************************
    ** Fluent setter for priority
    *******************************************************************************/
   public CmsRedirect withPriority(Integer priority)
   {
      this.priority = priority;
      return (this);
   }



   /*******************************************************************************
    ** Getter for preserveQueryParams
    *******************************************************************************/
   public Boolean getPreserveQueryParams()
   {
      return (this.preserveQueryParams);
   }



   /*******************************************************************************
    ** Setter for preserveQueryParams
    *******************************************************************************/
   public void setPreserveQueryParams(Boolean preserveQueryParams)
   {
      this.preserveQueryParams = preserveQueryParams;
   }



   /*******************************************************************************
    ** Fluent setter for preserveQueryParams
    *******************************************************************************/
   public CmsRedirect withPreserveQueryParams(Boolean preserveQueryParams)
   {
      this.preserveQueryParams = preserveQueryParams;
      return (this);
   }



   /*******************************************************************************
    ** Getter for hitCount
    *******************************************************************************/
   public Long getHitCount()
   {
      return (this.hitCount);
   }



   /*******************************************************************************
    ** Setter for hitCount
    *******************************************************************************/
   public void setHitCount(Long hitCount)
   {
      this.hitCount = hitCount;
   }



   /*******************************************************************************
    ** Fluent setter for hitCount
    *******************************************************************************/
   public CmsRedirect withHitCount(Long hitCount)
   {
      this.hitCount = hitCount;
      return (this);
   }



   /*******************************************************************************
    ** Getter for lastHitDate
    *******************************************************************************/
   public Instant getLastHitDate()
   {
      return (this.lastHitDate);
   }



   /*******************************************************************************
    ** Setter for lastHitDate
    *******************************************************************************/
   public void setLastHitDate(Instant lastHitDate)
   {
      this.lastHitDate = lastHitDate;
   }



   /*******************************************************************************
    ** Fluent setter for lastHitDate
    *******************************************************************************/
   public CmsRedirect withLastHitDate(Instant lastHitDate)
   {
      this.lastHitDate = lastHitDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for expiryDate
    *******************************************************************************/
   public Instant getExpiryDate()
   {
      return (this.expiryDate);
   }



   /*******************************************************************************
    ** Setter for expiryDate
    *******************************************************************************/
   public void setExpiryDate(Instant expiryDate)
   {
      this.expiryDate = expiryDate;
   }



   /*******************************************************************************
    ** Fluent setter for expiryDate
    *******************************************************************************/
   public CmsRedirect withExpiryDate(Instant expiryDate)
   {
      this.expiryDate = expiryDate;
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
   public CmsRedirect withSiteId(Long siteId)
   {
      this.siteId = siteId;
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
   public CmsRedirect withCreateDate(Instant createDate)
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
   public CmsRedirect withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }

}
