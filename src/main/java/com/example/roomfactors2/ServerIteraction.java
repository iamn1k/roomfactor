package com.example.roomfactors2;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.*;
import java.util.Map;

public class ServerIteraction {
    private String url = "http://127.0.0.1:7860";
    private JSONObject responseJSON = new JSONObject();
    // Read Image in RGB order

    public ServerIteraction() throws JSONException {
        responseJSON.put("status", 0);
        responseJSON.put("image", "");
    }

    public ServerIteraction(String url) {
        this.url = url;
    }
    public String getGenerateImages(String prompt , String imageInputPath) throws IOException, JSONException {
        try {
            byte[] imgBytes = Files.readAllBytes(Paths.get(imageInputPath));
            String encodedImage = Base64.getEncoder().encodeToString(imgBytes);
            String payload = "{\n" +
                    "    \"prompt\": \"" + prompt + "\",\n" +
                    "    \"steps\": 40,\n" +
                    "    \"sampler\": \"DPM++ 2M Karras\",\n" +
                    "    \"cfg_scale\": 7,\n" +
                    "    \"seed\": 2092222925,\n" +
                    "    \"size\": \"512x512\",\n" +
                    "    \"model_hash\": \"6ce0161689\",\n" +
                    "    \"model\": \"v1-5-pruned-emaonly\",\n" +
                    "    \"alwayson_scripts\": {\n" +
                    "        \"controlnet\": {\n" +
                    "            \"args\": [\n" +
                    "                {\n" +
                    "                    \"input_image\": \"" + encodedImage + "\",\n" +
                    "                    \"module\": \"mlsd\",\n" +
                    "                    \"model\": \"control_v11p_sd15_mlsd [aca30ff0]\",\n" +
                    "                    \"weight\": 1,\n" +
                    "                    \"resize_mode\": \"Crop and Resize\",\n" +
                    "                    \"low_vram\": false,\n" +
                    "                    \"processor_res\": 512,\n" +
                    "                    \"threshold_a\": 0.1,\n" +
                    "                    \"threshold_b\": 0.1,\n" +
                    "                    \"guidance_start\": 0,\n" +
                    "                    \"guidance_end\": 1,\n" +
                    "                    \"pixel_perfect\": false,\n" +
                    "                    \"control_mode\": \"ControlNet is more important\",\n" +
                    "                    \"save_detected_map\": true\n" +
                    "                }\n" +
                    "            ]\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n";
            // Отправка POST-запроса
            URL requestUrl = new URL(this.url + "/sdapi/v1/txt2img");
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            byte[] payloadBytes = payload.getBytes();
            connection.getOutputStream().write(payloadBytes);

            // Чтение ответа
            InputStream responseStream = connection.getInputStream();
            JsonReader jsonReader = Json.createReader(responseStream);
            JsonObject responseJson = jsonReader.readObject();

            // Извлечение данных из ответа
            JsonArray imagesArray = responseJson.getJsonArray("images");
            String result = imagesArray.getString(0);

            // Разделение строки для изображения и декодирование
            String imageData = result.split(",", 2)[0];
            byte[] imageBytes = Base64.getDecoder().decode(imageData);
            String encodedBytesImage = Base64.getEncoder().encodeToString(imageBytes);
            // Преобразование байтов изображения в BufferedImage
            //BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            responseJSON.put("status", 200);
            responseJSON.put("image", encodedBytesImage);
            return responseJSON.toString();
            // Сохранение изображения
            //ImageIO.write(image, "png", new File(imageOutPath));
        }catch (Exception e) {
            responseJSON.put("status", 0);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            responseJSON.put("error" ,sw.toString());
            return responseJSON.toString();
        }

    }
    public void getGenerateMask(String imageInputPath, String type) throws IOException, JSONException {
        try {
            byte[] imgBytes = Files.readAllBytes(Paths.get(imageInputPath));
            String encodedImage = Base64.getEncoder().encodeToString(imgBytes);
            if (type == "seg_mask") {
                String payload = "{\n" +
                        "    \"prompt\": \"" + "" + "\",\n" +
                        "    \"steps\": 3,\n" +
                        "    \"sampler\": \"DPM++ 2M Karras\",\n" +
                        "    \"cfg_scale\": 27,\n" +
                        "    \"seed\": 2464735849,\n" +
                        "    \"size\": \"600x744\",\n" +
                        "    \"width\": 600,\n" +
                        "    \"height\": 744,\n" +
                        "    \"model_hash\": \"6ce0161689\",\n" +
                        "    \"model\": \"v1-5-pruned-emaonly\",\n" +
                        "    \"denoising_strength\" : 0.82, \n" +
                        "    \"mask_blur\" : 4,\n" +
                        "" +
                        "    \"alwayson_scripts\": {\n" +
                        "        \"controlnet\": {\n" +
                        "            \"args\": [\n" +
                        "                {\n" +
                        "                    \"input_image\": \"" + encodedImage + "\",\n" +
                        "                    \"module\": \"seg_ofade20k\",\n" +
                        "                    \"model\": \"control_v11p_sd15_seg [e1f51eb9]\",\n" +
                        "                    \"weight\": 1,\n" +
                        "                    \"resize_mode\": \"Crop and Resize\",\n" +
                        "                    \"low_vram\": false,\n" +
                        "                    \"processor_res\": 512,\n" +
                        "                    \"guidance_start\": 0,\n" +
                        "                    \"guidance_end\": 1,\n" +
                        "                    \"pixel_perfect\": false,\n" +
                        "                    \"control_mode\": \"ControlNet is more important\",\n" +
                        "                    \"save_detected_map\": true\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n";
                // Отправка POST-запроса
                URL requestUrl = new URL(this.url + "/sdapi/v1/txt2img");
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");

                byte[] payloadBytes = payload.getBytes();
                connection.getOutputStream().write(payloadBytes);

                // Чтение ответа
                InputStream responseStream = connection.getInputStream();
                JsonReader jsonReader = Json.createReader(responseStream);
                JsonObject responseJson = jsonReader.readObject();
                // Создание файла для записи
                System.out.println("maskedJSON");
                // Запись JSON в файл

                // Извлечение данных из ответа
                JsonArray imagesArray = responseJson.getJsonArray("images");
                String result = imagesArray.getString(1);


                byte[] imageBytes = Base64.getDecoder().decode(result);
                String encodedBytesImage = Base64.getEncoder().encodeToString(imageBytes);
                // Вывод изображения на экран
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                // Вывод размера изображения в консоль
                System.out.println("Image dimensions: " + img.getWidth() + "x" + img.getHeight());
                //File outputfile = new File("C:\\Users\\nikit\\IdeaProjects\\roomfactors2\\src\\main\\resources\\com\\example\\roomfactors2\\images\\source_image_mask.png");
                File outputfile = new File("src/main/resources/com/example/roomfactors2/images/source_image_mask.png");
                ImageIO.write(img, "png", outputfile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private String convertBufferedImageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}