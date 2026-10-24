package vn.edu.auction.shared.dto;

import java.time.LocalDateTime;

public class ApiErrorResponse {
    
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public ApiErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path){
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ApiErrorResponse(){}

    public LocalDateTime getTimestamp(){return timestamp;}
    public int getStatus(){return status;}
    public String getError(){return error;}
    public String getMessage(){return message;}
    public String getPath(){return path;}

    public void setTimestamp(LocalDateTime newTimestamp){this.timestamp = newTimestamp;}
    public void setStatus(int newStatus){this.status = newStatus;}
    public void setError(String newError){this.error = newError;}
    public void setMessage(String newMessage){this.message = newMessage;}
    public void setPath(String newPath){this.path = newPath;}


}
