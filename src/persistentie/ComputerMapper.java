package persistentie;

import domein.Computer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ComputerMapper {

    public List<Computer> giveAllComputersFromFile() throws IOException {
        List<Computer> computers = null;
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(Path.of("Save game program data", "DataComputers.ser")))) {
            computers = (List<Computer>) input.readObject();
        } catch (EOFException ignored) {
        } catch (InvalidPathException ie) {
            System.err.println("Ongeldig pad.");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return computers;
    }

    public void writeComputersToFile(Collection<Computer> computers) {
        try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(Path.of("Save game program data", "DataComputers.ser")))) {
            output.writeObject(computers);
        } catch (InvalidPathException ie) {
            System.err.println("Ongeldig pad.");
        } catch (IOException io) {
            System.err.println("Kan bestand niet opnenen.");
        }
    }

}
