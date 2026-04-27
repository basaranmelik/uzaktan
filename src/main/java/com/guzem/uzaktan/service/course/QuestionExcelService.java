package com.guzem.uzaktan.service.course;

import com.guzem.uzaktan.dto.response.QuestionImportResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;

public interface QuestionExcelService {

    QuestionImportResult importFromExcel(Long courseId, MultipartFile file, Long requestingUserId);

    void exportToExcel(Long courseId, Long requestingUserId, OutputStream out) throws IOException;

    void generateTemplate(OutputStream out) throws IOException;
}
