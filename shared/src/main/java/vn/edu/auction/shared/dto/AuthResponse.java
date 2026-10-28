package vn.edu.auction.shared.dto;

public class AuthResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String token;
    private String role;

    public AuthResponse(Long userId, String fullName, String email, String token, String role){
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.token = token;
        this.role = role;
    }

    public AuthResponse(){}

    public Long getUserId(){return userId;}
    public String getFullName(){return fullName;}
    public String getEmail(){return email;}
    public String getToken(){return token;}
    public String getRole(){return role;}

    public void setUserId(Long newUserId){this.userId = newUserId;}
    public void setFullName(String newFullName){this.fullName = newFullName;}
    public void setEmail(String newEmail){this.email = newEmail;}
    public void setToken(String newToken){this.token = newToken;}
    public void setRole(String newRole){this.role = newRole;}

}
