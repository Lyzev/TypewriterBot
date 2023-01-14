import org.openqa.selenium.By
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.security.SecureRandom
import java.time.Duration


class TypewriterBot(
    private val username: String,
    private val password: String,
    private val minDelay: Long,
    private val maxDelay: Long,
    private val errorProbability: Int,
    private val levels: Int
) {

    var isRunning = false
        private set

    private val driver = FirefoxDriver()
    private val wait = WebDriverWait(driver, Duration.ofMillis(40))
    private val random = SecureRandom.getInstanceStrong()

    fun start() {
        isRunning = true
        try {
            driver["https://at4.typewriter.at/"]

            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[3]/div[2]/div[1]/form/div[1]/input")))
                .sendKeys(username)
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[3]/div[2]/div[1]/form/div[2]/input")))
                .sendKeys(password)
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[3]/div[2]/div[1]/form/div[3]/input")))
                .click()

            Thread.sleep(5000)

            for (i in 1..levels) {
                Thread.sleep(500)
                if (!isRunning) break
                doLevel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        isRunning = false
        try {
            driver.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun doLevel() {
        driver["https://at4.typewriter.at/index.php?r=typewriter/runLevel"]
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[6]/div[3]/div/button"))).click()
        if (!isRunning) return
        while (isRunning) {
            val actualLetter = driver.findElement(By.xpath("/html/body/div[3]/div[2]/div[3]/div[2]/div[2]/span[1]"))
            if (!isRunning || actualLetter == null || actualLetter.text == null || actualLetter.text.isEmpty()) return
            if (random.nextInt(100) + 1 <= errorProbability)
                driver.findElement(By.tagName("body")).sendKeys(((random.nextInt(26) + 'a'.code).toChar()).toString())
            else
                driver.findElement(By.tagName("body")).sendKeys(actualLetter.text)
            Thread.sleep(minDelay + random.nextInt((maxDelay - minDelay).toInt()))
        }
    }
}