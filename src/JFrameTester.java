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

    public JFrameTester() {

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

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

            }
        });
    }

    public static void main(String[] args) {

        JFrame jframe = new JFrame(getDate().substring(0, 10));
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setContentPane(new JFrameTester().panel1);
        jframe.setPreferredSize(new Dimension(920, 300));
        jframe.pack();
        jframe.setLocationRelativeTo(null);
        jframe.setVisible(true);

    }

    public String[] getSchedule() throws Exception {

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
            put("DAL", "Dalas Mavericks");
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
                for (Element match : e.nextElementSibling().select("tbody > tr")) {

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
                    } else { //must be a game that is finished or postponed
                        for (Element teamName : match.select("a.team-name abbr")) {
                            finishedTeams.add(teamName);
                        }
                        gameResults += match.child(2).text();
                        gameResults += "\n\n";
                    }
                }
            }
        }

        gameResults = gameResults.replaceAll("[^0-9,\n]", "");
        gameResults = gameResults.replaceAll(",", " | ");
        System.out.println(gameResults);

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
        System.out.println(gameResults);
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
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 8, new Insets(0, 0, 0, 0), -1, -1));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setBackground(new Color(-459526));
        label1.setForeground(new Color(-16645375));
        label1.setOpaque(true);
        label1.setText(" Scheduled Games");
        panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(114, 21), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setBackground(new Color(-16711422));
        label2.setForeground(new Color(-197380));
        label2.setOpaque(true);
        label2.setText(" Time ");
        panel1.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(39, 16), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setBackground(new Color(-262415));
        label3.setForeground(new Color(-16711424));
        label3.setOpaque(true);
        label3.setText("  Live Games   ");
        panel1.add(label3, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(91, 21), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        textArea1 = new JTextArea();
        textArea1.setText("");
        panel1.add(textArea1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setBackground(new Color(-16645627));
        label4.setForeground(new Color(-262917));
        label4.setOpaque(true);
        label4.setText(" Score ");
        panel1.add(label4, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textArea2 = new JTextArea();
        panel1.add(textArea2, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(50, 50), null, 0, false));
        textArea4 = new JTextArea();
        panel1.add(textArea4, new com.intellij.uiDesigner.core.GridConstraints(2, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(10, 10), null, 0, false));
        textArea3 = new JTextArea();
        panel1.add(textArea3, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(2, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textArea5 = new JTextArea();
        panel1.add(textArea5, new com.intellij.uiDesigner.core.GridConstraints(2, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setBackground(new Color(-459526));
        label5.setEnabled(true);
        label5.setForeground(new Color(-16711422));
        label5.setName("");
        label5.setOpaque(true);
        label5.setText("Finished Games");
        panel1.add(label5, new com.intellij.uiDesigner.core.GridConstraints(1, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(2, 6, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setBackground(new Color(-16579837));
        label6.setForeground(new Color(-197380));
        label6.setOpaque(true);
        label6.setText("Final Score");
        panel1.add(label6, new com.intellij.uiDesigner.core.GridConstraints(1, 7, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer5 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer5, new com.intellij.uiDesigner.core.GridConstraints(0, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        textArea6 = new JTextArea();
        panel1.add(textArea6, new com.intellij.uiDesigner.core.GridConstraints(2, 7, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 50), null, 0, false));
        refreshButton = new JButton();
        refreshButton.setBackground(new Color(-7950103));
        refreshButton.setFont(new Font(refreshButton.getFont().getName(), refreshButton.getFont().getStyle(), 8));
        refreshButton.setForeground(new Color(-7950103));
        refreshButton.setHorizontalAlignment(0);
        refreshButton.setText("Refresh");
        panel1.add(refreshButton, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}
