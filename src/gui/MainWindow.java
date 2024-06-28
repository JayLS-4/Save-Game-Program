package gui;

import domein.DomeinController;
import dto.ComputerDTO;
import dto.SaveGameDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.concurrent.Task;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.util.*;

public class MainWindow extends StackPane implements Initializable  {
    private final Stage stage;

    private final DomeinController dc;
    private final List<Tab> tabs = new ArrayList<>();
    private List<ComputerDTO> computers;
    private String hostname;

    private final List<ListView<String>> lvSaveGamesList = new ArrayList<>();
    private final List<TextField> textFieldNameList = new ArrayList<>();
    private final List<TextField> textFieldPathList = new ArrayList<>();
    private final List<Label> lblNameErrorList = new ArrayList<>();
    private final List<Label> lblPathErrorList = new ArrayList<>();
    private final List<ProgressBar> pbCopyingProgressList = new ArrayList<>();
    private final List<Label> lblProgressList = new ArrayList<>();
    private final List<Label> lblSizeList = new ArrayList<>();
    private final List<Button> btnAddSaveGameList = new ArrayList<>();
    private final List<Button> btnCopyToDriveList = new ArrayList<>();
    private final List<Button> btnCopyToComputerList = new ArrayList<>();



    public MainWindow(Stage stage) {
        this.stage = stage;
        dc = new DomeinController();
        loadFxmlScreen();
    }

    private void loadFxmlScreen() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
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
        if (!isRunningFromUSB()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Warning!");
            alert.setHeaderText("");
            alert.setContentText("This application has to be run from a USB device!");
            alert.showAndWait();
            Platform.exit();
        }

        hostname = "Unknown";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {
            System.out.println("Hostname cannot be resolved");
        }

        computers = dc.giveAllComputerDTOs();

        if (!computers.stream().map(ComputerDTO::name).toList().contains(hostname)) {
            dc.addNewComputer(hostname);
            computers = dc.giveAllComputerDTOs();
        }

        computers.forEach(this::makeTab);

        tpMainTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);

        showSaveGames();

        addMissingSaveGames();
    }

    private void addMissingSaveGames() {
        computers = dc.giveAllComputerDTOs();
        ComputerDTO currentComputer = computers.stream().filter(c -> c.name().equals(hostname)).findFirst().orElse(null);
        List<SaveGameDTO> uniqueSaveGamesList = new ArrayList<>();

        computers.stream().filter(c -> !c.name().equals(hostname)).forEach(c -> uniqueSaveGamesList.addAll(c.saveGames()));
        HashSet<SaveGameDTO> uniqueSaveGames = new HashSet<>(uniqueSaveGamesList.stream().filter(sg -> !sg.ignored() && !Objects.requireNonNull(currentComputer).saveGames().stream().map(SaveGameDTO::name).toList().contains(sg.name())).toList());

        for (SaveGameDTO sg : uniqueSaveGames) {
            Stage subStage = new Stage();
            subStage.initOwner(stage);
            subStage.initModality(Modality.WINDOW_MODAL);
            subStage.setTitle("Add Missing Save Games");
            AddMissingSaveGamesWindow subScene = new AddMissingSaveGamesWindow(dc, sg.name());
            subStage.setScene(new Scene(subScene));
            subStage.setResizable(false);
            subStage.showAndWait();
        }
        showSaveGames();
    }

    private void showSaveGames() {
        computers = dc.giveAllComputerDTOs();
        computers.forEach(c -> {
            int index = computers.stream().map(ComputerDTO::name).toList().indexOf(c.name());
            lvSaveGamesList.get(index).getItems().clear();
            c.saveGames().forEach(saveGameDTO -> lvSaveGamesList.get(index).getItems().add(String.format("%s | %s", saveGameDTO.name(), saveGameDTO.path())));
        });
    }

    private void makeTab(ComputerDTO computerDTO) {
        int index = computers.stream().map(ComputerDTO::name).toList().indexOf(computerDTO.name());
        Tab tab = new Tab(computerDTO.name());
        AnchorPane anchorPane = new AnchorPane();

        ListView<String> lvSaveGames = new ListView<>();
        TextField textFieldName = new TextField();
        Label lblAddSaveGame = new Label("Add new save game");
        Label lblName = new Label("Name:");
        Label lblPath = new Label("Path:");
        Label lblNameError = new Label();
        Label lblPathError = new Label();
        TextField textFieldPath = new TextField();
        Button btnBrowse = new Button("Browse");
        Button btnAddSaveGame = new Button("Add save game");
        Button btnCopyToComputer = new Button("Copy all to computer");
        Button btnCopyToDrive = new Button("Copy all to drive");
        ProgressBar pbCopyingProgress = new ProgressBar();
        Label lblProgress = new Label();
        Label lblSize = new Label();

        textFieldName.setLayoutX(114);
        textFieldName.setLayoutY(71);
        textFieldName.setPrefHeight(25);
        textFieldName.setPrefWidth(218);
        textFieldName.setDisable(true);
        textFieldNameList.add(textFieldName);

        lvSaveGames.setLayoutX(355);
        lvSaveGames.setLayoutY(13);
        lvSaveGames.setPrefHeight(346);
        lvSaveGames.setPrefWidth(231);

        ContextMenu contextMenu = new ContextMenu();
        CheckMenuItem ignored = new CheckMenuItem("Ignored");
        MenuItem delete = new MenuItem("Delete");
        contextMenu.getItems().addAll(ignored, delete);
        lvSaveGames.setContextMenu(contextMenu);
        lvSaveGamesList.add(lvSaveGames);

        lvSaveGames.setOnMouseClicked(event -> {
            computers = dc.giveAllComputerDTOs();
            if (event.getButton() == MouseButton.SECONDARY) {
                int selectedIndex = lvSaveGamesList.get(index).getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0) {
                    ignored.setSelected(computers.get(index).saveGames().get(selectedIndex).ignored());
                } else {
                    ignored.setSelected(false);
                }
            }
        });

        ignored.setOnAction(e -> {
            computers = dc.giveAllComputerDTOs();
            if (lvSaveGamesList.get(index).getSelectionModel().getSelectedItem() != null)
                dc.flipIgnored(computers.get(index).name(), lvSaveGamesList.get(index).getSelectionModel().getSelectedItem().split(" \\| ")[0]);
        });

        delete.setOnAction(e -> {
            String currentSaveGameFull = lvSaveGamesList.get(index).getSelectionModel().getSelectedItem();
            if (currentSaveGameFull != null) {
                String currentSaveGame = currentSaveGameFull.split(" \\| ")[0];
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, String.format("Do you want to delete \"%s\" from every computer?", currentSaveGame), ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                alert.setTitle("Confirmation");
                alert.setHeaderText("");
                alert.showAndWait();
                if (alert.getResult() == ButtonType.YES) {
                    for (int i = 0; i < tabs.size(); i++) {
                        if (lvSaveGamesList.get(i).getItems().stream().map(s -> s.split(" \\| ")[0]).toList().contains(currentSaveGame))
                            dc.removeSaveGameFromComputer(tabs.get(i).getText(), currentSaveGame);
                    }
                    lvSaveGamesList.forEach(lvsg -> lvsg.getItems().remove(currentSaveGame));
                    showSaveGames();
                }
                if (alert.getResult() == ButtonType.NO) {
                    dc.removeSaveGameFromComputer(tabs.get(index).getText(), currentSaveGame);
                    lvSaveGamesList.get(index).getItems().remove(currentSaveGame);
                    showSaveGames();
                }
            }
        });

        lblAddSaveGame.setLayoutX(55);
        lblAddSaveGame.setLayoutY(14);
        lblAddSaveGame.setFont(new Font("System Bold", 26));

        lblName.setLayoutX(22);
        lblName.setLayoutY(69);
        lblName.setFont(new Font(19));

        lblPath.setLayoutX(35);
        lblPath.setLayoutY(132);
        lblPath.setFont(new Font(19));

        lblNameError.setLayoutX(114);
        lblNameError.setLayoutY(97);
        lblNameError.setPrefHeight(34);
        lblNameError.setPrefWidth(219);
        lblNameError.setWrapText(true);
        lblNameError.setAlignment(Pos.TOP_LEFT);
        lblNameError.setTextFill(Color.color(1, 0, 0));
        lblNameErrorList.add(lblNameError);

        lblPathError.setLayoutX(114);
        lblPathError.setLayoutY(160);
        lblPathError.setTextFill(Color.color(1, 0, 0));
        lblPathErrorList.add(lblPathError);

        lblProgress.setLayoutX(14);
        lblProgress.setLayoutY(290);
        lblProgress.setPrefHeight(81);
        lblProgress.setPrefWidth(322);
        lblProgress.setWrapText(true);
        lblProgress.setAlignment(Pos.TOP_LEFT);
        lblProgressList.add(lblProgress);

        lblSize.setLayoutX(14);
        lblSize.setLayoutY(273);
        lblSize.setPrefHeight(17);
        lblSize.setPrefWidth(322);
        lblSize.setAlignment(Pos.CENTER_RIGHT);
        lblSize.setTextAlignment(TextAlignment.RIGHT);
        lblSizeList.add(lblSize);

        textFieldPath.setLayoutX(114);
        textFieldPath.setLayoutY(134);
        textFieldPath.setPrefHeight(25);
        textFieldPath.setPrefWidth(161);
        textFieldPath.setDisable(true);
        textFieldPathList.add(textFieldPath);

        btnBrowse.setLayoutX(277);
        btnBrowse.setLayoutY(134);
        btnBrowse.setDisable(true);

        btnAddSaveGame.setLayoutX(234);
        btnAddSaveGame.setLayoutY(192);
        btnAddSaveGame.setDisable(true);

        btnCopyToComputer.setLayoutX(13);
        btnCopyToComputer.setLayoutY(238);
        btnCopyToComputer.setDisable(true);
        btnCopyToComputerList.add(btnCopyToComputer);

        btnCopyToComputer.setOnAction(e -> {
            computers = dc.giveAllComputerDTOs();
            btnAddSaveGame.setDisable(true);
            btnCopyToComputer.setDisable(true);
            btnCopyToDrive.setDisable(true);

            ComputerDTO currentComputer = computers.stream()
                    .filter(c -> c.name().equals(hostname))
                    .findFirst()
                    .orElse(null);
            if (currentComputer != null) {
                copySaveGames(currentComputer.saveGames(), false);
            }
        });

        btnCopyToDrive.setLayoutX(232);
        btnCopyToDrive.setLayoutY(238);
        btnCopyToDrive.setDisable(true);
        btnCopyToDriveList.add(btnCopyToDrive);

        btnCopyToDrive.setOnAction(e -> {
            computers = dc.giveAllComputerDTOs();
            btnAddSaveGame.setDisable(true);
            btnCopyToComputer.setDisable(true);
            btnCopyToDrive.setDisable(true);

            ComputerDTO currentComputer = computers.stream()
                    .filter(c -> c.name().equals(hostname))
                    .findFirst()
                    .orElse(null);
            if (currentComputer != null) {
                copySaveGames(currentComputer.saveGames(), true);
            }
        });

        pbCopyingProgress.setLayoutX(14);
        pbCopyingProgress.setLayoutY(272);
        pbCopyingProgress.setPrefHeight(18);
        pbCopyingProgress.setPrefWidth(322);
        pbCopyingProgress.setProgress(0);
        pbCopyingProgress.setStyle("-fx-accent: rgb(0,200,0);");
        pbCopyingProgressList.add(pbCopyingProgress);

        btnBrowse.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            String pathString = "";
            File selectedDirectory = chooser.showDialog(null);
            if (selectedDirectory != null)
                pathString = selectedDirectory.getAbsolutePath();

            textFieldPathList.get(index).setText(pathString);
        });

        btnAddSaveGame.setOnAction(e -> {
            String name = textFieldNameList.get(index).getText();
            String path = textFieldPathList.get(index).getText();
            boolean validInputs = true;
            if (name.isBlank()) {
                lblNameErrorList.get(index).setText("Name cannot be empty");
                validInputs = false;
            } else if (name.contains("\\") || name.contains("/") || name.contains("|") || name.contains(":") || name.contains("*") || name.contains("?") || name.contains("\"") || name.contains("<") || name.contains(">")) {
                lblNameErrorList.get(index).setText("Name cannot conain any of the following characters: /|\\:\"*?<>");
                validInputs = false;
            } else
                lblNameErrorList.get(index).setText("");

            if (!isValidDirectory(path)) {
                lblPathErrorList.get(index).setText("Path is invalid");
                validInputs = false;
            } else {
                lblPathErrorList.get(index).setText("");
            }

            if (validInputs) {
                if (dc.giveAllComputerDTOs().get(index).saveGames().stream().map(s -> s.name().split(" \\| ")[0]).toList().contains(name)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Warning!");
                    alert.setHeaderText("");
                    alert.setContentText("Cannot create save game with a duplicate name!");
                    alert.showAndWait();
                } else {
                    dc.addSaveGameToComputer(computerDTO.name(), name, path);
                    showSaveGames();
                    textFieldNameList.get(index).setText("");
                    textFieldPathList.get(index).setText("");
                }
            }
        });

        btnAddSaveGameList.add(btnAddSaveGame);

        tab.setOnCloseRequest(event -> {
            if (dc.giveAllComputerDTOs().get(index).name().equals(hostname)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Warning!");
                alert.setHeaderText("");
                alert.setContentText("Cannot delete the computer you are currently on!");
                alert.showAndWait();
            } else {
                String currentComputername = computers.get(index).name();
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, String.format("Are you sure you want to delete %s?", currentComputername), ButtonType.YES, ButtonType.CANCEL);
                alert.setTitle("Confirmation");
                alert.setHeaderText("");
                alert.showAndWait();
                if (alert.getResult() == ButtonType.YES) {
                    dc.removeComputer(currentComputername);
                }
            }
            remakeTabs();
            showSaveGames();
        });

        anchorPane.getChildren().addAll(textFieldName, lvSaveGames, lblAddSaveGame, lblName, lblPath, textFieldPath, btnBrowse, btnAddSaveGame, btnCopyToComputer, btnCopyToDrive, lblNameError, lblPathError, pbCopyingProgress, lblProgress, lblSize);

        tab.setContent(anchorPane);
        tabs.add(tab);
        tpMainTabs.getTabs().add(tab);
        SingleSelectionModel<Tab> selectionModel = tpMainTabs.getSelectionModel();

        if (tab.getText().equals(hostname)) {
            selectionModel.select(tab);
            btnCopyToDrive.setDisable(false);
            btnBrowse.setDisable(false);
            btnAddSaveGame.setDisable(false);
            btnCopyToComputer.setDisable(false);
            textFieldName.setDisable(false);
            textFieldPath.setDisable(false);
        }
    }

    private void remakeTabs() {
        computers = dc.giveAllComputerDTOs();
        tabs.clear();
        tpMainTabs.getTabs().clear();
        textFieldNameList.clear();
        lvSaveGamesList.clear();
        lblPathErrorList.clear();
        lblNameErrorList.clear();
        textFieldPathList.clear();
        computers.forEach(this::makeTab);
        showSaveGames();
    }

    private boolean isValidDirectory(String directoryPath) {
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            return false;
        }

        Path path = Paths.get(directoryPath);
        return Files.exists(path) && Files.isDirectory(path);
    }

    private boolean isRunningFromUSB() {
        File currentDirectory = new File(System.getProperty("user.dir"));
        File root = currentDirectory.toPath().getRoot().toFile();
        FileSystemView fsv = FileSystemView.getFileSystemView();
        return fsv.isDrive(root) && fsv.getSystemTypeDescription(root).equalsIgnoreCase("USB Drive");
    }

    private void copySaveGames(List<SaveGameDTO> saveGames, boolean toDrive) {
        new Thread(() -> {
            // Calculate the total size of all save games for progress calculation
            long totalSize = saveGames.stream().mapToLong(sg -> {
                try {
                    return Files.walk(Paths.get(sg.path()))
                            .filter(Files::isRegularFile)
                            .mapToLong(p -> {
                                try {
                                    return Files.size(p);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return 0;
                                }
                            }).sum();
                } catch (IOException e) {
                    e.printStackTrace();
                    return 0;
                }
            }).sum();

            long[] copiedSize = {0};

            for (SaveGameDTO saveGame : saveGames) {
                Path sourcePath = toDrive ? Paths.get(saveGame.path()) : Paths.get(String.format("%s\\%s\\%s", getPathSaveGameOnUSB(), saveGame.name(), Paths.get(saveGame.path()).getFileName()));
                Path destinationPath = toDrive
                        ? Paths.get(String.format("%s\\%s\\%s", getPathSaveGameOnUSB(), saveGame.name(), sourcePath.getFileName()))
                        : Paths.get(saveGame.path());

                Platform.runLater(() -> lblProgressList.forEach(lbl -> lbl.setText("Deleting: " + destinationPath)));

                try {
                    // Delete existing destination directory
                    if (Files.exists(destinationPath)) {
                        Files.walk(destinationPath)
                                .sorted(Comparator.reverseOrder())
                                .map(Path::toFile)
                                .forEach(File::delete);
                    }

                    // Create new destination directory
                    Files.createDirectories(destinationPath.getParent());

                    // Copy files
                    Files.walk(sourcePath).forEach(source -> {
                        Path destination = destinationPath.resolve(sourcePath.relativize(source));
                        try {
                            if (Files.isDirectory(source)) {
                                Files.createDirectories(destination);
                            } else {
                                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                                long fileSize = Files.size(destination);
                                copiedSize[0] += fileSize;
                                double progress = (double) copiedSize[0] / totalSize;
                                Platform.runLater(() -> {
                                    pbCopyingProgressList.forEach(pb -> pb.setProgress(progress));
                                    lblSizeList.forEach(lbls -> lbls.setText(String.format("%.2f MB / %.2f MB", copiedSize[0] / (1024.0 * 1024.0), totalSize / (1024.0 * 1024.0) )));
                                    lblProgressList.forEach(lbl -> lbl.setText(String.format("Copying: %s", destination)));
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Platform.runLater(() -> {
                int index = computers.stream().map(ComputerDTO::name).toList().indexOf(hostname);
                pbCopyingProgressList.forEach(pb -> pb.setProgress(1.0));
                lblProgressList.forEach(lbl -> lbl.setText("Copying completed"));
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setHeaderText(null);
                alert.setContentText("All save games have been copied successfully.");
                alert.showAndWait();
                btnAddSaveGameList.get(index).setDisable(false);
                btnCopyToComputerList.get(index).setDisable(false);
                btnCopyToDriveList.get(index).setDisable(false);
            });
        }).start();
    }

    @FXML
    private TabPane tpMainTabs;

    private String getPathSaveGameOnUSB() {
        return String.format("%s\\Save game program data\\SaveGames", System.getProperty("user.dir"));
    }
}
