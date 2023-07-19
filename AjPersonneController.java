package sample;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AjPersonneController {
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private AnchorPane encreEtagere;
    @FXML
    private VBox etagere;
    @FXML
    private HBox zoneBoutton;
    @FXML
    private Pane Titre, titreGrille, titreEtudiant, titreEnseignant;
    @FXML
    private GridPane Grille, blocEnseignant, blocEtudiant;
    @FXML
    private JFXComboBox<String> Sexe, Fonction, Cycle, Niveau, Departement, Filiere, Grade, DeptEnseignant;
    @FXML
    private JFXTextField Nom, Prenom, LieuN, Matricule;
    @FXML
    private Label Valider, Annuler, Generer;
    @FXML
    private JFXDatePicker DateNaissance;
    @FXML
    private Spinner<Integer> Credits;

    private String[] cycles = {"Licence", "Master"}, grades = {"Assistant", "Chargé de Cours", "Maître de Conférence", "Autre"}, departements = {"Biochimie", "Chimie", "Mathématiques et Informatique", "Sciences Physiques", "Sciences Biologiques"};
    private String[][] niveaux = {{"Licence 1", "Licence 2", "Licence 3"}, {"Master 1", "Master 2"}};
    private EMMAnim anima;
    private EMMActionForm formActio;
    private EMModel modelo;
    private String dateFormate;
    private ArrayList<Node> badNodes;
    private boolean toutEstOK;
    private ArrayList<String> modData;

    @FXML
    private void initialize(){
        anima = new EMMAnim();
        formActio = new EMMActionForm();
        modelo = new EMModel();
        toutEstOK = false;
        badNodes = new ArrayList<>();
        Credits.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 300, 0));;

        for (Node GP : etagere.getChildren()){ // Tous les noeuds non verifiés sont considérés comme mauvais
            if (GP.getClass().equals(GridPane.class)){
                for (Node N : ((GridPane)GP).getChildren()){
                    if (!N.getClass().equals(Label.class)) {
                        if (N.getClass().equals(VBox.class))
                            badNodes.add(((VBox)N).getChildren().get(0));
                        else
                            badNodes.add(N);
                    }
                }
            }
        }
        Sexe.getItems().addAll("Masculin", "Féminin");
        Fonction.getItems().addAll("Etudiant", "Enseignant");
        DeptEnseignant.getItems().addAll("Bio-Chimie", "Chimie", "Mathématiques et Informatique", "Sciences Physique", "Sciences Biologie");
        Cycle.getItems().addAll(cycles);
        Grade.getItems().addAll(grades);
        Departement.getItems().addAll(departements);

    //--------( Action lors de la saisie/selection de données ) --------------------
        formActio.verificationGeneraleComboBoxes(etagere, badNodes);

        Nom.setOnKeyTyped(e -> {
            if(formActio.verificationOnEvent(Nom, "^[A-za-z].{2,29}$", "Le nom saisi n'est pas valide", "Nom valide")){
                if (badNodes.contains(Nom)) {
                    int idx = badNodes.indexOf(Nom);
                    badNodes.set(idx, null);
                }
            }
        });
        Prenom.setOnKeyTyped(e -> {
            if(formActio.verificationOnEvent(Prenom, "^[A-za-z].{2,29}$", "Le Prénom saisi n'est pas valide", "Prénom valide")){
                if (badNodes.contains(Prenom)) {
                    int idx = badNodes.indexOf(Prenom);
                    badNodes.set(idx, null);
                }
            }
        });
        DateNaissance.setOnAction(e -> {
            if (DateNaissance.getValue() != null) {
                dateFormate = DateNaissance.getValue().getDayOfMonth() + "/" + DateNaissance.getValue().getMonthValue() + "/" + DateNaissance.getValue().getYear();
                if (badNodes.contains(DateNaissance)) {
                    int idx = badNodes.indexOf(DateNaissance);
                    badNodes.set(idx, null);
                    formActio.removeMsgBelow((VBox) DateNaissance.getParent());
                }
            }
        });
        LieuN.setOnKeyTyped(e -> {
            if(formActio.verificationOnEvent(LieuN, "^[A-za-z].{2,29}$", "Le lieu saisi n'est pas valide", "Lieu valide")){
                if (badNodes.contains(LieuN)) {
                    int idx = badNodes.indexOf(LieuN);
                    badNodes.set(idx, null);
                }
            }
        });
        Matricule.setOnKeyTyped(e -> {
            if(formActio.verificationOnEvent(Matricule, "^[0-9][0-9][A-Z]\\d{4}[A-Z]$", "Le matricule saisi n'est pas valide", "Matricule valide")){
                if (badNodes.contains(Matricule)) {
                    int idx = badNodes.indexOf(Matricule);
                    badNodes.set(idx, null);
                }
            }
        });
        Fonction.setOnAction(e -> {
            if (Fonction.getValue() != null) {
                actionChoixFonction();
                if (badNodes.contains(Fonction)) {
                    int idx = badNodes.indexOf(Fonction);
                    badNodes.set(idx, null);
                }
                formActio.removeMsgBelow((VBox) Fonction.getParent());
            }
        });
        Cycle.setOnAction(e -> {
            if (Cycle.getValue() != null) {
                actionChoixCycle();
                if (badNodes.contains(Cycle)) {
                    int idx = badNodes.indexOf(Cycle);
                    badNodes.set(idx, null);
                }
                formActio.removeMsgBelow((VBox) Cycle.getParent());
            }
        });
        Departement.setOnAction(e -> {
            if (Departement.getValue() != null) {
                actionChoixDepartement();
                if (badNodes.contains(Departement)) {
                    int idx = badNodes.indexOf(Departement);
                    badNodes.set(idx, null);
                }
                formActio.removeMsgBelow((VBox) Departement.getParent());
            }
        });
        Credits.setOnKeyReleased(e -> {
            if (Credits.getEditor().getText() != null && !Credits.getEditor().getText().isEmpty()){
                if (!Credits.editorProperty().getValue().getText().matches("^[0-9]+$")){
                    Credits.getEditor().setText("0");
                }
            }
        });


    //------- (Action l'ors de la pression des bouttons) ------------------
        Valider.setOnMousePressed(e -> {
            formActio.btnSkinSwitch(Valider, "IMG/v4/bouttonON.png");
        });
        Valider.setOnMouseReleased(e -> {
            formActio.btnSkinSwitch(Valider, "IMG/v4/bouttonOFF.png");
        });
        Annuler.setOnMousePressed(e -> {
            formActio.btnSkinSwitch(Annuler, "IMG/v4/bouttonON.png");
        });
        Annuler.setOnMouseReleased(e -> {
            formActio.btnSkinSwitch(Annuler, "IMG/v4/bouttonOFF.png");
        });
        Generer.setOnMousePressed(e -> {
            formActio.btnSkinSwitch(Generer, "IMG/v4/bouttonON.png");
        });
        Generer.setOnMouseReleased(e -> formActio.btnSkinSwitch(Generer, "IMG/v4/bouttonOFF.png"));
    //-----------------------------------------------------------------------
        for (Node N : etagere.getChildren()) {
            if (N.getClass().equals(Pane.class))
                N.setScaleX(0);
            else {
                N.setScaleX(0);
                N.setScaleY(0);
            }
        }
        Titre.setScaleX(1);
        etagere.getChildren().removeAll(blocEnseignant, blocEtudiant, titreEnseignant, titreEtudiant);
        SequentialTransition st = anima.formShowTime(titreGrille, "Informations Personnelles", Grille, zoneBoutton, 500, true);
        st.setOnFinished(e -> {
            if (((ArrayList) encreEtagere.getUserData()).size() > 3){ // Si on a recu des données pour une modif
                modData = (ArrayList<String>) ((ArrayList) encreEtagere.getUserData()).get(3);
                loadingModValues(modData);
                Valider.setText("Modifier");
                Annuler.setText("Supprimer");
                Annuler.setOnMouseClicked(ev -> {
                    if (Fonction.getValue().equals("Etudiant") || Fonction.getValue().equals("Enseignant")){
                        modelo.suppression("Personne", "ID", modData.get(0));
                        if (Fonction.getValue().equals("Etudiant"))
                            modelo.suppression(Fonction.getValue(), "Matricule", Matricule.getText());
                        else
                            modelo.suppression(Fonction.getValue(), "Identite", modData.get(0));
                    }
                    actionAnnuler();
                });
            }else {
                modData = null;
                System.out.println("modData vide!");
            }
        });
        st.play();
    }

    @FXML
    private void actionChoixFonction(){
        int idx = etagere.getChildren().indexOf(Grille);
        if (Fonction.getValue().equals("Etudiant")){
            if (etagere.getChildren().contains(blocEnseignant)) {
                anima.decroissance(blocEnseignant, 3);
                anima.decroissance(titreEnseignant, 0);
                titreEnseignant.getChildren().clear();
                etagere.getChildren().removeAll(blocEnseignant, titreEnseignant);
            }
            if (!etagere.getChildren().contains(blocEtudiant)) {
                etagere.getChildren().add(idx + 1, titreEtudiant);
                etagere.getChildren().add(idx + 2, blocEtudiant);
                anima.formShowTime(titreEtudiant, "Champs Etudiant", blocEtudiant, null, 500, true).play();
            }
        }else{
            if (etagere.getChildren().contains(blocEtudiant)) {
                anima.decroissance(blocEtudiant, 3);
                anima.decroissance(titreEtudiant, 0);
                titreEtudiant.getChildren().clear();
                etagere.getChildren().removeAll(blocEtudiant, titreEtudiant);
            }
            if (!etagere.getChildren().contains(blocEnseignant)) {
                etagere.getChildren().add(idx + 1, titreEnseignant);
                etagere.getChildren().add(idx + 2, blocEnseignant);
                anima.formShowTime(titreEnseignant, "Champs Enseignant", blocEnseignant, null, 500, true).play();
            }
        }
    }
    @FXML
    private void actionChoixCycle(){
        if (modData == null)
            Niveau.getItems().clear();
        if (Cycle.getValue().equals("Licence"))
            Niveau.getItems().addAll(niveaux[0]);
        else
            Niveau.getItems().addAll(niveaux[1]);
    }
    @FXML private void actionChoixDepartement(){
        if (encreEtagere.getUserData() == null)
            Filiere.getItems().clear();
        switch (Departement.getItems().indexOf(Departement.getValue())){
            case 0:
                Filiere.getItems().add("Biochimie");
                break;
            case 1:
                if (Cycle.getValue().equals("Licence"))
                    Filiere.getItems().add("Chimie Licence");
                else
                    Filiere.getItems().add("Chimie Master");
                break;
            case 2:
                if (Cycle.getValue().equals("Licence"))
                    Filiere.getItems().addAll("Mathématiques Appliquée", "Informatique Fondamentale");
                else {
                    if (Niveau.getValue().equals("Master 1"))
                        Filiere.getItems().addAll("Mathématiques Appliquée", "Informatique Fondamentale");
                    if (Niveau.getValue().equals("Master 2"))
                        Filiere.getItems().addAll("Mathématiques Appliquée++", "Génie Logiciel", "Réseaux", "Web Sémentique");
                }
                break;
            case 3:
                if (Cycle.getValue().equals("Licence"))
                    Filiere.getItems().add("Physique Licence");
                else
                    Filiere.getItems().add("Physique Master");
                break;
            case 4:
                if (Cycle.getValue().equals("Licence"))
                    Filiere.getItems().addAll("Science Biologie", "Science de la Terre");
                else
                    Filiere.getItems().addAll("Biologie Animale", "Biologie Végétale", "Science de la Terre++");
                break;
        }
    }
    @FXML
    private void actionValider() throws SQLException, ClassNotFoundException {
        toutEstOK = formActio.verificationNivelle(0, 6, badNodes);
        if (toutEstOK) {
            toutEstOK = formActio.verificationNivelle(6, 5, badNodes) || formActio.verificationNivelle(11, 2, badNodes);
            if (toutEstOK) {
                long id = System.currentTimeMillis();
                boolean op;
//---------------------- (Ajout des données )--------------------------------------------------------------------------------
                if (((ArrayList<Object>) encreEtagere.getUserData()).size() <= 3) {
                    op = modelo.ajoutPersonne(id, Nom.getText(), Prenom.getText(), Sexe.getValue(), dateFormate, LieuN.getText());
                    if (Fonction.getValue().equals("Etudiant")) {
                        op = modelo.ajoutEtudiant(Matricule.getText(), id, Cycle.getValue(), Niveau.getValue(), Departement.getValue(), Filiere.getValue().toString(), 0);
                    } else {
                        op = modelo.ajoutEnseignant(System.currentTimeMillis(), id, Grade.getValue().toString(), DeptEnseignant.getValue().toString());
                    }
                    anima.infoBoxShow((ArrayList<Object>) encreEtagere.getUserData(), "Information", "Ajout d'une nouvelle personne éffectuée avec succès");
                    actionAnnuler();
                }else {
//-------------------------- (Modification des données) ----------------------------------------------------------------------
                    long idPersonne = Long.parseLong(modData.get(0));
                    modelo.updatePersonne(idPersonne, Nom.getText(), Prenom.getText(), Sexe.getValue(), dateFormate, LieuN.getText());
                    if (Fonction.getValue().equals("Etudiant"))
                        modelo.updateEtudiant(Matricule.getText(), Departement.getValue(), Filiere.getValue(), Cycle.getValue(), Niveau.getValue(), Credits.getValue());
                    else
                        modelo.updateEnseignant(Grade.getValue(), DeptEnseignant.getValue(), Long.parseLong(modData.get(0)));
                    anima.infoBoxShow((ArrayList<Object>) encreEtagere.getUserData(), "Information", "Modification des données personnelles éffectuée avec succès");
                    actionAnnuler();
                }
            } else {
                System.out.println("Etudiant/Enseignant Nope, tout n'est pas OK\t"+toutEstOK);
                for (Node N : badNodes){
                    if (N != null){
                        formActio.msgBelow(((VBox)N.getParent()), "Ce champ doit être rempli", "red");
                    }
                }
            }
        }else {
            System.out.println("Personne Tout n'est pas ok \t" + toutEstOK);
            for (Node N : badNodes){
                if (N != null){
                    formActio.msgBelow(((VBox)N.getParent()), "Ce champ doit être rempli", "red");
                }
            }
        }
    }

    @FXML
    private void actionAnnuler(){
        formActio.cancelling(etagere);
        if (etagere.getChildren().contains(blocEtudiant))
            etagere.getChildren().removeAll(blocEtudiant, titreEtudiant);
        else
            etagere.getChildren().removeAll(blocEnseignant, titreEnseignant);
    }
    @FXML
    private void idGeneration(){
        Matricule.setText((int)(Math.random()* (LocalDate.now().getYear() - 2000))+""+(char)((int)(Math.random()*25)+65)+""+(int)(Math.random()*9999)+"FS");
        if (badNodes.contains(Matricule)) {
            badNodes.set(badNodes.indexOf(Matricule), null);
            formActio.removeMsgBelow((VBox)Matricule.getParent());
        }
    }

