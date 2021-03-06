package testing.e2e;

import moxproxy.builders.LocalMoxProxy;
import moxproxy.enums.MoxProxyAction;
import moxproxy.enums.MoxProxyDirection;
import moxproxy.interfaces.MoxProxy;
import moxproxy.model.MoxProxyProcessedTrafficEntry;
import moxproxy.model.MoxProxyRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalProxyTest extends TestBase {

    private WebDriver driver;

    private static MoxProxy proxy;

    private static final String PROXY_RESPONSE_MODIFICATION = "[\"proxy\",[\"Only MoxProxy!\"],[\"https://moxproxy.com\"]]";
    private static final String ONLY_MOX_PROXY = "Only MoxProxy!";
    private static final String ERROR_RBODY = "TEST_ERROR";

    @BeforeAll
    static void beforeAll(){
        proxy = LocalMoxProxy.builder()
                .withPort(PROXY_PORT)
                .withRecorderWhiteList(Collections.singletonList(WIKIPEDIA))
                .build();
    }


    @BeforeEach
    void before() {
        proxy.startServer();
        driver = new FirefoxDriver(setupFirefox());
    }

    @AfterEach
    void after(){
        driver.quit();
        proxy.stopServer();
    }

    @Test
    void whenActionsPerformed_thenTrafficRecorded() {
        driver.get(WIKI_URL);
        List<MoxProxyProcessedTrafficEntry> requestTraffic = proxy.getAllRequestTraffic();
        List<MoxProxyProcessedTrafficEntry> responseTraffic = proxy.getAllResponseTraffic();
        assertThat(requestTraffic).isNotEmpty();
        assertThat(responseTraffic).isNotEmpty();
        assertThat(requestTraffic.stream().allMatch(x -> x.getUrl().contains(WIKIPEDIA))).isTrue();
        assertThat(responseTraffic.stream().allMatch(x -> x.getUrl().contains(WIKIPEDIA))).isTrue();
        assertThat(requestTraffic.stream().allMatch(x -> x.getBody() == null)).isTrue();
        assertThat(responseTraffic.stream().allMatch(x -> x.getBody() == null)).isTrue();
    }

    @Test
    void whenErrorResponseRule_thenErrorReturned() throws InterruptedException {

        MoxProxyRule rule = MoxProxyRule.builder()
                .withDirection(MoxProxyDirection.REQUEST)
                .withAction(MoxProxyAction.RESPOND)
                .withHttpRuleDefinition()
                    .withGetMethod()
                .withPathPattern(WIKIPEDIA_ORG_PATTERN)
                    .withStatusCode(500)
                    .withBody(ERROR_RBODY)
                    .havingHeaders()
                        .withHeader("content-type", "text/html; charset=utf-8")
                        .backToParent()
                .backToParent().build();

        proxy.createRule(rule);

        driver.get(WIKI_URL);

        Thread.sleep(SLEEP_TIME);

        assertThat(driver.getPageSource()).contains(ERROR_RBODY);
    }

    @Test
    void whenRuleExpired_thenModificationNotApplied() throws InterruptedException {

        MoxProxyRule rule = MoxProxyRule.builder()
                .withDirection(MoxProxyDirection.REQUEST)
                .withAction(MoxProxyAction.RESPOND)
                .withInvokeLimit(1)
                .withHttpRuleDefinition()
                .withGetMethod()
                .withPathPattern(WIKIPEDIA_ORG_PATTERN)
                .withStatusCode(500)
                .withBody(ERROR_RBODY)
                .havingHeaders()
                .withHeader("content-type", "text/html; charset=utf-8")
                .backToParent()
                .backToParent().build();

        proxy.createRule(rule);

        driver.get(WIKI_URL);

        Thread.sleep(SLEEP_TIME);

        assertThat(driver.getPageSource()).contains(ERROR_RBODY);

        driver.get(WIKI_URL);

        Thread.sleep(SLEEP_TIME);

        assertThat(driver.getPageSource()).doesNotContain(ERROR_RBODY);
    }

    @Test
    void whenResponseModified_thenModificationApplied() throws InterruptedException {

        MoxProxyRule rule = MoxProxyRule.builder()
                .withDirection(MoxProxyDirection.RESPONSE)
                .withAction(MoxProxyAction.MODIFY)
                .withHttpRuleDefinition()
                .withGetMethod()
                .withStatusCode(200)
                .withBody(PROXY_RESPONSE_MODIFICATION)
                .withPathPattern(SEARCH_PROXY)
                .backToParent().build();

        proxy.createRule(rule);

        driver.get(WIKI_URL);

        Thread.sleep(SLEEP_TIME);

        WebElement search = driver.findElement(BY_SEARCH);
        search.sendKeys(PROXY_TXT);

        Thread.sleep(SLEEP_TIME);

        WebElement suggestions = driver.findElement(By.className("suggestions-result"));
        String text = suggestions.getText();

        assertEquals(ONLY_MOX_PROXY, text);
    }

    @Test
    void whenBodyRemoved_thenNoResultsReturned() throws InterruptedException {

        MoxProxyRule rule = MoxProxyRule.builder()
                .withDirection(MoxProxyDirection.RESPONSE)
                .withAction(MoxProxyAction.DELETE)
                .withHttpRuleDefinition()
                .withGetMethod()
                .withStatusCode(200)
                .withDeleteBody()
                .havingHeaders()
                    .withHeader("content-type", "header-will-be-removed")
                    .backToParent()
                .withPathPattern(SEARCH_PROXY)
                .backToParent().build();

        proxy.createRule(rule);

        driver.get(WIKI_URL);

        Thread.sleep(SLEEP_TIME);

        WebElement search = driver.findElement(BY_SEARCH);
        search.sendKeys(PROXY_TXT);

        Thread.sleep(SLEEP_TIME);

        List<WebElement> suggestions = driver.findElements(By.className("suggestions-result"));

        assertEquals(0, suggestions.size());
    }

    @Test
    void whenRequestModified_thenModificationApplied() throws InterruptedException {

        String ipadAgent = "Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3";
        String xpath = "//img[@src='/static/images/mobile/copyright/wikipedia-wordmark-en.svg']";

        MoxProxyRule rule = MoxProxyRule.builder()
                .withDirection(MoxProxyDirection.REQUEST)
                .withAction(MoxProxyAction.MODIFY)
                .withHttpRuleDefinition()
                .withGetMethod()
                .withPathPattern(WIKIPEDIA_ORG_PATTERN)
                .havingHeaders()
                .withHeader("User-Agent", ipadAgent).backToParent()
                .backToParent().build();

        proxy.createRule(rule);

        driver.get(WIKI_URL);

        Thread.sleep(SLEEP_TIME);

        WebElement mobileMenu = driver.findElement(By.xpath(xpath));

        assertTrue(mobileMenu.isDisplayed());
    }
}
