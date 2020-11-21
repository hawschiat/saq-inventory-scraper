package io.github.hawschiat

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import kotlin.system.exitProcess

data class Inventory(val storeName: String, val storeId: String, val quantity: Int) {}

fun getInventory(productUrl: String): List<Inventory> {
    // Extra options to prevent error in headless environment
    val options = ChromeOptions()
    options.addArguments("--no-sandbox")
    options.setHeadless(true)
    // Start Chrome Driver with the specified options
    val driver = ChromeDriver(options)
    val wait = WebDriverWait(driver, 60)
    val inventory = mutableListOf<Inventory>()

    driver.get(productUrl)

    // Get the online stock
    val stockContainerList = driver.findElementsByClassName("stock-label-container")
    var onlineStock = 0
    if (stockContainerList.size > 0) {
        onlineStock = stockContainerList[0]
                .findElement(By.className("product-online-availability"))
                .findElement(By.tagName("span"))
                .text.toInt()
    }
    // Add data to map
    inventory.add(Inventory("Online", "0", onlineStock))

    // Wait for off canvas toggle to show
    val offCanvasToggleSelector = "div.available-in-store div.off-canvas-toggle button.action.toggle"
    wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(offCanvasToggleSelector)))
    // Toggle off canvas
    driver.executeScript("document.querySelector('${offCanvasToggleSelector}').click()")

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
        driver.executeScript("document.querySelector('${showMoreSelector}').click()")
        // Wait until more data has been loaded
        wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.cssSelector("div#off-canvas-instore > div.wrapper-store > div > " +
                                "div.store-locator-content > div.list-map-container > div.store-list-container > " +
                                "ul.store-list > li:nth-child(${storeCount + 1})")
                )
        )
        storeCount = driver.findElementsByCssSelector(storeListItemSelector).size
    }

    // Done. Now select all elements that contain inventory dat

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
    println("v0.1.2 by SC Haw\n")
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