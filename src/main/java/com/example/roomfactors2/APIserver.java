package com.example.roomfactors2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.Objects;

public class APIserver {

    public String apiRequest(String jsonString) throws JSONException, IOException {
        // Проверяем валидность данных, и подготавливаем их к
        jsonString = apiСheckInput(jsonString);
        // Проверяем значение "status" в jsonString
        JSONObject json = new JSONObject(jsonString);
        // Проверяем условие и возвращаем результат
        if (json.getInt("status") == 0) {
            return jsonString;
        }
        ServerIteraction si = new ServerIteraction();
        String jsonRequest = si.getGenerateImages(json.getString("eng_prompt"), (String) json.getString("imageInputPath"));
        return jsonRequest;
    }

    public String apiСheckInput(String jsonString) throws JSONException {
        JSONObject newJson = new JSONObject();
        try {
            // Преобразование строки в JSON-объект
            JSONObject json = new JSONObject(jsonString);
            String promptValue = "";
            // Проверка наличия и непустоты ключа "prompt"
            if (json.has("prompt") && !json.isNull("prompt")) {
                // Получение значения ключа "prompt"
                promptValue = json.getString("prompt");
            }

            JSONArray buttonsArray =  new JSONArray();
            // Проверка наличия и непустоты ключа "buttons"
            if (json.has("buttons") && !json.isNull("buttons")) {
                // Получение массива значений из ключа "buttons"
                buttonsArray = json.getJSONArray("buttons");
            }
            // Преобразование массива в строку
            StringBuilder buttonsString = new StringBuilder();
            for (int i = 0; i < buttonsArray.length(); i++) {
                buttonsString.append(buttonsArray.getString(i));
                if (i < buttonsArray.length() - 1) {
                    buttonsString.append(", ");
                }
            }

            // Создание новой строковой переменной eng_prompt
            String eng_prompt = promptValue + " with styles " + buttonsString.toString();

            newJson.put("eng_prompt", eng_prompt);

            // Получение значения ключа "imageInputPath" из исходного JSON
            String imageInputPath = json.optString("imageInputPath");
            newJson.put("imageInputPath", imageInputPath);
            newJson.put("status",200);

            // Пример: Вывод результатов
            System.out.println("New JSON: " + newJson.toString());

            return newJson.toString();
        }
        catch (Exception e) {
            newJson.put("status", 0);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            newJson.put("error" ,sw.toString());
            return newJson.toString();
        }
    }
}
