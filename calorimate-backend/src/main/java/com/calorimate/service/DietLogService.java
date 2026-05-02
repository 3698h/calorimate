package com.calorimate.service;

import com.calorimate.dto.AiRecordDTO;
import com.calorimate.dto.DietLogDTO;
import com.calorimate.dto.RecordAddDTO;
import com.calorimate.vo.DailyDietVO;

import java.time.LocalDate;

public interface DietLogService {

    void addDietLog(Long userId, DietLogDTO dto);

    void addAiDietLog(Long userId, AiRecordDTO dto);

    void addManualDietLog(Long userId, RecordAddDTO dto);

    DailyDietVO getDailyDietLogs(Long userId, LocalDate date);

    void updateDietLog(Long userId, Long id, DietLogDTO dto);

    void deleteDietLog(Long userId, Long id);
}
