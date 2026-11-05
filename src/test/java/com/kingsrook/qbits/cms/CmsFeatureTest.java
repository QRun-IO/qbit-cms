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


import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** CmsFeature flags and their dependencies.
 *******************************************************************************/
class CmsFeatureTest
{

   /*******************************************************************************
    ** The default set is what this version implements; dependencies hold.
    *******************************************************************************/
   @Test
   void testFeatures()
   {
      assertThat(CmsFeature.allAvailable()).containsExactly(CmsFeature.CORE);
      assertThat(CmsFeature.validate(CmsFeature.allAvailable())).isEmpty();
      assertThat(CmsFeature.validate(null)).containsExactly("features must include CORE");
      assertThat(CmsFeature.BLOG.getRequires()).containsExactly(CmsFeature.MEDIA);
      assertThat(CmsFeature.CORE.isAvailable()).isTrue();

      assertThat(CmsFeature.validate(EnumSet.of(CmsFeature.CORE, CmsFeature.BLOG)))
         .contains("feature BLOG requires MEDIA", "feature BLOG is not available in this version of qbit-cms");
      assertThat(CmsFeature.validate(EnumSet.of(CmsFeature.CORE, CmsFeature.PUBLISHING)))
         .contains("feature PUBLISHING requires BLOG or PROJECTS");
      assertThat(CmsFeature.validate(EnumSet.of(CmsFeature.CORE, CmsFeature.MEDIA, CmsFeature.BLOG, CmsFeature.STREAM, CmsFeature.PUBLISHING)))
         .noneMatch(e -> e.contains("requires"));
   }
}
