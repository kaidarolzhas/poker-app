package webapp.service;


import webapp.data.dto.LoginRequestDto;
import webapp.data.dto.LoginResponseDto;
import webapp.data.dto.RegistrationRequestDto;

public interface AuthService {
    void registration(RegistrationRequestDto registrationRequest);
    LoginResponseDto login(LoginRequestDto loginRequest);
}
