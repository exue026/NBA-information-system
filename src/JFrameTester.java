import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class JFrameTester {

    private JButton refreshButton;
    private JPanel panel1;
    private JTextArea textArea1;
    private JTextArea textArea3;
    private JTextArea textArea4;
    private JTextArea textArea2;
    private JTextArea textArea5;
    private JTextArea textArea6;
    private JButton throwInDBButton;
    private JComboBox comboBox1;
    private JTextArea textArea7;
    private JComboBox comboBox2;
    static JFrame jframe = new JFrame(getDate().substring(0, 10));
    private String name;
    private String specStat;
    private dbController dbController = new dbController();

    public JFrameTester() {

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                jframe.setSize(new Dimension(1100, 300));

                try {
                    String[] schedule = getSchedule();
                    textArea1.setText(schedule[0]);
                    textArea2.setText(schedule[1]);
                    textArea3.setText(schedule[2]);
                    textArea4.setText(schedule[3]);
                    textArea5.setText(schedule[4]);
                    textArea6.setText(schedule[5]);


                } catch (Exception exception) {
                    System.out.println("Exception thrown: " + exception);
                }

                jframe.setSize(new Dimension(1200, 400));

            }
        });

        throwInDBButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (Integer.parseInt(getDate().substring(11, 13)) < 22) {
                    return;
                }
                try {
                    String[] schedule = getSchedule();
                    if (!schedule[4].isEmpty() && !schedule[5].isEmpty()) {
                        storeData(formatFinishedTeams(schedule[4]), formatGameResults(schedule[5]));
                    }
                } catch (Exception E) {
                    E.printStackTrace();
                    System.out.println(E);
                }
            }
        });
        comboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                name = (String) comboBox1.getSelectedItem();

                if (specStat != null && name != null) {
                    System.out.println("hi");
                    if (specStat.equals("--ALL STATS--")) {
                        textArea7.setText(dbController.getAllTeamStats(name));
                    } else {
                        textArea7.setText(dbController.getTeamStat(name, specStat));
                    }
                }

            }
        });
        comboBox2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                specStat = (String) comboBox2.getSelectedItem();
                switch (specStat) {
                    case "Games Played":
                        specStat = "games_played";
                        break;
                    case "Games Won":
                        specStat = "games_won";
                        break;
                    case "Games Lost":
                        specStat = "games_lost";
                        break;
                    case "Win Percentage":
                        specStat = "win_percent";
                        break;
                    case "Points per Game":
                        specStat = "avg_points";
                        break;
                }

                if (specStat != null && name != null) {
                    if (specStat.equals("--ALL STATS--")) {
                        textArea7.setText(dbController.getAllTeamStats(name));
                    } else {
                        System.out.println(name);
                        System.out.println(specStat);
                        textArea7.setText(dbController.getTeamStat(name, specStat));
                    }
                }
            }
        });
    }

    public static void main(String[] args) {

        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setContentPane(new JFrameTester().panel1);
        jframe.setPreferredSize(new Dimension(800, 300));
        jframe.pack();
        jframe.setLocationRelativeTo(null);
        jframe.setVisible(true);

    }

    public void storeData(ArrayList<String> teams, ArrayList<Integer> results) {


        for (int i = 0; i < teams.size(); i++) {
            System.out.println(teams.get(i) + " " + results.get(i));
        }

        for (int i = 0; i < teams.size(); i += 2) {
            dbController.updateTeamStats(teams.get(i), true, results.get(i));
            dbController.updateTeamStats(teams.get(i + 1), false, results.get(i + 1));
        }

    }

    public ArrayList<String> formatFinishedTeams(String finishedTeams) {
        ArrayList<String> formatFinishedTeams = new ArrayList<String>();

        int indexTracker = 0;
        int beginning = 0;


        finishedTeams = finishedTeams.replace("\n", " ");


        System.out.println(finishedTeams);

        boolean isMatch = true;

        while (finishedTeams.indexOf("vs", beginning) != -1) {
            if (isMatch) {
                indexTracker = finishedTeams.indexOf("vs", beginning);
                formatFinishedTeams.add(finishedTeams.substring(beginning, indexTracker - 1));
                beginning = indexTracker + 3;
                isMatch = false;
            } else {
                indexTracker = finishedTeams.indexOf("  ", beginning);
                formatFinishedTeams.add(finishedTeams.substring(beginning, indexTracker));
                beginning = indexTracker + 2;
                isMatch = true;
            }
        }
        formatFinishedTeams.add(finishedTeams.substring(beginning, finishedTeams.length()));
        formatFinishedTeams.set(formatFinishedTeams.size() - 1, formatFinishedTeams.get(formatFinishedTeams.size() - 1).trim());

        return formatFinishedTeams;
    }

    public ArrayList<Integer> formatGameResults(String gameResults) {

        System.out.println(gameResults);

        ArrayList<Integer> formatGameResults = new ArrayList<Integer>();

        gameResults = gameResults.replace("\n", " ");
        gameResults = gameResults.replace(" | ", " ");
        gameResults = gameResults.replace("  ", " ");

        System.out.println(gameResults);

        for (String s : gameResults.split(" ")) {
            formatGameResults.add(Integer.parseInt(s));
        }

        return formatGameResults;
    }


    public static String[] getSchedule() throws Exception {

        System.out.println(getDate());
        String day = getDate().substring(8, 10);
        if (day.substring(0, 1).equals("0")) { //we want 04 --> 4
            day = day.substring(1, 2);
        }

        Elements liveTeams = new Elements();
        Elements scheduledTeams = new Elements();
        Elements finishedTeams = new Elements();
        String liveScores = "";
        String gameTimes = "";
        String gameResults = "";
        String numbers = "0,1,2,3,4,5,6,7,8,9";

        HashMap<String, String> teamNameMap = new HashMap<String, String>() {{
            put("ATL", "Atlanta Hawks");
            put("BKN", "Brooklyn Nets");
            put("BOS", "Boston Celtics");
            put("CHA", "Charlotte Hornets");
            put("CHI", "Chicago Bulls");
            put("CLE", "Cleveland Cavaliers");
            put("DAL", "Dallas Mavericks");
            put("DEN", "Denver Nuggets");
            put("DET", "Detroit Pistons");
            put("GS", "Golden State Warriors");
            put("HOU", "Houston Rockets");
            put("IND", "Indiana Pacers");
            put("LAC", "Los Angeles Clippers");
            put("LAL", "Los Angeles Lakers");
            put("MEM", "Memphis Grizzlies");
            put("MIA", "Miami Heat");
            put("MIL", "Milwaukee Bucks");
            put("MIN", "Minnesota Timberwolves");
            put("NO", "New Orleans Pelicans");
            put("NY", "New York Knicks");
            put("OKC", "Oklahoma City Thunder");
            put("ORL", "Orlando Magic");
            put("PHI", "Philadelphia 76ers");
            put("PHX", "Phoenix Suns");
            put("POR", "Portland Trail Blazers");
            put("SAC", "Sacramento Kings");
            put("SA", "San Antonio Spurs");
            put("TOR", "Toronto Raptors");
            put("UTAH", "Utah Jazz");
            put("WSH", "Washington Wizards");
        }};

        String baseURL = "http://www.espn.com/nba/schedule";
        Document doc = Jsoup.connect(baseURL).get();

        for (Element e : doc.select("h2.table-caption")) { //date of the section of games
            if (e.text().contains(day)) {

                liveScores = getLiveScores(e.nextElementSibling());
                Element currentMatches = e.nextElementSibling();
                for (Element match : currentMatches.select("tbody > tr")) {

                    if (match.children().hasClass("live")) { //check if game is live
                        for (Element teamName : match.select("a.team-name abbr")) {
                            liveTeams.add(teamName);
                        }
                    } else if (match.children().hasAttr("data-date")) { //check if its a scheduled game
                        for (Element teamName : match.select("a.team-name abbr")) {
                            scheduledTeams.add(teamName);
                        }
                        for (Element gameTime : match.select("td[data-date]")) {
                            gameTimes += extractTime(gameTime) + "\n\n";
                        }
                    }
                }
                Element finishedMatches = currentMatches.nextElementSibling();
                for (Element finishedMatch : finishedMatches.select("tbody > tr")) {
                    for (Element teamName : finishedMatch.select("a.team-name abbr")) {
                        finishedTeams.add(teamName);
                    }
                    gameResults += finishedMatch.child(2).text();
                    gameResults += "\n\n";

                }
            }
        }

        gameResults = gameResults.replaceAll("[^0-9,\n]", "");
        gameResults = gameResults.replaceAll(",", " | ");
        ArrayList<String> lTeams = new ArrayList<String>(Arrays.asList(liveTeams.text().split(" ")));
        ArrayList<String> sTeams = new ArrayList<String>(Arrays.asList(scheduledTeams.text().split(" ")));
        ArrayList<String> fTeams = new ArrayList<String>(Arrays.asList(finishedTeams.text().split(" ")));

        String scheduleList = "";
        String liveList = "";
        String finishedList = "";

        if (lTeams.size() == 1) {
            lTeams.clear();
            liveList += "None at this moment";
        }
        if (sTeams.size() == 1) {
            sTeams.clear();
            scheduleList += "None at this moment";
        }
        if (fTeams.size() == 1) {
            fTeams.clear();

        }
        for (int i = 0; i < lTeams.size(); i += 2) {

            liveList += (teamNameMap.get(lTeams.get(i)) + " vs " + teamNameMap.get(lTeams.get(i + 1)));
            liveList += "\n\n";
        }
        for (int i = 0; i < sTeams.size(); i += 2) {

            scheduleList += (teamNameMap.get(sTeams.get(i)) + " vs " + teamNameMap.get(sTeams.get(i + 1)));
            scheduleList += "\n\n";
        }
        for (int i = 0; i < fTeams.size(); i += 2) {

            finishedList += (teamNameMap.get(fTeams.get(i)) + " vs " + teamNameMap.get(fTeams.get(i + 1)));
            finishedList += "\n\n";
        }

        String[] schedule = {scheduleList, gameTimes, liveList, liveScores, finishedList, gameResults};
        return schedule;
    }

    public static String getDate() {
        Date date = Calendar.getInstance().getTime();
        return date.toString();
    }

    public static String extractTime(Element gameTime) {

        String time = gameTime.attr("data-date").substring(11, 16);
        int hour = Integer.parseInt(time.substring(0, 2)) + 7;
        if (hour >= 24) {
            hour -= 12;
        }
        time = Integer.toString(hour) + time.substring(2, 5) + " PM EST";
        return time;
    }

    public static String getLiveScores(Element element) throws Exception {

        Elements elements = element.select("td.live > a");
        String liveGameScores = "";
        for (Element root : elements) {
            Document doc = Jsoup.connect("http://www.espn.com" + root.attr("href")).get(); //live game 1
            Elements scores = doc.select("div.score"); //getting the score for a  game
            liveGameScores += scores.get(0).text() + " | " + scores.get(1).text() + "\n\n";
        }
        return liveGameScores;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(6, 9, new Insets(0, 0, 0, 0), -1, -1));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setBackground(new Color(-459526));
        label1.setForeground(new Color(-16645375));
        label1.setOpaque(true);
        label1.setText(" Scheduled Games");
        panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(114, 21), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setBackground(new Color(-16711422));
        label2.setForeground(new Color(-197380));
        label2.setOpaque(true);
        label2.setText(" Time ");
        panel1.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(39, 16), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setBackground(new Color(-262415));
        label3.setForeground(new Color(-16711424));
        label3.setOpaque(true);
        label3.setText("  Live Games   ");
        panel1.add(label3, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(91, 21), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        textArea1 = new JTextArea();
        textArea1.setText("");
        panel1.add(textArea1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), new Dimension(-1, 150), 0, false));
        final JLabel label4 = new JLabel();
        label4.setBackground(new Color(-16645627));
        label4.setForeground(new Color(-262917));
        label4.setOpaque(true);
        label4.setText(" Score ");
        panel1.add(label4, new com.intellij.uiDesigner.core.GridConstraints(1, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textArea2 = new JTextArea();
        panel1.add(textArea2, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(50, 50), new Dimension(-1, 150), 0, false));
        textArea4 = new JTextArea();
        textArea4.setText("");
        panel1.add(textArea4, new com.intellij.uiDesigner.core.GridConstraints(2, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(10, 10), new Dimension(-1, 150), 0, false));
        textArea3 = new JTextArea();
        textArea3.setText("");
        panel1.add(textArea3, new com.intellij.uiDesigner.core.GridConstraints(2, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), new Dimension(-1, 150), 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(2, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textArea5 = new JTextArea();
        textArea5.setText("");
        panel1.add(textArea5, new com.intellij.uiDesigner.core.GridConstraints(2, 6, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), new Dimension(-1, 150), 0, false));
        final JLabel label5 = new JLabel();
        label5.setBackground(new Color(-459526));
        label5.setEnabled(true);
        label5.setForeground(new Color(-16711422));
        label5.setName("");
        label5.setOpaque(true);
        label5.setText("Finished Games");
        panel1.add(label5, new com.intellij.uiDesigner.core.GridConstraints(1, 6, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(2, 7, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setBackground(new Color(-16579837));
        label6.setForeground(new Color(-197380));
        label6.setOpaque(true);
        label6.setText("Final Score");
        panel1.add(label6, new com.intellij.uiDesigner.core.GridConstraints(1, 8, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer5 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer5, new com.intellij.uiDesigner.core.GridConstraints(0, 6, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        refreshButton = new JButton();
        refreshButton.setBackground(new Color(-7950103));
        refreshButton.setFont(new Font(refreshButton.getFont().getName(), refreshButton.getFont().getStyle(), 8));
        refreshButton.setForeground(new Color(-7950103));
        refreshButton.setHorizontalAlignment(0);
        refreshButton.setText("Refresh");
        panel1.add(refreshButton, new com.intellij.uiDesigner.core.GridConstraints(0, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        throwInDBButton = new JButton();
        throwInDBButton.setFont(new Font(throwInDBButton.getFont().getName(), throwInDBButton.getFont().getStyle(), 8));
        throwInDBButton.setText("Throw in DB");
        panel1.add(throwInDBButton, new com.intellij.uiDesigner.core.GridConstraints(0, 8, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer6 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer6, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 3, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 50), new Dimension(-1, 75), new Dimension(-1, 100), 0, false));
        textArea6 = new JTextArea();
        textArea6.setText("");
        panel1.add(textArea6, new com.intellij.uiDesigner.core.GridConstraints(2, 8, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 50), new Dimension(-1, 150), 0, false));
        comboBox1 = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("ATLANTA HAWKS");
        defaultComboBoxModel1.addElement("Brooklyn Nets");
        defaultComboBoxModel1.addElement("Boston Celtics");
        defaultComboBoxModel1.addElement("Charlotte Hornets");
        defaultComboBoxModel1.addElement("Chicago Bulls");
        defaultComboBoxModel1.addElement("Cleveland Cavaliers");
        defaultComboBoxModel1.addElement("Dallas Mavericks");
        defaultComboBoxModel1.addElement("Denver Nuggets");
        defaultComboBoxModel1.addElement("Detroit Pistons");
        defaultComboBoxModel1.addElement("Golden State Warriors");
        defaultComboBoxModel1.addElement("Houston Rockets");
        defaultComboBoxModel1.addElement("Indiana Pacers");
        defaultComboBoxModel1.addElement("Los Angeles Clippers");
        defaultComboBoxModel1.addElement("Los Angeles Lakers");
        defaultComboBoxModel1.addElement("Memphis Grizzlies");
        defaultComboBoxModel1.addElement("Miami Heat");
        defaultComboBoxModel1.addElement("Milwaukee Bucks");
        defaultComboBoxModel1.addElement("Minnesota Timberwolves");
        defaultComboBoxModel1.addElement("New Orleans Pelicans");
        defaultComboBoxModel1.addElement("New York Knicks");
        defaultComboBoxModel1.addElement("Oklahoma City Thunders");
        defaultComboBoxModel1.addElement("Orlando Magic");
        defaultComboBoxModel1.addElement("Philadelphia 76ers");
        defaultComboBoxModel1.addElement("Phoenix Suns");
        defaultComboBoxModel1.addElement("Portland Trail Blazers");
        defaultComboBoxModel1.addElement("Sacramento Kings");
        defaultComboBoxModel1.addElement("San Antonio Spurs");
        defaultComboBoxModel1.addElement("Toronto Raptors");
        defaultComboBoxModel1.addElement("Utah Jazz");
        defaultComboBoxModel1.addElement("Washington Wizards");
        comboBox1.setModel(defaultComboBoxModel1);
        panel1.add(comboBox1, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textArea7 = new JTextArea();
        panel1.add(textArea7, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        comboBox2 = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("--ALL STATS--");
        defaultComboBoxModel2.addElement("Games Played");
        defaultComboBoxModel2.addElement("Games Won");
        defaultComboBoxModel2.addElement("Games Lost");
        defaultComboBoxModel2.addElement("Win Percentage");
        defaultComboBoxModel2.addElement("Points per Game");
        comboBox2.setModel(defaultComboBoxModel2);
        panel1.add(comboBox2, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}
