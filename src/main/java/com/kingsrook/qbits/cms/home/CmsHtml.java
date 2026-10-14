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

package com.kingsrook.qbits.cms.home;


import java.util.List;


/*******************************************************************************
 ** Small HTML building blocks for the CMS homes, readable at every width and
 ** in light and dark mode: text inherits the surrounding color, borders and
 ** fills are translucent, grids wrap to one column on a phone, long words
 ** break instead of scrolling sideways.
 *******************************************************************************/
public final class CmsHtml
{
   static final String MUTED = "opacity:.72";
   static final String WRAP  = "overflow-wrap:anywhere;min-width:0";
   static final String CARD  = "box-sizing:border-box;min-width:0;padding:14px 16px;border-radius:10px;border:1px solid rgba(127,127,127,.28);background:rgba(127,127,127,.06)";

   /*******************************************************************************
    ** A tile: label, big number, optional notes; the number links to href.
    **
    ** @param label the label
    ** @param value the value text
    ** @param href  the link (may be null)
    ** @param notes secondary lines (already HTML)
    *******************************************************************************/
   public record Tile(String label, String value, String href, List<String> notes)
   {
   }



   /*******************************************************************************
    ** A list row: a linked title and a muted detail.
    **
    ** @param title  the title
    ** @param href   the link (may be null)
    ** @param detail the detail
    *******************************************************************************/
   public record Row(String title, String href, String detail)
   {
   }



   /*******************************************************************************
    ** Not instantiable.
    *******************************************************************************/
   private CmsHtml()
   {
   }



   /*******************************************************************************
    ** HTML-escape text.
    *******************************************************************************/
   public static String escape(String text)
   {
      if(text == null)
      {
         return ("");
      }
      StringBuilder rs = new StringBuilder(text.length());
      for(char c : text.toCharArray())
      {
         switch(c)
         {
            case '<' -> rs.append("&lt;");
            case '>' -> rs.append("&gt;");
            case '&' -> rs.append("&amp;");
            case '"' -> rs.append("&quot;");
            case '\'' -> rs.append("&#39;");
            default -> rs.append(c);
         }
      }
      return (rs.toString());
   }



   /*******************************************************************************
    ** A link that inherits the text color.
    *******************************************************************************/
   public static String link(String href, String text)
   {
      return ("<a href=\"" + escape(href) + "\" style=\"color:inherit;text-decoration:underline;text-underline-offset:2px\">" + escape(text) + "</a>");
   }



   /*******************************************************************************
    ** Muted text (already HTML).
    *******************************************************************************/
   public static String muted(String html)
   {
      return ("<span style=\"" + MUTED + ";font-size:13px\">" + html + "</span>");
   }



   /*******************************************************************************
    ** A heading inside a widget.
    *******************************************************************************/
   public static String heading(String text)
   {
      return ("<div style=\"margin:18px 0 8px;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:.04em;" + MUTED + "\">" + escape(text) + "</div>");
   }



   /*******************************************************************************
    ** A responsive grid of tiles (one column on a phone).
    *******************************************************************************/
   public static String tiles(List<Tile> tiles)
   {
      StringBuilder html = new StringBuilder("<div style=\"display:grid;grid-template-columns:repeat(auto-fill,minmax(min(100%,13rem),1fr));gap:12px\">");
      for(Tile tile : tiles)
      {
         html.append("<div style=\"").append(CARD).append("\">");
         html.append("<div style=\"font-size:13px;").append(MUTED).append(';').append(WRAP).append("\">").append(escape(tile.label())).append("</div>");
         String value = tile.href() == null ? escape(tile.value())
            : "<a href=\"" + escape(tile.href()) + "\" style=\"color:inherit;text-decoration:none\">" + escape(tile.value()) + "</a>";
         html.append("<div style=\"font-size:26px;font-weight:700;line-height:1.3;").append(WRAP).append("\">").append(value).append("</div>");
         for(String note : tile.notes())
         {
            html.append("<div style=\"font-size:13px;").append(WRAP).append("\">").append(note).append("</div>");
         }
         html.append("</div>");
      }
      return (html.append("</div>").toString());
   }



   /*******************************************************************************
    ** A plain list of rows, each wrapping as needed.
    *******************************************************************************/
   public static String rows(List<Row> rows)
   {
      StringBuilder html = new StringBuilder("<ul style=\"list-style:none;margin:0;padding:0\">");
      for(Row row : rows)
      {
         html.append("<li style=\"display:flex;flex-wrap:wrap;align-items:baseline;column-gap:10px;row-gap:2px;padding:7px 0;border-bottom:1px solid rgba(127,127,127,.18)\">");
         html.append("<span style=\"flex:1 1 14rem;").append(WRAP).append("\">").append(row.href() == null ? escape(row.title()) : link(row.href(), row.title())).append("</span>");
         html.append("<span style=\"flex:0 1 auto;font-size:13px;").append(MUTED).append(';').append(WRAP).append("\">").append(escape(row.detail())).append("</span>");
         html.append("</li>");
      }
      return (html.append("</ul>").toString());
   }



   /*******************************************************************************
    ** An honest empty state.
    *******************************************************************************/
   public static String empty(String messageHtml)
   {
      return ("<div style=\"padding:14px 2px;font-size:14px;" + MUTED + ';' + WRAP + "\">" + messageHtml + "</div>");
   }



   /*******************************************************************************
    ** A warning banner (amber left border, readable in both modes).
    *******************************************************************************/
   public static String warning(String messageHtml)
   {
      return ("<div style=\"margin:0 0 12px;padding:8px 12px;border-left:3px solid #d97706;border-radius:4px;background:rgba(217,119,6,.10);font-size:14px;"
         + WRAP + "\">" + messageHtml + "</div>");
   }
}
