package com.guzem.uzaktan.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionImportResult {
    private int totalRows;
    private int successCount;
    private int updateCount;
    private int errorCount;
    private List<RowError> errors;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RowError {
        private int rowNumber;
        private String field;
        private String message;
    }
}
