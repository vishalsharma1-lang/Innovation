package com.cms.entity.vehicle;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "model_faq_question_answer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelFaq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Integer questionId;

    @Column(length = 1024)
    private String question;

    @Column(columnDefinition = "CLOB")
    private String answer;

    @Column(length = 50)
    private String category;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 22)
    private String filter;

    @Column(name = "filter_category", length = 30)
    private String filterCategory;
}
