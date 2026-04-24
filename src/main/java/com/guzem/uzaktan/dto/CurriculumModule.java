package com.guzem.uzaktan.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Kurs müfredatındaki bir modülü (bölümü) temsil eder.
 * Course.manualCurriculum alanında JSON listesi olarak saklanır.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurriculumModule {

    /** Modül başlığı — ör. "Mikroservis Mimarisi ve Temelleri" */
    private String title;

    /** İsteğe bağlı modül açıklaması */
    private String description;

    /** Modül konuları / başlıkları — ör. ["Mikroservis vs Monolitik", ...] */
    private List<String> topics = new ArrayList<>();
}
