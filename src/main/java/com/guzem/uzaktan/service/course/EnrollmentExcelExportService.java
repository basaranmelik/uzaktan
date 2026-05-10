package com.guzem.uzaktan.service.course;

import java.io.IOException;
import java.io.OutputStream;

public interface EnrollmentExcelExportService {

    void exportAllToExcel(OutputStream outputStream) throws IOException;

    void exportByCourseToExcel(Long courseId, OutputStream outputStream) throws IOException;
}
