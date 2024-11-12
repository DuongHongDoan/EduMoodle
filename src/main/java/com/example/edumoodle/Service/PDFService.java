package com.example.edumoodle.Service;

import com.example.edumoodle.Configuration.PageNumberHelper;
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
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
            writer.setPageEvent(new PageNumberHelper());
            document.open();

            // Tạo tiêu đề
            BaseFont vietnameseFont = BaseFont.createFont("src/main/resources/fonts/DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font boldFont = new Font(vietnameseFont, 13, Font.BOLD);
            Paragraph title = new Paragraph(quiz.getName(), new Font(vietnameseFont, 18, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            //tạo font thường
            Font nomalFont = new Font(vietnameseFont, 13, Font.NORMAL);
            //tạo font đáp án
            Font correct = new Font(vietnameseFont, 13, Font.BOLD, BaseColor.GREEN);
            Font incorrect = new Font(vietnameseFont, 13, Font.BOLD, BaseColor.RED);

            // Tạo bảng tổng hợp thông tin
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            float[] columnWidths = {1f, 2.5f}; // Tỷ lệ chiều rộng cho các cột (cột in đậm sẽ nhỏ hơn)
            table.setWidths(columnWidths);

            addTableCell(table, "Họ tên sinh viên", questionsDetail.getAttempt().getUsersDTO().getFullname(), boldFont, nomalFont);
            addTableCell(table, "Trạng thái", questionsDetail.getAttempt().getState(), boldFont, nomalFont);
            addTableCell(table, "Thời gian làm bài", questionsDetail.getAttempt().getDurationFormat(), boldFont, nomalFont);
            addTableCell(table, "Thời gian bắt đầu", questionsDetail.getAttempt().getTimestartAsLocalDateTime(), boldFont, nomalFont);
            addTableCell(table, "Thời gian kết thúc", questionsDetail.getAttempt().getTimefinishAsLocalDateTime(), boldFont, nomalFont);
            addTableCell(table, "Điểm", questionsDetail.getAttempt().getFormattedGrade()+"/"+quiz.getFormattedGrade(), boldFont, nomalFont);

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
                    // Tạo ký tự radio
                    String radioSymbol;
                    if (response.equals(question.getStudentResponse())) {
                        // Nếu đáp án được chọn, sử dụng ký tự radio đã chọn
                        radioSymbol = "●";
                    } else {
                        // Nếu đáp án không được chọn, sử dụng ký tự radio chưa chọn
                        radioSymbol = "○";
                    }

                    // Tạo đoạn văn với ký tự radio và đáp án
                    Paragraph answerParagraph = new Paragraph(radioSymbol + " " + response, nomalFont);
                    if (response.equals(question.getStudentResponse()) && response.equals(question.getCorrectResponse())) {
                        answerParagraph.add(new Chunk(" ✔", correct)); // Đáp án đúng được chọn
                    } else if (response.equals(question.getStudentResponse()) && !response.equals(question.getCorrectResponse())) {
                        answerParagraph.add(new Chunk(" ✘", incorrect)); // Đáp án sai được chọn
                    } else if (response.equals(question.getCorrectResponse())) {
                        answerParagraph.add(new Chunk(" ✔", correct)); // Đáp án đúng không được chọn
                    }

                    document.add(answerParagraph);
                }
                document.add(Chunk.NEWLINE); // Dòng trắng giữa các câu hỏi
                index++;
            }

            document.close();
        } catch (DocumentException | IOException e) {
            e.printStackTrace(); // Ghi log lỗi
            throw new RuntimeException("Lỗi khi tạo PDF", e);
        }
    }

    private void addTableCell(PdfPTable table, String header, String value, Font font1, Font font2) {
        PdfPCell cellHeader = new PdfPCell(new Phrase(header, font1));
        cellHeader.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cellHeader);

        PdfPCell cellValue = new PdfPCell(new Phrase(value, font2));
        table.addCell(cellValue);
    }
}
