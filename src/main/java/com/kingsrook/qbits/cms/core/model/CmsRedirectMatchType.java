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


import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;


/*******************************************************************************
 ** How a redirect's source path matches a request path. Its possible-value
 ** source is named after the redirect table (cms_redirect_match_type).
 *******************************************************************************/
public enum CmsRedirectMatchType implements PossibleValueEnum<String>
{
   EXACT("Exact path"),
   PREFIX("Path prefix"),
   REGEX("Regular expression");

   /** The logical possible-value source name (the prefix is added). */
   public static final String NAME = "redirect_match_type";

   private final String label;



   /*******************************************************************************
    ** Constructor.
    *******************************************************************************/
   CmsRedirectMatchType(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** The stored value.
    *******************************************************************************/
   @Override
   public String getPossibleValueId()
   {
      return (name());
   }



   /*******************************************************************************
    ** The label.
    *******************************************************************************/
   @Override
   public String getPossibleValueLabel()
   {
      return (label);
   }
}
