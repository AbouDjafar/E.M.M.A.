package sample;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AjUEController {
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private AnchorPane DadNode;
    @FXML
    private VBox etagere;
    @FXML
    private Pane titre, titre1, titre2;
    @FXML
    private GridPane grille1, grille2;
    @FXML
    private HBox zoneBoutton;
    @FXML
    private JFXSlider P_CC, P_TP, P_TPE, P_EE;
    @FXML
    private Spinner<Integer> D_CC, D_TP, D_TPE, D_EE, credits;
    @FXML
    private JFXTextField intitule, code;
    @FXML
    private Label valeurPCC, valeurPTP, valeurPTPE, valeurPEE, Valider, Annuler;

    private EMMAnim anima;
    private EMMActionForm formactio;
    private EMModel modelo;
    private ArrayList<Node> badNodes;
    private ArrayList<String> modData;

    @FXML
    private void initialize() {
        // Initialisation
        badNodes = new ArrayList<>();
        for (Node GP : etagere.getChildren()){
            if (GP.getClass().equals(GridPane.class)){
                for (Node N : ((GridPane)GP).getChildren()){
                    if (N.getClass().equals(VBox.class))
                        N = ((VBox)N).getChildren().get(0);
                    if (N.getClass().equals(JFXSlider.class))
                        ((JFXSlider)N).setValue(0);
                    if (N.getClass().equals(Spinner.class)) {
                        if (N.getId().equals("credits"))
                            ((Spinner<Integer>) N).setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
                        else
                            ((Spinner<Integer>) N).setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 100, 10));
                    }
                    if (N.getClass().equals(JFXTextField.class) || N.getClass().equals(JFXComboBox.class))
                        badNodes.add(N);
                }
            }
        }
        modelo = new EMModel();
        formactio = new EMMActionForm();
        anima = new EMMAnim();
        // Animation d'ouverture
        for (Node N : etagere.getChildren()) {
            if (N.getClass().equals(Pane.class))
                anima.decroissance(N, 0);
            else
                anima.decroissance(N, 2);
        }
        titre.setScaleX(1);
        SequentialTransition st = anima.formShowTime(titre1, "Informations Générales", grille1, zoneBoutton, 500, true);
        SequentialTransition st2 = anima.formShowTime(titre2, "Spécificités", grille2, null, 500, true);
        SequentialTransition sAnim = new SequentialTransition(st, st2);
        sAnim.setOnFinished(event -> {
            if (((ArrayList)DadNode.getUserData()).size() > 3){ // Si on a recu des données à modifier
                modData = (ArrayList<String>) ((ArrayList)DadNode.getUserData()).get(3);
                loadModData(modData);
            }
        });
        sAnim.play();
        // Configuration des événements
        P_CC.setOnMouseDragged(e -> {
            if (((int)P_CC.getValue() + (int)P_TP.getValue() + (int)P_TPE.getValue() + (int)P_EE.getValue()) > 100)
                valeurPCC.setStyle("-fx-text-fill: red");
            else
                valeurPCC.setStyle("-fx-text-fill: green");
            valeurPCC.setText((int)P_CC.getValue()+"%");
        });
        P_TP.setOnMouseDragged(e -> {
            if (((int)P_CC.getValue() + (int)P_TP.getValue() + (int)P_TPE.getValue() + (int)P_EE.getValue()) > 100)
                valeurPTP.setStyle("-fx-text-fill: red");
            else
                valeurPTP.setStyle("-fx-text-fill: green");
            valeurPTP.setText((int)P_TP.getValue()+"%");
        });
        P_TPE.setOnMouseDragged(e -> {
            if (((int)P_CC.getValue() + (int)P_TP.getValue() + (int)P_TPE.getValue() + (int)P_EE.getValue()) > 100)
                valeurPTPE.setStyle("-fx-text-fill: red");
            else
                valeurPTPE.setStyle("-fx-text-fill: green");
            valeurPTPE.setText((int)P_TPE.getValue()+"%");
        });
        P_EE.setOnMouseDragged(e -> {
            if (((int)P_CC.getValue() + (int)P_TP.getValue() + (int)P_TPE.getValue() + (int)P_EE.getValue()) > 100)
                valeurPEE.setStyle("-fx-text-fill: red");
            else
                valeurPEE.setStyle("-fx-text-fill: green");
            valeurPEE.setText((int)P_EE.getValue()+"%");
        });
        intitule.setOnKeyTyped(e -> {
            if(formactio.verificationOnEvent(intitule, "^[A-Za-z].{4,60}$", "Cet intitulé n'est pas valide", "intitulé valide") && (badNodes.contains(intitule))) {
                int idx = badNodes.indexOf(intitule);
                badNodes.set(idx, null);
            }
        });
        code.setOnKeyTyped(e -> {
            if(formactio.verificationOnEvent(code, "^[A-Z]{3}\\d{2,3}$", "Cet code n'est pas valide", "code d'UE valide") && (badNodes.contains(code))) {
                int idx = badNodes.indexOf(code);
                badNodes.set(idx, null);
            }
        });
    }

    @FXML
    private void actionValider(){
        boolean toutEstOk = formactio.verificationNivelle(0, 2, badNodes);
        if (toutEstOk){
            if (modData == null) {
                modelo.ajoutUE(code.getText(), intitule.getText(), D_CC.getValue(), D_TP.getValue(), D_TPE.getValue(), D_EE.getValue(), (int) P_CC.getValue(), (int) P_TP.getValue(), (int) P_TPE.getValue(), (int) P_EE.getValue(), credits.getValue());
                anima.infoBoxShow((ArrayList<Object>) DadNode.getUserData(), "Information (Succès)", "L'Unité d'enseignement a été enregistrée avec succès.");
                actionAnnuler();
            }else{
                modelo.updateUE(code.getText(), intitule.getText(), D_CC.getValue(), D_TP.getValue(), D_TPE.getValue(), D_EE.getValue(), P_CC.getValue(), P_TP.getValue(), P_TPE.getValue(), P_EE.getValue(), credits.getValue());
                anima.infoBoxShow((ArrayList<Object>) DadNode.getUserData(), "Information (Succès)", "L'Unité d'enseignement a été modifié avec succès.");
            }
        }else
            anima.infoBoxShow((ArrayList<Object>)DadNode.getUserData(), "Information (Erreur)", "Certains des champs du formulaire n'ont pas été correctement remplis. Impossible d'ajouter la nouvelle UE dans ces conditions");
    }
    @FXML
    private void actionAnnuler(){
        formactio.cancelling(etagere);
    }

    private void loadModData(ArrayList<String> data){
        if (data != null) {
            if (data.size() > 0) {
                code.setText(data.get(0));
                intitule.setText(data.get(1));
                D_CC.increment(Integer.parseInt(data.get(2)));
                D_TP.increment(Integer.parseInt(data.get(3)));
                D_TPE.increment(Integer.parseInt(data.get(4)));
                D_EE.increment(Integer.parseInt(data.get(5)));
                P_CC.setValue(Double.parseDouble(data.get(6)));
                valeurPCC.setText(P_CC.getValue()+"%");
                P_TP.setValue(Double.parseDouble(data.get(7)));
                valeurPTP.setText(P_TP.getValue()+"%");
                P_TPE.setValue(Double.parseDouble(data.get(8)));
                valeurPTPE.setText(P_TPE.getValue()+"%");
                P_EE.setValue(Double.parseDouble(data.get(9)));
                valeurPEE.setText(P_EE.getValue()+"%");
                credits.increment(Integer.parseInt(data.get(10)));
                code.setDisable(true);
                Valider.setText("Modifier");
                Annuler.setText("Supprimer");
                for (int i = 0; i < badNodes.size(); i++)
                    badNodes.set(i, null);
            }
        }
    }
}
