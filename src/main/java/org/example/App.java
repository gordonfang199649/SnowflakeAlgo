package org.example;


/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws Exception {
        org.example.SnowFlakeAlg snowFlakeAlg = new SnowFlakeAlg(41, 8, 14);
        System.out.println(snowFlakeAlg.nextId());
    }
}
