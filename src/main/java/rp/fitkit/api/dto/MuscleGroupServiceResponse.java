package rp.fitkit.api.dto;

public class MuscleGroupServiceResponse {
    private final MuscleGroupDto muscleGroupDto;
    private final String contentLanguage;

    public MuscleGroupServiceResponse(MuscleGroupDto muscleGroupDto, String contentLanguage) {
        this.muscleGroupDto = muscleGroupDto;
        this.contentLanguage = contentLanguage;
    }

    public MuscleGroupDto getMuscleGroupDto() {
        return muscleGroupDto;
    }

    public String getContentLanguage() {
        return contentLanguage;
    }
}
