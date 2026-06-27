package com.tcxconverter.parser;

import com.tcxconverter.model.Activity;
import com.tcxconverter.model.Lap;
import com.tcxconverter.model.TcxData;
import com.tcxconverter.model.Trackpoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TcxParser {

    public TcxData parse(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();

        String authorName = extractAuthorName(doc);
        List<Activity> activities = new ArrayList<>();

        NodeList activityNodes = doc.getElementsByTagNameNS("*", "Activity");
        for (int i = 0; i < activityNodes.getLength(); i++) {
            activities.add(parseActivity((Element) activityNodes.item(i)));
        }

        return new TcxData(activities, authorName);
    }

    private String extractAuthorName(Document doc) {
        NodeList authors = doc.getElementsByTagNameNS("*", "Author");
        if (authors.getLength() > 0) {
            NodeList names = ((Element) authors.item(0)).getElementsByTagNameNS("*", "Name");
            if (names.getLength() > 0) {
                return names.item(0).getTextContent().trim();
            }
        }
        return "";
    }

    private Activity parseActivity(Element activityEl) {
        String sport = activityEl.getAttribute("Sport");
        String id = directChildText(activityEl, "Id");
        String creatorName = extractCreatorName(activityEl);

        List<Lap> laps = new ArrayList<>();
        NodeList lapNodes = activityEl.getElementsByTagNameNS("*", "Lap");
        for (int i = 0; i < lapNodes.getLength(); i++) {
            Node node = lapNodes.item(i);
            if (node.getParentNode() == activityEl) {
                laps.add(parseLap((Element) node, laps.size() + 1));
            }
        }

        return new Activity(sport, id, laps, creatorName);
    }

    private String extractCreatorName(Element activityEl) {
        NodeList creators = activityEl.getElementsByTagNameNS("*", "Creator");
        if (creators.getLength() > 0) {
            NodeList names = ((Element) creators.item(0)).getElementsByTagNameNS("*", "Name");
            if (names.getLength() > 0) {
                return names.item(0).getTextContent().trim();
            }
        }
        return "";
    }

    private Lap parseLap(Element lapEl, int lapNumber) {
        String startTime = lapEl.getAttribute("StartTime");
        Double totalTime = parseDouble(directChildText(lapEl, "TotalTimeSeconds"));
        Double distance = parseDouble(directChildText(lapEl, "DistanceMeters"));
        Double maxSpeed = parseDouble(directChildText(lapEl, "MaximumSpeed"));
        Integer calories = parseInteger(directChildText(lapEl, "Calories"));
        Integer avgHr = heartRateValue(lapEl, "AverageHeartRateBpm");
        Integer maxHr = heartRateValue(lapEl, "MaximumHeartRateBpm");
        String intensity = directChildText(lapEl, "Intensity");
        Integer avgCadence = parseInteger(directChildText(lapEl, "Cadence"));
        String triggerMethod = directChildText(lapEl, "TriggerMethod");

        List<Trackpoint> trackpoints = new ArrayList<>();
        NodeList tpNodes = lapEl.getElementsByTagNameNS("*", "Trackpoint");
        for (int i = 0; i < tpNodes.getLength(); i++) {
            trackpoints.add(parseTrackpoint((Element) tpNodes.item(i), lapNumber));
        }

        return new Lap(lapNumber, startTime, totalTime, distance, maxSpeed, calories,
                avgHr, maxHr, intensity, avgCadence, triggerMethod, trackpoints);
    }

    private Trackpoint parseTrackpoint(Element tpEl, int lapNumber) {
        String time = directChildText(tpEl, "Time");

        Double lat = null, lon = null;
        NodeList posNodes = tpEl.getElementsByTagNameNS("*", "Position");
        if (posNodes.getLength() > 0) {
            Element pos = (Element) posNodes.item(0);
            lat = parseDouble(directChildText(pos, "LatitudeDegrees"));
            lon = parseDouble(directChildText(pos, "LongitudeDegrees"));
        }

        Double altitude = parseDouble(directChildText(tpEl, "AltitudeMeters"));
        Double distance = parseDouble(directChildText(tpEl, "DistanceMeters"));
        Integer heartRate = heartRateValue(tpEl, "HeartRateBpm");
        Integer cadence = parseInteger(directChildText(tpEl, "Cadence"));
        String sensorState = directChildText(tpEl, "SensorState");

        Double speed = null;
        Integer runCadence = null;
        Integer watts = null;
        NodeList tpxNodes = tpEl.getElementsByTagNameNS("*", "TPX");
        if (tpxNodes.getLength() > 0) {
            Element tpx = (Element) tpxNodes.item(0);
            speed = parseDouble(directChildText(tpx, "Speed"));
            runCadence = parseInteger(directChildText(tpx, "RunCadence"));
            watts = parseInteger(directChildText(tpx, "Watts"));
        }

        return new Trackpoint(lapNumber, time, lat, lon, altitude, distance,
                heartRate, cadence, speed, runCadence, watts, sensorState);
    }

    private Integer heartRateValue(Element parent, String elementName) {
        Element hrEl = firstDirectChild(parent, elementName);
        if (hrEl == null) return null;
        return parseInteger(directChildText(hrEl, "Value"));
    }

    private Element firstDirectChild(Element parent, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS("*", localName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getParentNode() == parent) {
                return (Element) node;
            }
        }
        return null;
    }

    private String directChildText(Element parent, String localName) {
        if (parent == null) return "";
        NodeList nodes = parent.getElementsByTagNameNS("*", localName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getParentNode() == parent) {
                return node.getTextContent().trim();
            }
        }
        return "";
    }

    private Double parseDouble(String value) {
        if (value == null || value.isEmpty()) return null;
        try { return Double.parseDouble(value); } catch (NumberFormatException e) { return null; }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isEmpty()) return null;
        try { return Integer.parseInt(value); } catch (NumberFormatException e) { return null; }
    }
}
