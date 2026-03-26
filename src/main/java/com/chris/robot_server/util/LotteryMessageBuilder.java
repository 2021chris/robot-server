package com.chris.robot_server.util;
import java.util.List;

import com.chris.robot_server.enums.OpenStatusEnum;
import com.chris.robot_server.vo.LotteryHistoryVO;

public class LotteryMessageBuilder {

    public static String build(LotteryHistoryVO vo) {
        return ReplyHeaderSimplebuild(vo.getExpect())
                + LotteryMessagebuild(vo.getNumbers(), 7);
    }

    public static String buildDraw(LotteryHistoryVO vo) {
        return ReplyHeaderbuild(vo.getExpect(),vo.getLotteryType())
                + LotteryMessagebuild(vo.getNumbers(), 7);
    }

    private static String LotteryMessagebuild(List<Integer> nums, int revealCount) {

        StringBuilder rowNum = new StringBuilder();
        StringBuilder rowZodiac = new StringBuilder();
        StringBuilder rowColor = new StringBuilder();

        for (int i = 0; i < revealCount; i++) {
            if (i < nums.size()) {
                int n = nums.get(i);
                rowNum.append(String.format("%02d", n)).append("  ");
                rowZodiac.append(LotteryUtil.getZodiacForNumber(n)).append("  ");
                rowColor.append(LotteryUtil.ballEmoji(n)).append(" ");
            } else {
                rowNum.append("--  ");
                rowZodiac.append("--  ");
                rowColor.append("⚪ ");
            }
        }

        return ""
                + rowNum.toString().trim() + "\n"
                + rowZodiac.toString().trim() + "\n"
                + rowColor.toString().trim()
                + "";
    }

    private static String ReplyHeaderSimplebuild(String expect) {
        return "新澳实时开奖六合彩第"+expect+"期开奖结果:\n";
    }

    private static String ReplyHeaderbuild(String expect,Byte lotteryType) {
        String ReplyHeader="";
        if(lotteryType==OpenStatusEnum.KL8.getCode()) {
            ReplyHeader="快乐8六合彩";
        } else if(lotteryType==OpenStatusEnum.Hongkong.getCode()) {
            ReplyHeader="香港六合彩";
        } else if(lotteryType==OpenStatusEnum.Xin_Aomen.getCode()) {
            ReplyHeader="新澳门六合彩";
        } else if(lotteryType==OpenStatusEnum.LaoAo.getCode()) {
            ReplyHeader="老澳门六合彩";
        }
        ReplyHeader = ReplyHeader+"第"+expect+"期开奖结果:\n";
        return ReplyHeader;
    }

    
}
