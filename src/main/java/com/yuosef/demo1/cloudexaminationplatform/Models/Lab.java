package com.yuosef.demo1.cloudexaminationplatform.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Lab {

    @Id
    @GeneratedValue
    private  Long Id;

    private  String labName;

    private Duration labDuration;

    private LocalDateTime labStartTime;

    private LocalDateTime labEndTime;
    @Enumerated(EnumType.STRING)
    private LabStatus status;

    @CreationTimestamp
    private Date createdAt;

    @ManyToOne
    @JoinColumn(name = "lab_Template")
    @JsonBackReference
    private LabTemplate labTemplate;

}
