package com.ivy.freport.writer.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class HeaderFooter extends PdfPageEventHelper {

    public void onEndPage(PdfWriter writer, Document document) {
        Rectangle rect = writer.getBoxSize("rect");
        
        Phrase phrase = PdfParagraph.changeChinese("第 "+ writer.getPageNumber() +" 页.", 9, false);
        ColumnText.showTextAligned(writer.getDirectContent(),
                                Element.ALIGN_CENTER,
                                phrase,
                                (rect.getLeft() + rect.getRight()) / 2,
                                rect.getBottom() - 45,
                                0);
    }
}