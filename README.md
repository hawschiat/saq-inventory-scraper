# SAQ Inventory Scraper

This simple tool was created to query the quantities of certain products across all available [SAQ](https://www.saq.com/) branches.

Since SAQ doesn't provide any public API, the only way to obtain this data is by manually browsing through the site.
An easy way to achieve this is to parse through the HTML page using a parser. However, since SAQ's product page doesn't 
load up the data for all stores at once, the need arises for a way to automate the process of clicking certain buttons 
on the product page in order to load the rest of the data.

This is where [Selenium Web Driver](https://www.selenium.dev/projects/) comes into action. This framework allows me to 
create scripts that automate the aforementioned processes, as well as scraping data from a given page.

### Requirements
- [Java Runtime Environment (JRE)](https://www.java.com/en/download/)
- [Google Chrome](https://www.google.com/chrome/)
- [Chromedriver](https://chromedriver.chromium.org/home)


#### For macOS users
Please ensure that you have JRE and Google Chrome installed on your machine.

I have provided a script to facilitate the installation of Chromedriver in the 
[released archive](https://github.com/hawschiat/saq-inventory-scraper/releases).
You may also follow the instructions below to install it yourself.

1. Install Homebrew
```$xslt
curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh | sh
```

2. Install the Chromedriver package
```$xslt
brew cask install chromedriver
```

### Usage guide

Run
```$xslt
java -jar saq-inventory-scraper-x.x.x.jar
```
The program will then prompt you for a url to the product page. e.g. https://www.saq.com/en/14252639

To save the outputs to CSV files, run the program with an additional argument:
```
java -jar saq-inventory-scraper-x.x.x.jar --saveFile
```
The program will prompt for a file name at each operation.

For macOS users, you can just run the provided scripts, which will automatically run the JAR file if your system 
meets the requirement.

### Disclaimer
I created this tool in July 2020, during which the tool worked perfectly for the product pages in SAQ. However, their 
website design may change in the future and break the tool. I try to keep up with the design whenever possible but there's
no guarantee for it.
