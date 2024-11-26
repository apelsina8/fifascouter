package ru.apelsina8.fifascouter.dto;

public class ScoreResponseDTO {

    private String time;
    private int firstTeamScore;
    private int secondTeamScore;

    public ScoreResponseDTO(String time, int firstTeamScore, int secondTeamScore) {
        this.time = time;
        this.firstTeamScore = firstTeamScore;
        this.secondTeamScore = secondTeamScore;
    }

    public int getSecondTeamScore() {
        return secondTeamScore;
    }

    public int getFirstTeamScore() {
        return firstTeamScore;
    }

    public String getTime() {
        return time;
    }
}

