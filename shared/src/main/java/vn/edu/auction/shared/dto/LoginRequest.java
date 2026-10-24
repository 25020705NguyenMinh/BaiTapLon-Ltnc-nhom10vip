package vn.edu.auction.shared.dto;

public class LoginRequest {

    private String email;
    private String password;

    public LoginRequest(String email, String password){
        this.email = email;
        this.password = password;
    }

    public String getEmail(){return email;}
    public String getPassword(){return password;}

    public void setEmail(String newEmail){this.email = newEmail;}
    public void setPassword(String newPassword){this.password = newPassword;}
    
}
