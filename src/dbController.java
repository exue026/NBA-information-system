import java.sql.*;

public class dbController {

    private Connection conn;


    public dbController() {

        try {
            //establish connection to db
            String url = "jdbc:mysql://localhost:3306/library";
            conn = DriverManager.getConnection(url, "root", "Sperling5");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    void printTeams() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM teams");

            while (rs.next()) {
                System.out.println(rs.getString("team_id") + " | " + rs.getString("name"));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void printStats() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM stats");

            while (rs.next()) {
                System.out.println(rs.getString("team_id") + " | " + rs.getString("games_played") + " | "
                        + rs.getString("games_won") + " | " + rs.getString("games_lost") + " | " + rs.getString("win_percent")
                + " | " + rs.getString("avg_points"));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    String getTeamStat(String name, String typeStat) {
        String specific_stat = "";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT team_id FROM teams WHERE name = \"" + name + "\"");
            String team_id = "";
            while(rs.next()) {
                team_id = rs.getString("team_id");
            }
            rs = stmt.executeQuery("SELECT " + typeStat + " FROM stats WHERE team_id = \"" + team_id + "\"");
            while(rs.next()) {
                specific_stat = rs.getString(typeStat);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return specific_stat;

    }
    String getAllTeamStats(String name) {
        String allStats = "";
        try {
            allStats += getTeamStat(name, "games_played");
            allStats += "\n";
            allStats += getTeamStat(name, "games_won");
            allStats += "\n";
            allStats += getTeamStat(name, "games_lost");
            allStats += "\n";
            allStats += getTeamStat(name, "win_percent");
            allStats += "\n";
            allStats += getTeamStat(name, "avg_points");
            allStats += "\n";
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return allStats;
    }


    void updateTeamStats(String name, boolean didWin, int points) {

        try {
            Statement stmt = conn.createStatement();

            //get team id;
            String team_name = "\"" + name + "\"";
            ResultSet rs = stmt.executeQuery("SELECT team_id FROM teams WHERE name =" + team_name);
            String team_id = "";
            while(rs.next()) {
                team_id = "\"" + rs.getString("team_id") + "\"";
            }
            //increment games_played by 1 and W/L by 1
            stmt.executeUpdate("UPDATE stats SET games_played = games_played + 1 WHERE team_id =" + team_id);
            if(didWin) {
                stmt.executeUpdate("UPDATE stats SET games_won = games_won + 1 WHERE team_id =" + team_id);
            }
            else {
                stmt.executeUpdate("UPDATE stats SET games_lost = games_lost + 1 WHERE team_id =" + team_id);
            }
            //retrieve updated numWins and total wins to calc and set new win_percent
            rs = stmt.executeQuery("SELECT games_won FROM stats WHERE team_id =" + team_id);
            double numWins = 0;
            while(rs.next()) {
                numWins = Double.parseDouble(rs.getString("games_won"));
            }
            rs = stmt.executeQuery("SELECT games_played FROM stats WHERE team_id =" + team_id);
            double gamesPlayed = 0;
            while(rs.next()) {
                gamesPlayed = Double.parseDouble(rs.getString("games_played"));
            }

            String winPercent = Double.toString(numWins/gamesPlayed);
            stmt.executeUpdate("UPDATE stats SET win_percent = " + winPercent + "WHERE team_id=" +team_id);

            //set average points
            rs = stmt.executeQuery("SELECT avg_points FROM stats WHERE team_id =" + team_id);
            double avgPoints = 0;
            while(rs.next()) {
                avgPoints = Double.parseDouble(rs.getString("avg_points"));
            }

            String avgPointsNow = Double.toString(((((gamesPlayed - 1) * avgPoints) + points) / gamesPlayed));

            stmt.executeUpdate("UPDATE stats SET avg_points = " + avgPointsNow + "WHERE team_id=" + team_id);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
