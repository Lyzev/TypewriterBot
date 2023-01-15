import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.security.SecureRandom
import java.time.Duration
import kotlin.properties.Delegates

/**
 * This class is a bot that automates the process of typing on the website "at4.typewriter.at"
 * It uses the Selenium library to interact with the website and complete levels.
 * It simulates human-like typing by introducing errors with a specified probability and using random delays between keystrokes.
 */
object TypewriterBot {

    /**
     * The username to use when logging in to the website
     */
    private lateinit var username: String

    /**
     * The password to use when logging in to the website
     */
    private lateinit var password: String

    /**
     * The minimum delay, in milliseconds, between keystrokes
     */
    private var minDelay by Delegates.notNull<Long>()

    /**
     * The maximum delay, in milliseconds, between keystrokes
     */
    private var maxDelay by Delegates.notNull<Long>()

    /**
     * The probability, as a percentage, of introducing errors while typing
     */
    private var errorProbability by Delegates.notNull<Int>()

    /**
     * The number of levels to complete
     */
    private var levels by Delegates.notNull<Int>()

    /**
     * Property that indicates if the bot is currently running
     */
    var isRunning by mutableStateOf(false)
        private set

    /**
     * The FirefoxDriver instance used to interact with the website
     */
    private lateinit var driver: WebDriver

    /**
     * The WebDriverWait instance used to wait for elements to load on the website
     */
    private lateinit var wait: WebDriverWait

    /**
     * The SecureRandom instance used to generate random numbers for delays and errors
     */
    private val random = SecureRandom.getInstanceStrong()

    /**
     * Setups the bot
     */
    fun setup(
        username: String,
        password: String,
        minDelay: Long, maxDelay: Long,
        errorProbability: Int,
        levels: Int
    ) {
        this.username = username
        this.password = password
        this.minDelay = minDelay
        this.maxDelay = maxDelay
        this.errorProbability = errorProbability
        this.levels = levels

        // initialize the browser driver and driver wait
        driver = WebDriverManager.firefoxdriver().create()
        wait = WebDriverWait(driver, Duration.ofSeconds(10))
    }

    /**
     * Start the bot
     */
    fun start() {
        isRunning = true
        try {
            // navigate to the website
            driver["https://at4.typewriter.at/"]

            // locate and fill in the login form
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[3]/div[2]/div[1]/form/div[1]/input")))
                .sendKeys(username)
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[3]/div[2]/div[1]/form/div[2]/input")))
                .sendKeys(password)
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[3]/div[2]/div[1]/form/div[3]/input")))
                .click()

            // wait for the user overview page to load
            wait.until(ExpectedConditions.urlToBe("https://at4.typewriter.at/index.php?r=user/overview"))

            // complete the specified number of levels
            for (i in 1..levels) {
                Thread.sleep(500)
                if (!isRunning) break // check if bot was stopped
                doLevel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Stop the bot
     */
    fun stop() {
        isRunning = false
        try {
            // close the browser window
            driver.quit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Completes a single level
     */
    private fun doLevel() {
        // navigate to the level page
        driver["https://at4.typewriter.at/index.php?r=typewriter/runLevel"]

        // click the start button
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[6]/div[3]/div/button"))).click()

        if (!isRunning) return // check if bot was stopped

        while (isRunning) {
            // the current letter to type
            val actualLetter = driver.findElement(By.xpath("/html/body/div[3]/div[2]/div[3]/div[2]/div[2]/span[1]"))

            // check if bot was stopped or if there are no more letters to type
            if (!isRunning || actualLetter == null || actualLetter.text == null || actualLetter.text.isEmpty()) return

            if (random.nextInt(100) + 1 <= errorProbability) {
                // type a random letter with errorProbability chance
                driver.findElement(By.tagName("body")).sendKeys(((random.nextInt(26) + 'a'.code).toChar()).toString())
            } else {
                // type the correct letter
                driver.findElement(By.tagName("body")).sendKeys(actualLetter.text)
            }

            // wait for a random delay between minDelay and maxDelay
            Thread.sleep(minDelay + random.nextInt((maxDelay - minDelay).toInt()))
        }
    }
}