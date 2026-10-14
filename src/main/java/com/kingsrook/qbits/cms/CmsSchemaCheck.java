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


import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.kingsrook.qbits.cms.metadata.CmsTableDefinition;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;


/*******************************************************************************
 ** The startup schema check, the "install doctor" (spec section 3.4): the
 ** qbit's changelog has run for every enabled feature (its create changesets
 ** are recorded in DATABASECHANGELOG under the qbit's logical file path) and
 ** every enabled table exists. The qbit never creates or alters anything; it
 ** only reports, and the startup mode decides whether that stops the app.
 *******************************************************************************/
public final class CmsSchemaCheck
{
   /** The baseline version of the changesets that create the tables. */
   static final String BASELINE = "1.0.0";



   /*******************************************************************************
    ** Not instantiable.
    *******************************************************************************/
   private CmsSchemaCheck()
   {
   }



   /*******************************************************************************
    ** The problems with the database behind a config (empty when it is fine).
    **
    ** @param qInstance the instance (for the backend)
    ** @param config    the CMS config
    ** @return one message per problem
    *******************************************************************************/
   public static List<String> check(QInstance qInstance, CmsQBitConfig config)
   {
      List<String> problems = new ArrayList<>();
      try(Connection connection = ConnectionManager.getConnection((RDBMSBackendMetaData) qInstance.getBackend(config.getBackendName())))
      {
         Set<String> recorded = recordedChangesets(connection);
         if(recorded == null)
         {
            problems.add("The qbit-cms changelog has not been run: there is no DATABASECHANGELOG table. Include "
               + CmsQBitConfig.CHANGELOG + " in your Liquibase changelog and run your migrations.");
            return (problems);
         }

         for(CmsTableDefinition definition : CmsTables.definitions(config))
         {
            String table = config.applyPrefix(definition.logicalName());
            if(!recorded.contains(BASELINE + "-create-" + definition.logicalName()))
            {
               problems.add("Changeset " + BASELINE + "-create-" + definition.logicalName() + " (" + table + ") has not run: run your migrations with "
                  + "cms.tablePrefix=" + config.getTableNamePrefix() + " and the " + definition.feature() + " feature in cms.features.");
            }
            else if(!tableExists(connection, table))
            {
               problems.add("Table " + table + " is missing although its changeset is recorded.");
            }
         }
      }
      catch(SQLException e)
      {
         problems.add("Could not check the CMS schema: " + e.getMessage());
      }
      return (problems);
   }



   /*******************************************************************************
    ** The qbit's recorded changeset ids, or null when Liquibase has never run.
    *******************************************************************************/
   static Set<String> recordedChangesets(Connection connection) throws SQLException
   {
      if(!tableExists(connection, "databasechangelog"))
      {
         return (null);
      }
      Set<String> ids = new HashSet<>();
      try(PreparedStatement statement = connection.prepareStatement("SELECT ID FROM DATABASECHANGELOG WHERE FILENAME = ?"))
      {
         statement.setString(1, CmsQBitConfig.CHANGELOG_LOGICAL_FILE_PATH);
         try(ResultSet resultSet = statement.executeQuery())
         {
            while(resultSet.next())
            {
               ids.add(resultSet.getString(1));
            }
         }
      }
      return (ids);
   }



   /*******************************************************************************
    ** Whether a table (or view) exists, in any case the database folds names to.
    *******************************************************************************/
   static boolean tableExists(Connection connection, String name) throws SQLException
   {
      DatabaseMetaData metaData = connection.getMetaData();
      for(String candidate : List.of(name, name.toUpperCase(Locale.ROOT), name.toLowerCase(Locale.ROOT)))
      {
         try(ResultSet resultSet = metaData.getTables(connection.getCatalog(), null, candidate, new String[] { "TABLE", "VIEW" }))
         {
            if(resultSet.next())
            {
               return (true);
            }
         }
      }
      return (false);
   }
}
