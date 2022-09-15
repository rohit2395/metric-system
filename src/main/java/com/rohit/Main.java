package com.rohit;

import com.rohit.stats.RecordMetrics;

import java.util.Scanner;

public class Main {

    public static int askSize(){
        Scanner sc = new Scanner(System.in);
        System.out.print("What is the size of the blob (it bytes)? : ");
        return sc.nextInt();
    }

    public static void main(String[] args) throws InterruptedException {

        Scanner sc = new Scanner(System.in);

        RecordMetrics rc = new RecordMetrics();

        while (true){
            System.out.println("=======================================");
            System.out.println("Please select the operation to perform");
            System.out.println("1. Put Blob AWS");
            System.out.println("2. Get Blob AWS");
            System.out.println("3. Print stats AWS");
            System.out.println("4. Put Blob AZURE");
            System.out.println("5. Get Blob AZURE");
            System.out.println("6. Print stats AZURE");
            System.out.println("7. Print stats Overall");
            System.out.println("8. EXIT");
            System.out.println("=======================================");
            System.out.print("Your choice: ");
            int choice = sc.nextInt();
            int size = 0;
            switch (choice){
                case 1:
                    size = askSize();
                    rc.putBlobAws(size);
                    break;
                case 2:
                    size = askSize();
                    rc.getBlobAws(size);
                    break;
                case 3:
                    rc.printStatsAws();
                    break;
                case 4:
                    size = askSize();
                    rc.putBlobAzure(size);
                    break;
                case 5:
                    size = askSize();
                    rc.getBlobAzure(size);
                    break;
                case 6:
                    rc.printStatsAzure();
                    break;
                case 7:
                    rc.printStatsAll();
                    break;
                default:
                    System.exit(0);
            }
            System.out.println("=======================================");
            System.out.println();
        }

    }
}
