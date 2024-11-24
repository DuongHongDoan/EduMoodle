package com.example.edumoodle.Configuration;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class PageNumberHelper extends PdfPageEventHelper {
    private Font pageNumberFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte cb = writer.getDirectContent();
        Phrase footer = new Phrase(String.valueOf(writer.getPageNumber()), pageNumberFont);

        // Vị trí hiển thị số trang (chính giữa phía dưới)
        float x = (document.right() - document.left()) / 2 + document.leftMargin();
        float y = document.bottom() - 10; // Khoảng cách từ dưới lên

        // Thêm số trang vào tài liệu
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer, x, y, 0);
    }
}
