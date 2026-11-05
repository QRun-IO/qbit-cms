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

package com.kingsrook.qbits.cms.security;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.permissions.CustomPermissionChecker;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QPermissionDeniedException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.replace.ReplaceInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.MetaDataWithPermissionRules;
import com.kingsrook.qqq.backend.core.model.session.QSession;


/*******************************************************************************
 ** Table permissions when CmsQBitConfig.openReads is on: reading needs no
 ** permission; an insert, update, delete or replace needs the table's
 ** .insert, .edit or .delete permission (the same names as the standard
 ** READ_INSERT_EDIT_DELETE rules, so roles grant the same names either way).
 **
 ** Only write inputs count as writes. Other permission questions (table meta
 ** data, which pass a plain table input) are answered as reads.
 *******************************************************************************/
public class CmsOpenReadsPermissionChecker implements CustomPermissionChecker
{

   /*******************************************************************************
    ** Throw if the current session may not perform the write the input describes.
    *******************************************************************************/
   @Override
   public void checkPermissionsThrowing(AbstractActionInput actionInput, MetaDataWithPermissionRules metaDataWithPermissionRules) throws QPermissionDeniedException
   {
      QSession session = QContext.getQSession();
      for(String suffix : requiredWriteSuffixes(actionInput))
      {
         if(session == null || !session.hasPermission(metaDataWithPermissionRules.getName() + "." + suffix))
         {
            throw (new QPermissionDeniedException("Permission denied."));
         }
      }
   }



   /*******************************************************************************
    ** The write permission suffixes an input requires (empty for reads).
    *******************************************************************************/
   static List<String> requiredWriteSuffixes(AbstractActionInput actionInput)
   {
      List<String> rs = new ArrayList<>();
      if(actionInput instanceof InsertInput)
      {
         rs.add("insert");
      }
      else if(actionInput instanceof UpdateInput)
      {
         rs.add("edit");
      }
      else if(actionInput instanceof DeleteInput)
      {
         rs.add("delete");
      }
      else if(actionInput instanceof ReplaceInput replaceInput)
      {
         rs.add("insert");
         rs.add("edit");
         if(Boolean.TRUE.equals(replaceInput.getPerformDeletes()))
         {
            rs.add("delete");
         }
      }
      return (rs);
   }
}
