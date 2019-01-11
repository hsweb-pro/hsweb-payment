package org.hswebframework.payment.api.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;

/**
 * 图片处理工具类
 * @author zhouhao
 */
public class ImageUtils {
    /**
     * 创建二维码图片
     * @param width 宽度
     * @param height 高度
     * @param content 二维码内容
     * @return 图片对象
     * @throws Exception
     * @see javax.imageio.ImageIO#write(RenderedImage, String, File)
     */
    public static BufferedImage createQrCode(Integer width, Integer height, String content) throws Exception {
        if (width == null){width = 200;}
        if (height == null){height = 200;}
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 2);
        BitMatrix bitMatrix = new MultiFormatWriter()
                .encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * 生成条码图片
     * @param width 宽度
     * @param height 高度
     * @param content 二维码内容
     * @return 图片对象
     * @throws Exception
     * @see javax.imageio.ImageIO#write(RenderedImage, String, File)
     */
    public static BufferedImage createBarCode(int width, int height, String content) throws Exception {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 2);
        BitMatrix bitMatrix = new MultiFormatWriter()
                .encode(content, BarcodeFormat.CODE_128, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}
