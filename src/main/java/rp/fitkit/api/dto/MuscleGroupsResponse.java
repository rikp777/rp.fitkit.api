package rp.fitkit.api.dto;

import java.util.List;

public class MuscleGroupsResponse {
    private List<MuscleGroupDto> muscleGroups;
    private List<String> omittedMuscleGroupCodes;

    public MuscleGroupsResponse(List<MuscleGroupDto> muscleGroups, List<String> omittedMuscleGroupCodes) {
        this.muscleGroups = muscleGroups;
        this.omittedMuscleGroupCodes = omittedMuscleGroupCodes;
    }

    public List<MuscleGroupDto> getMuscleGroups() {
        return muscleGroups;
    }

    public void setMuscleGroups(List<MuscleGroupDto> muscleGroups) {
        this.muscleGroups = muscleGroups;
    }

    public List<String> getOmittedMuscleGroupCodes() {
        return omittedMuscleGroupCodes;
    }

    public void setOmittedMuscleGroupCodes(List<String> omittedMuscleGroupCodes) {
        this.omittedMuscleGroupCodes = omittedMuscleGroupCodes;
    }
}
