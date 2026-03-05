package com.yuosef.cloudbasedlabexaminationplatform.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
