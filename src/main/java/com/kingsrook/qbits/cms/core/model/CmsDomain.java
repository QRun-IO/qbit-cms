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
 ** A host name (fully qualified domain name) of a site; one per site is
 ** primary.
 **
 ** The physical table name is the configured prefix plus TABLE_NAME (for
 ** example cms_domain); possible-value source names on the fields are
 ** logical and get the same prefix when the table is built.
 *******************************************************************************/
public class CmsDomain extends QRecordEntity
{
   public static final String TABLE_NAME = "domain";

   @QField(isEditable = false, isPrimaryKey = true)
   private Long id;

   @QField(isRequired = true, maxLength = 40, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String name;

   @QField(isRequired = true, maxLength = 20, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String shortName;

   @QField(maxLength = 255, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE, gridColumns = 12)
   private String description;

   @QField(isRequired = true, maxLength = 255, valueTooLongBehavior = ValueTooLongBehavior.ERROR, label = "FQDN")
   private String fqdn;

   @QField(isRequired = true, defaultValue = "false")
   private Boolean isPrimary;

   @QField(isRequired = true, possibleValueSourceName = "site", label = "Site")
   private Long siteId;

   @QField(isEditable = false, dynamicDefaultValueBehavior = DynamicDefaultValueBehavior.CREATE_DATE)
   private Instant createDate;

   @QField(isEditable = false, dynamicDefaultValueBehavior = DynamicDefaultValueBehavior.MODIFY_DATE)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public CmsDomain()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public CmsDomain(QRecord record) throws QException
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
   public CmsDomain withId(Long id)
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
   public CmsDomain withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for shortName
    *******************************************************************************/
   public String getShortName()
   {
      return (this.shortName);
   }



   /*******************************************************************************
    ** Setter for shortName
    *******************************************************************************/
   public void setShortName(String shortName)
   {
      this.shortName = shortName;
   }



   /*******************************************************************************
    ** Fluent setter for shortName
    *******************************************************************************/
   public CmsDomain withShortName(String shortName)
   {
      this.shortName = shortName;
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
   public CmsDomain withDescription(String description)
   {
      this.description = description;
      return (this);
   }



   /*******************************************************************************
    ** Getter for fqdn
    *******************************************************************************/
   public String getFqdn()
   {
      return (this.fqdn);
   }



   /*******************************************************************************
    ** Setter for fqdn
    *******************************************************************************/
   public void setFqdn(String fqdn)
   {
      this.fqdn = fqdn;
   }



   /*******************************************************************************
    ** Fluent setter for fqdn
    *******************************************************************************/
   public CmsDomain withFqdn(String fqdn)
   {
      this.fqdn = fqdn;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isPrimary
    *******************************************************************************/
   public Boolean getIsPrimary()
   {
      return (this.isPrimary);
   }



   /*******************************************************************************
    ** Setter for isPrimary
    *******************************************************************************/
   public void setIsPrimary(Boolean isPrimary)
   {
      this.isPrimary = isPrimary;
   }



   /*******************************************************************************
    ** Fluent setter for isPrimary
    *******************************************************************************/
   public CmsDomain withIsPrimary(Boolean isPrimary)
   {
      this.isPrimary = isPrimary;
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
   public CmsDomain withSiteId(Long siteId)
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
   public CmsDomain withCreateDate(Instant createDate)
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
   public CmsDomain withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }

}
