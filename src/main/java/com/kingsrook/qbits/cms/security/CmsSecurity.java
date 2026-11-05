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


import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import com.kingsrook.qbits.cms.CmsQBitConfig;
import com.kingsrook.qbits.cms.CmsTables;
import com.kingsrook.qbits.cms.metadata.CmsTableDefinition;
import com.kingsrook.qqq.backend.core.model.session.QSession;


/*******************************************************************************
 ** Turns identity-provider groups into CMS permissions (spec section 6.2).
 ** The host calls applySessionPermissions from its authentication customizer,
 ** the same integration point other qbits use; the qbit never talks to the
 ** identity provider itself.
 **
 ** Permission names: each table's .read, .insert, .edit and .delete
 ** (cms_page.edit), plus permissionBaseName.admin and .publish.
 *******************************************************************************/
public final class CmsSecurity
{
   public static final String ADMIN   = "admin";
   public static final String PUBLISH = "publish";



   /*******************************************************************************
    ** Not instantiable.
    *******************************************************************************/
   private CmsSecurity()
   {
   }



   /*******************************************************************************
    ** The roles a user's groups map to.
    **
    ** @param config the CMS config (its roleMapping)
    ** @param groups the user's identity-provider groups
    ** @return the roles
    *******************************************************************************/
   public static Set<CmsRole> rolesFor(CmsQBitConfig config, Collection<String> groups)
   {
      Set<CmsRole> roles = EnumSet.noneOf(CmsRole.class);
      if(groups != null && config.getRoleMapping() != null)
      {
         for(String group : groups)
         {
            CmsRole role = config.getRoleMapping().get(group);
            if(role != null)
            {
               roles.add(role);
            }
         }
      }
      return (roles);
   }



   /*******************************************************************************
    ** The permission names of a set of roles.
    **
    ** @param config the CMS config
    ** @param roles  the roles
    ** @return the permission names, sorted
    *******************************************************************************/
   public static Set<String> permissionsFor(CmsQBitConfig config, Set<CmsRole> roles)
   {
      Set<String> rs = new TreeSet<>();
      for(CmsTableDefinition definition : CmsTables.definitions(config))
      {
         String  table     = config.applyPrefix(definition.logicalName());
         boolean reference = definition.kind() == CmsTableDefinition.Kind.REFERENCE;
         for(CmsRole role : roles)
         {
            rs.add(table + ".read");
            switch(role)
            {
               case ADMIN -> addWrites(rs, table, true);
               case EDITOR, PUBLISHER ->
               {
                  if(!reference)
                  {
                     addWrites(rs, table, true);
                  }
               }
               case DRAFTER ->
               {
                  if(!reference)
                  {
                     addWrites(rs, table, false);
                  }
               }
               default ->
               {
                  //////////////////////////
                  // VIEWER: read only    //
                  //////////////////////////
               }
            }
         }
      }
      if(roles.contains(CmsRole.ADMIN))
      {
         rs.add(config.permission(ADMIN));
         rs.add(config.permission(PUBLISH));
      }
      if(roles.contains(CmsRole.PUBLISHER))
      {
         rs.add(config.permission(PUBLISH));
      }
      return (rs);
   }



   /*******************************************************************************
    ** Add the permissions of a user's groups to a session.
    **
    ** @param session the session
    ** @param groups  the user's identity-provider groups
    ** @param config  the CMS config
    *******************************************************************************/
   public static void applySessionPermissions(QSession session, Collection<String> groups, CmsQBitConfig config)
   {
      Set<String> permissions = new HashSet<>(session.getPermissions() == null ? Set.of() : session.getPermissions());
      permissions.addAll(permissionsFor(config, rolesFor(config, groups)));
      session.setPermissions(permissions);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void addWrites(Set<String> rs, String table, boolean delete)
   {
      rs.add(table + ".insert");
      rs.add(table + ".edit");
      if(delete)
      {
         rs.add(table + ".delete");
      }
   }
}
