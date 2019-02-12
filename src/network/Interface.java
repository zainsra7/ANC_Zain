/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author Zain
 */
public class Interface extends JFrame implements ActionListener {

    JPanel buttons;
    JButton normalConBtn;
    JButton slowConBtn;
    JButton splitBtn;
    JButton checkItrBtn;
    JButton outputBtn;
    JButton exitBtn;
    JTextArea screen;
    String screen_msg; //message to display on the screen

    Interface() {

        //Setting Main Window
        setTitle("Zain-AdvanceNetworks");
        setSize(500, 600);
        setLocation(550, 200);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //Disabling Exit Button of the main window
        setResizable(false);

        //Setting Buttons
        buttons = new JPanel();
        normalConBtn = new JButton("Normal Convergence");
        checkItrBtn = new JButton("Check Iteration");
        splitBtn = new JButton("Split Horizon");
        slowConBtn = new JButton("Slow Convergence");
        outputBtn = new JButton("Write Output");
        exitBtn = new JButton("Exit");

        outputBtn.setEnabled(false);
        checkItrBtn.setEnabled(false);

        screen_msg = "\tWelcome to Zain's Advance Networks\n Please click one of the below buttons to start building the network";

        screen = new JTextArea(screen_msg, 10, 50);
        screen.setEditable(false); //Read-Only TextArea
        screen.setBackground(Color.DARK_GRAY);
        screen.setForeground(Color.white);

        JScrollPane scroll = new JScrollPane(screen,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); //Making TextArea Scrollable

        //Linking ActionListeners for Button Presses
        normalConBtn.addActionListener(this);
        slowConBtn.addActionListener(this);
        checkItrBtn.addActionListener(this);
        splitBtn.addActionListener(this);
        outputBtn.addActionListener(this);
        exitBtn.addActionListener(this);

        //Adding buttons to the Panel
        buttons.add(normalConBtn);
        buttons.add(slowConBtn);
        buttons.add(splitBtn);
        buttons.add(checkItrBtn);
        buttons.add(outputBtn);
        buttons.add(exitBtn);

        buttons.setLayout(new GridLayout(3, 2));

        //Adding Panel and TextArea in the Frame (Main Window)
        add(scroll);
        add(buttons);

        setLayout(new GridLayout(2, 1));
        setVisible(true); //Displaying main Window
        Networks.setup();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == normalConBtn) {
            String filename = getFilename();
            if (filename != null) {
                String message = Networks.readNetworkFile(filename);
                if ("".equals(message)) {
                    screen.setText(screen_msg);
                    disableButtons();
                    Networks.bellmanFordAlgorithm(screen);
                    enableButtons();
                } else {
                    JOptionPane.showMessageDialog(null, message, "Error Message", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (e.getSource() == slowConBtn) {
            String filename = getFilename();
            if (filename != null) {
                String message = Networks.readSNetworkFile(filename);
                if ("".equals(message)) {
                    disableButtons();
                    int stopIteration;
                    try {
                        stopIteration = Integer.parseInt(getStopItertion());
                        screen.setText(screen_msg);
                        Networks.slowConvergence(false, stopIteration);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Please enter valid integer!", "Error Message", JOptionPane.ERROR_MESSAGE);
                    }
                    enableButtons();
                } else {
                    JOptionPane.showMessageDialog(null, message, "Error Message", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (e.getSource() == splitBtn) {
            String filename = getFilename();
            if (filename != null) {
                String message = Networks.readSNetworkFile(filename);
                if ("".equals(message)) {
                    disableButtons();
                    screen.setText(screen_msg);
                    Networks.slowConvergence(true, 10);
                    enableButtons();
                } else {
                    JOptionPane.showMessageDialog(null, message, "Error Message", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (e.getSource() == checkItrBtn) {
            try {
                int itrNumber = Integer.parseInt(getCheckItertion());
                if (itrNumber >= 0 && itrNumber <= Networks.totalIterations) {
                    screen.setText(screen_msg);
                    Networks.printIteration(itrNumber);
                } else {
                    JOptionPane.showMessageDialog(null, "Please try again with correct Iteration Number! [0-" + Networks.totalIterations + "]", "Error Message", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Please try again with valid integer!", "Error Message", JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == outputBtn) {
            String filename = getFilename();

            if (filename != null) {
                String message = Networks.writeToFile(filename);

                if (message.equals("")) {
                    JOptionPane.showMessageDialog(null, "Check " + filename + " for Output!", "Output Message", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, message, "Output Message", JOptionPane.ERROR_MESSAGE);
                }
            }

        } else if (e.getSource() == exitBtn) {
            int result = JOptionPane.showConfirmDialog(null, "Do you want to Exit the Program?");

            if (result == JOptionPane.YES_OPTION) {
                String message = "Thanks for using Zain's Network | @zainsra.com!";
                JOptionPane.showMessageDialog(null, message, "Message", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
        }
    }//end of actionPerformed

    //Disable/Enable buttons during different scenarios (Normal/Slow Convergence)
    private void disableButtons() {
        checkItrBtn.setEnabled(false);
        outputBtn.setEnabled(false);
    }

    private void enableButtons() {
        checkItrBtn.setEnabled(true);
        outputBtn.setEnabled(true);
    }

    public void writeToScreen(String text) {
        screen.append(text);
    }

    private String getFilename() {
        return JOptionPane.showInputDialog(this, "Please enter filename", "Network Filename", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getStopItertion() {
        return JOptionPane.showInputDialog(this, "As this is a count to infinity problem, Please enter the max number of iterations", "Iterations Needed", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getCheckItertion() {
        return JOptionPane.showInputDialog(this, "Please enter the iteration number to check, Total Iterations are " + Networks.totalIterations, "Check Iteration Number", JOptionPane.INFORMATION_MESSAGE);
    }
}
