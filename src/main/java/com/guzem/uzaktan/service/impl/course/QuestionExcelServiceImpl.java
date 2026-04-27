package com.guzem.uzaktan.service.impl.course;

import com.guzem.uzaktan.dto.request.QuestionCreateRequest;
import com.guzem.uzaktan.dto.request.QuestionUpdateRequest;
import com.guzem.uzaktan.dto.response.QuestionImportResult;
import com.guzem.uzaktan.dto.response.QuestionImportResult.RowError;
import com.guzem.uzaktan.dto.response.QuestionResponse;
import com.guzem.uzaktan.model.course.CorrectOption;
import com.guzem.uzaktan.service.course.QuestionBankService;
import com.guzem.uzaktan.service.course.QuestionExcelService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class QuestionExcelServiceImpl implements QuestionExcelService {

    private static final String[] HEADERS = {
            "ID", "Soru Metni", "A Şıkkı", "B Şıkkı", "C Şıkkı", "D Şıkkı", "E Şıkkı", "Doğru Cevap", "Açıklama"
    };

    private static final Set<String> VALID_OPTIONS = Set.of("A", "B", "C", "D", "E");

    private final QuestionBankService questionBankService;

    @Override
    public QuestionImportResult importFromExcel(Long courseId, MultipartFile file, Long requestingUserId) {
        List<RowError> errors = new ArrayList<>();
        int successCount = 0;
        int updateCount = 0;
        int totalRows = 0;

        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return singleError("Dosya", "Excel dosyasında sayfa bulunamadı.");
            }

            DataFormatter formatter = new DataFormatter();
            int lastRow = sheet.getLastRowNum();

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row, formatter)) continue;

                totalRows++;
                int displayRow = i + 1; // Excel'de kullanıcının gördüğü satır numarası

                try {
                    String idCell = getCellValue(row, 0, formatter).trim();
                    String questionText = getCellValue(row, 1, formatter).trim();
                    String optionA = getCellValue(row, 2, formatter).trim();
                    String optionB = getCellValue(row, 3, formatter).trim();
                    String optionC = getCellValue(row, 4, formatter).trim();
                    String optionD = getCellValue(row, 5, formatter).trim();
                    String optionE = getCellValue(row, 6, formatter).trim();
                    String correctOption = getCellValue(row, 7, formatter).trim().toUpperCase();
                    String explanation = getCellValue(row, 8, formatter).trim();

                    // Validasyon
                    List<RowError> rowErrors = new ArrayList<>();
                    validateRequired(rowErrors, displayRow, questionText, "Soru Metni");
                    validateRequired(rowErrors, displayRow, optionA, "A Şıkkı");
                    validateRequired(rowErrors, displayRow, optionB, "B Şıkkı");
                    validateRequired(rowErrors, displayRow, optionC, "C Şıkkı");
                    validateRequired(rowErrors, displayRow, optionD, "D Şıkkı");
                    validateRequired(rowErrors, displayRow, optionE, "E Şıkkı");

                    if (correctOption.isEmpty()) {
                        rowErrors.add(RowError.builder().rowNumber(displayRow).field("Doğru Cevap").message("Bu alan boş olamaz.").build());
                    } else if (!VALID_OPTIONS.contains(correctOption)) {
                        rowErrors.add(RowError.builder().rowNumber(displayRow).field("Doğru Cevap").message("A, B, C, D veya E olmalıdır.").build());
                    }

                    if (questionText.length() > 5000) {
                        rowErrors.add(RowError.builder().rowNumber(displayRow).field("Soru Metni").message("En fazla 5000 karakter olabilir.").build());
                    } else if (!questionText.isEmpty() && questionText.length() < 5) {
                        rowErrors.add(RowError.builder().rowNumber(displayRow).field("Soru Metni").message("En az 5 karakter olmalıdır.").build());
                    }

                    validateLength(rowErrors, displayRow, optionA, "A Şıkkı", 500);
                    validateLength(rowErrors, displayRow, optionB, "B Şıkkı", 500);
                    validateLength(rowErrors, displayRow, optionC, "C Şıkkı", 500);
                    validateLength(rowErrors, displayRow, optionD, "D Şıkkı", 500);
                    validateLength(rowErrors, displayRow, optionE, "E Şıkkı", 500);
                    if (explanation.length() > 5000) {
                        rowErrors.add(RowError.builder().rowNumber(displayRow).field("Açıklama").message("En fazla 5000 karakter olabilir.").build());
                    }

                    if (!rowErrors.isEmpty()) {
                        errors.addAll(rowErrors);
                        continue;
                    }

                    // ID varsa güncelle, yoksa yeni oluştur
                    if (!idCell.isEmpty()) {
                        Long questionId = parseId(idCell, displayRow);
                        if (questionId == null) {
                            errors.add(RowError.builder().rowNumber(displayRow).field("ID").message("Geçersiz ID formatı.").build());
                            continue;
                        }

                        QuestionUpdateRequest updateReq = new QuestionUpdateRequest();
                        updateReq.setQuestionText(questionText);
                        updateReq.setOptionA(optionA);
                        updateReq.setOptionB(optionB);
                        updateReq.setOptionC(optionC);
                        updateReq.setOptionD(optionD);
                        updateReq.setOptionE(optionE);
                        updateReq.setCorrectOption(CorrectOption.valueOf(correctOption));
                        updateReq.setExplanation(explanation.isEmpty() ? null : explanation);

                        questionBankService.updateQuestion(questionId, updateReq, requestingUserId);
                        updateCount++;
                    } else {
                        QuestionCreateRequest createReq = new QuestionCreateRequest();
                        createReq.setQuestionText(questionText);
                        createReq.setOptionA(optionA);
                        createReq.setOptionB(optionB);
                        createReq.setOptionC(optionC);
                        createReq.setOptionD(optionD);
                        createReq.setOptionE(optionE);
                        createReq.setCorrectOption(CorrectOption.valueOf(correctOption));
                        createReq.setExplanation(explanation.isEmpty() ? null : explanation);

                        questionBankService.createQuestion(courseId, createReq, requestingUserId);
                        successCount++;
                    }
                } catch (Exception e) {
                    errors.add(RowError.builder()
                            .rowNumber(displayRow)
                            .field(null)
                            .message(e.getMessage() != null ? e.getMessage() : "Beklenmeyen hata oluştu.")
                            .build());
                }
            }
        } catch (IOException e) {
            return singleError("Dosya", "Excel dosyası okunamadı: " + e.getMessage());
        }

        return QuestionImportResult.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .updateCount(updateCount)
                .errorCount(errors.size())
                .errors(errors)
                .build();
    }

    @Override
    public void exportToExcel(Long courseId, Long requestingUserId, OutputStream out) throws IOException {
        List<QuestionResponse> questions = questionBankService.findByCourse(courseId);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Soru Bankası");

            // Header stili
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Header satırı
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Veri satırları
            int rowIndex = 1;
            for (QuestionResponse q : questions) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(q.getId());
                row.createCell(1).setCellValue(q.getQuestionText());
                row.createCell(2).setCellValue(q.getOptionA());
                row.createCell(3).setCellValue(q.getOptionB());
                row.createCell(4).setCellValue(q.getOptionC());
                row.createCell(5).setCellValue(q.getOptionD());
                row.createCell(6).setCellValue(q.getOptionE());
                row.createCell(7).setCellValue(q.getCorrectOption().name());
                row.createCell(8).setCellValue(q.getExplanation() != null ? q.getExplanation() : "");
            }

            autoSizeColumns(sheet);
            workbook.write(out);
        }
    }

    @Override
    public void generateTemplate(OutputStream out) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Soru Bankası");

            // Header stili
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Header satırı
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Örnek satır
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue(""); // ID boş bırakılır
            exampleRow.createCell(1).setCellValue("Türkiye'nin başkenti neresidir?");
            exampleRow.createCell(2).setCellValue("İstanbul");
            exampleRow.createCell(3).setCellValue("Ankara");
            exampleRow.createCell(4).setCellValue("İzmir");
            exampleRow.createCell(5).setCellValue("Bursa");
            exampleRow.createCell(6).setCellValue("Antalya");
            exampleRow.createCell(7).setCellValue("B");
            exampleRow.createCell(8).setCellValue("Ankara, 1923'ten beri Türkiye'nin başkentidir.");

            // Doğru Cevap sütununa dropdown validation (1000 satır)
            DataValidationHelper dvHelper = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = dvHelper.createExplicitListConstraint(new String[]{"A", "B", "C", "D", "E"});
            CellRangeAddressList addressList = new CellRangeAddressList(1, 1000, 7, 7);
            DataValidation validation = dvHelper.createValidation(constraint, addressList);
            validation.setShowErrorBox(true);
            validation.createErrorBox("Geçersiz Değer", "Lütfen A, B, C, D veya E seçiniz.");
            sheet.addValidationData(validation);

            autoSizeColumns(sheet);
            workbook.write(out);
        }
    }

    // ── Yardımcı Metodlar ──

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();

        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);

        return style;
    }

    private void autoSizeColumns(XSSFSheet sheet) {
        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
            // Minimum genişlik: 15 karakter
            if (sheet.getColumnWidth(i) < 15 * 256) {
                sheet.setColumnWidth(i, 15 * 256);
            }
            // Maksimum genişlik: 60 karakter
            if (sheet.getColumnWidth(i) > 60 * 256) {
                sheet.setColumnWidth(i, 60 * 256);
            }
        }
    }

    private String getCellValue(Row row, int colIndex, DataFormatter formatter) {
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return formatter.formatCellValue(cell);
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        for (int i = 1; i <= 7; i++) { // ID hariç, zorunlu alanları kontrol et
            String val = getCellValue(row, i, formatter).trim();
            if (!val.isEmpty()) return false;
        }
        return true;
    }

    private void validateRequired(List<RowError> errors, int rowNumber, String value, String fieldName) {
        if (value.isEmpty()) {
            errors.add(RowError.builder().rowNumber(rowNumber).field(fieldName).message("Bu alan boş olamaz.").build());
        }
    }

    private void validateLength(List<RowError> errors, int rowNumber, String value, String fieldName, int maxLength) {
        if (value.length() > maxLength) {
            errors.add(RowError.builder().rowNumber(rowNumber).field(fieldName).message("En fazla " + maxLength + " karakter olabilir.").build());
        }
    }

    private Long parseId(String idStr, int displayRow) {
        try {
            // Excel'den numeric olarak gelebilir: "123.0" → "123"
            if (idStr.contains(".")) {
                idStr = idStr.substring(0, idStr.indexOf('.'));
            }
            return Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private QuestionImportResult singleError(String field, String message) {
        return QuestionImportResult.builder()
                .totalRows(0)
                .successCount(0)
                .updateCount(0)
                .errorCount(1)
                .errors(List.of(RowError.builder().rowNumber(0).field(field).message(message).build()))
                .build();
    }
}
