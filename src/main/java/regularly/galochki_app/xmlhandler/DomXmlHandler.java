package regularly.galochki_app.xmlhandler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import regularly.galochki_app.model.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class DomXmlHandler implements GalochkiXmlHandler {

    @Override
    public GalochkiXmlFile read(Path path) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(path.toFile());
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement(); // <xml>

            Element pageElement = (Element) root.getElementsByTagName(XmlTags.PAGE).item(0);
            Page page = parsePage(pageElement);

            Element activitiesElement = (Element) root.getElementsByTagName(XmlTags.ACTIVITIES).item(0);
            List<Activity> activities = parseActivities(activitiesElement);

            NodeList weekNodes = root.getElementsByTagName(XmlTags.GALOCHKI_WEEK);
            List<GalochkiWeek> weeks = parseWeeks(weekNodes);

            GalochkiXmlFile result = new GalochkiXmlFile();
            result.setPage(page);
            result.setGalochki(weeks);
            result.setActivites(activities);

            return result;

        } catch (Exception e) {
            throw new IOException("Failed to parse XML file: " + path, e);
        }
    }

    @Override
    public void write(Path path, GalochkiXmlFile file) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement(XmlTags.XML);
            doc.appendChild(root);

            writePage(doc, root, file.getPage());
            writeActivities(doc, root, file.getActivites());
            writeWeeks(doc, root, file.getGalochki());

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(path.toFile()));

        } catch (Exception e) {
            throw new IOException("Failed to write XML to: " + path, e);
        }
    }

    private Page parsePage(Element pageElement) {
        Page page = new Page();

        page.setName(pageElement.getAttribute(XmlTags.NAME));

        String typeStr = pageElement.getAttribute(XmlTags.TYPE).toUpperCase();
        page.setType(PageType.valueOf(typeStr));

        page.setDailyItog(Boolean.parseBoolean(pageElement.getAttribute(XmlTags.DAILY_ITOG)));
        page.setWeeklyItog(Boolean.parseBoolean(pageElement.getAttribute(XmlTags.WEEKLY_ITOG)));

        String dailyNormStr = pageElement.getAttribute(XmlTags.DAILY_NORM);
        page.setDailyNorm(dailyNormStr.isEmpty() ? null : Double.parseDouble(dailyNormStr));

        String weeklyNormStr = pageElement.getAttribute(XmlTags.WEEKLY_NORM);
        page.setWeeklyNorm(weeklyNormStr.isEmpty() ? null : Double.parseDouble(weeklyNormStr));

        return page;
    }

    private void writePage(Document doc, Element parent, Page page) {
        Element pageElement = doc.createElement(XmlTags.PAGE);

        pageElement.setAttribute(XmlTags.NAME, page.getName());
        pageElement.setAttribute(XmlTags.TYPE, page.getType().name().toLowerCase());
        pageElement.setAttribute(XmlTags.DAILY_ITOG, String.valueOf(page.isDailyItog()));
        pageElement.setAttribute(XmlTags.WEEKLY_ITOG, String.valueOf(page.isWeeklyItog()));

        if (page.getDailyNorm() != null) {
            pageElement.setAttribute(XmlTags.DAILY_NORM, String.valueOf(page.getDailyNorm()));
        }
        if (page.getWeeklyNorm() != null) {
            pageElement.setAttribute(XmlTags.WEEKLY_NORM, String.valueOf(page.getWeeklyNorm()));
        }

        parent.appendChild(pageElement);
    }

    private List<Activity> parseActivities(Element activitiesElement) {
        List<Activity> activities = new ArrayList<>();

        NodeList activityNodes = activitiesElement.getElementsByTagName(XmlTags.ACTIVITY);
        for (int i = 0; i < activityNodes.getLength(); i++) {
            Element activityElement = (Element) activityNodes.item(i);
            Activity activity = new Activity();

            activity.setName(activityElement.getAttribute(XmlTags.NAME));

            String typeStr = activityElement.getAttribute(XmlTags.TYPE).toUpperCase();
            activity.setType(ActivityType.valueOf(typeStr));

            String dailyNormStr = activityElement.getAttribute(XmlTags.DAILY_NORM);
            activity.setDailyNorm(dailyNormStr.isEmpty() ? null : Double.parseDouble(dailyNormStr));

            String weeklyNormStr = activityElement.getAttribute(XmlTags.WEEKLY_NORM);
            activity.setWeeklyNorm(weeklyNormStr.isEmpty() ? null : Double.parseDouble(weeklyNormStr));

            String periodicityStr = activityElement.getAttribute(XmlTags.PERIODICITY);
            activity.setPeriodicity(periodicityStr.isEmpty() ? null : Integer.parseInt(periodicityStr));

            activities.add(activity);
        }

        return activities;
    }

    private void writeActivities(Document doc, Element parent, List<Activity> activities) {
        Element activitiesElement = doc.createElement(XmlTags.ACTIVITIES);

        for (Activity activity : activities) {
            Element activityElement = doc.createElement(XmlTags.ACTIVITY);

            activityElement.setAttribute(XmlTags.NAME, activity.getName());
            activityElement.setAttribute(XmlTags.TYPE, activity.getType().name().toLowerCase());

            if (activity.getDailyNorm() != null) {
                activityElement.setAttribute(XmlTags.DAILY_NORM, String.valueOf(activity.getDailyNorm()));
            }
            if (activity.getWeeklyNorm() != null) {
                activityElement.setAttribute(XmlTags.WEEKLY_NORM, String.valueOf(activity.getWeeklyNorm()));
            }
            if (activity.getPeriodicity() != null) {
                activityElement.setAttribute(XmlTags.PERIODICITY, String.valueOf(activity.getPeriodicity()));
            }

            activitiesElement.appendChild(activityElement);
        }

        parent.appendChild(activitiesElement);
    }

    private List<GalochkiWeek> parseWeeks(NodeList weekNodes) {
        List<GalochkiWeek> weeks = new ArrayList<>();

        for (int i = 0; i < weekNodes.getLength(); i++) {
            Element weekElement = (Element) weekNodes.item(i);
            GalochkiWeek week = new GalochkiWeek();

            Element overflowEl = (Element) weekElement.getElementsByTagName(XmlTags.OVERFLOW).item(0);
            if (overflowEl != null) {
                String val = overflowEl.getAttribute(XmlTags.VALUE);
                week.setOverflow(val.isEmpty() ? null : Double.parseDouble(val));
            } else {
                week.setOverflow(null);
            }

            NodeList dayNodes = weekElement.getElementsByTagName(XmlTags.GALOCHKI_DAY);
            List<GalochkiDay> days = new ArrayList<>();

            for (int d = 0; d < dayNodes.getLength(); d++) {
                Element dayElement = (Element) dayNodes.item(d);
                GalochkiDay day = new GalochkiDay();
                String dayAttr = dayElement.getAttribute(XmlTags.DAY);
                day.setDay(dayAttr.isEmpty() ? -1 : Integer.parseInt(dayAttr));

                NodeList galochkaNodes = dayElement.getElementsByTagName(XmlTags.GALOCHKA);
                List<Galochka> galochkas = new ArrayList<>();

                for (int g = 0; g < galochkaNodes.getLength(); g++) {
                    Element galochkaEl = (Element) galochkaNodes.item(g);
                    Galochka galochka = new Galochka();

                    String valueStr = galochkaEl.getAttribute(XmlTags.VALUE);
                    galochka.setValue(valueStr.isEmpty() ? 0.0 : Double.parseDouble(valueStr));

                    galochkas.add(galochka);
                }

                day.setGalochki(galochkas);
                days.add(day);
            }

            week.setGalochki(days);
            weeks.add(week);
        }

        return weeks;
    }

    private void writeWeeks(Document doc, Element parent, List<GalochkiWeek> weeks) {
        for (GalochkiWeek week : weeks) {
            Element weekElement = doc.createElement(XmlTags.GALOCHKI_WEEK);

            if (week.getOverflow() != null) {
                Element overflowEl = doc.createElement(XmlTags.OVERFLOW);
                overflowEl.setAttribute(XmlTags.VALUE, String.valueOf(week.getOverflow()));
                weekElement.appendChild(overflowEl);
            }

            for (GalochkiDay day : week.getGalochki()) {
                Element dayElement = doc.createElement(XmlTags.GALOCHKI_DAY);
                dayElement.setAttribute(XmlTags.DAY, String.valueOf(day.getDay()));

                for (Galochka galochka : day.getGalochki()) {
                    Element galochkaEl = doc.createElement(XmlTags.GALOCHKA);
                    galochkaEl.setAttribute(XmlTags.VALUE, String.valueOf(galochka.getValue()));
                    dayElement.appendChild(galochkaEl);
                }

                weekElement.appendChild(dayElement);
            }

            parent.appendChild(weekElement);
        }
    }
}