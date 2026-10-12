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

package com.kingsrook.qbits.cms;


import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kingsrook.qbits.cms.security.CmsRole;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitConfig;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;


/*******************************************************************************
 ** Host-facing configuration for the CMS QBit (spec section 2.4).
 **
 ** Tables are named tableNamePrefix + lowercase snake_case (cms_page by
 ** default); the QQQ table name equals the backend (SQL) table name, and the
 ** prefix must equal the cms.tablePrefix property the host gives the
 ** changelog. Apps and widgets are named namePrefix + CamelCase (cmsPages).
 ** Table permissions are the table name plus .read, .insert, .edit or .delete
 ** (cms_page.edit); qbit-wide permissions are permissionBaseName + .action
 ** (cms.admin).
 *******************************************************************************/
public class CmsQBitConfig implements QBitConfig
{
   public static final String DEFAULT_TABLE_NAME_PREFIX   = "cms_";
   public static final String DEFAULT_NAME_PREFIX         = "cms";
   public static final String DEFAULT_PERMISSION_BASE     = "cms";
   public static final String DEFAULT_APP_LABEL           = "CMS";
   public static final String CHANGELOG                   = "qbit-cms/db/changelog/qbit-cms.changelog.yaml";
   public static final String CHANGELOG_LOGICAL_FILE_PATH = "qbit-cms/changelog";

   private static final Pattern TABLE_PREFIX = Pattern.compile("^[a-z][a-z0-9_]{0,20}_$");
   private static final Pattern NAME_PREFIX  = Pattern.compile("^[a-z][a-zA-Z0-9]{0,20}$");

   private String               backendName;
   private String               tableNamePrefix    = DEFAULT_TABLE_NAME_PREFIX;
   private String               namePrefix         = DEFAULT_NAME_PREFIX;
   private String               permissionBaseName = DEFAULT_PERMISSION_BASE;
   private Set<CmsFeature>      features           = CmsFeature.allAvailable();
   private String               appName;
   private String               appLabel           = DEFAULT_APP_LABEL;
   private String               parentAppName;
   private Boolean              openReads          = false;
   private CmsStartupMode       startupMode        = CmsStartupMode.FAIL_FAST;
   private Map<String, CmsRole> roleMapping        = new LinkedHashMap<>();

   private CmsRuntime runtime;



   /*******************************************************************************
    ** Validate against the instance: an RDBMS backend, well-formed prefixes,
    ** a consistent feature set, and a parent app that exists.
    *******************************************************************************/
   @Override
   public void validate(QInstance qInstance, List<String> errors)
   {
      if(assertCondition(StringUtils.hasContent(backendName), "backendName is required for the CMS QBit", errors))
      {
         QBackendMetaData backend = qInstance.getBackend(backendName);
         if(assertCondition(backend != null, "backendName " + backendName + " is not a backend on this instance", errors))
         {
            assertCondition(backend instanceof RDBMSBackendMetaData, "backend " + backendName + " must be an RDBMS backend (the CMS uses SQL joins and views)", errors);
         }
      }
      assertCondition(tableNamePrefix != null && TABLE_PREFIX.matcher(tableNamePrefix).matches(),
         "tableNamePrefix must be lowercase letters, digits and underscores, ending in _ (for example cms_): " + tableNamePrefix, errors);
      assertCondition(namePrefix != null && NAME_PREFIX.matcher(namePrefix).matches(),
         "namePrefix must start with a lowercase letter and be letters and digits (for example cms): " + namePrefix, errors);
      assertCondition(StringUtils.hasContent(permissionBaseName), "permissionBaseName is required", errors);
      errors.addAll(CmsFeature.validate(features));
      if(StringUtils.hasContent(parentAppName))
      {
         assertCondition(qInstance.getApp(parentAppName) != null, "parentAppName " + parentAppName + " is not an app on this instance (define it before producing the CMS)", errors);
      }
      assertCondition(roleMapping != null, "roleMapping may be empty but not null", errors);
      assertCondition(startupMode != null, "startupMode is required", errors);
   }



   /*******************************************************************************
    ** Tables without their own backend use the configured backend.
    *******************************************************************************/
   @Override
   public String getDefaultBackendNameForTables()
   {
      return (backendName);
   }



   /*******************************************************************************
    ** The physical (and QQQ) table name of a logical table name.
    **
    ** @param logicalTableName e.g. "page"
    ** @return e.g. "cms_page"
    *******************************************************************************/
   public String applyPrefix(String logicalTableName)
   {
      return (tableNamePrefix + logicalTableName);
   }



