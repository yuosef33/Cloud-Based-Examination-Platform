package com.yuosef.cloudbasedlabexaminationplatform.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Lab extends AuditingBase {

    @Id
    @GeneratedValue
    private  Long Id;

    private  String labName;

    private String labDescription;

    private String labInstructions;

    private Duration labDuration;

    private LocalDateTime labStartTime;

    private LocalDateTime labEndTime;

    @Enumerated(EnumType.STRING)
    private LabStatus status;

    @ManyToOne
    @JoinColumn(name = "lab_Template")
    @JsonBackReference
    private LabTemplate labTemplate;

    @JsonIgnore
    @OneToMany(mappedBy = "lab")
    private List<VmInstance> vms;

}
