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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;


/*******************************************************************************
 ** The feature areas of the CMS (spec section 2.3). A host turns features on
 ** and off in CmsQBitConfig.features; a feature that is off produces no meta
 ** data, and its changesets are skipped by the cms.features changelog
 ** property.
 **
 ** CORE is always on. A feature that is not built yet (available = false) is
 ** rejected by validation, so a host never believes it installed something
 ** that is not there.
 *******************************************************************************/
public enum CmsFeature
{
   /** Sites, domains, redirects, pages, page sections, snippets, menus. */
   CORE(true),

   /** The media library, media types and the storage SPI. */
   MEDIA(false),

   /** Blogs, posts, tags, authors, post media and versions. */
   BLOG(false, MEDIA),

   /** Projects, project categories and project updates. */
   PROJECTS(false, MEDIA),

   /** Photo albums, folders, photos and photo tags. */
   PHOTOS(false, MEDIA),

   /** Publishing processes, publications and receipts. */
   PUBLISHING(false),

   /** The public content stream across content kinds. */
   STREAM(false, BLOG),

   /** CMS Data Health rules, for hosts with a Data Health framework (D10). */
   DATA_HEALTH_RULES(false);

   private final boolean          available;
   private final List<CmsFeature> requires;



   /*******************************************************************************
    ** Constructor.
    **
    ** @param available whether this version of the qbit implements the feature
    ** @param requires  features this one needs
    *******************************************************************************/
   CmsFeature(boolean available, CmsFeature... requires)
   {
      this.available = available;
      this.requires = List.of(requires);
   }



   /*******************************************************************************
    ** Whether this version of the qbit implements the feature.
    *******************************************************************************/
   public boolean isAvailable()
   {
      return (available);
   }



   /*******************************************************************************
    ** The features this one needs (beyond CORE, which is always on).
    *******************************************************************************/
   public List<CmsFeature> getRequires()
   {
      return (requires);
   }



   /*******************************************************************************
    ** Every feature this version implements: the default feature set.
    *******************************************************************************/
   public static Set<CmsFeature> allAvailable()
   {
      EnumSet<CmsFeature> rs = EnumSet.noneOf(CmsFeature.class);
      for(CmsFeature feature : values())
      {
         if(feature.available)
         {
            rs.add(feature);
         }
      }
      return (rs);
   }



   /*******************************************************************************
    ** Validation messages for a feature set: unknown-in-this-version features,
    ** missing dependencies, and PUBLISHING without BLOG or PROJECTS.
    **
    ** @param features the requested features
    ** @return the problems (empty when valid)
    *******************************************************************************/
   public static List<String> validate(Set<CmsFeature> features)
   {
      List<String> errors = new ArrayList<>();
      if(features == null || !features.contains(CORE))
      {
         errors.add("features must include CORE");
         return (errors);
      }
      for(CmsFeature feature : features)
      {
         if(!feature.available)
         {
            errors.add("feature " + feature + " is not available in this version of qbit-cms");
         }
         for(CmsFeature required : feature.requires)
         {
            if(!features.contains(required))
            {
               errors.add("feature " + feature + " requires " + required);
            }
         }
      }
      if(features.contains(PUBLISHING) && !features.contains(BLOG) && !features.contains(PROJECTS))
      {
         errors.add("feature PUBLISHING requires BLOG or PROJECTS");
      }
      return (errors);
   }
}
