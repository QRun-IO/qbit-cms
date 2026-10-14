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


/*******************************************************************************
 ** What the qbit does when its startup schema check finds the database
 ** behind the code (spec section 3.4, the "install doctor").
 *******************************************************************************/
public enum CmsStartupMode
{
   /** Refuse to start, listing what is missing (the production default). */
   FAIL_FAST,

   /** Start, log the problems and show them on the CMS home. */
   DEGRADED,

   /** Do not check (unit tests that build meta data without a database). */
   SKIP
}