   /*******************************************************************************
    ** A prefixed app, widget or process name.
    **
    ** @param suffix e.g. "Pages" or "PagesHome"
    ** @return e.g. "cmsPages"
    *******************************************************************************/
   public String name(String suffix)
   {
      return (namePrefix + suffix);
   }



   /*******************************************************************************
    ** The name of the CMS top app (appName, else the name prefix).
    *******************************************************************************/
   public String getEffectiveAppName()
   {
      return (StringUtils.hasContent(appName) ? appName : namePrefix);
   }



   /*******************************************************************************
    ** A qbit-wide permission name.
    **
    ** @param action e.g. "admin"
    ** @return e.g. "cms.admin"
    *******************************************************************************/
   public String permission(String action)
   {
      return (permissionBaseName + "." + action);
   }



   /*******************************************************************************
    ** Whether a feature is on.
    *******************************************************************************/
   public boolean hasFeature(CmsFeature feature)
   {
      return (features != null && features.contains(feature));
   }



   /*******************************************************************************
    ** Description for logs (the config holds no secrets yet).
    *******************************************************************************/
   @Override
   public String toString()
   {
      return ("CmsQBitConfig{backendName=" + backendName + ", tableNamePrefix=" + tableNamePrefix + ", namePrefix=" + namePrefix
         + ", features=" + features + ", app=" + getEffectiveAppName() + ", parentAppName=" + parentAppName + ", openReads=" + openReads
         + ", startupMode=" + startupMode + ", roleMapping=" + roleMapping + "}");
   }



   /*******************************************************************************
    ** Getter for backendName
    *******************************************************************************/
   public String getBackendName()
   {
      return (this.backendName);
   }



   /*******************************************************************************
    ** Setter for backendName
    *******************************************************************************/
   public void setBackendName(String backendName)
   {
      this.backendName = backendName;
   }



