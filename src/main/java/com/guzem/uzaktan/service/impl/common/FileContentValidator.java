package com.guzem.uzaktan.service.impl.common;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Dosya içeriğinin gerçek MIME tipini (magic number) tespit ederek
 * izin verilen türlerle karşılaştırır.
 *
 * Sadece uzantı kontrolü yetersizdir; kullanıcı uzantıyı değiştirip
 * zararlı içerik yükleyebilir (polyglot dosyalar).
 */
@Component
public class FileContentValidator {

    private final Tika tika = new Tika();

    public void validate(MultipartFile file, Set<String> allowedMimeTypes, String fileType) throws IOException {
        String detected;
        try (InputStream is = file.getInputStream()) {
            detected = tika.detect(is);
        }
        if (!allowedMimeTypes.contains(detected)) {
            throw new IllegalArgumentException(
                    "Geçersiz " + fileType + " içeriği. Tespit edilen format: " + detected +
                    ", izin verilen formatlar: " + allowedMimeTypes);
        }
    }
}
