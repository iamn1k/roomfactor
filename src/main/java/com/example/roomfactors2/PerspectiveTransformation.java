package com.example.roomfactors2;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.min;

public class PerspectiveTransformation {

    public static void createPerspectiveDeformation(String typeInterior, String pathToImage, String pathToNewMask, String pathToOutputFile) {
        Mat image = Imgcodecs.imread(pathToImage);
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        Mat thresh = new Mat();
        Imgproc.threshold(gray, thresh, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat newMask = Imgcodecs.imread(pathToNewMask);
        contours.sort((o1, o2) -> Double.compare(Imgproc.contourArea(o2), Imgproc.contourArea(o1)));
        MatOfPoint largestContour = contours.get(0);
        Mat mask = Mat.zeros(gray.size(), CvType.CV_8UC1);
        Imgproc.drawContours(mask, Collections.singletonList(largestContour), 0, new Scalar(255), -1);
        double epsilon = 0.1 * Imgproc.arcLength(new MatOfPoint2f(largestContour.toArray()), true);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Imgproc.approxPolyDP(new MatOfPoint2f(largestContour.toArray()), approxCurve, epsilon, true);
        RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(largestContour.toArray()));
        Point[] boxPoints = new Point[4];
        rect.points(boxPoints);
        double angle = rect.angle;
        Point center = rect.center;
        int rows = image.rows();
        int cols = image.cols();
        Mat M = Imgproc.getRotationMatrix2D(center, angle, 1);
        Imgproc.warpAffine(image, image, M, new Size(cols, rows));
        MatOfPoint2f targetCorners = new MatOfPoint2f(new Point(0, 0), new Point(newMask.cols(), 0), new Point(newMask.cols(), newMask.rows()), new Point(0, newMask.rows()));
        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(approxCurve, targetCorners);
        Mat warpedImage = new Mat();
        Imgproc.warpPerspective(image, warpedImage, perspectiveTransform, newMask.size());
        if (typeInterior.equals("bedside")) {
            Core.rotate(warpedImage, warpedImage, Core.ROTATE_90_COUNTERCLOCKWISE);
        }
        double[] whitePixels;
        List<Integer> whitePixelsX = new ArrayList<>();
        List<Integer> whitePixelsY = new ArrayList<>();
        for (int i = 0; i < newMask.rows(); i++) {
            for (int j = 0; j < newMask.cols(); j++) {
                whitePixels = newMask.get(i, j);
                if (whitePixels != null && (whitePixels.length == 3 && whitePixels[0] == 255 && whitePixels[1] == 255 && whitePixels[2] == 255)) {
                    whitePixelsX.add(j);
                    whitePixelsY.add(i);
                }
            }
        }
        Point p1 = new Point(Collections.min(whitePixelsX), Collections.min(whitePixelsY));
        Point p2 = new Point(Collections.min(whitePixelsX), Collections.max(whitePixelsY));
        Point p3 = new Point(Collections.max(whitePixelsX), Collections.max(whitePixelsY));
        Point p4 = new Point(Collections.max(whitePixelsX), Collections.min(whitePixelsY));
        Size newSize = new Size(p3.x - p2.x, p2.y - p1.y);
        Point centerPoint = new Point(p2.x + Math.floor((p3.x - p2.x) / 2) - 1, p1.y + Math.floor((p2.y - p1.y) / 2));
        Mat resultImage = newMask.clone();
        System.out.println(centerPoint.x + " "  +centerPoint.y);
        Mat warpedResized = new Mat();
        Imgproc.resize(warpedImage, warpedResized, newSize, 0, 0, Imgproc.INTER_LINEAR);

        Mat subRegion = resultImage.submat((int) (centerPoint.y - Math.floor(newSize.height / 2)),
                (int) (centerPoint.y + Math.floor(newSize.height / 2)),
                (int) (centerPoint.x - Math.floor(newSize.width / 2)),
                (int) (centerPoint.x + Math.floor(newSize.width / 2)));
        //Imgcodecs.imwrite(pathToWarpedResizedOutputFile, subRegion);
        warpedResized.copyTo(subRegion);

        // Apply the modified subRegion back into the resultImage
        for (int y = (int) (centerPoint.y - Math.floor(newSize.height / 2)); y < (int) (centerPoint.y + Math.floor(newSize.height / 2)); y++) {
            for (int x = (int) (centerPoint.x - Math.floor(newSize.width / 2)); x < (int) (centerPoint.x + Math.floor(newSize.width / 2)); x++) {
                resultImage.put(y, x, warpedResized.get(y - (int) (centerPoint.y - Math.floor(newSize.height / 2)), x - (int) (centerPoint.x - Math.floor(newSize.width / 2))));
            }
        }

        for (int i = 0; i < resultImage.rows(); i++) {
            for (int j = 0; j < resultImage.cols(); j++) {
                if (Arrays.stream(newMask.get(i, j)).min().orElse(0) != 255) {
                    resultImage.put(i, j, 0, 0, 0); // resultImage.put(i, j, 0, 0, 0);
                }
            }
        }

        Imgcodecs.imwrite(pathToOutputFile, resultImage);

    }
    public static String rgbToHex(double[] tup) {
        return String.format("#%02x%02x%02x", (int)tup[0], (int)tup[1], (int)tup[2]);
    }

    public static void concatMaskAndImages(String[] masks, String seg, String base, String output_path) {
        Mat segImg = Imgcodecs.imread(seg);
        Mat baseImg = Imgcodecs.imread(base);

        for (String mask : masks) {
            Mat maskImg = Imgcodecs.imread(mask);
            if (mask.contains("closet")) {
                for (int i = 0; i < segImg.rows(); i++) {
                    for (int j = 0; j < segImg.cols(); j++) {
                            if (!rgbToHex(maskImg.get(i, j)).equals("#000000")) {
                                baseImg.put(i, j, maskImg.get(i, j));
                            }
                    }
                }
            }
            if (mask.contains("bedside")) {
                for (int i = 0; i < segImg.rows(); i++) {
                    for (int j = 0; j < segImg.cols(); j++) {
                            if (!rgbToHex(maskImg.get(i, j)).equals("#000000")) {
                                baseImg.put(i, j, maskImg.get(i, j));
                        }
                    }
                }
            }
        }

        Imgcodecs.imwrite(output_path, baseImg);
    }
}
