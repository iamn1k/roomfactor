package com.example.roomfactors2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import javafx.scene.image.ImageView;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class DesignGeneratorController {
    APIserver apiServer = new APIserver();
    @FXML
    private TextField promptTextField;
    @FXML
    private Button chooseSketchButton;
    @FXML
    private RadioButton modernRadioButton;

    @FXML
    private RadioButton classicRadioButton;

    @FXML
    private RadioButton minimalismRadioButton;
    @FXML
    private HBox styleRadioButtonsContainer;
    @FXML
    private VBox imageContainer;

    @FXML
    private Label imageLabel;
    private Stage previousImageStage; // Добавляем поле для хранения предыдущего окна
    private String imagePath;
    @FXML
    private Button chooseCloset;
    @FXML
    private Button chooseBedside;
    @FXML
    private CheckBox userInteriorCheckBox;
    private File selectedFileCloset;
    private File selectedFileBedside;
    @FXML
    private void initialize() {
        // Set initial visibility of the buttons
        chooseCloset.setVisible(false);
        chooseBedside.setVisible(false);

        // Bind button visibility to the selected property of the CheckBox
        chooseCloset.visibleProperty().bind(userInteriorCheckBox.selectedProperty());
        chooseBedside.visibleProperty().bind(userInteriorCheckBox.selectedProperty());
        System.out.println("Controller initialized.");
    }
    @FXML
    private void chooseSketch(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выбрать набросок");
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath();

            // Создаем новый ImageView для отображения изображения
            ImageView imageView = new ImageView(new Image("file:" + imagePath));

            // Получаем размеры изображения
            double imageWidth = imageView.getImage().getWidth();
            double imageHeight = imageView.getImage().getHeight();

            // Создаем новое окно для отображения изображения
            Stage imageStage = new Stage();
            imageStage.setTitle("Открытое изображение");

            // Закрываем предыдущее окно, если оно было открыто
            if (previousImageStage != null) {
                previousImageStage.close();
            }

            // Сохраняем текущее окно для использования при следующем открытии
            previousImageStage = imageStage;

            // Получаем размеры экрана пользователя
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getBounds();
            double screenWidth = bounds.getWidth();
            double screenHeight = bounds.getHeight();

            // Если размеры изображения совпадают с размерами экрана, уменьшаем в 2 раза
            if (imageWidth > screenWidth * 0.6 || imageHeight > screenHeight * 0.6) {
                imageView.setFitWidth(imageWidth / 2);
                imageView.setFitHeight(imageHeight / 2);
                imageWidth = imageView.getBoundsInParent().getWidth();
                imageHeight = imageView.getBoundsInParent().getHeight();
            }

            // Создаем VBox для размещения ImageView
            VBox vbox = new VBox(imageView);
            vbox.setAlignment(Pos.CENTER);

            // Создаем сцену с VBox
            Scene scene = new Scene(vbox, imageWidth, imageHeight); // Устанавливаем размеры сцены

            // Устанавливаем Scene в Stage и отображаем его
            imageStage.setScene(scene);
            imageStage.show();
        }
    }

    @FXML
    private void generateDesign(ActionEvent event) throws JSONException, IOException {
        if (imagePath == null || imagePath.isEmpty()) {
            // Display a dialog to inform the user to select an image
            showAlert("Ошибка", "Выберите изображение с диска.");
        } else {
            try {
                if (userInteriorCheckBox.isSelected()) {
                    ChangePixel chPix = new ChangePixel();
                    // Получить сегментированную маску всего изображения
                    ServerIteraction si = new ServerIteraction();
                    si.getGenerateMask(this.imagePath.replace("\\", "\\\\"),"seg_mask");
                    // Получение маски шкафа
                    chPix.changeColor("src/main/resources/com/example/roomfactors2/images/source_image_mask.png",
                            Arrays.asList("#06ffff", "#07ffff")
                            ,"#ffffff",
                            "src/main/resources/com/example/roomfactors2/images/output_source_image_mask1.png");

                    // Получение маски тумбочки
                    chPix.changeColor("src/main/resources/com/example/roomfactors2/images/source_image_mask.png",
                            Arrays.asList("#e005ff", "#e003ff")
                            ,"#ffffff",
                            "src/main/resources/com/example/roomfactors2/images/output_source_image_mask2.png");
                    System.out.println("masked_generated");
                    // Получение текстуры шкафа
                    PerspectiveTransformation.createPerspectiveDeformation("closet",
                            selectedFileCloset.getAbsolutePath(),
                            "src/main/resources/com/example/roomfactors2/images/output_source_image_mask1.png",
                            "src/main/resources/com/example/roomfactors2/images/output/closet.png");
                    System.out.println("closet_generated");
                    // Получение текстуры тумбочки
                    PerspectiveTransformation.createPerspectiveDeformation("bedside",
                            selectedFileBedside.getAbsolutePath(),
                            "src/main/resources/com/example/roomfactors2/images/output_source_image_mask2.png",
                            "src/main/resources/com/example/roomfactors2/images/output/bedside_mask.png"
                            );
                    System.out.println("perspective_generated");
                    // Соединить две текстуры на эскиз
                    PerspectiveTransformation.concatMaskAndImages(
                            new String[]{"src/main/resources/com/example/roomfactors2/images/output/bedside_mask.png",
                                    "src/main/resources/com/example/roomfactors2/images/output/closet.png"},
                            "src/main/resources/com/example/roomfactors2/images/source_image_mask.png",
                            this.imagePath.replace("\\", "\\\\"),
                            "src/main/resources/com/example/roomfactors2/images/output/input_to_controlnet.png"
                    );
                    // Выполнить генерацию интерьера с заданными элементами
                    showAlert("Успех","Элементы интерьера успешно помещены в интерьер!");
                } else {
                    // Send JSON file with the specified fields
                    String jsonResponse = apiServer.apiRequest(generateJsonFile());

                    // Parse the JSON response
                    JSONObject responseJson = new JSONObject(jsonResponse);
                    int status = responseJson.getInt("status");

                    if (status == 0) {
                        // Display an error dialog with the error message from the response
                        showAlert("Ошибка", "Произошла непредвиденная ошибка сервера, status = 0");
                        System.out.println(responseJson.getString("error"));
                    } else {
                        // Extract the image data as byte[] from the response
                        String encodedImage = responseJson.getString("image"); // Your encoded string;
                        byte[] imageBytes = Base64.getDecoder().decode(encodedImage);
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
                        // Display a new window with the received image data
                        displayImageWindow(image);
                    }
                }
            }
            catch(JSONException | IOException e){
                // Handle JSON or IO exceptions
                showAlert("Ошибка", "Произошла ошибка при обработке запроса.");
            } catch(Exception e){
                // Handle any other exceptions
                e.printStackTrace(); // This will print the stack trace of the exception to the console
                showAlert("Ошибка", "Произошла непредвиденная ошибка.");
            }

        }
    }

    private BufferedImage convertStringToBufferedImage(String base64Image) throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(bis);
    }
    // Method to display a new window with the received image
    // Method to display a new window with the received image
    private void displayImageWindow(BufferedImage image) {
        try {
            // Create a new JavaFX Stage
            Stage stage = new Stage();
            stage.setTitle("Полученное изображение");

            // Create a WritableImage from the BufferedImage
            WritableImage writableImage = new WritableImage(image.getWidth(), image.getHeight());
            PixelWriter pixelWriter = writableImage.getPixelWriter();
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pixelWriter.setArgb(x, y, image.getRGB(x, y));
                }
            }

            // Create an ImageView to display the WritableImage
            ImageView imageView = new ImageView(writableImage);

            // Create a VBox layout for the window
            VBox vbox = new VBox(imageView);
            vbox.setAlignment(Pos.CENTER);

            // Create a Scene with the VBox layout
            Scene scene = new Scene(vbox, image.getWidth(), image.getHeight());

            // Set the Scene to the Stage
            stage.setScene(scene);

            // Show the Stage
            stage.show();
        } catch (Exception e) {
            // Handle any exceptions that may occur while displaying the image window
            showAlert("Ошибка", "Произошла ошибка при отображении изображения.");
        }
    }
    private String generateJsonFile() {
        String prompt = promptTextField.getText();
        List<String> selectedButtons = new ArrayList<>();

        // Получаем все дочерние элементы HBox (предполагаем, что это радиокнопки)
        for (javafx.scene.Node radioButton : styleRadioButtonsContainer.getChildren()) {
            if (radioButton instanceof RadioButton && ((RadioButton) radioButton).isSelected()) {
                selectedButtons.add(((RadioButton) radioButton).getText());
            }
        }

        // Create a JSON string
        String jsonString = "{\n" +
                "  \"prompt\": \"" + prompt + "\",\n" +
                "  \"buttons\": " + selectedButtons.toString() + ",\n" +
                "  \"imageInputPath\": \"" + this.imagePath.replace("\\", "\\\\") + "\"\n" +
                "}";
        System.out.println(jsonString);

         //Save the JSON string to a file
        try {
            FileWriter fileWriter = new FileWriter("output.json");
            fileWriter.write(jsonString);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showAlert("Успех", "JSON файл успешно сгенерирован.");
        return jsonString;

    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Get the primary screen bounds
        Screen primaryScreen = Screen.getPrimary();
        Rectangle2D screenBounds = primaryScreen.getBounds();

        // Create a Stage for the Alert
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();

        // Set the modality to APPLICATION_MODAL to prevent interaction with other windows
        alertStage.initModality(Modality.APPLICATION_MODAL);

        // Set the maximum size of the alert stage to half of the screen size
        alertStage.setMaxWidth(screenBounds.getWidth() / 2);
        alertStage.setMaxHeight(screenBounds.getHeight() / 2);

        // Show the alert
        alert.showAndWait();
    }
    @FXML
    private void chooseCloset(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выбрать шкаф");
        selectedFileCloset = fileChooser.showOpenDialog(new Stage());
        if (selectedFileCloset != null) {
            String closetImagePath = selectedFileCloset.getAbsolutePath();
            System.out.println(closetImagePath);
        }
    }
    @FXML
    private void chooseBedside(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выбрать тумбочку");
        selectedFileBedside = fileChooser.showOpenDialog(new Stage());
        if (selectedFileBedside != null) {
            String bedsideImagePath = selectedFileBedside.getAbsolutePath();
            System.out.println(bedsideImagePath);
        }
    }

}