package controller;

import DAO.ConferenceDAO;
import DAO.PlaceDAO;
import DAO.RoomDAO;
import DTO.MyConferencesDTO;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import pojo.Conference;
import pojo.Place;
import pojo.Room;
import utils.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class AddCfrController extends Controller {

    @FXML
    private GridPane paneAddCfr;

    @FXML
    private TextArea addCfr_name;

    @FXML
    private DatePicker addCfr_date;

    @FXML
    private  ComboBox<LocalTime> addCfr_time = new ComboBox<LocalTime>();

    @FXML
    private  ComboBox<Place> addCfr_place  = new ComboBox<Place>();

    @FXML
    private  ComboBox<Room> addCfr_room  = new ComboBox<Room>();

    @FXML
    private TextField addCfr_numberAttendees;

    @FXML
    private Button addCfr_picture;

    @FXML
    private TextArea addCfr_generalDesc;

    @FXML
    private TextArea addCfr_detiailDesc;

    @FXML
    private Button btnAddCfrForm;

    @FXML
    private Text numPicture;

    @FXML
    private Pane paneAddCfrError;

    @FXML
    private Text addCfr_error;

    List<File> fileList = new ArrayList<>();
    Room room;
    LocalDate date;
    LocalTime time;


    public void loadView() {
        addCfr_name.clear();
        addCfr_generalDesc.clear();
        addCfr_detiailDesc.clear();
        addCfr_numberAttendees.clear();
        numPicture.setText(Utils.convertUTF8IntoString("Đã chọn: 0 file"));

        int id = ConferenceDAO.getMaxId() + 1;

        //get date
        addCfr_date.setOnAction((EventHandler) t -> date = addCfr_date.getValue());

        //get time
        ObservableList<LocalTime> timeOptions = FXCollections.observableArrayList();
        int i = 14;
        while (i / 2 < 21) {
            timeOptions.add(LocalTime.of(i / 2, i % 2 * 30));
            i++;
        }
        addCfr_time.setItems(timeOptions);
        addCfr_time.setConverter(new StringConverter<LocalTime>() {
            @Override
            public String toString(LocalTime object) {
                if(object!=null) {
                    return object.toString();
                }
                return "";
            }

            @Override
            public LocalTime fromString(String string) {
                return addCfr_time.getItems().stream().filter(ap ->
                        ap.toString().equals(string)).findFirst().orElse(null);
            }
        });
        addCfr_time.valueProperty().addListener(new ChangeListener<LocalTime>() {
            @Override
            public void changed(ObservableValue<? extends LocalTime> observable, LocalTime oldValue, LocalTime newValue) {
                time = newValue;
            }
        });

        //get room and place
        ObservableList<Place> placeOptions = PlaceDAO.getPlacesList();

        addCfr_place.setItems(placeOptions);
        addCfr_place.setConverter(new StringConverter<Place>() {
            @Override
            public String toString(Place object) {
                if(object!=null) {
                    return object.getName();
                }
                return "";
            }

            @Override
            public Place fromString(String string) {
                return addCfr_place.getItems().stream().filter(ap ->
                        ap.getName().equals(string)).findFirst().orElse(null);
            }
        });

        addCfr_place.valueProperty().addListener(new ChangeListener<Place>() {
            @Override
            public void changed(ObservableValue<? extends Place> observable, Place oldValue,
                                Place newValue) {
                ObservableList<Room> roomOptions = RoomDAO.getRoomsListByPlace(newValue);
                addCfr_room.setItems(roomOptions);
                addCfr_room.setConverter(new StringConverter<Room>() {
                    @Override
                    public String toString(Room object) {
                        return object.getName() + " - " + object.getCapacity();
                    }

                    @Override
                    public Room fromString(String string) {
                        return addCfr_room.getItems().stream().filter(ap ->
                                ap.getName().equals(string)).findFirst().orElse(null);
                    }
                });
                addCfr_room.valueProperty().addListener((observable1, oldValue1, newValue1) -> room = newValue1);
            }
        });

        chooseCfrPicture();

        // submit form
        btnAddCfrForm.setOnAction(event -> {
            String error = Utils.convertUTF8IntoString("Lỗi:");
            boolean flag = true;
            int numberAttendees = 0;
            Timestamp datetime = null;
            String name = addCfr_name.getText();
            String generalDesc = addCfr_generalDesc.getText();
            String detailDesc = addCfr_detiailDesc.getText();
            if (date != null && time != null && room != null) {
                datetime = Timestamp.valueOf(LocalDateTime.of(date, time));
                try {
                    numberAttendees = Integer.parseInt(addCfr_numberAttendees.getText());
                    if (numberAttendees < 10) {
                        error = error.concat(Utils.convertUTF8IntoString("\nSố người tham gia tối tiêu là 10"));
                        flag = false;
                    }
                    if (numberAttendees > room.getCapacity()) {
                        error = error.concat(Utils.convertUTF8IntoString("\nSố người tham gia tối đa là ")
                                + room.getCapacity());
                        flag = false;
                    }
                } catch (NumberFormatException e) {
                    error = error.concat(Utils.convertUTF8IntoString("\nSố người tham dự nhập sai định dạng"));
                    flag = false;
                }
                if (name.equals("") || generalDesc.equals("") ||
                        detailDesc.equals("")) {
                    error = error.concat(Utils.convertUTF8IntoString("\nCác trường không được để trống"));
                    flag = false;
                }

            } else {
                error = error.concat(Utils.convertUTF8IntoString("\nCác trường không được để trống"));
                flag = false;
            }
            System.out.println(error);
            addCfr_error.setText(error);
            if (flag) {
                Conference cfr = new Conference(id, name, room, generalDesc, detailDesc, datetime, numberAttendees);
                ConferenceDAO.insertConference(cfr);
                paneAddCfrError.setVisible(false);
                try {
                    addScreen("/scene/cfr_list.fxml");
                    fileSaved(fileList, id);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else paneAddCfrError.setVisible(true);
        });
    }

    public void chooseCfrPicture() {
        final boolean t  = true;
        addCfr_picture.setOnAction(event -> {
            FileChooser file = new FileChooser();
            List<String> lstFile = new ArrayList<>();
            lstFile.add("*.png");
            lstFile.add("*.jpg");
            file.getExtensionFilters().add(new FileChooser.ExtensionFilter("picture file", lstFile));
            List<File> fl = file.showOpenMultipleDialog(null);
            while (fl.size() > 5) {
                fl.remove(fl.size() - 1);
            }
            numPicture.setText(Utils.convertUTF8IntoString("Đã chọn: ")
                    + fl.size() + " file");
            fileList = fl;
        });
    }

    public static void fileSaved(List<File> fileList, int id) throws IOException {
        String spath = "src/main/resources/picture/" + id + "/";
        File file = new File(spath);
        Path path = Paths.get(file.getAbsolutePath());
        Files.createDirectories(path);
        int i = 1;
        for (File f : fileList) {
            if (f != null) {
                try{
                    BufferedImage originalImage = ImageIO.read(f);
                    BufferedImage resizeImageJpg = Utils.createResizedCopy(originalImage, 220, 180,true);
                    File newFile = new File(path.toString() + "/" + i + ".jpg");
                    ImageIO.write(resizeImageJpg, "jpg", newFile);

                } catch(IOException e) {
                    System.out.println("error resize: "+e.getMessage());
                }
            }
            i++;
        }
    }

    public void addScreen(String path) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        stackPane.getChildren().add(loader.load());
        Controller controller = loader.getController();
        controller.getRoot(stackPane);
        controller.loadView();
    }

}
