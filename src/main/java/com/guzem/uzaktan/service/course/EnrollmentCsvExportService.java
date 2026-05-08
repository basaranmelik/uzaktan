package com.guzem.uzaktan.service.course;

import java.io.IOException;
import java.io.OutputStream;

public interface EnrollmentCsvExportService {

    void exportAllToCsv(OutputStream outputStream) throws IOException;

    void exportByCourseToCsv(Long courseId, OutputStream outputStream) throws IOException;
}
