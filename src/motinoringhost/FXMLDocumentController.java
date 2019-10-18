/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package motinoringhost;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 *
 * @author antoxa
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML
    private Button button;
    @FXML
    private Pane pane;
    @FXML
    private TextArea textArea;
    
    private Monitoring monitoring;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        if (button.getText().compareTo("Stop") == 0) {
            monitoring.stop();
            button.setText("Start");        
        } else {
            monitoring = new Monitoring(this);
            monitoring.execute();
            button.setText("Stop");        
        }
    }
    
    public void setBackgroudColor(String color) {
        pane.setBackground(new Background(new BackgroundFill(Color.web(color), CornerRadii.EMPTY, Insets.EMPTY)));                                
    }
    
    public void addMessage (String msg) {        
        textArea.setText(textArea.getText() + "\n" + msg);
        System.out.println(msg);
    }
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        textArea.setText("Пока все ОК!");
    }    
    
}
