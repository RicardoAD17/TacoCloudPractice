package tacos.ops;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@NoArgsConstructor
@Document(collection = "ops_announcements")
public class OpsAnnouncement {

    @Id
    private String id; 

    @NotBlank(message = "El texto del anuncio no puede estar vacío")
    @Size(max = 200, message = "El texto no debe exceder los 200 caracteres")
    @Pattern(regexp = "^[^\\p{Cc}]+$", message = "El texto no debe contener caracteres de control")
    private String text;

    private String severity; 

    private Date createdAt = new Date();

    @NotNull(message = "La fecha de expiración es obligatoria")
    private Date expiresAt;

    private String createdBy;

    private boolean active = true;
}