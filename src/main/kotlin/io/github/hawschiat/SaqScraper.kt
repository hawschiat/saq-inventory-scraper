package io.github.hawschiat

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import kotlin.system.exitProcess

data class Inventory(val storeName: String, val storeId: String, val quantity: Int) {}

fun getInventory(productUrl: String): List<Inventory> {
    val driver = ChromeDriver()
    val wait = WebDriverWait(driver, 60)
    driver.get(productUrl)
    // Toggle off canvas
    driver.findElementByClassName("available-in-store")
            .findElement(By.className("off-canvas-toggle"))
            .findElement(By.cssSelector("button.action.toggle")).click()

    // Wait until data has been loaded
    val storeListItemSelector = "div#off-canvas-instore > div.wrapper-store > div > " +
            "div.store-locator-content > div.list-map-container > div.store-list-container > " +
            "ul.store-list > li"
    wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(storeListItemSelector)))

    var storeCount = driver.findElementsByCssSelector(storeListItemSelector).size

    // Load the rest of the data by clicking the "Show more stores" button
    val showMoreSelector = "div#off-canvas-instore > div.wrapper-store > div > " +
            "div.store-locator-content > div.list-map-container > div.store-list-container > " +
            "div.list-footer > div.list-footer-content > div.action-toolbar.pagination > button.action"

    // Keep loading data while possible
    while (driver.findElementByCssSelector(showMoreSelector).isDisplayed) {
        driver.findElementByCssSelector(showMoreSelector).click()
        // Wait until more data has been loaded
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("div#off-canvas-instore > div.wrapper-store > div > " +
                        "div.store-locator-content > div.list-map-container > div.store-list-container > " +
                        "ul.store-list > li:nth-child(${storeCount + 1})")
        ))
        storeCount = driver.findElementsByCssSelector(storeListItemSelector).size
    }

    // Done. Now select all elements that contain inventory data
    val inventory = mutableListOf<Inventory>()

    driver.findElementsByCssSelector(storeListItemSelector)
            .forEach { el ->
                // Extract data
                val name = el.findElement(
                        By.cssSelector("div.store-list-item-content > div.store-list-item-header > " +
                                "div.name > h4[data-bind=\"text: \$data.name\"]")
                ).text
                val id = el.findElement(
                        By.cssSelector("div.store-list-item-content > div.store-list-item-header > " +
                                "div.name > span[data-bind=\"text: id\"]")
                ).text
                val count = el.findElement(
                        By.cssSelector("div.store-list-item-content > div.more-details > " +
                                "div.disponibility-favorite > div.disponibility > " +
                                "strong[data-bind=\"text: \$data.qty\"]")
                ).text.toInt()

                // Add data to map
                inventory.add(Inventory(name, id, count))
            }

    driver.close()

    return inventory.toList()
}

fun main(args: Array<String>) {
    val parser = ArgParser("saqScraper")
    val saveFile by parser
            .option(ArgType.Boolean,
                    shortName = "s",
                    description = "Save output into a CSV file (existing data will be overwritten)"
            )
            .default(false)
    parser.parse(args)

    println("   _____         ____     _____                                \n" +
            "  / ____|  /\\   / __ \\   / ____|                               \n" +
            " | (___   /  \\ | |  | | | (___   ___ _ __ __ _ _ __   ___ _ __ \n" +
            "  \\___ \\ / /\\ \\| |  | |  \\___ \\ / __| '__/ _` | '_ \\ / _ \\ '__|\n" +
            "  ____) / ____ \\ |__| |  ____) | (__| | | (_| | |_) |  __/ |   \n" +
            " |_____/_/    \\_\\___\\_\\ |_____/ \\___|_|  \\__,_| .__/ \\___|_|   \n" +
            "                                              | |              \n" +
            "                                              |_|              ")
    println("v0.1.0 by SC Haw\n")
    while (true) {
        println("Please enter the url for the product you'd like to query, or 'exit'.")
        val input = readLine()
        if (input.equals("exit", true)) {
            exitProcess(0)
        } else if (input != null) {
            var fileName: String? = null
            // Ask for a filename if the save file argument has been passed in
            if (saveFile) {
                while (fileName == null) {
                    println("Please enter a filename (without .csv extension) for your output:")
                    fileName = readLine()
                }
            }
            val inventory = getInventory(input)
            val totalCount: Int = inventory.map { it.quantity }.reduce { acc, i -> acc + i }

            if (saveFile && fileName != null) {
                csvWriter().open("$fileName.csv") {
                    writeRow(listOf("Store", "Inventory count"))
                    inventory.forEach {
                        writeRow(listOf("${it.storeName} (${it.storeId})", it.quantity))
                    }
                    writeRow(listOf("Total", totalCount))
                }
                println("Output has been saved to $fileName.csv successfully.\n")
            }

            println(
                    table {
                        header {
                            cellStyle {
                                border = true
                            }
                            row {
                                cell("Store")
                                cell("Inventory count")
                            }
                        }
                        body {
                            cellStyle {
                                border = true
                            }
                            inventory.map {
                                row {
                                    cell("${it.storeName} (${it.storeId})")
                                    cell(it.quantity) {
                                        alignment = TextAlignment.MiddleCenter
                                    }
                                }
                            }
                        }
                        footer {
                            cellStyle {
                                border = true
                            }
                            row {
                                cell("Total")
                                cell(totalCount) {
                                    alignment = TextAlignment.MiddleCenter
                                }
                            }
                        }
                    }
            )
            println()
        }
    }
}