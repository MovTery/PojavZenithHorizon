package com.movtery.pojavzh.utils.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ImageUtils {
    /***
     * 通过读取文件的头部信息来判断文件是否为图片
     * @param filePath 文件路径
     * @return 返回是否为图片
     */
    public static boolean isImage(File filePath) {
        try (FileInputStream input = new FileInputStream(filePath)) {
            byte[] header = new byte[4];
            if (input.read(header, 0, 4) != -1) {
                return (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) || //JPEG
                        (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && header[2] == (byte) 0x4E && header[3] == (byte) 0x47) || //PNG
                        (header[0] == (byte) 0x47 && header[1] == (byte) 0x49 && header[2] == (byte) 0x46) || //GIF
                        (header[0] == (byte) 0x42 && header[1] == (byte) 0x4D) || //BMP
                        ((header[0] == (byte) 0x49 && header[1] == (byte) 0x49 && header[2] == (byte) 0x2A && header[3] == (byte) 0x00) || //TIFF
                                (header[0] == (byte) 0x4D && header[1] == (byte) 0x4D && header[2] == (byte) 0x00 && header[3] == (byte) 0x2A)); //TIFF
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    /**
     * 通过计算图片的长款比例来计算缩放后的长款数据
     * @param imageWidth 原始图片的长
     * @param imageHeight 原始图片的宽
     * @param maxSize 需要限制在多大的空间
     * @return 返回一个缩放后的长宽数据对象
     */
    public static Dimension resizeWithRatio(int imageWidth, int imageHeight, int maxSize){
        double widthRatio = (double) maxSize / imageWidth;
        double heightRatio = (double) maxSize / imageHeight;

        //选择较小的缩放比例，确保长宽按比例缩小且不超过maxSize限制
        double ratio = Math.min(widthRatio, heightRatio);
        int newWidth = (int) (imageWidth * ratio);
        int newHeight = (int) (imageHeight * ratio);

        return new Dimension(newWidth, newHeight);
    }
}
