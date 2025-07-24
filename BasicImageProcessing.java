package cv;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class BasicImageProcessing {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			File f = new File("Dice.jpg");
			BufferedImage image = ImageIO.read(f);
			
			File one = new File("HW16GradingImage1.jpg");
			BufferedImage imageOne = ImageIO.read(one);
			
			File two = new File("HW16GradingImage2.jpg");
			BufferedImage imageTwo = ImageIO.read(two);
			
			int[][] imageOneArray = convert2Array(imageOne);
			
			int[][] imageTwoArray = convert2Array(imageTwo);
			
			int[][] backgroundSubtracted = backgroundSubtraction(imageOneArray, imageTwoArray, 50);
			writeArrayToImage(backgroundSubtracted, "BackgroundSubtracted.png");
			
			System.out.println("Image read successful");
			System.out.println("Height: " + image.getHeight() + " Length: " + image.getWidth());
			// printData(image);
			ImageIO.write(image, "jpg", new File("DuplicateImage.jpg"));
			// Convert to greyscale array of ints
			int[][] imageArray = convert2Array(image);
			System.out.println("Image converted to greyscale array");
			writeArrayToImage(imageArray, "GreyDice.png");
			System.out.println("Running Sobel Filter");
			int[][] edgeArray = sobelFilter(image, 450);
			writeArrayToImage(edgeArray, "EdgeImage450.png");
			int[][] binaryImage = convert2Binary(imageArray, 100);
			writeArrayToImage(binaryImage, "BinaryImage.png");
			int[][] erodedImage = erodeImage(binaryImage);
			writeArrayToImage(erodedImage, "ErodedImage.png");
			
			// maxOutBlue(image);
		} catch (Exception e) {
			System.out.println("Error: " + e);

		}
	}

	public static int[][] convert2Binary(int[][] originalImage, int threshold) {
		int[][] binaryImage = new int[originalImage.length][originalImage[0].length];
		for (int i = 0; i < originalImage.length; i++) {
			for (int j = 0; j < originalImage[i].length; j++) {
				if (originalImage[i][j] >= threshold) // Make this a white pixel
					binaryImage[i][j] = 255;
				else // make it a black pixel
					binaryImage[i][j] = 0;
			}

		}
		return binaryImage;
	}

	public static int[][] erodeImage(int[][] originalImage) {
		int[][] erodedImage = new int[originalImage.length][originalImage[0].length];
		for (int i = 1; i < originalImage.length - 1; i++) {
			for (int j = 1; j < originalImage[0].length - 1; j++) {
				erodedImage[i][j] = 0; // Assume 0 unless it changes
				if (originalImage[i][j] == 255) { // Original has a white pixel
					// Call a function to determine the eroded value
					erodedImage[i][j] = erodePixel(originalImage, i, j);
				}
			}
		}
		return erodedImage;
	}

	public static int[][] backgroundSubtraction(int[][] img1, int[][] img2, int threshold) {
		int[][] result = new int[img1.length][img1[0].length];
		for(int i = 0; i < img1.length; i++) {
			for( int j = 0; j < img1[0].length; j++) {
				int difference = Math.abs(img1[i][j] - img2[i][j]);
				// Return black or white pixels if image is above or below the threshold
				result[i][j] = (difference > threshold) ? 255 : 0;
			}
		}
		return result;
	}
	
	public static int erodePixel(int[][] originalImage, int i, int j) {
		for (int x = -1; x < 2; x++) {
			for (int y = -1; y < 2; y++) {
				if (originalImage[i + x][j + y] == 0) // If any neighbors = 0, erode
					return 0;
			}
		}
		// If we get here, all neighbors 255
		return 255;
	}

	public static int getFilterValue(int[][] image, int row, int column, int threshold) {
		int xFilter = 0;
		int yFilter = 0;
		int[] xWeights = { -1, 0, 1, -2, 0, 2, -1, 0, 1 };
		int[] yWeights = { 1, 2, 1, 0, 0, 0, -1, -2, -1 };
		int weightsIndex = 0;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				xFilter += (image[row + i][column + j] * xWeights[weightsIndex]);
				yFilter += (image[row + i][column + j] * yWeights[weightsIndex]);
				weightsIndex++;
			}
		}
		// Calculate the filter value
		int filter = (int) Math.sqrt(Math.pow(xFilter, 2) + Math.pow(yFilter, 2));
		if (filter > threshold)
			return 255; // Edge pixel that meets threshold
		else
			return 0; // Edge pixel that doesn't meet threshold
	}

	public static int[][] sobelFilter(BufferedImage image, int threshold) {
		int[][] array = convert2Array(image);
		int[][] edgeArray = new int[array.length][array[0].length];
		for (int i = 1; i < array.length - 1; i++) {
			for (int j = 1; j < array[0].length - 1; j++) {
				edgeArray[i][j] = getFilterValue(array, i, j, threshold);
			}
		}
		return edgeArray;
	}

	public static int[][] convert2Array(BufferedImage image) {
		int[][] imageArray = new int[image.getHeight()][image.getWidth()];
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
				Color c = new Color(image.getRGB(j, i));
				int greyscaleValue = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
				imageArray[i][j] = greyscaleValue;
			}
		}
		return imageArray;
	}

	public static void writeArrayToImage(int[][] imageArray, String fileName) throws IOException {
		BufferedImage image = new BufferedImage(imageArray[0].length, imageArray.length, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < imageArray.length; i++) // Rows
		{
			for (int j = 0; j < imageArray[0].length; j++) // Columns
			{
				Color c = new Color(imageArray[i][j], imageArray[i][j], imageArray[i][j]);
				image.setRGB(j, i, c.getRGB());
			}
		}
		ImageIO.write(image, "png", new File(fileName));
	}

	public static void maxOutBlue(BufferedImage image) throws IOException {
		for (int i = 50; i < 100; i++) {
			for (int j = 50; j < 100; j++) {
				Color original = new Color(image.getRGB(j, i));
				Color newColor = new Color(original.getRed(), original.getGreen(), 225);
				image.setRGB(j, i, newColor.getRGB());
			}
		}
		ImageIO.write(image, "jpg", new File("BlueMaxedOut.jpg"));
	}

	public static void printData(BufferedImage image) {
		for (int i = 40; i < 60; i++) {
			for (int j = 40; j < 60; j++) {
				Color c = new Color(image.getRGB(j, i));
				System.out.println(i + " " + j + " " + c.getRed() + " " + c.getGreen() + " " + c.getBlue());
			}
		}
	}
}