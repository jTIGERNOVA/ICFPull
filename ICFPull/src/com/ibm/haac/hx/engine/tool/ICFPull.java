package com.ibm.haac.hx.engine.tool;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.fluent.Request;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by antonioj on 6/15/2016.
 */
public class ICFPull {
    private static final String WEB_URI = "http://apps.who.int/classifications/icfbrowser/Browse.aspx?code=";

    private List<ICFClassification> allClassifications = Collections.synchronizedList(new ArrayList<ICFClassification>(600));
    private AtomicInteger count = new AtomicInteger(0);

    private static boolean isValid(String html) {
        if (StringUtil.isBlank(html))
            return true;

        Document doc = Jsoup.parse(html);
        Element title = doc.select("#Title").first();

        boolean valid = (title == null) ? false : !title.html().toLowerCase().startsWith("error");

        return valid;
    }

    private static void loadICFClassification(String html, ICFClassification classification) {
        if (StringUtil.isBlank(html)) {
            System.err.println("Error parsing html");
            return;
        }

        System.out.println(String.format("Loading %s classification HTML...", classification.getCode()));

        Document doc = Jsoup.parse(html);
        Element title = doc.select("#Title").first();
        Element desc = doc.select("#Description").first();

        classification.setTitle(title.html());
        classification.setDescription(desc.html());

        System.out.println(String.format("Loading %s classification HTML done", classification.getCode()));
    }

    private List<ICFClassification> getClassificationsDef(String prefix, int start, int end) {
        List<ICFClassification> defs = new ArrayList<>((end - start) + 1);

        String code;

        for (int i = start; i <= end; i++) {
            code = prefix + i;
            ICFClassification classification = new ICFClassification();

            classification.setPrefix(prefix);
            classification.setIntCode(i);
            classification.setCode(code);

            defs.add(classification);
        }

        return defs;
    }

    private ICFClassificationResult getClassificationData(ICFClassification classification) {
        String uri = WEB_URI + classification.getCode();

        ICFClassificationResult result = new ICFClassificationResult();

        try {
            System.out.println(String.format(">>Performing WHO ICF lookup for code %s...", classification.getCode()));

            String html = Request.Get(uri)
                    .execute().returnContent().asString();

            result.success = isValid(html);

            if (result.success) {
                System.out.println(String.format("ICF Classification %s was found!", classification.getCode()));

                loadICFClassification(html, classification);

                result.icfClassification = classification;
                count.set(count.intValue() + 1);
            }
        } catch (Throwable e) {
            System.err.println("Error when executing: " + uri.toString());
            e.printStackTrace();
        }

        return result;
    }

    private boolean findAllClassifications(ICFClassificationResult result) {
        if (!result.success)
            return false;

        //get classifications with code between 1 and 9
        int newStart = Integer.parseInt(result.icfClassification.getIntCode() + "0");
        int newEnd = newStart + 9;

        List<ICFClassification> classifications = getClassificationsDef(result.icfClassification.getPrefix(), newStart, newEnd);
        ICFClassificationResult newResult;

        for (ICFClassification classification : classifications) {
            newResult = getClassificationData(classification);

            if (newResult.success) {
                newResult.icfClassification.setDepthLevel(result.icfClassification.getDepthLevel() + 1);

                newResult.icfClassification.setParentClassification(result.icfClassification);
                result.icfClassification.addChildClassifications(newResult.icfClassification);

                findAllClassifications(newResult);
            }
        }

        return true;
    }

