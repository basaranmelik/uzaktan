package com.guzem.uzaktan.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssessmentItem {

    /** Değerlendirme türü — ör. "Sınav/test", "Uygulama/proje", "Katılım/performans" */
    private String type;

    /** Açıklama */
    private String description;

    /** Ağırlık yüzdesi — ör. 40 */
    private Integer weight;
}
