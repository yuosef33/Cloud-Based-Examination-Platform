package com.yuosef.demo1.cloudexaminationplatform.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    public LabTemplate(String amiName, String amiId, User createdBy) {
        this.amiName = amiName;
        this.amiId = amiId;
        this.createdBy = createdBy;
    }
}
