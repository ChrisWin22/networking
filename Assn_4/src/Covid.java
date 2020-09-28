import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Covid {

    public static void main(String[] args) throws IOException {
        getUtahStateNumbers();
        getBYUNumbers();
        getUVUNumbers();
    }

    public static void getUtahStateNumbers() throws IOException {
        URL url = new URL("https://www.usu.edu/covid-19/");
        String html = new String(url.openStream().readAllBytes());

        String dataRow = "<td class=\"text-bold bg-bright-light text-white text-center\">";
        String endDataRow = "</td>";
        int active = 0;
        int fall = 0;
        int total = 0;

        int index = html.indexOf(dataRow, 0) + dataRow.length();
        int endIndex = html.indexOf(endDataRow, index);

        active = Integer.parseInt(html.substring(index, endIndex));

        index = html.indexOf(dataRow, index) + dataRow.length();
        endIndex = html.indexOf(endDataRow, index);

        fall = Integer.parseInt(html.substring(index,endIndex));

        index = html.indexOf(dataRow, index) + dataRow.length();
        endIndex = html.indexOf(endDataRow, index);

        total = Integer.parseInt(html.substring(index,endIndex));

        System.out.println("----------USU----------");
        System.out.println("Active Cases: " + active);
        System.out.println("Fall Semester Cases: " + fall);
        System.out.println("Total Cases: " + total);
    }

    public static void getBYUNumbers() throws IOException {
        URL url = new URL("https://www.byu.edu/coronavirus/");
        String html = new String(url.openStream().readAllBytes());

        String basicTD = "<td colspan=\"1\" rowspan=\"1\">";
        String endTD = "</td>";
        int active = 0;
        int resolved = 0;
        int total = 0;

        int index = html.indexOf("Active cases");
        index = html.indexOf(basicTD, index) + basicTD.length();
        int endIndex = html.indexOf(endTD, index);

        active = Integer.parseInt(html.substring(index, endIndex));

        index = html.indexOf("Cases no longer in isolation");
        index = html.indexOf(basicTD, index) + basicTD.length();
        endIndex = html.indexOf(endTD, index);

        resolved = Integer.parseInt(html.substring(index, endIndex));

        index = html.indexOf("Total reported cases");
        index = html.indexOf(basicTD, index) + basicTD.length();
        endIndex = html.indexOf(endTD, index);

        String totalString = html.substring(index, endIndex);
        totalString = totalString.replace(",", "");
        total = Integer.parseInt(totalString);

        System.out.println("----------BYU----------");
        System.out.println("Active Cases: " + active);
        System.out.println("Reolved Cases: " + resolved);
        System.out.println("Total Reported: " + total);


    }

    public static void getUVUNumbers() throws IOException {
        URL url = new URL("https://www.uvu.edu/returntocampus/");
        String html = new String(url.openStream().readAllBytes());
        int student = 0;
        int faculty = 0;
        int total = 0;

        String basicStyle = "<td style=\"text-align: center;\">";
        String normalStyle = "<td style=\"text-align: center;\" colspan=\"2\">";
        String basicEnd = "</td>";

        int index = html.indexOf("Positive cases past seven days");
        index = html.indexOf(basicStyle, index) + basicStyle.length();
        int endIndex = html.indexOf(basicEnd, index);

        student = Integer.parseInt(html.substring(index, endIndex));

        index = html.indexOf(basicStyle, index) + basicStyle.length();
        endIndex = html.indexOf(basicEnd, index);

        faculty = Integer.parseInt(html.substring(index, endIndex));

        index = html.indexOf("Total positive cases since August 24");
        index = html.indexOf(normalStyle, index) + normalStyle.length();
        endIndex = html.indexOf(basicEnd, index);

        total = Integer.parseInt(html.substring(index, endIndex));

        System.out.println("----------UVU----------");
        System.out.println("Student: " + student);
        System.out.println("Faculty: " + faculty);
        System.out.println("Total: " + total);
        System.out.println("*Note: UVU numbers are past seven days (except for total) and only include self reported cases");


    }
}
