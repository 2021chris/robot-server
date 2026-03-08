package com.chris.robot_server.service;

import java.io.IOException;
import java.util.List;

import com.chris.robot_server.vo.LotteryRow;

public interface ImageGeneratorService {

    byte[] generateLotteryImage(List<LotteryRow> rows) throws IOException;
}
