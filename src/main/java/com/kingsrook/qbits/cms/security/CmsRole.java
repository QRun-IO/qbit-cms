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


/*******************************************************************************
 ** The CMS role bundles (spec section 6.2). A host maps its identity
 ** provider's groups to roles in CmsQBitConfig.roleMapping, and
 ** CmsSecurity.applySessionPermissions turns a user's groups into the
 ** permissions of their roles.
 *******************************************************************************/
public enum CmsRole
{
   /** Everything, including reference data (types) and the admin permission. */
   ADMIN,

   /** Read and write content; read reference data. No publishing. */
   EDITOR,

   /** EDITOR plus publishing. */
   PUBLISHER,

   /** Read content, create and edit drafts; no deletes, no publishing. */
   DRAFTER,

   /** Read everything. */
   VIEWER
}
