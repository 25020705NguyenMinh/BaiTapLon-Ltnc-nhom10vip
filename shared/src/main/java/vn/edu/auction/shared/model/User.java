package vn.edu.auction.shared.model;

import vn.edu.auction.shared.enums.UserRole;
import vn.edu.auction.shared.enums.UserStatus;

public abstract class User {

    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private UserStatus status;

    public User(Long id, String fullName, String email, UserRole role, UserStatus status){
        this.id = id;
        this.fullName = fullName;
        this.email = email; 
        this.role = role;
        this.status = status;
    }

    //dự phòng tự khởi tạo
    public User(){}

    public Long getId(){return id;}
    public String getFullName(){return fullName;}
    public String getEmail(){return email;}
    public UserRole getRole(){return role;}
    public UserStatus getStatus(){return status;}

    public void setId(Long newId){this.id = newId;}
    public void setFullName(String newFullName){this.fullName = newFullName;}
    public void setEmail(String newEmail){this.email = newEmail;}
    public void setRole(UserRole newRole){this.role = newRole;}
    public void setStatus(UserStatus newStatus){this.status = newStatus;}

}