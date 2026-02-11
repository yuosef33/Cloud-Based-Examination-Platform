package com.yuosef.demo1.cloudexaminationplatform.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    private String name;

    private String email;


    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String pwd;

    @Column(name = "create_dt")
    @JsonIgnore
    private Date createDt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name ="user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "authoritie_id")
    )
    List<Authority> authorities;

    @JsonManagedReference
    @OneToMany(mappedBy = "createdBy")
    @JsonIgnore
    private List<LabTemplate> templates;

    public User(String name, String email, String mobileNumber, String pwd, Date createDt, List<Authority> authorities) {
        this.name = name;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.pwd = pwd;
        this.createDt = createDt;
        this.authorities = authorities;
    }

}
