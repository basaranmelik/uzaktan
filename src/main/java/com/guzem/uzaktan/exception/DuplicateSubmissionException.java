package com.guzem.uzaktan.exception;

public class DuplicateSubmissionException extends RuntimeException {

    public DuplicateSubmissionException(Long assignmentId, Long userId) {
        super(String.format("Bu ödev için zaten bir teslim bulunmaktadır. (Ödev ID: %d, Kullanıcı ID: %d)",
                assignmentId, userId));
    }
}
