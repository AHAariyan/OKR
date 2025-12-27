package com.onnorokom.okr.service.impl;

import com.onnorokom.okr.model.KeyResult;
import com.onnorokom.okr.model.Objective;
import com.onnorokom.okr.service.CalculationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalculationServiceImpl implements CalculationService {

    @Override
    public double calculateKrProgress(double start, double target, double current) {
        if (Math.abs(target - start) < 0.0001) {
            return (current >= target) ? 1.0 : 0.0;
        }

        double progress = (current - start) / (target - start);
        return Math.max(0.0, Math.min(1.0, progress)); // clamp 0..1
    }

    @Override
    public double calculateObjectiveProgress(List<KeyResult> keyResults) {
        if (keyResults == null || keyResults.isEmpty()) {
            return 0.0;
        }
        double sum = keyResults.stream().mapToDouble(KeyResult::getComputedProgress).sum();
        return sum / keyResults.size();
    }

    @Override
    public double calculateSheetProgress(List<Objective> objectives) {
        if (objectives == null || objectives.isEmpty()) {
            return 0.0;
        }
        double sum = objectives.stream().mapToDouble(Objective::getComputedProgress).sum();
        return sum / objectives.size();
    }
}
