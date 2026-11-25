package edu.hitsz.application;

public enum GameDifficulty {
    EASY("简单", "easy", "bg.jpg"),
    NORMAL("普通", "normal", "bg2.jpg"),
    HARD("困难", "hard", "bg5.jpg");

    private final String displayName;
    private final String fileName;
    private final String backgroundImageName;

    GameDifficulty(String displayName, String fileName, String backgroundImageName) {
        this.displayName = displayName;
        this.fileName = fileName;
        this.backgroundImageName = backgroundImageName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getBackgroundImageName() {
        return backgroundImageName;
    }

    public static GameDifficulty fromName(String name) {
        if (name != null) {
            for (GameDifficulty difficulty : values()) {
                if (difficulty.name().equals(name)) {
                    return difficulty;
                }
            }
        }
        return EASY;
    }
}
