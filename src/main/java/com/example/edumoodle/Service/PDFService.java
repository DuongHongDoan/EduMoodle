package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.AttemptViewDTO;
import com.example.edumoodle.DTO.QuestionDetail;
import com.example.edumoodle.DTO.QuizzesDTO;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PDFService {

    //tải cho tiết bài thi ra file PDF
    public void exportAttemptDetailPDF(AttemptViewDTO questionsDetail, QuizzesDTO.QuizzesListDTO quiz, List<QuestionDetail> questionDetails, String filePath) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Tạo tiêu đề
            BaseFont vietnameseFont = BaseFont.createFont(
                    "fonts/VNTIME.TTF", BaseFont.IDENTITY_H, BaseFont.EMBEDDED
            );
            Font boldFont = new Font(vietnameseFont, 13, Font.BOLD);
            Paragraph title = new Paragraph("Chi tiết bài thi", boldFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Tạo bảng tổng hợp thông tin
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            addTableCell(table, "Tên bài thi:", quiz.getName(), boldFont);
            addTableCell(table, "Họ tên sinh viên:", questionsDetail.getAttempt().getUsersDTO().getFullname(), boldFont);
            addTableCell(table, "Trạng thái:", questionsDetail.getAttempt().getState(), boldFont);
            addTableCell(table, "Thời gian làm bài", questionsDetail.getAttempt().getDurationFormat(), boldFont);
            addTableCell(table, "Thời gian bắt đầu:", questionsDetail.getAttempt().getTimestartAsLocalDateTime(), boldFont);
            addTableCell(table, "Thời gian kết thúc:", questionsDetail.getAttempt().getTimefinishAsLocalDateTime(), boldFont);
            addTableCell(table, "Điểm:", questionsDetail.getAttempt().getFormattedGrade()+"/"+quiz.getFormattedGrade(), boldFont);

            document.add(table);

            // Thêm chi tiết câu hỏi và câu trả lời
            int index = 1;
            for (QuestionDetail question : questionDetails) {
                if(question.isCorrect()) {
                    document.add(new Paragraph("Đúng", boldFont));
                }else {
                    document.add(new Paragraph("Sai", boldFont));
                }
                document.add(new Paragraph("Câu " + index + ": " + question.getQuestionText(), boldFont));
                for (String response : question.getAllResponses()) {
                    Paragraph answerParagraph = new Paragraph(response, boldFont);
                    if (response.equals(question.getStudentResponse()) && response.equals(question.getCorrectResponse())) {
                        answerParagraph.add(new Chunk(" ✔", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.GREEN))); // Đáp án đúng được chọn
                    } else if (response.equals(question.getStudentResponse()) && !response.equals(question.getCorrectResponse())) {
                        answerParagraph.add(new Chunk(" ✘", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.RED))); // Đáp án sai được chọn
                    } else if (response.equals(question.getCorrectResponse())) {
                        answerParagraph.add(new Chunk(" ✔", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.GREEN))); // Đáp án đúng không được chọn
                    }

                    document.add(answerParagraph);
                }
                document.add(new Paragraph("Câu trả lời của sinh viên: " + question.getStudentResponse(), boldFont));
                document.add(new Paragraph("Đáp án đúng: " + question.getCorrectResponse(), boldFont));
                document.add(Chunk.NEWLINE); // Dòng trắng giữa các câu hỏi
                index++;
            }

            document.close();
        } catch (DocumentException | IOException e) {
            e.printStackTrace(); // Ghi log lỗi
            throw new RuntimeException("Lỗi khi tạo PDF", e);
        }
    }

    private void addTableCell(PdfPTable table, String header, String value, Font font) {
        PdfPCell cellHeader = new PdfPCell(new Phrase(header, font));
        cellHeader.setBorder(Rectangle.NO_BORDER);
        table.addCell(cellHeader);

        PdfPCell cellValue = new PdfPCell(new Phrase(value));
        cellValue.setBorder(Rectangle.NO_BORDER);
        table.addCell(cellValue);
    }
}
