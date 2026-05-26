package com.siegeofaldoria;

import javax.swing.*;

/**
 * Entry point for Siege of Aldoria Tower Defense.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Siege of Aldoria");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);

            Game game = new Game();
            window.add(game.getPanel());
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);

            game.start();
        });
    }
}
