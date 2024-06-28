package gui;

import domein.DomeinController;
import dto.ComputerDTO;
import dto.SaveGameDTO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class AddMissingSaveGamesWindow extends AnchorPane implements Initializable {

    private final DomeinController dc;
    private final String nameSaveGame;
    private String hostname;

    public AddMissingSaveGamesWindow(DomeinController dc, String nameSaveGame) {
        this.dc = dc;
        this.nameSaveGame = nameSaveGame;
        loadFxmlScreen();
    }

    private void loadFxmlScreen() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AddMissingSaveGamesWindow.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hostname = "Unknown";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {
            System.out.println("Hostname cannot be resolved");
        }

        lblMissingSaveGame.setText(String.format("Add missing save game for: %s", nameSaveGame));
    }

    @FXML
    void btnAddSaveGameOnAction(ActionEvent event) {
        String path = tfPath.getText();
        if (!isValidDirectory(path)) {
            lblPathError.setText("Path is invalid");
        } else {
            dc.addSaveGameToComputer(hostname, nameSaveGame, path);
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
        }
    }

    @FXML
    void btnBrowseOnAction(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        String pathString = "";
        File selectedDirectory = chooser.showDialog(null);
        if (selectedDirectory != null)
            pathString = selectedDirectory.getAbsolutePath();

        tfPath.setText(pathString);
    }

    @FXML
    void btnIgnoreOnAction(ActionEvent event) {
        for (ComputerDTO c : dc.giveAllComputerDTOs()) {
            if (c.saveGames().stream().map(SaveGameDTO::name).toList().contains(nameSaveGame)) {
                dc.flipIgnored(c.name(), nameSaveGame);
            }
        }
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    private boolean isValidDirectory(String directoryPath) {
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            return false;
        }

        Path path = Paths.get(directoryPath);
        return Files.exists(path) && Files.isDirectory(path);
    }

    @FXML
    private Button btnAddSaveGame;

    @FXML
    private Button btnBrowse;

    @FXML
    private Button btnIgnore;

    @FXML
    private Label lblMissingSaveGame;

    @FXML
    private Label lblPathError;

    @FXML
    private TextField tfPath;
}