    public void pullICFClassifications(String group) throws InterruptedException {
        //for some reason, executing on multiple threads cause response issues from the server. maybe a request limit per second
        //for now, 2 threads seem to return consistent results
        int threadCount = 2;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        if ("b".equalsIgnoreCase(group)) {

            executeForGroupB(executorService);
        } else if ("s".equalsIgnoreCase(group)) {

            executeForGroupS(executorService);
        } else if ("d".equalsIgnoreCase(group)) {

            executeForGroupD(executorService);
        } else if ("e".equalsIgnoreCase(group)) {

            executeForGroupE(executorService);
        } else {
            System.err.println(String.format("Group %s is not supported", group));
            return;
        }

        executorService.shutdown();
        executorService.awaitTermination(480, TimeUnit.MINUTES);

        Collections.sort(allClassifications);

        System.out.println("*****************");
        System.out.println(String.format("All ICF Classifications (Found %s for %s)", count, group));
        System.out.println("*****************");
        //time to print
        JsonArray array = new JsonArray();

        for (ICFClassification classification : allClassifications) {
            array.add(classification.getJsonObject());
        }

        //save file
        try {
            File file = new File(String.format("icf-%s.json", group));

            System.out.println("Writing json to " + file.getAbsolutePath() + "...");
            FileUtils.write(file,
                    new GsonBuilder().setPrettyPrinting().create().toJson(array), Charset.defaultCharset());

            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeForGroupB(ExecutorService executorService) {
        String group = "b";

        executorService.execute(getICFDefinitions(group, 110, 139));
        executorService.execute(getICFDefinitions(group, 140, 189));
        executorService.execute(getICFDefinitions(group, 198, 199));
        executorService.execute(getICFDefinitions(group, 210, 289));
        executorService.execute(getICFDefinitions(group, 298, 299));
        executorService.execute(getICFDefinitions(group, 310, 340));
        executorService.execute(getICFDefinitions(group, 398, 399));
        executorService.execute(getICFDefinitions(group, 410, 469));
        executorService.execute(getICFDefinitions(group, 498, 499));
        executorService.execute(getICFDefinitions(group, 510, 559));
        executorService.execute(getICFDefinitions(group, 598, 599));
        executorService.execute(getICFDefinitions(group, 610, 679));
        executorService.execute(getICFDefinitions(group, 698, 699));
        executorService.execute(getICFDefinitions(group, 710, 789));
        executorService.execute(getICFDefinitions(group, 798, 799));
        executorService.execute(getICFDefinitions(group, 810, 869));
        executorService.execute(getICFDefinitions(group, 898, 899));
    }

    private void executeForGroupS(ExecutorService executorService) {
        String group = "s";

        executorService.execute(getICFDefinitions(group, 110, 150));
        executorService.execute(getICFDefinitions(group, 198, 199));
        executorService.execute(getICFDefinitions(group, 210, 260));
        executorService.execute(getICFDefinitions(group, 298, 299));
        executorService.execute(getICFDefinitions(group, 310, 340));
        executorService.execute(getICFDefinitions(group, 398, 399));
        executorService.execute(getICFDefinitions(group, 410, 430));
        executorService.execute(getICFDefinitions(group, 498, 499));
        executorService.execute(getICFDefinitions(group, 510, 580));
        executorService.execute(getICFDefinitions(group, 598, 599));
        executorService.execute(getICFDefinitions(group, 610, 630));
        executorService.execute(getICFDefinitions(group, 698, 699));
        executorService.execute(getICFDefinitions(group, 710, 770));
        executorService.execute(getICFDefinitions(group, 798, 799));
        executorService.execute(getICFDefinitions(group, 810, 840));
        executorService.execute(getICFDefinitions(group, 898, 899));
    }

    private void executeForGroupD(ExecutorService executorService) {
        String group = "d";

        executorService.execute(getICFDefinitions(group, 110, 129));
        executorService.execute(getICFDefinitions(group, 130, 159));
        executorService.execute(getICFDefinitions(group, 160, 179));
        executorService.execute(getICFDefinitions(group, 198, 199));
        executorService.execute(getICFDefinitions(group, 210, 240));
        executorService.execute(getICFDefinitions(group, 298, 299));
        executorService.execute(getICFDefinitions(group, 310, 329));
        executorService.execute(getICFDefinitions(group, 330, 349));
        executorService.execute(getICFDefinitions(group, 350, 369));
        executorService.execute(getICFDefinitions(group, 398, 399));
        executorService.execute(getICFDefinitions(group, 410, 429));
        executorService.execute(getICFDefinitions(group, 430, 449));
        executorService.execute(getICFDefinitions(group, 450, 469));
        executorService.execute(getICFDefinitions(group, 470, 489));
        executorService.execute(getICFDefinitions(group, 498, 499));
        executorService.execute(getICFDefinitions(group, 510, 570));
        executorService.execute(getICFDefinitions(group, 598, 599));
        executorService.execute(getICFDefinitions(group, 610, 629));
        executorService.execute(getICFDefinitions(group, 630, 649));
        executorService.execute(getICFDefinitions(group, 650, 669));
        executorService.execute(getICFDefinitions(group, 698, 699));
        executorService.execute(getICFDefinitions(group, 710, 729));
        executorService.execute(getICFDefinitions(group, 730, 779));
        executorService.execute(getICFDefinitions(group, 798, 799));
        executorService.execute(getICFDefinitions(group, 810, 839));
        executorService.execute(getICFDefinitions(group, 840, 859));
        executorService.execute(getICFDefinitions(group, 860, 879));
        executorService.execute(getICFDefinitions(group, 898, 899));
        executorService.execute(getICFDefinitions(group, 910, 950));
        executorService.execute(getICFDefinitions(group, 998, 999));
    }

    private void executeForGroupE(ExecutorService executorService) {
        String group = "e";

        executorService.execute(getICFDefinitions(group, 110, 165));
        executorService.execute(getICFDefinitions(group, 210, 260));
        executorService.execute(getICFDefinitions(group, 298, 299));
        executorService.execute(getICFDefinitions(group, 310, 360));
        executorService.execute(getICFDefinitions(group, 398, 399));
        executorService.execute(getICFDefinitions(group, 410, 465));
        executorService.execute(getICFDefinitions(group, 498, 499));
        executorService.execute(getICFDefinitions(group, 510, 595));
        executorService.execute(getICFDefinitions(group, 598, 599));
    }

    public Runnable getICFDefinitions(final String prefix, final int start, final int end) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                List<ICFClassification> classificationsDefs = getClassificationsDef(prefix, start, end);

                for (ICFClassification classification : classificationsDefs) {
                    ICFClassificationResult dataFound = getClassificationData(classification);

                    if (dataFound.success) {
                        findAllClassifications(dataFound);

                        allClassifications.add(dataFound.icfClassification);
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        return runnable;
    }
}
