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


import java.util.List;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;


/*******************************************************************************
 ** Runtime state of one CMS install: its config and the result of the startup
 ** schema check. Code that runs per request (widget renderers, processes)
 ** finds it by the install's name prefix, so two installs in one instance
 ** (two namespaces) stay apart.
 *******************************************************************************/
public class CmsRuntime
{
   private final CmsQBitConfig config;
   private final List<String>  schemaProblems;



   /*******************************************************************************
    ** Constructor.
    **
    ** @param config         the install's config
    ** @param schemaProblems what the startup schema check found (empty when fine
    **                       or not checked)
    *******************************************************************************/
   public CmsRuntime(CmsQBitConfig config, List<String> schemaProblems)
   {
      this.config = config;
      this.schemaProblems = List.copyOf(schemaProblems);
   }



   /*******************************************************************************
    ** The config of the CMS install with a name prefix on an instance.
    **
    ** @param qInstance  the instance
    ** @param namePrefix the install's name prefix (CmsQBitConfig.namePrefix)
    ** @return the config, if installed
    *******************************************************************************/
   public static Optional<CmsQBitConfig> findConfig(QInstance qInstance, String namePrefix)
   {
      if(qInstance == null || qInstance.getQBits() == null)
      {
         return (Optional.empty());
      }
      for(QBitMetaData qBit : qInstance.getQBits().values())
      {
         if(qBit.getConfig() instanceof CmsQBitConfig config && config.getNamePrefix().equals(namePrefix))
         {
            return (Optional.of(config));
         }
      }
      return (Optional.empty());
   }



   /*******************************************************************************
    ** Getter for config
    *******************************************************************************/
   public CmsQBitConfig getConfig()
   {
      return (config);
   }



   /*******************************************************************************
    ** Getter for schemaProblems
    *******************************************************************************/
   public List<String> getSchemaProblems()
   {
      return (schemaProblems);
   }
}
