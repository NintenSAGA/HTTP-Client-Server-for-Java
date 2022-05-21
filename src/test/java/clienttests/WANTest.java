package clienttests;

import client.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.Util.*;


public class WANTest {

    @Test
    public void test1() throws IOException, InterruptedException {
        testAndCompareWebPage("jyywiki.cn", "/OS/2022/labs/L3");
        testAndCompareWebPage("jyywiki.cn", "/OS/2022/");
    }

    @Test
    public void test2() throws IOException, InterruptedException {
    }
}
