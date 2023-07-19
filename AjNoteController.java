package sample;

import com.jfoenix.controls.JFXComboBox;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AjNoteController {
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private AnchorPane ancetre;
    @FXML
    private VBox etagere;
    @FXML
    private Pane titre, titre1, titre2;
    @FXML
    private GridPane grille1, grille2;
    @FXML
    private Label Valider, Annuler, LabelEtudiantChoisi;
    @FXML
    private HBox zoneBoutton, infosElu;
    @FXML
    private Spinner<Double> NoteCC, NoteTP, NoteTPE, NoteExam1, NoteExam2;
    @FXML
    private TableView<Integer> ListeEtudiants;
    @FXML
    private TableColumn<Integer, String> colonneMatricule, colonneNom, colonnePrenom;
    @FXML
    private JFXComboBox CodeUE;

    private EMMAnim anima;
    private EMModel modelo;
    private EMMActionForm formactio;
    private ArrayList<Node> badNodes;
    private ArrayList<String> infosUE, etudiantsSansNote, LMatricule, LNom, LPrenom;
    private double moyenne;

    @FXML
    private void initialize(){
        anima = new EMMAnim();
        modelo = new EMModel();
        formactio = new EMMActionForm();
        badNodes = formactio.loadingBadNodes(etagere);
        LPrenom = new ArrayList<>();
        LNom = new ArrayList<>();
        LMatricule = new ArrayList<>();

        for (Node N : etagere.getChildren()) {
            if (N.getClass().equals(Pane.class))
                anima.decroissance(N, 0);
            else
                anima.decroissance(N, 2);
        }
        etagere.getChildren().removeAll(ListeEtudiants, infosElu);
        titre.setScaleX(1);
        SequentialTransition st = anima.formShowTime(titre1, "Choix des Références", grille1, zoneBoutton, 500, true);
        SequentialTransition st2 = anima.formShowTime(titre2, "Remplissage des Notes", grille2, null, 500, true);
        SequentialTransition sAnim = new SequentialTransition(st, st2);
        sAnim.play();

        CodeUE.getItems().addAll(modelo.getSuggestions("UE", "Code", null));
        CodeUE.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                infosUE = modelo.getInformations("UE", "Code", newVal.toString());
                NoteCC.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Integer.parseInt(infosUE.get(3)), 0));
                NoteTP.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Integer.parseInt(infosUE.get(4)), 0));
                NoteTPE.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Integer.parseInt(infosUE.get(5)), 0));
                NoteExam1.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Integer.parseInt(infosUE.get(6)), 0));
                NoteExam2.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Integer.parseInt(infosUE.get(6)), 0));
                etudiantsSansNote = modelo.listeEtudiantSansNote(newVal.toString());

                formactio.loadingContentsInTableView(ListeEtudiants, new TableColumn[]{colonneMatricule, colonneNom, colonnePrenom}, new ArrayList[]{LMatricule, LNom, LPrenom}, etudiantsSansNote, 0);

                if (!etagere.getChildren().contains(ListeEtudiants) && !etagere.getChildren().contains(infosElu)) {
                    int idx = etagere.getChildren().indexOf(grille1);
                    etagere.getChildren().add(idx + 1, ListeEtudiants);
                    etagere.getChildren().add(idx + 2, infosElu);
                    SequentialTransition seqAnim = new SequentialTransition(anima.croissance(ListeEtudiants, 1, 2, 500), anima.croissance(infosElu, 1, 2, 500));
                    seqAnim.play();
                }
                badNodes = formactio.outOfBadNodes(CodeUE, badNodes);
            }
        });

        ListeEtudiants.setOnMouseClicked(e -> {
           int idx = ListeEtudiants.getSelectionModel().getFocusedIndex();
           String str = LMatricule.get(idx)+" "+LPrenom.get(idx)+" "+LNom.get(idx);
           LabelEtudiantChoisi.setText(str);
           badNodes = formactio.outOfBadNodes(ListeEtudiants, badNodes);
        });

    }

    @FXML
    private void actionValider(){
        moyenne = 0.0;
        boolean toutEstOk = formactio.verificationNivelle(0, 2, badNodes);
        if (toutEstOk){
            // Calcul de la moyenne
            moyenne = (NoteCC.getValue() / Integer.parseInt(infosUE.get(3))) * Double.parseDouble(infosUE.get(7))
                + (NoteTP.getValue() / Integer.parseInt(infosUE.get(4))) * Double.parseDouble(infosUE.get(8))
                + (NoteTPE.getValue() / Integer.parseInt(infosUE.get(5))) * Double.parseDouble(infosUE.get(9)) ;
            moyenne += (NoteExam2.getValue() > 0) ? (NoteExam2.getValue() / Integer.parseInt(infosUE.get(6))) * Double.parseDouble(infosUE.get(10)) : (NoteExam1.getValue() / Integer.parseInt(infosUE.get(6))) * Double.parseDouble(infosUE.get(10));
            moyenne *= 0.2;
            modelo.ajouterNotes(CodeUE.getValue().toString(), LabelEtudiantChoisi.getText().split(" ")[0], NoteCC.getValue(), NoteTP.getValue(), NoteTPE.getValue(), NoteExam1.getValue(), NoteExam2.getValue(), moyenne);
            anima.infoBoxShow((ArrayList<Object>) ancetre.getUserData(), "Information (Succès)", "Création éffective de nouvelles notes pour l'étudiant choisi.");
            actionAnnuler();
        }else
            anima.infoBoxShow((ArrayList<Object>) ancetre.getUserData(), "Information  (Erreur)", "Certains champs n'ont pas été correcttement rempli");
    }

    @FXML
    private void actionAnnuler(){
        formactio.cancelling(etagere);
    }
}
