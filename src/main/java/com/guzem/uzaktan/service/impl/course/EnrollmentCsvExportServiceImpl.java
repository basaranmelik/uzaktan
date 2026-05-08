package com.guzem.uzaktan.service.impl.course;

import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.course.Enrollment;
import com.guzem.uzaktan.repository.course.EnrollmentRepository;
import com.guzem.uzaktan.service.course.EnrollmentCsvExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentCsvExportServiceImpl implements EnrollmentCsvExportService {

    private final EnrollmentRepository enrollmentRepository;

    private static final DateTimeFormatter CSV_DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public void exportAllToCsv(OutputStream outputStream) throws IOException {
        List<Enrollment> enrollments = enrollmentRepository.findAllWithDetails();
        writeCsv(enrollments, outputStream);
    }

    @Override
    public void exportByCourseToCsv(Long courseId, OutputStream outputStream) throws IOException {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdWithDetails(courseId);
        writeCsv(enrollments, outputStream);
    }

    private void writeCsv(List<Enrollment> enrollments, OutputStream outputStream) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        writer.write('\ufeff');

        writer.println("Kayıt ID,Kullanıcı Adı,Kullanıcı Soyadı,E-posta,Telefon,Kurs Adı,Eğitmen,Kurs Türü," +
                       "Kayıt Durumu,İlerleme (%),Kayıt Tarihi,Kurs Başlangıç,Kurs Bitiş,Ders Günleri,Ders Saati");

        for (Enrollment e : enrollments) {
            User user = e.getUser();
            Course course = e.getCourse();

            writer.print(escapeCsv(e.getId()));
            writer.print(',');
            writer.print(escapeCsv(user.getFirstName()));
            writer.print(',');
            writer.print(escapeCsv(user.getLastName()));
            writer.print(',');
            writer.print(escapeCsv(user.getEmail()));
            writer.print(',');
            writer.print(user.getPhoneNumber() != null ? escapeCsv(user.getPhoneNumber()) : "");
            writer.print(',');
            writer.print(escapeCsv(course.getTitle()));
            writer.print(',');
            writer.print(course.getInstructorName() != null ? escapeCsv(course.getInstructorName()) : "");
            writer.print(',');
            writer.print(course.getCourseType() != null ? course.getCourseType().getDisplayName() : "");
            writer.print(',');
            writer.print(escapeCsv(e.getStatus().getDisplayName()));
            writer.print(',');
            writer.print(e.getProgressPercentage());
            writer.print(',');
            writer.print(e.getEnrollmentDate() != null ? e.getEnrollmentDate().format(CSV_DATE_FMT) : "");
            writer.print(',');
            writer.print(course.getStartDate() != null ? course.getStartDate().format(CSV_DATE_FMT) : "");
            writer.print(',');
            writer.print(course.getEndDate() != null ? course.getEndDate().format(CSV_DATE_FMT) : "");
            writer.print(',');
            writer.print(course.getScheduleDays() != null ? escapeCsv(course.getScheduleDays()) : "");
            writer.print(',');
            writer.print(formatScheduleTime(course));
            writer.println();
        }

        writer.flush();
    }

    private String formatScheduleTime(Course course) {
        if (course.getScheduleStartTime() == null && course.getScheduleEndTime() == null) return "";
        String start = course.getScheduleStartTime() != null ? course.getScheduleStartTime() : "";
        String end = course.getScheduleEndTime() != null ? course.getScheduleEndTime() : "";
        return escapeCsv(start + " - " + end);
    }

    private String escapeCsv(Object value) {
        String s = value != null ? value.toString() : "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
