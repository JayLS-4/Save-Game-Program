package dto;

import java.util.List;

public record ComputerDTO(String name, List<SaveGameDTO> saveGames) {
}
