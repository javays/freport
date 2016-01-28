package com.ivy.freport.writer.pdf;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;

public class PdfParagraph extends Paragraph {
    
    public static final Logger logger = Logger.getLogger(PdfParagraph.class);

    private static final long serialVersionUID = -244970043180837974L;
    
    public static BaseFont bfChinese = null;
    
    static{
        try {
//            String fontPath = FileUtils.getFilePath("META-INF/config/ecbil.bill/tmpl/simsun.ttc");
            String fontPath = Thread.currentThread().getContextClassLoader().getResource("com/resources/simsun.ttc").getPath();
            bfChinese = BaseFont.createFont(fontPath + ",1",
                                        BaseFont.IDENTITY_H,
                                        BaseFont.EMBEDDED);
        } catch (DocumentException | IOException e) {
            logger.error("", e);
            e.printStackTrace();
        }
    }

    public PdfParagraph(String content) {
        super(content, getChineseFont(12, false));
    }

    public PdfParagraph(String content, int fontSize, boolean isBold) {
        super(content, getChineseFont(fontSize, isBold));
    }

    // 设置字体-返回中文字体
    protected static Font getChineseFont(int nfontsize, boolean isBold) {
        Font fontChinese = null;
        if (isBold) {
            fontChinese = new Font(bfChinese, nfontsize, Font.BOLD);
        } else {
            fontChinese = new Font(bfChinese, nfontsize, Font.NORMAL);
        }
        
        return fontChinese;
    }

    // 转化中文
    protected Chunk changeChunk(String str, int nfontsize, boolean isBold) {
        Font fontChinese = getChineseFont(nfontsize, isBold);
        Chunk chunk = new Chunk(str, fontChinese);
        return chunk;
    }

    // 转化中文
    protected static Phrase changeChinese(String str, int nfontsize, boolean isBold) {
        Font fontChinese = getChineseFont(nfontsize, isBold);
        Phrase ph = new Phrase(str, fontChinese);
        return ph;
    }
}