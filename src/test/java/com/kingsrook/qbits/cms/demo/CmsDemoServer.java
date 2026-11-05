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

package com.kingsrook.qbits.cms.demo;


import com.kingsrook.qbits.cms.CmsQBitConfig;
import com.kingsrook.qbits.cms.CmsQBitProducer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.AbstractQQQApplication;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.branding.QBrandingMetaData;
import com.kingsrook.qqq.backend.module.postgres.model.metadata.PostgreSQLBackendMetaData;
import com.kingsrook.qqq.middleware.javalin.QApplicationJavalinServer;


/*******************************************************************************
 ** A local demo host for looking at the CMS in the Next UI (screenshots): a
 ** blank QQQ app with one PostgreSQL backend and the qbit, nothing else.
 ** Not part of the build; run it from the IDE or with exec:java against a
 ** database the qbit's changelog has run on:
 **
 ** CMS_DEMO_DB_PORT (default 55433), CMS_DEMO_DB_NAME (cmsdemo),
 ** CMS_DEMO_DB_USER (postgres), CMS_DEMO_DB_PASSWORD, CMS_DEMO_PORT (8000).
 **
 ** It signs everyone in anonymously, so it runs the CMS with openReads.
 *******************************************************************************/
public class CmsDemoServer extends AbstractQQQApplication
{

   /*******************************************************************************
    ** Start the demo.
    *******************************************************************************/
   public static void main(String[] args) throws Exception
   {
      new QApplicationJavalinServer(new CmsDemoServer())
         .withServeFrontendNext(true)
         .withServeFrontendMaterialDashboard(false)
         .withPort(Integer.parseInt(env("CMS_DEMO_PORT", "8000")))
         .start();
   }



   /*******************************************************************************
    ** The demo instance.
    *******************************************************************************/
   @Override
   public QInstance defineQInstance() throws QException
   {
      QInstance qInstance = new QInstance();
      qInstance.addBackend(new PostgreSQLBackendMetaData()
         .withName("database")
         .withHostName("localhost")
         .withPort(Integer.parseInt(env("CMS_DEMO_DB_PORT", "55433")))
         .withDatabaseName(env("CMS_DEMO_DB_NAME", "cmsdemo"))
         .withUsername(env("CMS_DEMO_DB_USER", "postgres"))
         .withPassword(env("CMS_DEMO_DB_PASSWORD", "")));
      qInstance.withInstanceDefaultAuthentication(new QAuthenticationMetaData().withName("anonymous").withType(QAuthenticationType.FULLY_ANONYMOUS));
      qInstance.setBranding(new QBrandingMetaData().withAppName("QBit CMS demo").withCompanyName("QRun-IO"));

      new CmsQBitProducer()
         .withQBitConfig(new CmsQBitConfig().withBackendName("database").withOpenReads(true))
         .produce(qInstance);
      return (qInstance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String env(String name, String defaultValue)
   {
      String value = System.getenv(name);
      return (value == null || value.isEmpty() ? defaultValue : value);
   }
}
