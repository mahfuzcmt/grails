package com.webcommander.controllers.site.captcha

import com.webcommander.manager.CacheManager
import java.awt.Color
import java.awt.Font
import org.apache.commons.lang.RandomStringUtils
import javax.imageio.ImageIO
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import com.webcommander.captcha.SimpleCaptchaService

class SimpleCaptchaController {

    private static final DEFAULT_CAPTCHA_CHARS = ('A'..'Z').step(1).join()

    def captcha(){
        def captcha = newCaptcha()
        try {
            ImageIO.write(captcha, "PNG", response.outputStream)
        } catch (Throwable ignored) {}
    }

    private static Graphics2D createGraphic(BufferedImage image, Font font) {
        Graphics2D g2d = image.createGraphics()
        g2d.font = font
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        return g2d
    }

    private newCaptcha(){
        response.contentType = "image/png"
        response.setHeader("Cache-control", "no-cache")

        String captchaCharSet = DEFAULT_CAPTCHA_CHARS
        final int height = 200
        final int width = 200
        final int fontSize = 24
        final int captchaLength = 6
        final int bottomPadding = 16
        final int lineSpacing = 10
        final boolean isItalic = true
        final String fontFamily = "helvetica"

        String captchaSolution = RandomStringUtils.random(captchaLength,captchaCharSet.toCharArray())
        BufferedImage captchaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        Font oldFont = new Font(fontFamily, (isItalic ? Font.BOLD | Font.ITALIC : Font.BOLD), fontSize)
        Map attributes = oldFont.getAttributes()

        Font font = new java.awt.Font(attributes);
        Graphics2D g2d = createGraphic(captchaImage, font)
        Rectangle2D fontRect = font.getStringBounds(captchaSolution, g2d.fontRenderContext)

        captchaImage = new BufferedImage((int) fontRect.width + bottomPadding, (int) fontRect.height + bottomPadding, BufferedImage.TYPE_INT_RGB)
        g2d = createGraphic(captchaImage, font)

        g2d.color = Color.WHITE
        g2d.fillRect(0, 0, width, height)

        g2d.color = Color.GRAY

        int y1 = lineSpacing
        int x2 = lineSpacing

        int x1 = 0
        int y2 = 0

        while (x1 < width || x2 < width || y1 < height || y2 < height) {
            g2d.drawLine(x1, y1, x2, y2)
            if (y1 < height) {
                x1 = 0
                y1 += lineSpacing
            } else if (x1 < width) {
                y1 = height
                x1 += lineSpacing
            } else {
                x1 = width
                y1 = height
            }

            if (x2 < width) {
                y2 = 0
                x2 += lineSpacing
            } else if (y2 < height) {
                x2 = width
                y2 += lineSpacing
            } else {
                y2 = height
                x2 = width
            }
        }

        g2d.color = Color.BLACK
        g2d.drawString(captchaSolution, (int) (bottomPadding / 2), (int) (bottomPadding / 4) + (int) fontRect.height)

        def session = request.getSession(true)
        session.setAttribute(SimpleCaptchaService.CAPTCHA_SOLUTION_ATTR, captchaSolution)
        CacheManager.cache("session", captchaImage, session.id, SimpleCaptchaService.CAPTCHA_IMAGE_ATTR)
        return captchaImage
    }

}
