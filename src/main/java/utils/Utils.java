package utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static String convertUTF8IntoString(String rawString){
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(rawString);
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    public static String TimeFormat(Timestamp timestamp){
        String hour = new SimpleDateFormat("HH").format(timestamp);
        String minute = new SimpleDateFormat("mm").format(timestamp);
        String date = new SimpleDateFormat("dd/MM/yyyy").format(timestamp);
       return hour + Utils.convertUTF8IntoString("h")+ minute
               +Utils.convertUTF8IntoString(" - ") + date;
    }

    public static String md5(String str){
        String result = "";
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(str.getBytes());
            BigInteger bigInteger = new BigInteger(1,digest.digest());
            result = bigInteger.toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static BufferedImage createResizedCopy(BufferedImage originalImage, int framesWidth, int framesHeight,
                                                  boolean preserveAlpha) {
        Dimension imageOld = new Dimension(originalImage.getWidth(),originalImage.getHeight());
        Dimension frames = new Dimension(framesWidth,framesHeight);
        Dimension imageNew = getScaledDimension(imageOld,frames);
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        int width = (int) Math.round(imageNew.getWidth());
        int height = (int) Math.round(imageNew.getHeight());
        BufferedImage scaledBI = new BufferedImage(width,height,imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        return scaledBI;
    }

    public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {

        int original_width = imgSize.width;
        int original_height = imgSize.height;
        int bound_width = boundary.width;
        int bound_height = boundary.height;
        int new_width = original_width;
        int new_height = original_height;

        // first check if we need to scale width
        if (original_width > bound_width) {
            //scale width to fit
            new_width = bound_width;
            //scale height to maintain aspect ratio
            new_height = (new_width * original_height) / original_width;
        }

        // then check if we need to scale even with the new height
        if (new_height > bound_height) {
            //scale height to fit instead
            new_height = bound_height;
            //scale width to maintain aspect ratio
            new_width = (new_height * original_width) / original_height;
        }

        return new Dimension(new_width, new_height);
    }

}