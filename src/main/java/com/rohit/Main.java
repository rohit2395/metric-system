package com.rohit;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        Scanner sc = new Scanner(System.in);

        RecordMetrics rc = new RecordMetrics();

        while (true){
            System.out.println("=======================================");
            System.out.println("Please select the operation to perform");
            System.out.println("1. Put Blob");
            System.out.println("2. Get Blob");
            System.out.println("3. Put Blob Failed");
            System.out.println("4. Get Blob Failed");
            System.out.println("5. Put Blob Retry");
            System.out.println("6. Get Blob Retry");
            System.out.println("7. Print stats");
            System.out.println("8. EXIT");
            System.out.println("=======================================");
            System.out.println();
            System.out.print("Your choice: ");
            int choice = sc.nextInt();
            int size = 0;
            if(choice != 8) {
                System.out.print("What is the size of the blob (it bytes)? : ");
                size = sc.nextInt();
            }
            System.out.println("=======================================");
            System.out.println();
            switch (choice){
                case 1:
                    rc.putBlob(size);
                    break;
                case 2:
                    rc.getBlob(size);
                    break;
                case 3:
                    rc.putBlobFailed(size);
                    break;
                case 4:
                    rc.getBlobFailed(size);
                    break;
                case 5:
                    rc.putBlobRetry(size);
                    break;
                case 6:
                    rc.getBlobRetry(size);
                    break;
                case 7:
                    rc.printStats();
                default:
                    System.exit(0);
            }
            System.out.println();
        }

    }
}
