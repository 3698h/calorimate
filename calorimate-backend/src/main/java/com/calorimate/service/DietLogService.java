package com.calorimate.service;

import com.calorimate.dto.DietLogDTO;
import com.calorimate.vo.DailyDietVO;

import java.time.LocalDate;

public interface DietLogService {

    void addDietLog(Long userId, DietLogDTO dto);

    DailyDietVO getDailyDietLogs(Long userId, LocalDate date);

    void updateDietLog(Long userId, Long id, DietLogDTO dto);

    void deleteDietLog(Long userId, Long id);
}
