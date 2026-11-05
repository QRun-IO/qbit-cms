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
 ** What the startup schema check reads to decide the database is up to date.
 *******************************************************************************/
public enum CmsSchemaVerification
{
   /**
    * The Liquibase history (DATABASECHANGELOG) records every changeset and the tables exist
    * (the original check).  Needs read access to the history table, which a DML-only
    * runtime identity does not have.
    */
   CHANGELOG_HISTORY,

   /**
    * Only the database catalog: every table of the installed features exists and has every
    * column the qbit's metadata maps.  Needs no Liquibase tables and no privilege beyond
    * connecting, so it is the check for a runtime identity that owns nothing and may not
    * read migration history.
    */
   CATALOG
}
