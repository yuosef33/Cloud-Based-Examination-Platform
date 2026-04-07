package com.yuosef.cloudbasedlabexaminationplatform.models;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VmInstance extends AuditingBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String instanceId;

    private String publicIp;

    private Integer vncPort;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private VmStatus status;

    private String terraformStateKey;

    private String runId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private LabTemplate labTemplate;

    @ManyToOne
    @JoinColumn(name = "lab_id")
    private Lab lab;

}