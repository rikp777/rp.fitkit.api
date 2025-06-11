package rp.fitkit.api.dto;

import lombok.Value;

import java.time.LocalDate;

@Value
public class ProgressDataPointDto {
    LocalDate date;
    double estimatedOneRepMax;
    double weight;
    int reps;
}
