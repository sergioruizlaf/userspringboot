package dekra.user.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Collection;


@Data
@NoArgsConstructor
@Entity
@Table(name = "USER")
@ApiModel(value = "User Model", description = "User Model for user controller request response")
public class User implements UserDetails{

    public User(String username, String password, String name, String surname, String email, Boolean active,
                Integer age, LocalDateTime creationDate) {
        this.userName = username;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.active = active;
        this.age = age;
        this.creationDate = creationDate;
    }
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ApiModelProperty(value="UserName", required = true, position=1)
    private String userName;

    private String name;

    private String surname;

    @Size(min = 4, max = 50)
    @Email
    private String email;

    @Size(min = 4,  message = "Minimum length: 4 characters")
    @NotNull
    @ApiModelProperty(value="User password", required = true, position=2)
    private String password;

    @Min(value = 15, message = "Should not be less than 15")
    @Max(value = 65, message = "Should not be greater than 65")
    private Integer age;

    @ApiModelProperty(value="Active user",  position=2)
    private boolean active;

    private LocalDateTime lastLogging;

    private LocalDateTime creationDate;

    @Transient
    private String token;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return false;
    }
}
