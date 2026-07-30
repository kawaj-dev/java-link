package com.javalink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Java Linkを起動するためのクラスです。
 */
@SpringBootApplication
public class JavaLinkApplication {

    /**
     * アプリを最初に動かす入口です。
     *
     * @param args アプリ起動時に渡される情報
     */
    public static void main(String[] args) {
        // Spring Bootに「Java Linkを起動してください」と伝えます。
        SpringApplication.run(JavaLinkApplication.class, args);
    }
}
