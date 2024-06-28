package domein;

import java.io.Serializable;
import java.util.Objects;

public class SaveGame implements Serializable {

    private String path;
    private String name;
    private boolean ignored;


    public SaveGame(String name, String path, boolean ignored) {
        setPath(path);
        setName(name);
        setIgnored(ignored);
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SaveGame saveGame = (SaveGame) o;
        return Objects.equals(path, saveGame.path) && Objects.equals(name, saveGame.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, name);
    }

    @Override
    public String toString() {
        return String.format("%s : %s", getName(), getPath());
    }
}
