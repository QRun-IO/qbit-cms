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


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.kingsrook.qbits.cms.metadata.CmsAppBuilder;
import com.kingsrook.qbits.cms.metadata.CmsTableBuilder;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerMultiOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Produces the CMS QBit: standard QQQ tables for the enabled features, their
 ** possible-value sources, joins and child lists, the CMS app with one home
 ** per area, and permission rules on everything (spec QRun-IO/qqq#1083).
 **
 ** <pre>
 ** new CmsQBitProducer()
 **    .withQBitConfig(new CmsQBitConfig()
 **       .withBackendName("rdbms")
 **       .withRole("cms-admins", CmsRole.ADMIN))
 **    .produce(qInstance);
 ** </pre>
 **
 ** The database comes from the qbit's Liquibase changelog
 ** (CmsQBitConfig.CHANGELOG), which the host includes and runs; its
 ** cms.tablePrefix property must equal the config's tableNamePrefix. At
 ** startup the producer checks that the changelog ran (startupMode).
 **
 ** The producer builds everything itself instead of scanning for component
 ** producers, so the output depends only on the config.
 *******************************************************************************/
public class CmsQBitProducer implements QBitMetaDataProducer<CmsQBitConfig>
{
   private static final QLogger LOG = QLogger.getLogger(CmsQBitProducer.class);

   public static final String GROUP_ID    = "com.kingsrook.qbits";
   public static final String ARTIFACT_ID = "qbit-cms";
   public static final String VERSION     = "0.1.0";

   private CmsQBitConfig qBitConfig;
   private String        namespace;



   /*******************************************************************************
    ** Fluent setter for the config.
    *******************************************************************************/
   public CmsQBitProducer withQBitConfig(CmsQBitConfig config)
   {
      this.qBitConfig = config;
      return (this);
   }



   /*******************************************************************************
    ** Getter for the config.
    *******************************************************************************/
   @Override
   public CmsQBitConfig getQBitConfig()
   {
      return (qBitConfig);
   }



   /*******************************************************************************
    ** Fluent setter for the namespace. A second install in one instance needs
    ** its own namespace; the namespace becomes the table prefix, name prefix
    ** and permission base unless the config sets them.
    *******************************************************************************/
   public CmsQBitProducer withNamespace(String namespace)
   {
      this.namespace = namespace;
      return (this);
   }



   /*******************************************************************************
    ** Getter for the namespace.
    *******************************************************************************/
   @Override
   public String getNamespace()
   {
      return (namespace);
   }



   /*******************************************************************************
    ** The qbit's identity.
    *******************************************************************************/
   @Override
   public QBitMetaData getQBitMetaData()
   {
      return (new QBitMetaData().withGroupId(GROUP_ID).withArtifactId(ARTIFACT_ID).withVersion(VERSION).withNamespace(namespace).withConfig(qBitConfig));
   }



   /*******************************************************************************
    ** Producers found by package scanning have no config: skip them.
    *******************************************************************************/
   @Override
   public boolean isEnabled()
   {
      return (qBitConfig != null);
   }



   /*******************************************************************************
    ** Validate, build and add everything to the instance, then check the
    ** schema. Returns an empty output, so a host that also adds the output to
    ** the instance adds nothing twice.
    *******************************************************************************/
   @Override
   public MetaDataProducerMultiOutput produce(QInstance qInstance) throws QException
   {
      if(qBitConfig == null)
      {
         throw (new QException("CMS QBit configuration validation failed: no config was supplied (call withQBitConfig)"));
      }
      applyNamespace();

      List<String> errors = new ArrayList<>();
      qBitConfig.validate(qInstance, errors);
      if(!errors.isEmpty())
      {
         throw (new QException("CMS QBit configuration validation failed: " + String.join("; ", errors)));
      }

      CmsTableBuilder.Built       tables = new CmsTableBuilder(qBitConfig).build(qInstance);
      CmsAppBuilder.Built         apps   = new CmsAppBuilder(qBitConfig).build();
      MetaDataProducerMultiOutput output = new MetaDataProducerMultiOutput();
      tables.tables().values().forEach(output::add);
      tables.possibleValueSources().forEach(output::add);
      tables.joins().forEach(output::add);
      tables.widgets().forEach(output::add);
      apps.widgets().forEach(output::add);
      apps.apps().forEach(output::add);

      List<String> collisions = collisions(qInstance, output);
      if(!collisions.isEmpty())
      {
         throw (new QException("CMS QBit names collide with names already on the instance (set a different tableNamePrefix and namePrefix, or a namespace): "
            + String.join(", ", collisions)));
      }

      QBitMetaData qBitMetaData = getQBitMetaData();
      qInstance.addQBit(qBitMetaData);
      for(QTableMetaData table : output.getEach(QTableMetaData.class))
      {
         if(qBitConfig.getTableMetaDataCustomizer() != null)
         {
            qBitConfig.getTableMetaDataCustomizer().customizeMetaData(qInstance, table);
         }
         table.setSourceQBitName(qBitMetaData.getName());
      }
      output.addSelfToInstance(qInstance);

      if(StringUtils.hasContent(qBitConfig.getParentAppName()))
      {
         qInstance.getApp(qBitConfig.getParentAppName()).withChild(qInstance.getApp(qBitConfig.getEffectiveAppName()));
      }

      postProduceActions(output, qInstance);
      return (new MetaDataProducerMultiOutput());
   }



   /*******************************************************************************
    ** Check the schema per the startup mode and set the runtime.
    *******************************************************************************/
   @Override
   public void postProduceActions(MetaDataProducerMultiOutput output, QInstance qInstance) throws QException
   {
      List<String> problems = qBitConfig.getStartupMode() == CmsStartupMode.SKIP ? List.of() : CmsSchemaCheck.check(qInstance, qBitConfig);
      if(!problems.isEmpty() && qBitConfig.getStartupMode() == CmsStartupMode.FAIL_FAST)
      {
         throw (new QException("The CMS database is not up to date (startupMode FAIL_FAST): " + String.join(" ", problems)));
      }
      if(!problems.isEmpty())
      {
         LOG.warn("The CMS database is not up to date; starting degraded", logPair("problems", problems));
      }
      qBitConfig.setRuntime(new CmsRuntime(problems));

      LOG.info("CMS QBit produced", logPair("version", VERSION), logPair("backendName", qBitConfig.getBackendName()),
         logPair("tablePrefix", qBitConfig.getTableNamePrefix()), logPair("features", qBitConfig.getFeatures()),
         logPair("app", qBitConfig.getEffectiveAppName()), logPair("startupMode", qBitConfig.getStartupMode()));
   }



   /*******************************************************************************
    ** A namespace fills in prefixes the config leaves at their defaults.
    *******************************************************************************/
   private void applyNamespace()
   {
      if(!StringUtils.hasContent(namespace))
      {
         return;
      }
      String snake = namespace.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
      if(CmsQBitConfig.DEFAULT_TABLE_NAME_PREFIX.equals(qBitConfig.getTableNamePrefix()))
      {
         qBitConfig.setTableNamePrefix(snake.endsWith("_") ? snake : snake + "_");
      }
      if(CmsQBitConfig.DEFAULT_NAME_PREFIX.equals(qBitConfig.getNamePrefix()))
      {
         StringBuilder camel = new StringBuilder();
         for(String part : snake.split("_"))
         {
            if(!part.isEmpty())
            {
               camel.append(camel.isEmpty() ? part : Character.toUpperCase(part.charAt(0)) + part.substring(1));
            }
         }
         qBitConfig.setNamePrefix(camel.toString());
      }
      if(CmsQBitConfig.DEFAULT_PERMISSION_BASE.equals(qBitConfig.getPermissionBaseName()))
      {
         qBitConfig.setPermissionBaseName(snake.replaceAll("_+$", ""));
      }
   }



   /*******************************************************************************
    ** Names the qbit would add that the instance already has.
    *******************************************************************************/
   static List<String> collisions(QInstance qInstance, MetaDataProducerMultiOutput output)
   {
      List<String> rs = new ArrayList<>();
      output.getEach(QTableMetaData.class).stream().filter(t -> qInstance.getTable(t.getName()) != null).forEach(t -> rs.add("table " + t.getName()));
      output.getEach(QPossibleValueSource.class).stream().filter(p -> qInstance.getPossibleValueSource(p.getName()) != null).forEach(p -> rs.add("possible-value source " + p.getName()));
      output.getEach(QJoinMetaData.class).stream().filter(j -> qInstance.getJoin(j.getName()) != null).forEach(j -> rs.add("join " + j.getName()));
      output.getEach(QWidgetMetaData.class).stream().filter(w -> qInstance.getWidget(w.getName()) != null).forEach(w -> rs.add("widget " + w.getName()));
      output.getEach(QAppMetaData.class).stream().filter(a -> qInstance.getApp(a.getName()) != null).forEach(a -> rs.add("app " + a.getName()));
      return (rs);
   }
}
