package com.ibm.haac.hx.engine.tool;

import java.util.Scanner;

public class Main {


    public static void main(String[] args) throws InterruptedException {

        ICFPull icfPull = new ICFPull();

        System.out.println("Enter ICF classification group (b, d, e, or s):");
        String group = new Scanner(System.in).nextLine();

        //b Body Functions
        icfPull.pullICFClassifications(group);
    }

}
