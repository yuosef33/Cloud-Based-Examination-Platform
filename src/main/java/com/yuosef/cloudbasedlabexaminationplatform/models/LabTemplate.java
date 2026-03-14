package com.yuosef.cloudbasedlabexaminationplatform.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LabTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;
    @Column(unique = true, nullable = false)
    private String amiName;

    private String amiId;

    @CreationTimestamp
    private Date createdAt;

    @ManyToOne
    @JoinColumn(name = "createdBy")
    @JsonBackReference
    private User createdBy;

    @JsonManagedReference
    @OneToMany(mappedBy = "labTemplate")
    private List<Lab> labs;

    @OneToMany(mappedBy = "labTemplate")
    @JsonIgnore
    private List<VmInstance> instances;

    public LabTemplate(String amiName, String amiId, User createdBy) {
        this.amiName = amiName;
        this.amiId = amiId;
        this.createdBy = createdBy;
    }
    public LabTemplate(String amiName, String amiId) {
        this.amiName = amiName;
        this.amiId = amiId;
    }
}
