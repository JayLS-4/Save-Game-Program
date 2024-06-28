package domein;

import dto.ComputerDTO;
import dto.SaveGameDTO;

import java.util.List;
import java.util.stream.Collectors;

public class DomeinController {

    ComputerRepository computerRepository;

    public DomeinController() {
        computerRepository = new ComputerRepository();
    }

    public List<ComputerDTO> giveAllComputerDTOs() {
        if (computerRepository.getComputers() == null || computerRepository.getComputers().isEmpty()) {
            return null;
        }
        return computerRepository.getComputers().stream().map(this::createComputerDTO).collect(Collectors.toList());
    }

    public void addSaveGameToComputer(String nameCopmuter, String nameSaveGame, String path) {
        computerRepository.addSaveGameToComputer(nameCopmuter, nameSaveGame, path);
    }

    public void removeSaveGameFromComputer(String nameComputer, String nameSaveGame) {
        computerRepository.removeSaveGameFromComputer(nameComputer, nameSaveGame);
    }

    public void flipIgnored(String nameComputer, String nameSaveGame) {
        computerRepository.flipIgnored(nameComputer, nameSaveGame);
    }

    public void addNewComputer(String name) {
        computerRepository.addComputer(new Computer(name));
    }

    public void removeComputer(String name) {
        computerRepository.removeComputer(name);
    }

    private ComputerDTO createComputerDTO(Computer computer) {
        return new ComputerDTO(computer.getName(), giveListSaveGameDTOs(computer.getSaveGames()));
    }

    private List<SaveGameDTO> giveListSaveGameDTOs(List<SaveGame> saveGames) {
        return saveGames.stream().map(saveGame -> new SaveGameDTO(saveGame.getName(), saveGame.getPath(), saveGame.isIgnored())).collect(Collectors.toList());
    }
}
