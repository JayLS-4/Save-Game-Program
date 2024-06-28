package domein;

import persistentie.ComputerMapper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ComputerRepository {

    private List<Computer> computers;
    private final ComputerMapper computerMapper;

    public ComputerRepository() {
        computerMapper = new ComputerMapper();
        getAllComputersFromFile();
    }

    public List<Computer> getComputers() {
        return computers;
    }

    public void saveComputers() {
        computerMapper.writeComputersToFile(computers);
    }

    public void addComputer(Computer computer) {
        computers.add(computer);
        saveComputers();
        getAllComputersFromFile();
    }

    public void removeComputer(String name) {
        computers.remove(getComputerFromName(name));
        saveComputers();
        getAllComputersFromFile();
    }

    public void addSaveGameToComputer(String nameComputer, String nameSaveGame, String path) {
        Computer computer = getComputerFromName(nameComputer);
        computer.addNewSaveGame(nameSaveGame, path);
        saveComputers();
        getAllComputersFromFile();
    }

    public void removeSaveGameFromComputer(String nameComputer, String nameSaveGame) {
        Computer computer = getComputerFromName(nameComputer);
        computer.removeSaveGame(nameSaveGame);
        saveComputers();
        getAllComputersFromFile();
    }

    public void flipIgnored(String nameComputer, String nameSaveGame) {
        Computer computer = getComputerFromName(nameComputer);
        computer.flipIgnored(nameSaveGame);
        saveComputers();
        getAllComputersFromFile();
    }

    private void getAllComputersFromFile() {
        try {
            computers = computerMapper.giveAllComputersFromFile();
            if (computers == null) {
                computers = new ArrayList<>();
                addComputer(new Computer(getHostName()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Computer getComputerFromName(String name) {
        return computers.stream().filter(computer -> computer.getName().equals(name)).findFirst().orElse(null);
    }

    private String getHostName() {
        String hostname = "Unknown";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {
            System.out.println("Hostname cannot be resolved");
        }
        return hostname;
    }
}
