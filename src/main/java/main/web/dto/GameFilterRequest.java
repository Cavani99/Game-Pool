package main.web.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GameFilterRequest {
    private List<UUID> categories;
    private List<UUID> companies;
}
