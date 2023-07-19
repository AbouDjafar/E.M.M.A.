package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class RechercheController {
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private VBox etagere;
    @FXML
    private Pane titre, titre1, titre2;
    @FXML
    private VBox VB1;
    @FXML
    private GridPane grille1, grille2, grille3, sousFiltres;
    @FXML
    private JFXTextField motRecherche;
    @FXML
    private JFXButton btnRechercher;
    @FXML
    private TableView<Integer> resultsTable;
    @FXML
    private JFXComboBox<String> categorie;

    private EMMAnim anima;
    private EMModel modelo;
    private EMMActionForm formaxio;
    private ArrayList<String> suggestions;

    @FXML
    private void initialize() {
        anima = new EMMAnim();
        modelo = new EMModel();
        formaxio = new EMMActionForm();
        suggestions = new ArrayList<>();
//------- (Déclaration des sous filtres) --------------
        JFXComboBox<String> cycle = new JFXComboBox<>(), niveau = new JFXComboBox<>(), departement = new JFXComboBox<>(), filiere = new JFXComboBox<>();
        JFXComboBox<String> grade = new JFXComboBox<>(); // grade enseignant
        Spinner<Integer> credits = new Spinner<>(); // nombre de credits de l'UE
        JFXComboBox<String> UE = new JFXComboBox<>(); // UE à afficher les Notes
        JFXTextField matricule = new JFXTextField(); // Matricule des étudiants inscrits à l'UE
        matricule.setPromptText("Matricule de l'étudiant inscrit");
        JFXComboBox<String> anneeAcademique = new JFXComboBox<>();
        anneeAcademique.getItems().addAll(modelo.getAcademicYears());
//-----------------------------------------------------
        for (Node N : etagere.getChildren()) {
            if (N.getClass().equals(Pane.class))
                anima.decroissance(N, 0);
            else
                anima.decroissance(N, 2);
        }
        titre.setScaleX(1);
        SequentialTransition sAnim = new SequentialTransition();
        Animation st1, st2, st3;
        st1 = anima.formShowTime(titre1, "Zone de Recherche", grille2, null, 500, true);
        st2 = anima.croissance(grille1, 1, 2, 500);
        st3 = anima.formShowTime(titre2, "Listage des résultats", grille3, null, 500, true);
        sAnim.getChildren().addAll(st1, st2, st3);
        sAnim.play();
        anima.croissance(VB1, 1, 2, 500).play();

        categorie.getItems().addAll("Etudiant", "Enseignant", "UE", "Note");
        categorie.valueProperty().addListener((obs, oldVal, newVal) -> {
            resultsTable.getColumns().clear();
            if (newVal != null){
                motRecherche.setDisable(false);
                btnRechercher.setDisable(false);
                // Chargement des filtres
                sousFiltres.getChildren().clear();
                if (newVal.equals("Note")){ //TODO
                    sousFiltres.add(new Label("UE"), 0, 0);
                    sousFiltres.add(UE, 1, 0);
                    sousFiltres.add(new Label("Année Académique"), 0, 1);
                    sousFiltres.add(anneeAcademique, 1, 1);
                    UE.getItems().addAll(modelo.getSuggestions("UE", "Code", null));
                    UE.setOnAction(e -> { actionBtnRechercher();});
                }
            }
            actionBtnRechercher();
        });

        motRecherche.setOnKeyReleased(e -> {
            if (categorie.getValue().equals("Etudiant")) {
                suggestions.clear();
                // Remplissage des suggestions et eventuel data parsing
                StringBuilder tmp = new StringBuilder();
                for (String str : modelo.getSuggestionNoms("Etudiant", "Matricule", motRecherche.getText(), null, null, null, null)){
                    tmp.append(str);
                    if (!str.matches("^[0-9]{2}[A-Z]\\d{4}[A-Z]{2}$")) {
                        suggestions.add(tmp.toString());
                        tmp = new StringBuilder();
                    }else
                        tmp.append(" - ");
                }
                formaxio.showAutoCompletion(motRecherche, (AnchorPane) etagere.getParent(), suggestions);
            }

            if (categorie.getValue().equals("Enseignant")) {
                suggestions.clear();
                // Remplissage des suggestions et eventuel data parsing
                int i = 0;
                StringBuilder tmp = new StringBuilder();
                for (String str : modelo.getSuggestionNoms("Enseignant", "Grade", motRecherche.getText(), null, null, null, null)){
                    i = (i + 1) % 2;
                    tmp.append(str);
                    if (i == 0) {
                        suggestions.add(tmp.toString());
                        tmp = new StringBuilder();
                    }else
                        tmp.append(" - ");
                }
                formaxio.showAutoCompletion(motRecherche, (AnchorPane) etagere.getParent(), suggestions);
            }

            if (categorie.getValue().equals("UE")) {
                suggestions.clear();
                // Remplissage des suggestions et eventuel data parsing
                StringBuilder tmp = new StringBuilder();
                for (String str : modelo.getSuggestionsFromMultiColum("UE", new String[]{"Code", "Intitule"}, motRecherche.getText())){
                    tmp.append(str);
                    if (!str.matches("^[A-Z]{3}\\d{3}$")) {
                        suggestions.add(tmp.toString());
                        tmp = new StringBuilder();
                    }else
                        tmp.append(" - ");
                }
                formaxio.showAutoCompletion(motRecherche, (AnchorPane) etagere.getParent(), suggestions);
            }

            if (categorie.getValue().equals("Note")){ // TODO: si c'est cohérent avec l'ensemble, faire une autocompletion
                suggestions.clear();
            }
        });

        (etagere.getParent()).setOnMouseClicked(e -> {
            formaxio.hideAutoCompletion((AnchorPane) etagere.getParent());
        });

    }

    @FXML
    private void actionBtnRechercher(){
        if (categorie.getValue().equals("Etudiant")){
            peuplementTableView(new String[]{"Matricule", "Nom", "Prenom", "Sexe", "DateNaissance", "LieuNaissance", "Departement", "Filiere", "Cycle", "Niveau", "Credits"}, "Etudiant", true);
        }

        if (categorie.getValue().equals("Enseignant")){
            peuplementTableView(new String[]{"Nom", "Prenom", "Sexe", "DateNaissance", "LieuNaissance", "Departement", "Grade"}, "Enseignant", true);
        }

        if (categorie.getValue().equals("UE")){
            peuplementTableView(new String[]{"Code", "Intitule", "Credit"}, "UE", false);
        }

        if (categorie.getValue().equals("Note")){
            JFXComboBox<String> codeUE = (JFXComboBox) sousFiltres.getChildren().get(1);
            if (codeUE != null)
                peuplementTableView(new String[]{"Matricule", "Nom", "Prenom", "CC", "TP", "TPE", "Session Normale", "Session de Rattrapage", "Moyenne"}, codeUE.getValue(), false);
            else
                resultsTable.getColumns().clear();
        }
    }

    private TableColumn<Integer, String>[] generateTableColumns(String[] tableColumnsID){
        TableColumn<Integer, String>[] TC = new TableColumn[tableColumnsID.length];
        for (int i = 0; i < TC.length; i++){
            TC[i] = new TableColumn<>(tableColumnsID[i]);
            TC[i].setId(tableColumnsID[i]);
        }
        return TC;
    }
    private void peuplementTableView(String[] colonnesTable, String table, boolean deviant) {
        // Colonnes == colonnes de la table
        // table == une des tables de la base de donnée
        // deviant si c'est une table liée à la table personne (Etudiant ou Enseignant)
        String filtrage = null;
        if (motRecherche.getText() != null) {
            if (motRecherche.getText().contains(" - "))
                filtrage = motRecherche.getText().split(" - ")[0];
            else
                filtrage = motRecherche.getText();
        }
        TableColumn<Integer, String>[] colonnes = generateTableColumns(colonnesTable);
        ArrayList<String>[] valeurColonnes = new ArrayList[colonnes.length];
        for (int i = 0; i < valeurColonnes.length; i++)
            valeurColonnes[i] = new ArrayList<>();
        if (colonnesTable.length > 0 && table != null && !table.isEmpty()) {
            ArrayList<String> sortieBD;
            if (table.matches("^[A-Z]{3}\\d{3}$")){
                sortieBD = modelo.procesVerbalUE(table, filtrage);
            }else {
                sortieBD = modelo.appelGeneral(table, colonnesTable, filtrage, deviant);
            }
            formaxio.loadingContentsInTableView(resultsTable, colonnes, valeurColonnes, sortieBD, 1);
        }else {
            System.out.println("fonction mal paramétré, vérifier les valeurs de params");
        }
    }
}
