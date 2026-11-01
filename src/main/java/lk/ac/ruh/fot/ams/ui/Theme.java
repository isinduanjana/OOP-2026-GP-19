package lk.ac.ruh.fot.ams.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

/**
 * [COMMON PART] Colours, fonts and look-and-feel used by every screen,
 * so all four members' screens look like one product.
 *
 * Uses FlatLaf (from pom.xml) when available, otherwise falls back to Nimbus.
 */
public final class Theme {

    // ---- base palette
    public static final Color BG          = new Color(0xF3F5FA);
    public static final Color SURFACE     = Color.WHITE;
    public static final Color SIDEBAR     = new Color(0x111827);
    public static final Color SIDEBAR_HOVER = new Color(0x1F2937);
    public static final Color BORDER      = new Color(0xE3E7EF);
    public static final Color TEXT        = new Color(0x111827);
    public static final Color TEXT_MUTED  = new Color(0x6B7280);
    public static final Color TEXT_ON_DARK = new Color(0xE5E7EB);
    public static final Color TABLE_STRIPE = new Color(0xF8FAFD);

    // ---- brand / status colours
    public static final Color PRIMARY  = new Color(0x2F54EB);   // University blue
    public static final Color SUCCESS  = new Color(0x16A34A);
    public static final Color WARNING  = new Color(0xD97706);
    public static final Color DANGER   = new Color(0xDC2626);
    public static final Color INFO     = new Color(0x0891B2);

    // ---- one accent per role (each member's dashboard)
    public static final Color ADMIN_ACCENT     = new Color(0x4F46E5); // M1 indigo
    public static final Color LECTURER_ACCENT  = new Color(0x0D9488); // M2 teal
    public static final Color TO_ACCENT        = new Color(0xEA580C); // M3 orange
    public static final Color STUDENT_ACCENT   = new Color(0x7C3AED); // M4 violet

    public static final String FONT_FAMILY = pickFont();

    public static final Font FONT          = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BOLD     = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_SMALL    = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_TITLE    = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_HEADING  = new Font(FONT_FAMILY, Font.BOLD, 16);
    public static final Font FONT_STAT     = new Font(FONT_FAMILY, Font.BOLD, 26);

    private Theme() {
    }

    private static String pickFont() {
        List<String> available = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String f : new String[]{"Segoe UI", "Inter", "Roboto", "Helvetica Neue", "Noto Sans", "DejaVu Sans"}) {
            if (available.contains(f)) return f;
        }
        return Font.SANS_SERIF;
    }

    /** Lighter tint of a colour (for soft backgrounds of badges / cards). */
    public static Color tint(Color c, double amount) {
        int r = (int) (c.getRed() + (255 - c.getRed()) * amount);
        int g = (int) (c.getGreen() + (255 - c.getGreen()) * amount);
        int b = (int) (c.getBlue() + (255 - c.getBlue()) * amount);
        return new Color(r, g, b);
    }

    public static Color darker(Color c, double amount) {
        return new Color((int) (c.getRed() * (1 - amount)), (int) (c.getGreen() * (1 - amount)),
                (int) (c.getBlue() * (1 - amount)));
    }

    /** Installs FlatLaf (or Nimbus) and our global defaults. Call once at start-up. */
    public static void install() {
        boolean flat = false;
        try {
            // loaded by reflection so the project still compiles without FlatLaf
            Class<?> laf = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            UIManager.setLookAndFeel((javax.swing.LookAndFeel) laf.getDeclaredConstructor().newInstance());
            flat = true;
        } catch (Exception | LinkageError e) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
                // keep default look and feel
            }
        }
        UIManager.put("defaultFont", FONT);
        for (String key : new String[]{"Label.font", "Button.font", "TextField.font", "PasswordField.font",
                "ComboBox.font", "Table.font", "TableHeader.font", "TextArea.font", "List.font",
                "CheckBox.font", "RadioButton.font", "TabbedPane.font", "ToolTip.font", "OptionPane.messageFont",
                "Spinner.font", "FormattedTextField.font"}) {
            UIManager.put(key, FONT);
        }
        UIManager.put("Panel.background", new ColorUIResource(BG));
        UIManager.put("OptionPane.background", new ColorUIResource(SURFACE));
        if (flat) {
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.focusColor", new ColorUIResource(tint(PRIMARY, 0.55)));
            UIManager.put("Component.focusedBorderColor", new ColorUIResource(PRIMARY));
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.selectionBackground", new ColorUIResource(tint(PRIMARY, 0.82)));
            UIManager.put("Table.selectionForeground", new ColorUIResource(TEXT));
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("TabbedPane.selectedBackground", new ColorUIResource(SURFACE));
            UIManager.put("TabbedPane.underlineColor", new ColorUIResource(PRIMARY));
            UIManager.put("TabbedPane.tabHeight", 34);
        }
    }
}
