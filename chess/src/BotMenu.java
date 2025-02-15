import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BotMenu extends JFrame implements ActionListener{

    boolean whitetomove = true;

    BotMenu(){
        super("Choose a bot of your skill level");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        new HomeScreen().setVisible(true);
                    }
                    
                });
            }
        });
        setResizable(false);
        getContentPane().setBackground(Color.decode("#a1c47e"));
        setLayout(new FlowLayout(FlowLayout.CENTER));

        addoptions();

        addbot("Beginner","700");
        addbot("Casual","1100");
        addbot("Intermediate","1600");
        addbot("Advanced","2100");
        addbot("Impossible","∞");
    }

    private void addoptions(){
        JPanel space = new JPanel();
        space.setPreferredSize(new Dimension(600,75));
        space.setBackground(Color.decode("#a1c47e"));
        space.setLayout(new FlowLayout(FlowLayout.CENTER));
        add(space);

        JRadioButton white = new JRadioButton("Start as white");
        white.setBackground(Color.decode("#a1c47e"));
        white.setPreferredSize(new Dimension(150,60));
        white.setFont(new Font("Times New Roman",Font.BOLD,15));
        white.setFocusable(false);
        white.setSelected(true);
        white.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                whitetomove=white.isSelected();
            }
            
        });
        space.add(white);
    }

    private void addbot(String name, String elo){
        JButton b = new JButton();
        b.setText("<html>"+name+" Mode - <br> Estimated Elo: "+elo+"</html>");
        b.setFocusable(false);
        b.setPreferredSize(new Dimension(155,80));
        b.setFont(new Font("Times New Roman",Font.BOLD,15));
        b.setBackground(Color.lightGray);
        b.addActionListener(this);
        b.setName(name);
        add(b);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();
        new Bots(button.getName(),whitetomove).setVisible(true);
        dispose();
    }
}
