package org.tpl.chat.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReactionInputModel {
    @NotBlank
    private String reaction;
}
