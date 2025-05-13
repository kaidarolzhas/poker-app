package webapp.data.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegistrationRequestDto {
    @NotBlank(message = "Username cannot be blank")
    @Pattern(
            regexp = "^[a-zA-Z0-9]+$",
            message = "Username can only contain letters and numbers, without spaces"
    )
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 5, message = "Password must be at least 5 characters long")
    private String password;
}
