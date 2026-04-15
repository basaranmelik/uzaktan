package com.guzem.uzaktan.exception;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("status", 404);
        return "error/404";
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({DuplicateEnrollmentException.class, CourseFullException.class, DuplicateSubmissionException.class})
    public String handleConflict(RuntimeException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("status", 409);
        return "error/409";
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(UnauthorizedActionException.class)
    public String handleForbidden(UnauthorizedActionException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("status", 403);
        return "error/403";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidation(MethodArgumentNotValidException ex, Model model) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Geçersiz değer",
                        (existing, replacement) -> existing
                ));
        model.addAttribute("fieldErrors", fieldErrors);
        model.addAttribute("status", 400);
        return "error/400";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("status", 400);
        return "error/400";
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public String handleOptimisticLock(Model model) {
        model.addAttribute("message",
                "Bu kayıt siz görüntülerken başka bir yönetici tarafından değiştirildi. " +
                "Lütfen sayfayı yenileyip tekrar deneyin.");
        model.addAttribute("status", 409);
        return "error/409";
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrity(DataIntegrityViolationException ex, Model model) {
        log.warn("Veritabanı kısıtlama ihlali: {}", ex.getMostSpecificCause().getMessage());
        model.addAttribute("message", "Bu işlem veritabanı kısıtlamalarıyla çelişiyor. Kayıt zaten mevcut olabilir.");
        model.addAttribute("status", 409);
        return "error/409";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSize(MaxUploadSizeExceededException ex, Model model) {
        model.addAttribute("message", "Yüklenen dosya boyutu izin verilen maksimum boyutu aşıyor.");
        model.addAttribute("status", 400);
        return "error/400";
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleBrokenPipe(AsyncRequestNotUsableException ex) {
        log.debug("İstemci bağlantısı koptu (Broken pipe): {}", ex.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public void handleIOException(IOException ex, HttpServletResponse response) throws IOException {
        String message = ex.getMessage();
        if (message != null && (message.contains("Broken pipe") || message.contains("Connection reset"))) {
            log.debug("İstemci bağlantısı koptu: {}", message);
            return;
        }
        log.error("IO hatası: {}", message, ex);
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        log.error("İşlenmeyen hata: {}", ex.getMessage(), ex);
        model.addAttribute("message", "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyiniz.");
        model.addAttribute("status", 500);
        return "error/500";
    }
}
