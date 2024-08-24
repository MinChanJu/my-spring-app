package com.example.my_spring_app.model;

public class UserDTO {
    private Long id;
    private String name;
    private String userId;
    private Integer authority;
    private String email;

    // 기본 생성자 (필수는 아니지만, 필요할 수 있음)
    public UserDTO() {
    }

    // 모든 필드를 매개변수로 받는 생성자
    public UserDTO(Long id, String name, String userId, Integer authority, String email) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.authority = authority;
        this.email = email;
    }

    // 게터와 세터 (Getter and Setter)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getAuthority() {
        return authority;
    }

    public void setAuthority(Integer authority) {
        this.authority = authority;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
