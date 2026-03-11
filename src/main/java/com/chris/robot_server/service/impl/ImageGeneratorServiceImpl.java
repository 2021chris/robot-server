package com.chris.robot_server.service.impl;

import java.io.IOException;
import java.util.List;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.chris.robot_server.service.ImageGeneratorService;
import com.chris.robot_server.util.LotteryUtil;
import com.chris.robot_server.vo.LotteryRow;

@Service
public class ImageGeneratorServiceImpl implements ImageGeneratorService {

    @Override
    public byte[] generateLotteryImage(List<LotteryRow> rows) throws IOException {
        int headerHeight = 60;
        int rowHeight = 58; // 每行更高，圆球更显眼
        int ballSize = 38; // 圆球直径
        int ballNumberFontSize = 22;
        int periodFontSize = 20;

        int width = 900;
        int height = headerHeight + rows.size() * rowHeight + 20; // 底部多一点空间

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // 开启抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 背景白色
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // ────────────────────────────────────────────────
        // 表头 - 黑色背景
        g2d.setColor(new Color(20, 20, 25)); // 深黑灰
        g2d.fillRect(0, 0, width, headerHeight);

        // 表头文字 - 白色 + 稍大字体
        g2d.setFont(new Font("SimHei", Font.BOLD, 22));
        g2d.setColor(Color.WHITE);

        int colPositions[] = { 30, 180, 380, 580, 780, 980, 1180, 750 }; // 各列起始x（可微调）
        // 期数 正码1~7 特码 特码结果

        g2d.drawString("期数", colPositions[0], 40);
        g2d.drawString("正码", colPositions[1], 40);
        g2d.drawString("特码", colPositions[7] - 60, 40);
        g2d.drawString("特码结果", colPositions[7] + 30, 40);

        // ────────────────────────────────────────────────
        // 画网格线（灰色细线）
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(1.0f));

        // 所有横线
        for (int i = 0; i <= rows.size() + 1; i++) {
            int y = headerHeight + i * rowHeight;
            g2d.drawLine(0, y, width, y);
        }

        // 竖线（根据列位置）
        int[] verticalLines = { 0, 140, 660, width }; // 简化：期数 | 正码区 | 特码 | 结果
        // 如果想更精细，可以把正码7个球各自分一格，但通常正码区是一个大格
        for (int x : verticalLines) {
            g2d.drawLine(x, 0, x, height);
        }

        // ────────────────────────────────────────────────
        // 内容行
        g2d.setFont(new Font("SimHei", Font.PLAIN, ballNumberFontSize));
        int y = headerHeight;

        for (LotteryRow row : rows) {
            y += rowHeight;

            // 期数（靠左）
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("SimHei", Font.BOLD, periodFontSize));
            g2d.drawString(row.getPeriod(), colPositions[0], y - 18);

            // 正码 7个球
            int x = colPositions[1];
            g2d.setFont(new Font("SimHei", Font.PLAIN, ballNumberFontSize));

            for (int num : row.getNumbers()) {
                drawBall(g2d, x, y - 38, ballSize, num, true); // true = 有生肖
                x += 75; // 每个球间距（可调 70~80）
            }

            // 特码
            x = colPositions[7] - 60;
            drawBall(g2d, x, y - 38, ballSize, row.getSpecialNumber(), true);

            // 特码结果（大/小 单/双）
            g2d.setFont(new Font("SimHei", Font.BOLD, 20));
            String result = getSizeForSpecialNumber(row.getSpecialNumber());
            Color resultColor = result.contains("大") ? new Color(200, 0, 0)
                    : result.contains("小") ? new Color(0, 100, 200)
                            : Color.BLACK;

            g2d.setColor(resultColor);
            g2d.drawString(result, colPositions[7] + 30, y - 18);
        }

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    // 抽取出来的画单个球 + 号码 + 生肖 方法
    private void drawBall(Graphics2D g2d, int x, int y, int size, int num, boolean showZodiac) {
        // 圆球背景色
        Color color = LotteryUtil.getColorForNumber(num);
        g2d.setColor(color);
        g2d.fillOval(x, y, size, size);

        // 白色描边（可选，更像官方风格）
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.8f));
        g2d.drawOval(x, y, size, size);

        // 号码（居中）
        String numStr = String.format("%02d", num);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 22)); // 数字用Arial更清晰
        FontMetrics fm = g2d.getFontMetrics();
        int textW = fm.stringWidth(numStr);
        int textH = fm.getAscent() - fm.getDescent();
        g2d.drawString(numStr, x + (size - textW) / 2, y + (size + textH) / 2);

        // 生肖（球右边）
        if (showZodiac) {
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("SimHei", Font.PLAIN, 16));
            g2d.drawString(LotteryUtil.getZodiacForNumber(num), x + size + 8, y + size / 2 + 6);
        }
    }

    private String getSizeForSpecialNumber(int specialNum) {
        String size = "";
        if (specialNum == 49)
            size = "和";
        else if (specialNum >= 25)
            size = "大";
        else
            size = "小";

        if (specialNum == 49)
            size = size + "和";
        else if (specialNum % 2 == 0)
            size = size + "双";
        else
            size = size + "单";
        // 示例规则：特码为偶数为大单，奇数为小单
        return size;
    }

}
