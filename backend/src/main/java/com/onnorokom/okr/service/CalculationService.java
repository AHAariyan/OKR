package com.onnorokom.okr.service;

import com.onnorokom.okr.model.KeyResult;
import com.onnorokom.okr.model.Objective;
import com.onnorokom.okr.model.OkrSheet;

import java.util.List;

public interface CalculationService {
    double calculateKrProgress(double start, double target, double current);

    double calculateObjectiveProgress(List<KeyResult> keyResults);

    double calculateSheetProgress(List<Objective> objectives);
}