   /*******************************************************************************
    ** Fluent setter for backendName
    *******************************************************************************/
   public CmsQBitConfig withBackendName(String backendName)
   {
      this.backendName = backendName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableNamePrefix
    *******************************************************************************/
   public String getTableNamePrefix()
   {
      return (this.tableNamePrefix);
   }



   /*******************************************************************************
    ** Setter for tableNamePrefix
    *******************************************************************************/
   public void setTableNamePrefix(String tableNamePrefix)
   {
      this.tableNamePrefix = tableNamePrefix;
   }



   /*******************************************************************************
    ** Fluent setter for tableNamePrefix
    *******************************************************************************/
   public CmsQBitConfig withTableNamePrefix(String tableNamePrefix)
   {
      this.tableNamePrefix = tableNamePrefix;
      return (this);
   }



   /*******************************************************************************
    ** Getter for namePrefix
    *******************************************************************************/
   public String getNamePrefix()
   {
      return (this.namePrefix);
   }



   /*******************************************************************************
    ** Setter for namePrefix
    *******************************************************************************/
   public void setNamePrefix(String namePrefix)
   {
      this.namePrefix = namePrefix;
   }



   /*******************************************************************************
    ** Fluent setter for namePrefix
    *******************************************************************************/
   public CmsQBitConfig withNamePrefix(String namePrefix)
   {
      this.namePrefix = namePrefix;
      return (this);
   }



   /*******************************************************************************
    ** Getter for permissionBaseName
    *******************************************************************************/
   public String getPermissionBaseName()
   {
      return (this.permissionBaseName);
   }



   /*******************************************************************************
    ** Setter for permissionBaseName
    *******************************************************************************/
   public void setPermissionBaseName(String permissionBaseName)
   {
      this.permissionBaseName = permissionBaseName;
   }



   /*******************************************************************************
    ** Fluent setter for permissionBaseName
    *******************************************************************************/
   public CmsQBitConfig withPermissionBaseName(String permissionBaseName)
   {
      this.permissionBaseName = permissionBaseName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for features
    *******************************************************************************/
   public Set<CmsFeature> getFeatures()
   {
      return (this.features);
   }



   /*******************************************************************************
    ** Setter for features
    *******************************************************************************/
   public void setFeatures(Set<CmsFeature> features)
   {
      this.features = features == null || features.isEmpty() ? EnumSet.noneOf(CmsFeature.class) : EnumSet.copyOf(features);
   }



   /*******************************************************************************
    ** Fluent setter for features
    *******************************************************************************/
   public CmsQBitConfig withFeatures(Set<CmsFeature> features)
   {
      setFeatures(features);
      return (this);
   }



   /*******************************************************************************
    ** Getter for appName
    *******************************************************************************/
   public String getAppName()
   {
      return (this.appName);
   }



   /*******************************************************************************
    ** Setter for appName
    *******************************************************************************/
   public void setAppName(String appName)
   {
      this.appName = appName;
   }



   /*******************************************************************************
    ** Fluent setter for appName
    *******************************************************************************/
   public CmsQBitConfig withAppName(String appName)
   {
      this.appName = appName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for appLabel
    *******************************************************************************/
   public String getAppLabel()
   {
      return (this.appLabel);
   }



   /*******************************************************************************
    ** Setter for appLabel
    *******************************************************************************/
   public void setAppLabel(String appLabel)
   {
      this.appLabel = appLabel;
   }



   /*******************************************************************************
    ** Fluent setter for appLabel
    *******************************************************************************/
   public CmsQBitConfig withAppLabel(String appLabel)
   {
      this.appLabel = appLabel;
      return (this);
   }



   /*******************************************************************************
    ** Getter for parentAppName
    *******************************************************************************/
   public String getParentAppName()
   {
      return (this.parentAppName);
   }



   /*******************************************************************************
    ** Setter for parentAppName
    *******************************************************************************/
   public void setParentAppName(String parentAppName)
   {
      this.parentAppName = parentAppName;
   }



   /*******************************************************************************
    ** Fluent setter for parentAppName
    *******************************************************************************/
   public CmsQBitConfig withParentAppName(String parentAppName)
   {
      this.parentAppName = parentAppName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for openReads
    *******************************************************************************/
   public Boolean getOpenReads()
   {
      return (this.openReads);
   }



   /*******************************************************************************
    ** Setter for openReads
    *******************************************************************************/
   public void setOpenReads(Boolean openReads)
   {
      this.openReads = openReads;
   }



   /*******************************************************************************
    ** Fluent setter for openReads: when true, reading CMS tables needs no
    ** permission (writes always do).
    *******************************************************************************/
   public CmsQBitConfig withOpenReads(Boolean openReads)
   {
      this.openReads = openReads;
      return (this);
   }



   /*******************************************************************************
    ** Getter for startupMode
    *******************************************************************************/
   public CmsStartupMode getStartupMode()
   {
      return (this.startupMode);
   }



   /*******************************************************************************
    ** Setter for startupMode
    *******************************************************************************/
   public void setStartupMode(CmsStartupMode startupMode)
   {
      this.startupMode = startupMode;
   }



   /*******************************************************************************
    ** Fluent setter for startupMode
    *******************************************************************************/
   public CmsQBitConfig withStartupMode(CmsStartupMode startupMode)
   {
      this.startupMode = startupMode;
      return (this);
   }



   /*******************************************************************************
    ** Getter for roleMapping
    *******************************************************************************/
   public Map<String, CmsRole> getRoleMapping()
   {
      return (this.roleMapping);
   }



   /*******************************************************************************
    ** Setter for roleMapping
    *******************************************************************************/
   public void setRoleMapping(Map<String, CmsRole> roleMapping)
   {
      this.roleMapping = roleMapping;
   }



   /*******************************************************************************
    ** Fluent setter for roleMapping: identity-provider group name to CMS role.
    *******************************************************************************/
   public CmsQBitConfig withRoleMapping(Map<String, CmsRole> roleMapping)
   {
      this.roleMapping = roleMapping;
      return (this);
   }



   /*******************************************************************************
    ** The install's runtime state, set when the qbit is produced (not part of
    ** the config's JSON: tables carry their source qbit config to the frontend).
    *******************************************************************************/
   @JsonIgnore
   public CmsRuntime getRuntime()
   {
      return (this.runtime);
   }



   /*******************************************************************************
    ** Setter for runtime (the producer sets it).
    *******************************************************************************/
   public void setRuntime(CmsRuntime runtime)
   {
      this.runtime = runtime;
   }



   /*******************************************************************************
    ** Fluent adder for one roleMapping entry.
    *******************************************************************************/
   public CmsQBitConfig withRole(String groupName, CmsRole role)
   {
      if(this.roleMapping == null)
      {
         this.roleMapping = new LinkedHashMap<>();
      }
      this.roleMapping.put(groupName, role);
      return (this);
   }
}
