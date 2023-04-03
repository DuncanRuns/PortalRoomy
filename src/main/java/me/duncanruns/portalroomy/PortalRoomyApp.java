package me.duncanruns.portalroomy;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PortalRoomyApp {
    public static final String VERSION = getVersion();
    private static final Pattern F3_C_PATTERN = Pattern.compile("^/execute in minecraft:overworld run tp @s -?\\d+\\.\\d\\d -?\\d+\\.\\d\\d -?\\d+\\.\\d\\d -?\\d+\\.\\d\\d -?\\d+\\.\\d\\d$");
    private static final Pattern F3_C_NETHER_PATTERN = Pattern.compile("^/execute in minecraft:the_nether run tp @s -?\\d+\\.\\d\\d -?\\d+\\.\\d\\d -?\\d+\\.\\d\\d -?\\d+\\.\\d\\d -?\\d+\\.\\d\\d$");
    private static final Pattern XYZY_PATTERN = Pattern.compile("-?\\d+\\.\\d\\d -?\\d+\\.\\d\\d -?\\d+\\.\\d\\d -?\\d+\\.\\d\\d");
    private static String lastPaste = "";

    private static JLabel calculatedLabel;

    private static String calculatedOut = "";
    private static String teleportOut = "";

    private static JLabel currentLabel;
    private static String currentOut = "";

    private static JButton angleButton;
    private static String angleOut = "";

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        JFrame jFrame = new JFrame();
        jFrame.setTitle("PortalRoomy v" + VERSION);
        jFrame.setSize(new Dimension(600, 200));

        jFrame.getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.fill = 1;

        gbc.gridx = 0;

        currentLabel = new JLabel("Current coordinates will appear here");
        currentLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        currentLabel.setHorizontalAlignment(SwingConstants.CENTER);
        jFrame.add(currentLabel, gbc);

        JButton copyCurrent = new JButton("Copy Coordinates");
        copyCurrent.addActionListener(e -> Clipboard.copy(getCurrentOut()));
        jFrame.add(copyCurrent, gbc);

        angleButton = new JButton("Copy Angle");
        angleButton.addActionListener(e -> Clipboard.copy(getAngleOut()));
        jFrame.add(angleButton, gbc);

        gbc.gridx = 2;

        calculatedLabel = new JLabel("Calculated coordinates will appear here");
        calculatedLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        calculatedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        jFrame.add(calculatedLabel, gbc);

        JButton coordsButton = new JButton("Copy Coordinates");
        coordsButton.addActionListener(e -> Clipboard.copy(getCalculatedOut()));
        jFrame.add(coordsButton, gbc);

        JButton copyTeleport = new JButton("Copy Teleport Command");
        copyTeleport.addActionListener(e -> Clipboard.copy(getTeleportOut()));
        jFrame.add(copyTeleport, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;

        JLabel arrowLabel = new JLabel("\uD83E\uDC46");
        arrowLabel.setFont(arrowLabel.getFont().deriveFont(30f));
        jFrame.add(arrowLabel, gbc);

        jFrame.setAlwaysOnTop(true);
        jFrame.setVisible(true);

        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                loop();
            } catch (Exception ignored) {
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
    }

    public static String getCurrentOut() {
        return currentOut;
    }

    public static String getAngleOut() {
        return angleOut;
    }

    private static String getCalculatedOut() {
        return calculatedOut;
    }

    public static String getTeleportOut() {
        return teleportOut;
    }

    private static void loop() throws Exception {
        String newPaste = Clipboard.paste();
        if (!newPaste.equals(lastPaste)) {
            lastPaste = newPaste;
            int matchy = 0;
            if (F3_C_PATTERN.matcher(newPaste).matches()) {
                matchy = 1;
            } else if (F3_C_NETHER_PATTERN.matcher(newPaste).matches()) {
                matchy = 2;
            }

            if (matchy > 0) {
                Matcher matcher = XYZY_PATTERN.matcher(newPaste);
                matcher.find();
                MatchResult matchResult = matcher.toMatchResult();
                String[] numStrings = newPaste.substring(matchResult.start(), matchResult.end()).split(" ");

                processNums(numStrings, matchy == 2);
            }
        }
    }

    private static void processNums(String[] numStrings, boolean fromNether) {
        double x = Math.floor(Double.parseDouble(numStrings[0]));
        int y = (int) Math.floor(Double.parseDouble(numStrings[1]));
        double z = Math.floor(Double.parseDouble(numStrings[2]));
        int yaw = ((int) Double.parseDouble(numStrings[3])) % 360;
        if (yaw > 180) yaw -= 360;
        if (yaw < -180) yaw += 360;

        int outx;
        int outz;

        if (fromNether) {
            outx = (int) Math.floor(x * 8);
            outz = (int) Math.floor(z * 8);
        } else {
            outx = (int) Math.floor(x / 8);
            outz = (int) Math.floor(z / 8);
        }

        String coords = String.format("%d %d %d", outx, y, outz);
        if (fromNether) {
            calculatedLabel.setText("Calc'd Overworld Coords: " + coords);
            teleportOut = "/execute in overworld run tp @s " + coords;
        } else {
            calculatedLabel.setText("Calc'd Nether Coords: " + coords);
            teleportOut = "/execute in the_nether run tp @s " + coords;
        }
        calculatedOut = coords;

        String currentCoords = String.format("%d %d %d", (int) x, y, (int) z);

        currentLabel.setText("Current Position: " + currentCoords);
        currentOut = currentCoords;

        String currentAngle = Integer.toString(yaw);
        angleButton.setText("Copy Angle (" + currentAngle + ")");
        angleOut = currentAngle;
    }

    private static String getVersion() {
        // Thanks to answers from this: https://stackoverflow.com/questions/33020069/how-to-get-version-attribute-from-a-gradle-build-to-be-included-in-runtime-swing
        String ver = PortalRoomyApp.class.getPackage().getImplementationVersion();
        if (ver == null) {
            return "DEV";
        }
        return ver;
    }
}
