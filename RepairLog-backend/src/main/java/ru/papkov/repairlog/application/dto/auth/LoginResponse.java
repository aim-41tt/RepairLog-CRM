package ru.papkov.repairlog.application.dto.auth;

import java.util.List;

/**
 * Ответ при успешной авторизации.
 * Содержит JWT-токен, ФИО и список ролей пользователя.
 *
 * @author aim-41tt
 */
public class LoginResponse {

    /** JWT-токен доступа. */
    private String token;

    /** Тип токена (всегда Bearer). */
    private String tokenType = "Bearer";

    /** Полное имя сотрудника в формате "Фамилия И.О." */
    private String fullName;

    /** Список ролей пользователя. */
    private List<String> roles;

    public LoginResponse() {}

    public LoginResponse(String token, String fullName, List<String> roles) {
        this.token = token;
        this.fullName = fullName;
        this.roles = roles;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
