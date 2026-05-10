package com.guzem.uzaktan.service.impl.course;

import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.course.Enrollment;
import com.guzem.uzaktan.repository.course.EnrollmentRepository;
import com.guzem.uzaktan.service.course.EnrollmentExcelExportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentExcelExportServiceImpl implements EnrollmentExcelExportService {

    private final EnrollmentRepository enrollmentRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String[] HEADERS = {
            "No", "Ad", "Soyad", "E-posta", "Telefon",
            "Kayıt Durumu", "İlerleme (%)", "Kayıt Tarihi",
            "Kurs Başlangıç", "Kurs Bitiş", "Ders Günleri", "Ders Saati"
    };

    @Override
    public void exportAllToExcel(OutputStream outputStream) throws IOException {
        List<Enrollment> enrollments = enrollmentRepository.findAllWithDetails();

        Map<String, List<Enrollment>> grouped = new LinkedHashMap<>();
        for (Enrollment e : enrollments) {
            String courseTitle = e.getCourse().getTitle();
            grouped.computeIfAbsent(courseTitle, k -> new java.util.ArrayList<>()).add(e);
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            if (grouped.isEmpty()) {
                workbook.createSheet("Kayıt Yok");
            } else {
                for (Map.Entry<String, List<Enrollment>> entry : grouped.entrySet()) {
                    String safeName = sanitizeSheetName(entry.getKey());
                    XSSFSheet sheet = workbook.createSheet(safeName);
                    writeSheet(sheet, entry.getValue());
                }
            }
            workbook.write(outputStream);
        }
    }

    @Override
    public void exportByCourseToExcel(Long courseId, OutputStream outputStream) throws IOException {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdWithDetails(courseId);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            String title = enrollments.isEmpty() ? enrollments.get(0).getCourse().getTitle() : "Kayıtlar";
            XSSFSheet sheet = workbook.createSheet(sanitizeSheetName(title));
            writeSheet(sheet, enrollments);
            workbook.write(outputStream);
        }
    }

    private void writeSheet(XSSFSheet sheet, List<Enrollment> enrollments) {
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Enrollment e : enrollments) {
            User user = e.getUser();
            Course course = e.getCourse();
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(rowNum - 1);
            row.createCell(1).setCellValue(user.getFirstName() != null ? user.getFirstName() : "");
            row.createCell(2).setCellValue(user.getLastName() != null ? user.getLastName() : "");
            row.createCell(3).setCellValue(user.getEmail() != null ? user.getEmail() : "");
            row.createCell(4).setCellValue(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
            row.createCell(5).setCellValue(e.getStatus() != null ? e.getStatus().getDisplayName() : "");
            row.createCell(6).setCellValue(e.getProgressPercentage() != null ? e.getProgressPercentage() : 0);
            row.createCell(7).setCellValue(e.getEnrollmentDate() != null ? e.getEnrollmentDate().format(DATE_FMT) : "");
            row.createCell(8).setCellValue(course.getStartDate() != null ? course.getStartDate().format(DATE_FMT) : "");
            row.createCell(9).setCellValue(course.getEndDate() != null ? course.getEndDate().format(DATE_FMT) : "");
            row.createCell(10).setCellValue(course.getScheduleDays() != null ? course.getScheduleDays() : "");
            row.createCell(11).setCellValue(formatScheduleTime(course));
        }

        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }
        sheet.createFreezePane(0, 1);
    }

    private String formatScheduleTime(Course course) {
        String start = course.getScheduleStartTime();
        String end = course.getScheduleEndTime();
        if (start == null && end == null) return "";
        return (start != null ? start : "") + " - " + (end != null ? end : "");
    }

    private String sanitizeSheetName(String name) {
        String safe = name.replaceAll("[\\\\/*?\\[\\]:]", "").trim();
        return safe.length() > 31 ? safe.substring(0, 31) : safe;
    }
}
