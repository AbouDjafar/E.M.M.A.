package sample;

import java.sql.*;
import java.util.ArrayList;

public class EMModel {
    private Connection connector;

    public EMModel() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String dbURL = "jdbc:sqlite:"+System.getProperty("user.dir")+"/EMMA.db";
        try {
            connector = DriverManager.getConnection(dbURL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void fermerLaConnexion(){
        try {
            connector.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected boolean ajoutPersonne(long id, String nom, String pNom, String sexe, String dateNais, String lieuNais) throws SQLException {
        String codeSQL = "INSERT INTO Personne VALUES ("+id+",'"+nom+"','"+pNom+"','"+sexe+"','"+dateNais+"','"+lieuNais+"')";
        Statement state = connector.createStatement();
        state.execute(codeSQL);
        state.close();
        return true;
    }

    protected boolean ajoutEtudiant(String matricule, long idPersonne, String cycle, String niv, String dept, String filiere, int credits) throws SQLException {
        String codeSQL = "INSERT INTO Etudiant VALUES ("+idPersonne+",'"+matricule+"','"+dept+"','"+filiere+"','"+cycle+"','"+niv+"',"+credits+")";
        Statement state = connector.createStatement();
        state.execute(codeSQL);
        state.close();
        return true;
    }

    protected  boolean ajoutEnseignant(long id, long idPersonne, String grade, String dept) throws SQLException {
        String codeSQL = "INSERT INTO Enseignant VALUES ("+id+","+idPersonne+",'"+grade+"','"+dept+"')";
        Statement state = connector.createStatement();
        state.execute(codeSQL);
        state.close();
        return true;
    }

    protected boolean ajoutUE(String code, String intitule, int DenominateurCC, int DenominateurTP, int DenominateurTPE, int DenominateurEE, int PourcentageCC, int PourcentageTP, int PourcentageTPE, int PourcentageEE, int credits){
        String codeSQL = "INSERT INTO UE VALUES ('"+code+"','"+intitule+"',"+DenominateurCC+","+DenominateurTP+","+DenominateurTPE+","+DenominateurEE+","+PourcentageCC+","+PourcentageTP+","+PourcentageTPE+","+PourcentageEE+","+credits+")";
        try {
            Statement state = connector.createStatement();
            state.execute(codeSQL);
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    protected boolean inscription(String UE, String IDEnseignant, String MatriculeEtudiant, String anneeAcademique){
        long idInscription = System.currentTimeMillis();
        String codeSQL = (IDEnseignant == null) ? "INSERT INTO Inscription VALUES ("+idInscription+",'"+UE+"',NULL,'"+MatriculeEtudiant+"','"+anneeAcademique+"')" : "INSERT INTO Inscription VALUES ("+idInscription+",'"+UE+"','"+IDEnseignant+"',NULL,'"+anneeAcademique+"')";
        try {
            Statement state = connector.createStatement();
            state.execute(codeSQL);
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    protected boolean ajouterNotes(String codeUE, String matriculeEtudiant, double noteCC, double noteTP, double noteTPE, double noteEE1, double noteEE2, double moy){
        String id = ""+codeUE+matriculeEtudiant;
        String codeSQL = "INSERT INTO Note VALUES ('"+id+"','"+codeUE+"','"+matriculeEtudiant+"',"+noteCC+","+noteTP+","+noteTPE+","+noteEE1+","+noteEE2+","+moy+")";
        try {
            Statement state = connector.createStatement();
            state.execute(codeSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    protected ArrayList<String> getSuggestions(String table, String colonne, String filtre){
        ArrayList<String> S = new ArrayList<>();
        try {
            String reqSQL = "SELECT "+colonne+" FROM "+table;
            Statement state = connector.createStatement();
            ResultSet resultats;
            if (filtre == null || filtre.isEmpty()){
                resultats = state.executeQuery(reqSQL);
            }else {
                reqSQL += " WHERE "+colonne+" LIKE '%"+filtre+"%' ESCAPE '\\' LIMIT 0, 49999;";
                resultats = state.executeQuery(reqSQL);
            }
            while (resultats.next())
                S.add(resultats.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return S;
    }

    /*protected ArrayList<String> getSuggestionsEnhaced(String table, String colonne, String filtre, String tableBarage, String colonneBarage){
        ArrayList<String> S = new ArrayList<>();
        try {
            String reqSQL = "SELECT "+colonne+" FROM "+table;
            Statement state = connector.createStatement();
            ResultSet resultats;
            if (tableBarage != null && colonneBarage != null){
                reqSQL += " WHERE "+colonne+" NOT IN (SELECT "+tableBarage+"."+colonneBarage+" FROM "+tableBarage+") AND ";
            }
            if (filtre == null || filtre.isEmpty()){
                if (tableBarage != null && colonneBarage != null)
                    reqSQL = reqSQL.replace("AND", "");
                resultats = state.executeQuery(reqSQL);
            }else {
                if (tableBarage == null || colonneBarage == null)
                    reqSQL += " WHERE ";
                reqSQL += colonne+" LIKE '%"+filtre+"%' ESCAPE '\\' LIMIT 0, 49999";
                resultats = state.executeQuery(reqSQL);
            }
            while (resultats.next())
                S.add(resultats.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return S;
    }*/

    protected ArrayList<String> getSuggestionNoms(String table, String colonne, String filtre, String tableBarage, String colonneBarage, String filtreBarage, String valeurFiltreBarage){
        // On recherche des Noms, Prenoms dans Personne et une certaine colonne d'une certaine table selon un filtre et restreint des données "barage"
        // On renvoit en sortie un arrayList selon leque aux index pairs on a les valeurs de la colonne et aux index impairs les valeurs de Prenom et Nom
        ArrayList<String> S = new ArrayList<>();
        try {
            String reqSQL = "SELECT "+table+"."+colonne+",Nom,Prenom FROM Personne, "+table+" WHERE "+table+".Identite = Personne.ID ";
            Statement state = connector.createStatement();
            ResultSet resultats;
            if (filtre != null && !filtre.isEmpty()){
                reqSQL += "AND (Personne.Nom LIKE '%"+filtre+"%' ESCAPE '\\' OR Personne.Prenom LIKE '%"+filtre+"%' ESCAPE '\\' OR "+table+"."+colonne+" LIKE '%"+filtre+"%' ESCAPE '\\' ) ";
            }
            if (tableBarage != null && colonneBarage != null)
                reqSQL += " AND "+table+"."+colonne+" NOT IN (SELECT "+tableBarage+"."+colonneBarage+" FROM "+tableBarage+" WHERE "+tableBarage+"."+filtreBarage+" = '"+valeurFiltreBarage+"' AND "+tableBarage+"."+colonneBarage+" IS NOT NULL) ";
            resultats = state.executeQuery(reqSQL);

            while (resultats.next()) {
                S.add(resultats.getString(1));
                S.add(resultats.getString(2) + " " + resultats.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return S;
    }

    protected ArrayList<String> getInformations(String table, String ColonneFiltre, String valeurFiltre){
        ArrayList<String> S = new ArrayList<>();
        if ((table != null || !table.isEmpty()) && (ColonneFiltre != null || !ColonneFiltre.isEmpty())){
            String codeSQL = "SELECT * FROM "+table+" WHERE "+ColonneFiltre+"='"+valeurFiltre+"'";
            try {
                Statement state = connector.createStatement();
                ResultSet resultats = state.executeQuery(codeSQL);
                ResultSetMetaData meta = resultats.getMetaData();
                S.add(""+meta.getColumnCount()); // L'index 0 nous renseignera juste sur le nombre de colonnes afin de savoir transformer ce vecteur en matrice plus tard
                while (resultats.next()) {
                    for (int i = 0; i < meta.getColumnCount(); i++)
                        S.add(resultats.getString(i+1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return S;
    }

    protected ArrayList<String> listeEtudiantSansNote(String CodeUE){
        ArrayList<String> S = new ArrayList<>();
        String codeSQL = "SELECT Matricule,Nom,Prenom FROM Etudiant,Personne,Inscription "+
        "WHERE Matricule = Inscription.Etudiant AND Personne.ID = Etudiant.Identite "+
        "AND Inscription.UE = '"+CodeUE+"' AND Etudiant.Matricule NOT IN (SELECT Note.Etudiant FROM Note WHERE UE = '"+CodeUE+"')";
        try {
            Statement state = connector.createStatement();
            ResultSet resultats = state.executeQuery(codeSQL);
            ResultSetMetaData meta = resultats.getMetaData();
            S.add(""+meta.getColumnCount()); // L'index 0 nous renseignera juste sur le nombre de colonnes afin de savoir transformer ce vecteur en matrice plus tard
            while (resultats.next()) {
                for (int i = 0; i < meta.getColumnCount(); i++)
                    S.add(resultats.getString(i+1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return S;
    }

    protected ArrayList<String> getSuggestionsFromMultiColum(String table, String[] colonnes, String filtre){
        ArrayList<String> S = new ArrayList<>();
        StringBuilder codeSQL = new StringBuilder("SELECT ");
        for (String str : colonnes)
            codeSQL.append(str).append(",");
        codeSQL.deleteCharAt(codeSQL.lastIndexOf(","));
        codeSQL.append(" FROM "+table);
        if (filtre != null){
            codeSQL.append(" WHERE ");
            for (int i = 0; i < colonnes.length; i++) {
                if (i == colonnes.length - 1)
                    codeSQL.append(table+"."+colonnes[i] + " LIKE '%" + filtre + "%' ESCAPE '\\' LIMIT 0, 49999;");
                else
                    codeSQL.append(table+"."+colonnes[i] + " LIKE '%" + filtre + "%' ESCAPE '\\' OR ");
            }
        }
        try {
            Statement state = connector.createStatement();
            ResultSet resultats = state.executeQuery(String.valueOf(codeSQL));
            while (resultats.next()){
                for (int i = 1; i <= colonnes.length; i++)
                    S.add(resultats.getString(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return S;
    }

    protected ArrayList<String> appelGeneral(String table, String[] colonnes, String filtre, boolean deviant){
        ArrayList<String> S = new ArrayList<>();
        StringBuilder codeSQL = new StringBuilder("SELECT ");
        for (String str : colonnes)
            codeSQL.append(str).append(",");
        codeSQL.deleteCharAt(codeSQL.lastIndexOf(","));
        codeSQL.append(" FROM ").append(table);
        if (deviant)
            codeSQL.append(" INNER JOIN Personne ON Personne.ID = ").append(table).append(".Identite");
        if (filtre != null){
            codeSQL.append(" WHERE ");
            for (String str : colonnes)
                codeSQL.append(str).append(" LIKE '%").append(filtre).append("%' ESCAPE '\\' OR ");
            codeSQL.deleteCharAt(codeSQL.lastIndexOf("R")); codeSQL.deleteCharAt(codeSQL.lastIndexOf("O"));
            codeSQL.append(" LIMIT 0, 49999");
        }
        try {
            Statement state = connector.createStatement();
            ResultSet resultats = state.executeQuery(codeSQL.toString());
            ResultSetMetaData meta = resultats.getMetaData();
            while (resultats.next()){
                for (int i = 0; i < meta.getColumnCount(); i++)
                    S.add(resultats.getString(i+1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return S;
    }

    protected ArrayList<String> procesVerbalUE(String codeUE, String filtre){
        ArrayList<String> S = new ArrayList<>();
        String codeSQL = "SELECT Matricule, Nom, Prenom, CC, TP, TPE, Session1_EE, Session2_EE, Moyenne " +
                "FROM Etudiant, Personne, Note WHERE Etudiant.Identite = Personne.ID AND Note.Etudiant = Etudiant.Matricule AND Note.UE = '"+codeUE+"'";
        if (filtre != null)
            codeSQL += " AND (Matricule LIKE '%"+filtre+"%' ESCAPE '\\' OR Nom LIKE '%"+filtre+"%' ESCAPE '\\' OR Prenom LIKE '%"+filtre+"%' ESCAPE '\\' OR CC LIKE '%"+filtre+"%' ESCAPE '\\' OR TP LIKE '%"+filtre+"%' ESCAPE '\\' OR TPE LIKE '%"+filtre+"%' ESCAPE '\\' OR Session1_EE LIKE '%"+filtre+"%' ESCAPE '\\' OR Session2_EE LIKE '%"+filtre+"%' ESCAPE '\\')";
        codeSQL += "GROUP BY Nom ORDER BY Moyenne DESC ;";
        try {
            Statement state = connector.createStatement();
            ResultSet resultats = state.executeQuery(codeSQL);
            ResultSetMetaData meta = resultats.getMetaData();
            while (resultats.next()){
                for (int i = 0; i < meta.getColumnCount(); i++)
                    S.add(resultats.getString(i+1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return S;
    }

    protected ArrayList<String> getAcademicYears(){
        ArrayList<String> S = new ArrayList<>();
        try {
            Statement state = connector.createStatement();
            ResultSet resultats = state.executeQuery("SELECT DISTINCT AnneeAcademique FROM Inscription");
            while (resultats.next())
                S.add(resultats.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return S;
    }

    protected void updatePersonne(long id, String nom, String prenom, String sexe, String dateN, String lieuN){
        String codeSQL = "UPDATE Personne SET Nom = '"+nom+"', Prenom = '"+prenom+"', Sexe = '"+sexe+"', DateNaissance = '"+dateN+"', LieuNaissance = '"+lieuN+"' WHERE ID = "+id;
        try {
            Statement state = connector.createStatement();
            state.execute(codeSQL);
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void updateEtudiant(String matricule, String dept, String filiere, String cycle, String niveau, int credits){
        String codeSQL = "UPDATE Etudiant SET Departement = '"+dept+"', Filiere = '"+filiere+"', Cycle = '"+cycle+"', Niveau = '"+niveau+"', Credits = "+credits+" WHERE Matricule = '"+matricule+"'";
        try {
            Statement state = connector.createStatement();
            state.execute(codeSQL);
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void updateEnseignant(String grade, String dept, long id){
        String codeSQL = "UPDATE Enseignant SET Grade = '"+grade+"', Departement = '"+dept+"' WHERE Identite = "+id;
        try {
            Statement state = connector.createStatement();
            state.execute(codeSQL);
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void updateUE(String code, String intitule, int dcc, int dtp, int dtpe, int dee, double pcc, double ptp, double ptpe, double pee, int credit){
        String codeSQL = "UPDATE UE SET Intitule = '"+intitule+"', D_CC = "+dcc+", D_TP = "+dtp+", D_TPE = "+dtpe+", D_EE = "+dee+", P_CC = "+pcc+", P_TP = "+ptp+", P_TPE = "+ptpe+", P_EE = "+pee+" WHERE Code = '"+code+"'";
        try {
            Statement state = connector.createStatement();
            state.execute(codeSQL);
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void updateNote(String codeUE, String matricule, double cc, double tp, double tpe, double s1, double s2, double moyenne){
        double moy = 0;
        String codeSQL = "UPDATE Note SET CC = "+cc+", TP = "+tp+", TPE = "+tpe+", Session1_EE = "+s1+", Session2_EE = "+s2+", Moyenne = "+moyenne+" WHERE UE = '"+codeUE+"' AND Etudiant = '"+matricule+"'";
        try {
            Statement state = connector.createStatement();
            state.execute(codeSQL);
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void suppression(String table, String colonneClePrimaire, String valeurClePrimaire){
        String codeSQL = "DELETE FROM "+table+" WHERE "+colonneClePrimaire+" = ";
        codeSQL += (table.equals("Enseignant")) ? Long.parseLong(valeurClePrimaire) : "'"+valeurClePrimaire+"'";
        try {
            Statement state = connector.createStatement();
            state.execute(codeSQL);
            state.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