//************************ Si c'est pour une modif de données ************************************
    private void loadingModValues(ArrayList<String> data) {
        if (data != null) {
            if (data.size() > 8) { // Etudiant
                Matricule.setText(data.get(1));
                Nom.setText(data.get(2));
                Prenom.setText(data.get(3));
                Sexe.setValue(data.get(4));
                String[] laDateN = data.get(5).split("/");
                DateNaissance.setValue(LocalDate.of(Integer.parseInt(laDateN[2]), Integer.parseInt(laDateN[1]), Integer.parseInt(laDateN[0])));
                LieuN.setText(data.get(6));
                Fonction.setValue("Etudiant");
                Departement.setValue(data.get(7));
                Filiere.setValue(data.get(8));
                Cycle.setValue(data.get(9));
                Niveau.setValue(data.get(10));
                Credits.increment(Integer.parseInt(data.get(11)));
                Fonction.setValue("Etudiant");
                Credits.setDisable(false);
                Matricule.setDisable(true);
                Generer.setDisable(true);
                actionChoixDepartement();
                actionChoixCycle();
            } else { //Enseignant
                Nom.setText(data.get(1));
                Prenom.setText(data.get(2));
                Sexe.setValue(data.get(3));
                String[] laDateN = data.get(4).split("/");
                DateNaissance.setValue(LocalDate.of(Integer.parseInt(laDateN[2]), Integer.parseInt(laDateN[1]), Integer.parseInt(laDateN[0])));
                LieuN.setText(data.get(5));
                Fonction.setValue("Enseignant");
                Grade.setValue(data.get(7));
                DeptEnseignant.setValue(data.get(6));
            }
            Fonction.setDisable(true);
            actionChoixFonction();
            for (int i = 0; i < badNodes.size(); i++)
                badNodes.set(i, null);
        }else
            System.out.println("aucune donnée de modif recue");
    }

}
