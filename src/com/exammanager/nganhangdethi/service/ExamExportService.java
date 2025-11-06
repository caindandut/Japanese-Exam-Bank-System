package com.exammanager.nganhangdethi.service;

import com.exammanager.nganhangdethi.model.Exam;
import com.exammanager.nganhangdethi.model.ExamQuestionDetail;
import com.exammanager.nganhangdethi.model.Question;
// import com.exammanager.nganhangdethi.model.Choice; // Không cần nếu không xuất lựa chọn
//import com.exammanager.nganhangdethi.model.QuestionType; // Cần để hiển thị loại của từng câu hỏi (nếu muốn)

// Imports cho PDFBox
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;  


// Imports cho Apache POI (DOCX)
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

public class ExamExportService {

    private SimpleDateFormat dateFormat;
    private static final String UNICODE_FONT_RESOURCE_PATH = "/font/NotoSansJP-Regular.ttf"; 

    public ExamExportService() {
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    private float writeWrappedText(PDPageContentStream contentStream, String text,
                                   org.apache.pdfbox.pdmodel.font.PDFont font, float fontSize,
                                   float x, float availableWidth, float startY, float leading) throws IOException {
        if (font == null) {
            
            throw new IOException("Font is null. Cannot write text. Ensure '" + UNICODE_FONT_RESOURCE_PATH + "' is loaded successfully for the current document.");
        }
        contentStream.setFont(font, fontSize);
        
        List<String> linesToWrite = new ArrayList<>();
        String[] manualLines = text.split("\n", -1); 

        for (String manualLine : manualLines) {
            if (manualLine.isEmpty() && manualLines.length > 1 && (linesToWrite.isEmpty() || !linesToWrite.get(linesToWrite.size()-1).isEmpty()) ) {
                linesToWrite.add(""); 
                continue;
            }
            String remaining = manualLine;
            while (remaining.length() > 0) {
                int breakPoint = remaining.length();
                int lastValidSpace = -1;
                for (int k = 0; k < remaining.length(); k++) {
                    String sub = remaining.substring(0, k + 1);
                    float width = fontSize * font.getStringWidth(sub) / 1000;
                    if (Character.isWhitespace(remaining.charAt(k))) {
                        lastValidSpace = k;
                    }
                    if (width > availableWidth) {
                        breakPoint = (lastValidSpace > 0) ? lastValidSpace + 1 : k; 
                        if (k == 0 && lastValidSpace == -1 && breakPoint <= 0) breakPoint = 1; 
                        break;
                    }
                }
                if (breakPoint == 0 && remaining.length() > 0) breakPoint = 1;
                linesToWrite.add(remaining.substring(0, breakPoint));
                remaining = remaining.substring(breakPoint).trim();
            }
        }

        float totalHeightUsedThisBlock = 0;
        if (!linesToWrite.isEmpty()) {
            contentStream.beginText();
            contentStream.newLineAtOffset(x, startY); 
            for (int i = 0; i < linesToWrite.size(); i++) {
                contentStream.showText(linesToWrite.get(i));
                totalHeightUsedThisBlock += leading;
                if (i < linesToWrite.size() - 1) { 
                    contentStream.newLineAtOffset(0, -leading);
                }
            }
            contentStream.endText();
        } else if (text.equals("\n") || (text.isEmpty() && manualLines.length > 1) ) {
             totalHeightUsedThisBlock = leading;
        }
        return totalHeightUsedThisBlock; 
    }
    
    private int countLines(String text, org.apache.pdfbox.pdmodel.font.PDFont font, float fontSize, float maxWidth) throws IOException {
        if (text == null || text.isEmpty()) return 1; 
        if (font == null) {
            System.err.println("countLines: Font is null, cannot accurately count lines for: " + text.substring(0, Math.min(text.length(), 30)) + "...");
            return text.split("\n", -1).length > 0 ? text.split("\n", -1).length : 1;
        }
        int lines = 0;
        String[] paragraphs = text.split("\n", -1);
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty() && paragraphs.length > 1 && lines > 0) { 
                lines++;
                continue;
            }
            String remaining = paragraph;
            while (remaining.length() > 0) {
                lines++;
                int breakPoint = remaining.length();
                int lastValidSpace = -1; 
                for (int k = 0; k < remaining.length(); k++) {
                    String sub = remaining.substring(0, k + 1);
                    float width = fontSize * font.getStringWidth(sub) / 1000;
                    if (Character.isWhitespace(remaining.charAt(k))) {
                        lastValidSpace = k;
                    }
                    if (width > maxWidth) {
                        breakPoint = (lastValidSpace > 0) ? lastValidSpace + 1 : k; 
                        if (k == 0 && lastValidSpace == -1 && breakPoint <= 0) breakPoint = 1; 
                        break;
                    }
                }
                if (breakPoint == 0 && remaining.length() > 0) breakPoint = 1; 
                remaining = remaining.substring(breakPoint).trim();
            }
        }
        return Math.max(1, lines); 
    }

    public boolean exportExamToPdf(Exam exam, String filePath, Consumer<String> progressPublisher, ObjIntConsumer<Integer> progressSetter) {
        progressPublisher.accept("Bắt đầu xuất PDF (Đề thi): " + exam.getExamName());
        progressSetter.accept(null, 10);

        PDType0Font localUnicodeFont = null; // Font sẽ được load cho mỗi lần xuất

        try (PDDocument document = new PDDocument()) { // PDDocument này sẽ được dùng cho toàn bộ quá trình tạo PDF
            // Load font bên trong try-with-resources của document
            try (InputStream fontStream = ExamExportService.class.getResourceAsStream(UNICODE_FONT_RESOURCE_PATH)) {
                if (fontStream != null) {
                    localUnicodeFont = PDType0Font.load(document, fontStream); 
                    progressPublisher.accept("Sử dụng font Unicode: " + UNICODE_FONT_RESOURCE_PATH);
                } else {
                    String errorMsg = "LỖI NGHIÊM TRỌNG: Không tìm thấy file font: " + UNICODE_FONT_RESOURCE_PATH + ". Không thể xuất PDF.";
                    progressPublisher.accept(errorMsg); 
                    System.err.println(errorMsg);
                    progressSetter.accept(null, 0); 
                    return false; 
                }
            } catch (IOException e) {
                String errorMsg = "Lỗi nghiêm trọng khi tải font PDF: " + e.getMessage();
                progressPublisher.accept(errorMsg); 
                System.err.println(errorMsg); 
                e.printStackTrace();
                progressSetter.accept(null, 0); 
                return false; 
            }

            org.apache.pdfbox.pdmodel.font.PDFont currentFont = localUnicodeFont;

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float margin = 50;
            float usableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float yPosition = page.getMediaBox().getHeight() - margin;
            
            float titleFontSize = 16; float headerFontSize = 12; float questionFontSize = 11;
            float titleLeading = titleFontSize * 1.4f;
            float headerLeading = headerFontSize * 1.4f;
            float questionTextLeading = questionFontSize * 1.4f; 
            float blockSpacing = headerLeading * 0.6f;

            
            yPosition -= writeWrappedText(contentStream, "ĐỀ THI: " + exam.getExamName(), currentFont, titleFontSize, margin, usableWidth, yPosition, titleLeading);
            yPosition -= blockSpacing; 

            String levelText = "Cấp độ: " + exam.getLevel().getLevelName();
            yPosition -= writeWrappedText(contentStream, levelText, currentFont, headerFontSize, margin, usableWidth, yPosition, headerLeading);
            
            // THÊM THỜI GIAN LÀM BÀI VÀO PDF
            if (exam.getDurationMinutes() != null && exam.getDurationMinutes() > 0) {
                yPosition -= blockSpacing; 
                String durationText = "Thời gian làm bài: " + exam.getDurationMinutes() + " phút";
                yPosition -= writeWrappedText(contentStream, durationText, currentFont, headerFontSize, margin, usableWidth, yPosition, headerLeading);
            }
            
            String dateText = "Ngày tạo: " + (exam.getGeneratedAt() != null ? dateFormat.format(exam.getGeneratedAt()) : "N/A");
            yPosition -= blockSpacing; 
            yPosition -= writeWrappedText(contentStream, dateText, currentFont, headerFontSize, margin, usableWidth, yPosition, headerLeading);
            yPosition -= (headerLeading + blockSpacing); 
            progressSetter.accept(null, 30);

            
            List<ExamQuestionDetail> questions = exam.getExamQuestions();

            for (int i = 0; i < questions.size(); i++) {
                ExamQuestionDetail eqd = questions.get(i);
                Question q = eqd.getQuestion();
                progressPublisher.accept("Đang xử lý câu hỏi PDF " + (i + 1) + "/" + questions.size());

                String questionFullText = (eqd.getQuestionOrder()) + ". " + q.getQuestionText();
               

                float questionTextHeight = countLines(questionFullText, currentFont, questionFontSize, usableWidth) * questionTextLeading;
                float audioTextHeight = 0;
                if (q.getAudioPath() != null && !q.getAudioPath().isEmpty()){
                     audioTextHeight = countLines("   (Audio: " + q.getAudioPath() + ")", currentFont, questionFontSize -1, usableWidth -10) * questionTextLeading;
                }
                float blockHeight = questionTextHeight + audioTextHeight + (questionTextLeading * 1.5f); 

                if (yPosition - blockHeight < margin) {
                    contentStream.close(); page = new PDPage(PDRectangle.A4); document.addPage(page);
                    contentStream = new PDPageContentStream(document, page); yPosition = page.getMediaBox().getHeight() - margin; 
                }

                yPosition -= writeWrappedText(contentStream, questionFullText, currentFont, questionFontSize, margin, usableWidth, yPosition, questionTextLeading);
                
                if (q.getAudioPath() != null && !q.getAudioPath().isEmpty()) {
                    yPosition -= questionTextLeading / 2; 
                    String audioText = "   (Audio: " + q.getAudioPath() + ")";
                    yPosition -= writeWrappedText(contentStream, audioText, currentFont, questionFontSize -1, margin +10, usableWidth -10, yPosition, questionTextLeading);
                }
                
                yPosition -= (questionTextLeading + blockSpacing); 
                

                
                progressSetter.accept(null, 30 + (int) ((i + 1.0) / questions.size() * 60.0) );
            }

            
            contentStream.close(); 
            document.save(filePath); 
            progressPublisher.accept("Xuất Đề thi PDF thành công (chỉ đề bài): " + filePath);
            progressSetter.accept(null, 100);
            return true;

        } catch (IOException e) {
            progressPublisher.accept("Lỗi IOException khi xuất PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            progressPublisher.accept("Lỗi không xác định khi xuất PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportExamToDocx(Exam exam, String filePath, Consumer<String> progressPublisher, ObjIntConsumer<Integer> progressSetter) {
        progressPublisher.accept("Bắt đầu xuất DOCX (Đề thi): " + exam.getExamName());
        progressSetter.accept(null, 10);

        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(new File(filePath))) {

            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("ĐỀ THI: " + exam.getExamName());
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            titleRun.setFontFamily("Times New Roman"); 
            titleRun.addBreak();

            XWPFParagraph info = document.createParagraph();
            info.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun infoRun = info.createRun();
            infoRun.setFontFamily("Times New Roman");
            infoRun.setFontSize(12);
            infoRun.setText("Cấp độ: " + exam.getLevel().getLevelName());
            infoRun.addBreak();

            // THÊM THỜI GIAN LÀM BÀI VÀO DOCX
            if (exam.getDurationMinutes() != null && exam.getDurationMinutes() > 0) {
                infoRun.setText("Thời gian làm bài: " + exam.getDurationMinutes() + " phút");
                infoRun.addBreak();
            }

            infoRun.setText("Ngày tạo: " + (exam.getGeneratedAt() != null ? dateFormat.format(exam.getGeneratedAt()) : "N/A"));
            infoRun.addBreak();
            infoRun.addBreak();
            progressSetter.accept(null, 30);
            
            List<ExamQuestionDetail> questions = exam.getExamQuestions();
            
            for (int i = 0; i < questions.size(); i++) {
                ExamQuestionDetail eqd = questions.get(i);
                Question q = eqd.getQuestion();
                progressPublisher.accept("Đang xử lý câu hỏi DOCX " + (i + 1) + "/" + questions.size());

                XWPFParagraph qParagraph = document.createParagraph();
                XWPFRun qRun = qParagraph.createRun();
                qRun.setFontFamily("Times New Roman");
                qRun.setFontSize(12);
                qRun.setText((eqd.getQuestionOrder()) + ". " + q.getQuestionText());
                
              

                if (q.getAudioPath() != null && !q.getAudioPath().isEmpty()) {
                    XWPFRun audioRun = qParagraph.createRun();
                    audioRun.addBreak();
                    audioRun.addTab();
                    audioRun.setText("(Audio: " + q.getAudioPath() + ")");
                    audioRun.setFontFamily("Times New Roman");
                    audioRun.setFontSize(10);
                    audioRun.setItalic(true);
                }
                
                document.createParagraph().createRun().addBreak(); 
                progressSetter.accept(null, 30 + (int) ((i + 1.0) / questions.size() * 60.0) );
            }
            
            progressSetter.accept(null, 95); 

            document.write(out);
            progressPublisher.accept("Xuất Đề thi DOCX thành công (chỉ đề bài): " + filePath);
            progressSetter.accept(null, 100);
            return true;

        } catch (IOException e) {
            progressPublisher.accept("Lỗi IOException khi xuất DOCX: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            progressPublisher.accept("Lỗi không xác định khi xuất DOCX: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
