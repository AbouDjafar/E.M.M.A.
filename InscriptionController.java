package sample;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.ResourceBundle;

public class InscriptionController {
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private AnchorPane ancetre;
    @FXML
    private VBox etagere;
    @FXML
    private Pane titre, titre1, titre2, titre3;
    @FXML
    private GridPane grille1, grille2, grille3;
    @FXML
    private HBox zoneBoutton, zoneBoutonsRadio;
    @FXML
    private JFXRadioButton AA1, AA2;
    @FXML
    private Label Valider, Annuler;

    @FXML
    private JFXComboBox fonction, codeUE;
    @FXML
    private JFXTextField matriculeEtudiant, nomEnseignant;

    private EMMAnim anima;
    private EMModel modelo;
    private EMMActionForm formactio;
    private ArrayList<Node> badNodes;
    private ArrayList<String> idEnseignant;

    @FXML
    private void initialize(){
        anima = new EMMAnim();
        modelo = new EMModel();
        formactio = new EMMActionForm();
        badNodes = new ArrayList<>();
        idEnseignant = new ArrayList<>();
        int anneeActuelle = LocalDate.now().getYear();
        AA1.setText((anneeActuelle - 1)+"-"+anneeActuelle);
        AA2.setText(anneeActuelle+"-"+(anneeActuelle + 1));

        codeUE.getItems().addAll(modelo.getSuggestions("UE", "Code", null));
        fonction.getItems().addAll("Etudiant", "Enseignant");
        for (Node N : etagere.getChildren()) {
            if (N.getClass().equals(Pane.class))
                anima.decroissance(N, 0);
            else {
                anima.decroissance(N, 2);
                if (N.getClass().equals(GridPane.class)){
                    for (Node No : ((GridPane)N).getChildren()){
                        if (No.getClass().equals(VBox.class)){
                            badNodes.add(((VBox)No).getChildren().get(0));
                        }
                    }
                }
            }
        }
        etagere.getChildren().removeAll(titre2, titre3, grille2, grille3);
        titre.setScaleX(1);
        SequentialTransition sAnim = anima.formShowTime(titre1, "Informations de Base", grille1, zoneBoutton, 500, true);
        sAnim.play();

        matriculeEtudiant.setOnKeyReleased(e -> {
            if (matriculeEtudiant.getText().length() > 0) {
                //ArrayList<String> suggestions = modelo.getSuggestions("Etudiant", "Matricule", matriculeEtudiant.getText());
                ArrayList<String> suggestions = modelo.getSuggestionNoms("Etudiant", "Matricule", matriculeEtudiant.getText(), "Inscription", "Etudiant", "UE", codeUE.getValue().toString());
                ArrayList<String> tmp = new ArrayList<>();
                for (int i = 0; i < suggestions.size() / 2; i++)
                    tmp.add(suggestions.get(2 * i) +" - "+ suggestions.get(2 * i + 1));
                formactio.showAutoCompletion(matriculeEtudiant, ancetre, tmp);
            }
        });
        matriculeEtudiant.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(oldVal)) {
                if (newVal.contains(" - "))
                    matriculeEtudiant.setText(newVal.split(" - ")[0]);
                if (formactio.verificationOnEvent(matriculeEtudiant, "^[0-9]{2}[A-Z]\\d{4}[A-Z]{2}$", "Matricule Invalide", "Matricule valide") && badNodes.contains(matriculeEtudiant)) {
                    int idx = badNodes.indexOf(matriculeEtudiant);
                    badNodes.set(idx, null);
                }
            }
            if (newVal.length() < 1)
                formactio.hideAutoCompletion(ancetre);
        });

        nomEnseignant.setOnKeyReleased(e -> {
            if (nomEnseignant.getText().length() > 0) {
                ArrayList<String> sugg = modelo.getSuggestionNoms("Enseignant", "ID", nomEnseignant.getText(), "Inscription", "Enseignant", "UE", codeUE.getValue().toString());
                ArrayList<String> suggestions = new ArrayList<>();
                idEnseignant.addAll(sugg);
                for (int i = 0; i < sugg.size() / 2; i++) {
                    suggestions.add(sugg.get(2 * i + 1));
                }
                formactio.showAutoCompletion(nomEnseignant, ancetre, suggestions);
            }
        });
        nomEnseignant.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(oldVal)) {
                int idEns = idEnseignant.indexOf(nomEnseignant.getText());
                if (idEns < 0){
                    formactio.msgBelow((VBox) nomEnseignant.getParent(), "aucun enseignant de ce nom", "red");
                }else {
                    if (badNodes.contains(nomEnseignant)) { // Si le nom est bien parmi ceux proposés
                        int idx = badNodes.indexOf(nomEnseignant);
                        badNodes.set(idx, null);
                    }
                    formactio.msgBelow((VBox) nomEnseignant.getParent(), "nom valide", "green");
                }
            }
            if (newVal.length() < 1)
                formactio.hideAutoCompletion(ancetre);
        });

        codeUE.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (oldVal){ // lors de la perte du focus
                if (codeUE.getValue() != null && badNodes.contains(codeUE)){
                    int idx = badNodes.indexOf(codeUE);
                    badNodes.set(idx, null);
                }
            }
        });

        fonction.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (oldVal){ // lors de la perte du focus
                if (fonction.getValue() != null && badNodes.contains(fonction)){
                    int idx = badNodes.indexOf(fonction);
                    badNodes.set(idx, null);
                }
            }
        });

        ((AnchorPane)etagere.getParent()).setOnMouseClicked(e -> {
            formactio.hideAutoCompletion((AnchorPane) etagere.getParent());
        });
    }

    @FXML
    private void actionChoixFonction(){
        int idx = etagere.getChildren().indexOf(grille1);
        SequentialTransition sAnim = new SequentialTransition();
        if (fonction.getValue() != null) {
            if (fonction.getValue().equals("Etudiant")) {
                if (etagere.getChildren().contains(grille3)) {
                    etagere.getChildren().removeAll(titre3, grille3);
                    anima.decroissance(grille3, 3);
                    anima.decroissance(titre3, 0);
                    titre3.getChildren().clear();
                }
                if (!etagere.getChildren().contains(titre2) && !etagere.getChildren().contains(grille2)) {
                    etagere.getChildren().add(idx + 1, titre2);
                    etagere.getChildren().add(idx + 2, grille2);
                    sAnim.getChildren().addAll(anima.formShowTime(titre2, "Etudiant", grille2, null, 500, true));
                }
            } else {
                if (etagere.getChildren().contains(grille2)) {
                    etagere.getChildren().removeAll(titre2, grille2);
                    anima.decroissance(grille2, 3);
                    anima.decroissance(titre2, 0);
                    titre2.getChildren().clear();
                }
                if (!etagere.getChildren().contains(titre3) && !etagere.getChildren().contains(grille3)) {
                    etagere.getChildren().add(idx + 1, titre3);
                    etagere.getChildren().add(idx + 2, grille3);
                    sAnim.getChildren().addAll(anima.formShowTime(titre3, "Enseignant", grille3, null, 500, true));
                }
            }
            sAnim.play();
        }
    }

    @FXML
    private void actionValider(){
        boolean toutEstOk = formactio.verificationNivelle(0, 2, badNodes);
        if (toutEstOk){
            if (fonction.getValue().equals("Etudiant")){
                if (formactio.verificationNivelle(2, 1, badNodes)){
                    modelo.inscription(codeUE.getValue().toString(), null, matriculeEtudiant.getText(), (AA1.isArmed()) ? AA1.getText() : AA2.getText());
                    anima.infoBoxShow((ArrayList<Object>)ancetre.getUserData(), "Information (Succès)", "Etudiant inscrit avec succès à une unité d'enseignement");
                }
            }else {
                if (formactio.verificationNivelle(3, 1, badNodes)){
                    int idx = idEnseignant.indexOf(nomEnseignant.getText());
                    ++idx;
                    modelo.inscription(codeUE.getValue().toString(), idEnseignant.get(idx - 2), null, (AA1.isArmed()) ? AA1.getText() : AA2.getText());
                    anima.infoBoxShow((ArrayList<Object>)ancetre.getUserData(), "Information Succès", "Enseiganat inscrit avec succès à l'unité d'enseignement");
                }
            }
            actionAnnuler();
        }else
            anima.infoBoxShow((ArrayList<Object>)ancetre.getUserData(), "Information (Echec)", "Certaines des informations renseignées ne sont pas correctes");
    }

    @FXML
    private void actionAnnuler(){
        formactio.cancelling(etagere);
    }
}
