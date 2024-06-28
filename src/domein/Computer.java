package domein;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Computer implements Serializable {

    private List<SaveGame> saveGames;
    private String name;

    public Computer(String name) {
        setName(name);
        saveGames = new ArrayList<>();
    }

    public void flipIgnored(String nameSaveGame) {
        int index = saveGames.stream().map(SaveGame::getName).toList().indexOf(nameSaveGame);
        saveGames.get(index).setIgnored(!saveGames.get(index).isIgnored());
    }

    public void addNewSaveGame(String name, String path) {
        saveGames.add(new SaveGame(name, path, false));
    }

    public void removeSaveGame(String nameSaveGame) {
        int index = saveGames.stream().map(SaveGame::getName).toList().indexOf(nameSaveGame);
        saveGames.remove(index);
    }

    public List<SaveGame> getSaveGames() {
        return saveGames;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Computer computer = (Computer) o;
        return Objects.equals(name, computer.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return getSaveGames().stream().map(SaveGame::toString).collect(Collectors.joining("\n"));
    }
}
