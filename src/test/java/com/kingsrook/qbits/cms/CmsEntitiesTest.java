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


import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import com.kingsrook.qbits.cms.metadata.CmsTableDefinition;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Every CMS entity round-trips through QRecord with its getters, setters and
 ** fluent setters, and every definition's sections name real fields.
 *******************************************************************************/
class CmsEntitiesTest
{

   /*******************************************************************************
    ** Round trip each entity.
    *******************************************************************************/
   @Test
   void testEntitiesRoundTrip() throws Exception
   {
      for(CmsTableDefinition definition : CmsTables.definitions())
      {
         Class<? extends QRecordEntity> entityClass = definition.entityClass();
         QRecordEntity                  entity      = entityClass.getConstructor().newInstance();
         QRecord                        record      = new QRecord();
         for(Field field : entityClass.getDeclaredFields())
         {
            if(Modifier.isStatic(field.getModifiers()))
            {
               continue;
            }
            String       name   = field.getName();
            String       cap    = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            Serializable value  = sample(field.getType());
            Method       setter = entityClass.getMethod("set" + cap, field.getType());
            Method       getter = entityClass.getMethod("get" + cap);
            Method       wither = entityClass.getMethod("with" + cap, field.getType());
            setter.invoke(entity, value);
            assertThat(getter.invoke(entity)).as(entityClass.getSimpleName() + "." + name).isEqualTo(value);
            assertThat(wither.invoke(entity, value)).isSameAs(entity);
            record.setValue(name, value);
         }
         QRecordEntity copy = entityClass.getConstructor(QRecord.class).newInstance(record);
         assertThat(copy.toQRecord().getValues()).as(entityClass.getSimpleName()).isEqualTo(entity.toQRecord().getValues());

         for(CmsTableDefinition.Section section : definition.sections())
         {
            for(String fieldName : section.fields())
            {
               assertThat(entityClass.getDeclaredField(fieldName)).as(definition.logicalName() + " section field " + fieldName).isNotNull();
            }
         }
      }
   }



   /*******************************************************************************
    ** A sample value of a field type.
    *******************************************************************************/
   private static Serializable sample(Class<?> type)
   {
      if(type == Long.class)
      {
         return (7L);
      }
      if(type == Integer.class)
      {
         return (3);
      }
      if(type == Boolean.class)
      {
         return (true);
      }
      if(type == Instant.class)
      {
         return (Instant.parse("2026-10-12T00:00:00Z"));
      }
      return ("value");
   }
}
