package vn.edu.auction.shared.dto;

import vn.edu.auction.shared.enums.UserRole;

public class RegisterRequest {

    private String fullName;
    private String email;
    private String password;
    private UserRole role;
    
    public RegisterRequest(String fullName, String email, String password, UserRole role){
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public RegisterRequest(){}

    public String getFullName(){return fullName;}
    public String getEmail(){return email;}
    public String getPassword(){return password;}
    public UserRole getRole(){return role;}

    public void setFullName(String newFullName){this.fullName = newFullName;}
    public void setEmail(String newEmail){this.email = newEmail;}
    public void setPassword(String newPassword){this.password = newPassword;}
    public void setRole(UserRole newRole){this.role = newRole;}
    
}
