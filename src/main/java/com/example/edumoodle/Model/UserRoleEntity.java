package com.example.edumoodle.Model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "tbl_User_Role")
public class UserRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "userid", referencedColumnName = "id_user")
    private UsersEntity usersEntity;

    @ManyToOne
    @JoinColumn(name = "roleid", referencedColumnName = "id_role")
    private RolesEntity rolesEntity;

    @OneToMany(mappedBy = "userRoleEntity")
    private List<CourseAssignmentEntity> courseAssign;

    public UserRoleEntity() {}

    public UserRoleEntity(Integer id, UsersEntity usersEntity, RolesEntity rolesEntity) {
        this.id = id;
        this.usersEntity = usersEntity;
        this.rolesEntity = rolesEntity;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UsersEntity getUsersEntity() {
        return usersEntity;
    }

    public void setUsersEntity(UsersEntity usersEntity) {
        this.usersEntity = usersEntity;
    }

    public RolesEntity getRolesEntity() {
        return rolesEntity;
    }

    public void setRolesEntity(RolesEntity rolesEntity) {
        this.rolesEntity = rolesEntity;
    }
}
