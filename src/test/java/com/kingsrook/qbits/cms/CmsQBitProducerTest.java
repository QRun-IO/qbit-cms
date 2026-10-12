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


import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitConfigValidationException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** Unit test for CmsQBitProducer
 *******************************************************************************/
class CmsQBitProducerTest
{

   /*******************************************************************************
    ** A configured qbit registers itself in the instance.
    *******************************************************************************/
   @Test
   void testProduce() throws Exception
   {
      QInstance           qInstance = new QInstance();
      CmsQBitConfig config    = new CmsQBitConfig().withBackendName("rdbms");
      CmsQBitProducer producer  = new CmsQBitProducer().withQBitConfig(config).withNamespace("cms");

      assertThat(producer.isEnabled()).isTrue();
      assertThat(producer.getQBitConfig()).isSameAs(config);
      assertThat(producer.getNamespace()).isEqualTo("cms");
      assertThat(config.getDefaultBackendNameForTables()).isEqualTo("rdbms");

      producer.produce(qInstance);
      assertThat(qInstance.getQBits()).hasSize(1);
      assertThat(qInstance.getQBits().values().iterator().next().getArtifactId()).isEqualTo("qbit-cms");
   }



   /*******************************************************************************
    ** Without a config the producer is disabled; without a backend it fails.
    *******************************************************************************/
   @Test
   void testDisabledAndInvalid()
   {
      assertThat(new CmsQBitProducer().isEnabled()).isFalse();

      CmsQBitConfig config = new CmsQBitConfig();
      config.setBackendName(null);
      assertThat(config.getBackendName()).isNull();
      assertThatThrownBy(() -> new CmsQBitProducer().withQBitConfig(config).produce(new QInstance()))
         .isInstanceOf(QBitConfigValidationException.class);
   }



   /*******************************************************************************
    ** Names follow the owner rule: lowercase snake_case with the full qbit name.
    *******************************************************************************/
   @Test
   void testNaming()
   {
      assertThat(CmsQBitConfig.TABLE_NAME_PREFIX).isEqualTo("cms_").matches("^[a-z][a-z0-9_]*_$");
      assertThat(CmsQBitConfig.PERMISSION_BASE_NAME).isEqualTo("cms");
   }

}
