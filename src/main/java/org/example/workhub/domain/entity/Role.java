package org.example.workhub.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.example.workhub.domain.entity.common.FlagUserDateAuditing;

import java.io.Serializable;
import java.util.List;


@Entity
@Table(name = "tbl_roles")
@Getter
@Setter
@NoArgsConstructor
public class Role  {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Ten Role khong duoc de trong!")
  private String name;

  private String description;


  @ManyToMany(fetch = FetchType.LAZY)
  @JsonIgnoreProperties("roles")
  @JoinTable(
          name = "tbl_permission_role",
          joinColumns = @JoinColumn(name = "role_id"),
          inverseJoinColumns = @JoinColumn(name = "permission_id")
  )
  private List<Permission> permissions;

  @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
  @JsonIgnore
  private List<User> users;


  public Role(String roleName, String description, List<Permission> permissionList) {
    this.name = roleName;
    this.description = description;
    this.permissions = permissionList;
  }

}