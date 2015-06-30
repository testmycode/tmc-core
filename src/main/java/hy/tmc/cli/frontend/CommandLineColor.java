package hy.tmc.cli.frontend;

public enum CommandLineColor {

    BLACK(0, false),
    DARK_GRAY(0, true),
    RED(1, false),
    LIGHT_RED(1, true),
    GREEN(2, false),
    LIGHT_GREEN(2, true),
    YELLOW(3, false),
    LIGHT_YELLOW(3, true),
    BLUE(4, false),
    LIGHT_BLUE(4, true),
    MAGENTA(5, false),
    LIGHT_MAGENTA(5, true),
    CYAN(6, false),
    LIGHT_CYAN(6, true),
    LIGHT_GRAY(7, false),
    WHITE(7, true);

    private final String escape = "\u001B";
    private final int baseCode;
    private final String foregroundCode;
    private final String backgroundCode;

    private CommandLineColor(int code, boolean light) {
        this.baseCode = code;
        if (light) {
            this.foregroundCode = "9";
            this.backgroundCode = "10";
        } else {
            this.foregroundCode = "3";
            this.backgroundCode = "4";
        }
    }

    /**
     * Get the color code corresponding to this color.
     * 
     * @param foreground true if you want the foreground code, false otherwise
     * @return the foreground or background color code for this color
     */
    public String getColorCode(boolean foreground) {
        if (foreground) {
            return generateCode(foregroundCode);
        } else {
            return generateCode(backgroundCode);
        }
    }

    private String generateCode(String prefix) {
        return escape + "[" + prefix + baseCode + "m";
    }
}
