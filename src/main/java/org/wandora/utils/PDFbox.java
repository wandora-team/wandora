/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * https://wandora.org
 *
 * Copyright (C) 2004-2026 Wandora Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */



package org.wandora.utils;

import java.io.File;
import java.net.URI;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.wandora.utils.logger.Log4j2Logger;

/**
 *
 * @author akivela
 */
public class PDFbox {
	private static final Log4j2Logger logger = Log4j2Logger.getLogger(PDFbox.class);



    public static String extractTextOutOfPDF(String url) {
        PDDocument doc = null;
        try {
            if(url.startsWith("file:")) {
                doc = PDDocument.load(new File(url));
            }
            else {
                doc = PDDocument.load(new URI(url).toURL().openStream());
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String content = stripper.getText(doc);
            doc.close();
            return content;
        }
        catch(Exception e) {
            logger.error(e);
        }
        return null;
    }


}
