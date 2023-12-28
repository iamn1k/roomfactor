package com.example.roomfactors2;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.List;

public class ChangePixel {
    public void changeColor(String image_path, List<String> old_colors, String new_color, String output_path) throws IOException {
        BufferedImage image = ImageIO.read(new File(image_path));
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] pixels = new int[height][width];

        // Преобразуем цвета в форматы RGB
        List<Color> oldRGB = new ArrayList<>();
        for (String color : old_colors) {
            int r = Integer.valueOf(color.substring(1, 3), 16);
            int g = Integer.valueOf(color.substring(3, 5), 16);
            int b = Integer.valueOf(color.substring(5, 7), 16);
            oldRGB.add(new Color(r, g, b));
        }
        int newR = Integer.valueOf(new_color.substring(1, 3), 16);
        int newG = Integer.valueOf(new_color.substring(3, 5), 16);
        int newB = Integer.valueOf(new_color.substring(5, 7), 16);

        // Получаем данные о пикселях
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y][x] = image.getRGB(x, y);
            }
        }

        // Заменяем цвета пикселей
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = new Color(pixels[y][x]);
                if (oldRGB.contains(pixelColor)) {
                    pixels[y][x] = new Color(newR, newG, newB).getRGB();
                } else {
                    pixels[y][x] = Color.BLACK.getRGB(); // Set background color to black
                }
            }
        }

        // Создаем новое изображение с измененными цветами пикселей
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, pixels[y][x]);
            }
        }

        // Находим множество всех пиксели, которые находятся сверху, справа, снизу, и слева от текущего пикселя до конца изображения.
        // Если во всех четырех множествах есть хотя бы один белый пиксель, то исходный черный пиксель перекрашиваем в белый цвет
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (pixels[y][x] == Color.BLACK.getRGB()) {
                    Set<Integer> topPixels = new HashSet<>();
                    Set<Integer> bottomPixels = new HashSet<>();
                    Set<Integer> leftPixels = new HashSet<>();
                    Set<Integer> rightPixels = new HashSet<>();
                    for (int i = y; i < height; i++) {
                        if (pixels[i][x] == Color.WHITE.getRGB()) {
                            bottomPixels.add(i);
                            break;
                        }
                    }
                    for (int i = y; i >= 0; i--) {
                        if (pixels[i][x] == Color.WHITE.getRGB()) {
                            topPixels.add(i);
                            break;
                        }
                    }
                    for (int i = x; i < width; i++) {
                        if (pixels[y][i] == Color.WHITE.getRGB()) {
                            rightPixels.add(i);
                            break;
                        }
                    }
                    for (int i = x; i >= 0; i--) {
                        if (pixels[y][i] == Color.WHITE.getRGB()) {
                            leftPixels.add(i);
                            break;
                        }
                    }
                    if (!topPixels.isEmpty() && !bottomPixels.isEmpty() && !leftPixels.isEmpty() && !rightPixels.isEmpty()) {
                        pixels[y][x] = Color.WHITE.getRGB();
                    }
                }
            }
        }

        // Создаем новое изображение с измененными цветами пикселей
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, pixels[y][x]);
            }
        }

        // Сохраняем полученное изображение
        ImageIO.write(image, "png", new File(output_path));
    }

}
