package client;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.UnknownHostException;

/*** ClientGUI
 * @author Sihan Peng
 */

public class ClientGUI {
    private JPanel panel1;
    private JButton button1;
    private JTextField text1;
    private JLabel TopLabel;
    private JLabel label1;
    private JLabel label2;
    private JTextField text2;
    private JComboBox cb;

    public ClientGUI() {




        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Client myClient = new Client();
                myClient.setActions();
                //TODO: ADD pop out error message

                try{
                    myClient.establishSocket();
                }
                catch (IOException e){
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,e.getClass().getCanonicalName(), "Failure",
                            JOptionPane.ERROR_MESSAGE);
                    System.out.println("Exit");
                    return;
                }
                String word = text1.getText();
                String action = cb.getSelectedItem().toString();
                String meaning = text2.getText();
                String[] result = myClient.sendMsg(action, word, meaning);
                System.out.println("Message send");
                if (result[0] == "Success"){
                    switch (action){
                        case "Query":
                            text2.setText(result[1]);
                            break;
                        case "Add":
                        case "Delete":
                            JOptionPane.showMessageDialog(null,action+" Option Success",
                                    "Success",JOptionPane.INFORMATION_MESSAGE);
                            //Add success
                            break;
                    }
                }
                else{
                    //Fail
                    JOptionPane.showMessageDialog(null,result[1],
                            "Failure",JOptionPane.ERROR_MESSAGE);
                    System.out.println(result[1]);
                }
                myClient.closeSocket();
                System.out.println("Socket close");
            }
        });
    }

    public static void main(String[] args){
        JFrame frame = new JFrame("ClientGUI");
        frame.setContentPane(new ClientGUI().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }
}
