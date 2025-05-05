package ba.sum.fsre.hackaton.user.home;

// LessonCard.java
public class LessonCard {
    private int iconResId; // Resource ID for the icon
    private String title; // Title of the card
    private String description; // Description of the card
    private String status; // Status of the lesson
    private String buttonText; // Text for the button

    // Constructor
    public LessonCard(int iconResId, String title, String description, String status, String buttonText) {
        this.iconResId = iconResId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.buttonText = buttonText;
    }

    // Getters
    public int getIconResId() {
        return iconResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getButtonText() {
        return buttonText;
    }
}
